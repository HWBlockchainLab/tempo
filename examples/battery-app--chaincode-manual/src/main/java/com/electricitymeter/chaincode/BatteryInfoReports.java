// Copyright 2023 Huawei Cloud Computing Technology Co., Ltd.
// 
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
// 
//     http://www.apache.org/licenses/LICENSE-2.0
// 
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and;
// limitations under the License.

package com.batteryrecorder.chaincode;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.hyperledger.tempo.tscc.api.TsAPIProvider;
import org.hyperledger.tempo.dsl.interfaces.report.ReportQuery;
import org.hyperledger.tempo.dsl.report.ReportQueryBuilder;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;
import org.hyperledger.fabric.contract.Context;
import org.hyperledger.fabric.contract.ContractInterface;
import org.hyperledger.fabric.contract.annotation.*;
import org.hyperledger.fabric.shim.ChaincodeException;
import org.hyperledger.fabric.shim.ChaincodeStub;
import org.hyperledger.fabric.shim.ledger.CompositeKey;
import org.hyperledger.fabric.shim.ledger.KeyValue;
import org.hyperledger.fabric.shim.ledger.QueryResultsIterator;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.logging.Logger;

@Contract(name = "com.batteryrecorder.chaincode.BatteryInfoReports",
        info = @Info(title = "com.batteryrecorder.chaincode.BatteryInfoReports",
                description = "", version = "1.0", license = @License(name = "SPDX-License-Identifier: ", url = ""),
                contact = @Contact(email = "java-contract@example.com", name = "java-contract", url = "http://java-contract.me")))
@Default
@SuppressWarnings("unchecked")
public class BatteryInfoReports implements ContractInterface {
    private final static Logger LOG = Logger.getLogger(BatteryInfoReports.class.getName());

    public Context createContext(ChaincodeStub stub) {
        return new Context(stub);
    }

    public void init(ChaincodeStub stub) {
        init(new Context(stub));
    }

    @Transaction
    public void init(Context ctx) {

    }

    @Transaction
    public void instantiate(Context ctx) {
    }

    @Transaction
    public String runBatteryTypeReports(Context ctx) throws IOException {
        final String[] HEADERS = new String[]{"batteryType", "count"};
        final List<String> params = ctx.getStub().getParameters();
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(JsonParser.Feature.ALLOW_SINGLE_QUOTES, true);
        Map<String, Object> parsedParams = null;
        try {
            parsedParams = objectMapper.readValue(params.get(0), HashMap.class);
        } catch (JsonProcessingException e) {
            throw new ChaincodeException(e);
        }

        final ReportQuery fluxAllBatteries = ReportQueryBuilder.build("BatteryInfoReport - Battery Info for all batteries", null, "battery_record");
        fluxAllBatteries
                .andFilters().tag("batteryType").then()
                .duplicate("batteryType", "type")
                .groupBy("type")
                .count()
                .renameValueColumnNameTo("countBatteryType")
                .keep("type", "countBatteryType");//

        final String flux = fluxAllBatteries.toString();
        final String ql = "select * from battery_record where batteryPower > 80 and batteryTemperature > 80";
        LOG.info("About to run flux Report query after binding parameters:\n" + ql);

        String result = TsAPIProvider.provide(TsAPIProvider.PROVIDERS.INFLUX, ctx).getTsQueryResultWithParams(ql, parsedParams);
        
        if (null == result) {
            return "";
        }

        final int pos = result.indexOf('#');
        if (pos != -1) {
            result = result.substring(pos);
        } else {
            return "";
        }

        result = result.substring(result.indexOf('#'));

        LOG.info("Result:" + result);
        final Reader in = new StringReader(result);
        final Iterable<CSVRecord> records = CSVFormat.DEFAULT
                .withCommentMarker('#')
                .withFirstRecordAsHeader()
                .parse(in);

        final Map<String, List<String>> batteryRecords = new HashMap<>();
        LOG.info("records:" + records.toString());
        for (CSVRecord record : records) {
            LOG.info("Record:" + record.toString());
            final String batteryType = record.get("type");
            final String count = record.get("countBatteryType");

            batteryRecords.put(batteryType, Arrays.asList(count));
        }

        final StringWriter out = new StringWriter();
        try (CSVPrinter printer = new CSVPrinter(out, CSVFormat.DEFAULT
                .withHeader(HEADERS)
                .withDelimiter(','))) {
            for (Map.Entry<String, List<String>> batteryRecordEntry : batteryRecords.entrySet()) {
                printer.printRecord(batteryRecordEntry.getKey(), batteryRecordEntry.getValue().get(0));
            }
        }

        return out.toString();

    }

    @Transaction
    public String runBatteryExceedThresholdReports(Context ctx) throws IOException, ParseException {
        final String[] HEADERS = new String[]{"batteryId", "time", "batteryBrand","batteryType", "batteryPower", "batteryTemperature"};
        final Map<String,String> temperatureThreshold = new BatteryTypeRegister().getAllBatteryTypeThreshold(ctx);
        final List<String> params = ctx.getStub().getParameters();
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(JsonParser.Feature.ALLOW_SINGLE_QUOTES, true);
        Map<String, Object> parsedParams = null;
        try {
            parsedParams = objectMapper.readValue(params.get(0), HashMap.class);
        } catch (JsonProcessingException e) {
            throw new ChaincodeException(e);
        }

        final ReportQuery fluxAllBatteries = ReportQueryBuilder.build("BatteryExceedThresholdReports - Battery Exceed Threshold Reports", null, "battery_record");
        fluxAllBatteries
                .orFilters()
                .field("batteryPower").exists().field("batteryTemperature").exists()
                .then()
                .fieldsAsColumns();
                //.andFilters().column("batteryPower").greater(80).column("batteryTemperature").greater(80);

        final String flux = fluxAllBatteries.toString();
        LOG.info("About to run flux Report query after binding parameters:\n" + flux);

        String result = TsAPIProvider.provide(TsAPIProvider.PROVIDERS.INFLUX, ctx).getTsQueryResultWithParams(flux, parsedParams);
        LOG.info("result is \n" + result);
        
        if (null == result) {
            LOG.info("result is null");
            return "";
        }

        final int pos = result.indexOf('#');
        if (pos != -1) {
            result = result.substring(pos);
        } else {
            return "";
        }

        result = result.substring(result.indexOf('#'));

        LOG.info("Result:" + result);
        final Reader in = new StringReader(result);
        final Iterable<CSVRecord> records = CSVFormat.DEFAULT
                .withCommentMarker('#')
                .withFirstRecordAsHeader()
                .parse(in);

        final Map<String, List<String>> batteryRecords = new HashMap<>();
        LOG.info("records:" + records.toString());
        int iterateTimes = 1;
        for (CSVRecord record : records) {
            LOG.info("Record:" + record.toString());
            final String batteryId = record.get("batteryId");
            final String time = record.get("_time");
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
            format.setTimeZone(TimeZone.getTimeZone("GMT"));
            Date date = format.parse(time);
            format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            format.setTimeZone(TimeZone.getTimeZone("GMT+8"));
            final String targetDate = format.format(date);
            final String batteryBrand = record.get("batteryBrand");
            final String batteryType = record.get("batteryType");
            final String batteryPower = record.get("batteryPower");
            final String batteryTemperature = record.get("batteryTemperature");
            if (Integer.parseInt(batteryTemperature) >= Integer.parseInt(temperatureThreshold.get(batteryType))) {
                batteryRecords.put(String.valueOf(iterateTimes), Arrays.asList(batteryId,targetDate,batteryBrand,batteryType,batteryPower,batteryTemperature));
                iterateTimes++;
            } 
        }
        LOG.info("batteryRecords:" + batteryRecords.toString());

        final StringWriter out = new StringWriter();
        try (CSVPrinter printer = new CSVPrinter(out, CSVFormat.DEFAULT
                .withHeader(HEADERS)
                .withDelimiter(','))) {
            for (Map.Entry<String, List<String>> batteryRecordEntry : batteryRecords.entrySet()) {
                printer.printRecord(batteryRecordEntry.getValue().get(0), batteryRecordEntry.getValue().subList(1,batteryRecordEntry.getValue().size()));
            }
        }

        return out.toString();

    }

    @Override
    public void unknownTransaction(Context ctx) {
        throw new ChaincodeException("Undefined contract method called:" + ctx.getStub().getFunction());
    }

    @Override
    public void beforeTransaction(Context ctx) {
    }
}
