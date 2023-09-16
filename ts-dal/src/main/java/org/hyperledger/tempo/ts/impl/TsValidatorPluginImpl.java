package org.hyperledger.tempo.ts.impl;

import org.hyperledger.tempo.ts.TsValidatorPlugin;
import org.springframework.stereotype.Component;

@Component
public class TsValidatorPluginImpl implements TsValidatorPlugin {
    public Boolean validate(Object txData) {
        return true;
    }
}

