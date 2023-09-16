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

package org.hyperledger.tempo.dsl.chaincode;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.hyperledger.fabric.shim.ChaincodeException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@SuppressWarnings("unchecked")
public class ChaincodeUtils {
    private final static Logger LOG = Logger.getLogger(ChaincodeUtils.class.getName());

    public static String bindQueryParameters(String format, List<String> parameters) {
        Map<String, String> values = new HashMap<>();
        for (int i = 0; i < parameters.size(); ++i) {
            values.put(Integer.toString(i), parameters.get(i));
        }
        final StringBuilder formatter = new StringBuilder(format);
        final List<Object> valueList = new ArrayList<>();
        final Matcher matcher = Pattern.compile("\\$\\{(\\w+)}").matcher(format);

        while (matcher.find()) {
            final String key = matcher.group(1);

            final String formatKey = String.format("${%s}", key);
            int index = formatter.indexOf(formatKey);

            if (index != -1) {
                formatter.replace(index, index + formatKey.length(), "%s");
                valueList.add(values.get(key));
            }
        }

        return String.format(formatter.toString(), valueList.toArray());
    }

    public static  Map<String, Object>  deserializeParameters(String jsonParam)  {
        LOG.info("param : " + jsonParam);
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(JsonParser.Feature.ALLOW_SINGLE_QUOTES, true);
        Map<String, Object> parsedParams = null;
        try {
            parsedParams = objectMapper.readValue(jsonParam, HashMap.class);
        } catch (JsonProcessingException e) {
            throw new ChaincodeException(e);
        }

        return  parsedParams;
    }
}
