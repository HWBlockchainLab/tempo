package org.hyperledger.tempo.tscc.api;


import org.hyperledger.tempo.tscc.TsCCApi;
import org.hyperledger.fabric.contract.Context;

public class TsAPIProvider {

    public static enum PROVIDERS {
        INFLUX
    }

    private static final String BAD_PROVIDER = "Invalid provider given";

    public static TsCCApi provide(PROVIDERS providerName, Context ctx) {
        switch (providerName) {
            case INFLUX:
                return new InfluxTsApi(ctx);
            default:
                throw new IllegalArgumentException(BAD_PROVIDER);
        }

    }
}
