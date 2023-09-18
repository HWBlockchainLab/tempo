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
import org.hyperledger.tempo.applications.batteryrecorder.BatteryRecord;
import org.hyperledger.tempo.dsl.dto.Point;
import org.hyperledger.fabric.contract.Context;
import org.hyperledger.fabric.contract.ContractInterface;
import org.hyperledger.fabric.contract.annotation.*;
import org.hyperledger.fabric.shim.ChaincodeException;
import org.hyperledger.fabric.shim.ChaincodeStub;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

@Contract(name = "com.batteryrecorder.chaincode.BatteryRecordManaul",
        info = @Info(title = "com.batteryrecorder.chaincode.BatteryRecordManaul",
                description = "", version = "1.0", license = @License(name = "SPDX-License-Identifier: ", url = ""),
                contact = @Contact(email = "java-contract@example.com", name = "java-contract", url = "http://java-contract.me")))
@Default
public class BatteryRecordManaul implements ContractInterface {

    private final static Logger LOG = Logger.getLogger(BatteryRecordManaul.class.getName());

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
    public void ingest(Context ctx) throws JsonProcessingException {
        final List<String> params = ctx.getStub().getParameters();

        final ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(JsonParser.Feature.ALLOW_SINGLE_QUOTES, true);
        for (String param : params) {
            BatteryRecord record;
            record = objectMapper.readValue(param, BatteryRecord.class);
            Point p = new Point();
            p.setMeasurement("battery_record");
            p.setTime(record.getTime(), TimeUnit.MILLISECONDS);
            p.addTag("batteryId", "" + record.getBatteryId());
            p.addTag("batteryType", "" + record.getBatteryType());
            p.addTag("batteryBrand", "" + record.getBatteryBrand());
            p.addField("batteryPower", record.getBatteryPower());
            p.addField("batteryTemperature", record.getBatteryTemperature());
            final String line = p.toLineProtocol();
            LOG.info("Ingesting: " + line);
            TsAPIProvider.provide(TsAPIProvider.PROVIDERS.INFLUX, ctx).putTsState(line);
        }
    }

    @Override
    public void unknownTransaction(Context ctx) {
        throw new ChaincodeException("Undefined contract method called:" + ctx.getStub().getFunction());
    }

    @Override
    public void beforeTransaction(Context ctx) {
    }
}
