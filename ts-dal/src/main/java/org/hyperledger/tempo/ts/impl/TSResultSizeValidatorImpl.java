package org.hyperledger.tempo.ts.impl;

import org.hyperledger.tempo.ts.InfluxDBConfig;
import org.hyperledger.tempo.ts.TsResultValidator;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class TSResultSizeValidatorImpl implements TsResultValidator {

    private static final Log LOGGER = LogFactory.getLog(TSResultSizeValidatorImpl.class);
    int maxResultLimit;

    @Autowired
    public TSResultSizeValidatorImpl(InfluxDBConfig influxDBConfig) {
        this.maxResultLimit = influxDBConfig.getMaxResponseSize();
    }

    public Error validate(String result) {
        byte[] resultBytesString = result.getBytes();
        LOGGER.debug("TSResultSizeValidator :: validate :: maxResultLimit: " + maxResultLimit);
        LOGGER.debug("TSResultSizeValidator :: validate :: resultBytesString: " + resultBytesString.length);
        if (resultBytesString.length > maxResultLimit) {
            return new Error("query response exceeds max size");
        } else {
            return null;
        }
    }
}

