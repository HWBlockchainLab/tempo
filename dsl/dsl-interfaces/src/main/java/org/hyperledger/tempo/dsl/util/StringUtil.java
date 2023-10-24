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

package org.hyperledger.tempo.dsl.util;

import java.math.BigDecimal;
import java.math.BigInteger;

public class StringUtil {

    public static final char COMMA = ',';
    public static final char ESCAPE = '\\';
    public static final char SPACE = ' ';
    public static final char EQUAL = '=';
    public static final char DOUBLE_QUOTE = '"';
    public static final char QUOTE = '\'';

    public static String UNSIGNED_LONG_MAX_VALUE = "18446744073709551615";

    public static final char I = 'i';
    public static final char NEGATIVE  = '-';

    public static boolean isNumeric(String strNum) {
        if (strNum == null ) {
            return false;
        }else{
            if(strNum.charAt(strNum.length()-1) == I){
                strNum = strNum.substring(0,strNum.length()-1);
            }
        }
        try {
            BigDecimal d = new BigDecimal(strNum);
        } catch (NumberFormatException nfe) {
            return false;
        }
        return true;
    }

    public static Number parseNumber(String str){
        String strNum = str;
        if(strNum.charAt(strNum.length()-1) == I){
            try {

                strNum = strNum.substring(0,strNum.length()-1);
                BigInteger res = new BigInteger(strNum);
                if(strNum.charAt(0) == NEGATIVE){
                    if(res.compareTo(BigInteger.valueOf(Long.MAX_VALUE)) ==1 || BigInteger.valueOf(Long.MIN_VALUE).compareTo(res) ==1)
                        throw new IllegalArgumentException("Unable to parse Integer "+str  );
                    Long result = Long.parseLong(strNum);
                    return result;
                } else{
                    if(res.compareTo(BigInteger.valueOf(Long.MAX_VALUE)) !=1 ) {
                        Long result = Long.parseLong(strNum);
                        return result;
                    }
                    if(res.compareTo(new BigInteger(UNSIGNED_LONG_MAX_VALUE)) ==1 ){
                        throw new IllegalArgumentException("Unable to parse Integer "+str);
                    }

                    return res;
                }
            }catch (NumberFormatException e){
                throw new IllegalArgumentException("Unable to parse Integer "+str,e );
            }
        } else{
            try {
                Double res = new Double(strNum);
                return res;
            } catch (NumberFormatException e){
                try{
                    BigDecimal res = new BigDecimal(strNum);
                    return res;
                }catch (NumberFormatException ex) {
                    throw new IllegalArgumentException("Unable to parse Decimal " + str,ex);
                }
            }
        }
    }

    public static boolean isBoolean(String str){
        if(str == null){
            return false;
        }
        if (str.equalsIgnoreCase("true") || str.equalsIgnoreCase("false")) {
            return true;
        }
        return false;
    }

    public static boolean parseBoolean(String str){
        return Boolean.parseBoolean(str);
    }


}
