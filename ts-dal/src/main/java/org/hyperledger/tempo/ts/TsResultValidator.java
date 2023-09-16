package org.hyperledger.tempo.ts;

public interface TsResultValidator {
    Error validate(String result);
};