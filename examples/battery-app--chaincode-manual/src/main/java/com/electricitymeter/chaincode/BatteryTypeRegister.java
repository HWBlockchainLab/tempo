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

import org.hyperledger.fabric.contract.Context;
import org.hyperledger.fabric.contract.ContractInterface;
import org.hyperledger.fabric.contract.annotation.*;
import org.hyperledger.fabric.shim.ChaincodeException;
import org.hyperledger.fabric.shim.ChaincodeStub;
import org.hyperledger.fabric.shim.ledger.KeyValue;
import org.hyperledger.fabric.shim.ledger.QueryResultsIterator;
import org.hyperledger.fabric.shim.ledger.KeyModification;
import java.util.logging.Logger;
import java.util.*;
import java.text.SimpleDateFormat;

@Contract(name = "com.batteryrecorder.chaincode.BatteryTypeRegister",
        info = @Info(title = "com.batteryrecorder.chaincode.BatteryTypeRegister",
                description = "", version = "1.0", license = @License(name = "SPDX-License-Identifier: ", url = ""),
                contact = @Contact(email = "java-contract@example.com", name = "java-contract", url = "http://java-contract.me")))
@Default
public class BatteryTypeRegister implements ContractInterface {

    private final static Logger LOG = Logger.getLogger(BatteryTypeRegister.class.getName());

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
    public void registerBatteryType(Context ctx) {
        final List<String> params = ctx.getStub().getParameters();
        LOG.info("params:\n" + params);

        for (String param : params) {
            final String batteryType = param.split(":")[0];
            final String batteryIndicator = param.split(":")[1];
            LOG.info("key:\n" + batteryType);
            LOG.info("value:\n" + batteryIndicator);
            ctx.getStub().putStringState(batteryType, batteryIndicator);
        }
    }

    @Transaction
    public String getRegisterdBatteryType(Context ctx) {
        final List<String> params = ctx.getStub().getParameters();
        LOG.info("params:\n" + params);
        List<String> batteryTypeLists = new LinkedList<>();
        for (String param : params) {
            batteryTypeLists.add(param);
        }
        String batteryTypes = getBatteryTypeThreshold(ctx, batteryTypeLists).toString();
        LOG.info("batteryTypes:\n" + batteryTypes);
        return batteryTypes;
    }

    @Transaction
    public String getAllRegisterdBatteryType(Context ctx) {
        String batteryTypes = getAllBatteryTypeThreshold(ctx).toString();
        LOG.info("batteryTypes:\n" + batteryTypes);
        return batteryTypes;
    }

    @Override
    public void unknownTransaction(Context ctx) {
        throw new ChaincodeException("Undefined contract method called:" + ctx.getStub().getFunction());
    }

    @Override
    public void beforeTransaction(Context ctx) {
    }

    public Map<String,String> getBatteryTypeThreshold(Context ctx, List<String> batteryTypeLists) {
        Map<String,String> threshold = new HashMap<>();
        for(String batteryType : batteryTypeLists) {
            final String result = ctx.getStub().getStringState(batteryType);
            LOG.info("battery type: \n" + batteryType);
            LOG.info("result: \n" + result);
            threshold.put(batteryType, result);
        }
        LOG.info("threshold: \n" + threshold);
        return threshold;
    }

    @Transaction
    public String getBatteryTypeModifyHistory(Context ctx) {
        SimpleDateFormat format =  new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        format.setTimeZone(TimeZone.getTimeZone("GMT+8"));
        final List<String> params = ctx.getStub().getParameters();
        LOG.info("params:\n" + params);
        List<String> batteryTypeLists = new LinkedList<>();
        for (String param : params) {
            batteryTypeLists.add(param);
        }
        List<String> batteryTypeModifyHistoryList = new LinkedList<>();
        for(String batteryType : batteryTypeLists) {
            StringBuffer sb = new StringBuffer();
            final QueryResultsIterator<KeyModification> history = ctx.getStub().getHistoryForKey(batteryType);
            Iterator<KeyModification> iter = history.iterator();
            while (iter.hasNext()) {
                KeyModification modification = iter.next();
                Date date = Date.from(modification.getTimestamp());
                String time = format.format(date);
                sb.append(String.format("{%s:%s, time: %s, transactionID: %s} ", batteryType, modification.getStringValue(), time, modification.getTxId()));
            }
            batteryTypeModifyHistoryList.add(sb.toString());
        }
        return batteryTypeModifyHistoryList.toString();
    }

    public Map<String,String> getAllBatteryTypeThreshold(Context ctx) {
        Map<String,String> threshold = new HashMap<>();
        final QueryResultsIterator<KeyValue> queryResult = ctx.getStub().getStateByRange("","");
        Iterator<KeyValue> iter = queryResult.iterator();
        while (iter.hasNext()) {
          KeyValue kv = iter.next();
          String key = kv.getKey();
          String value = kv.getStringValue();
          LOG.info("key: \n" + key);
          LOG.info("value: \n" + value);
          threshold.put(key,value);
        }
        return threshold;
    }
}
