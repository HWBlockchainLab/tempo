package org.hyperledger.tempo.tscc.api;


import org.hyperledger.tempo.ts.TsDalPlugin;
import org.hyperledger.tempo.tscc.TsCCApi;
import org.hyperledger.fabric.contract.Context;
import org.influxdb.dto.Point;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Map;

class InfluxTsApi implements TsCCApi {

    private final Context ctx;
    private final TsDalPlugin tsPlugin;
    private org.hyperledger.tempo.ts.dto.Point influxValidatePoint;

    private static final String HASH_FUNCTION = "SHA-256";

    public InfluxTsApi(Context ctx) {
        this.ctx = ctx;
        this.tsPlugin = SpringFactoryListener.getBean(TsDalPlugin.class);
    }

    @Override
    public void putTsState(Point p) {
        saveKeyValue(p);
    }

    @Override
    public void putTsState(String lp) {
        saveKeyValue(lp);
    }

    @Override
    public String getTsQueryResult(String query) {
        return tsPlugin.query(query, ctx.getStub().getChannelId());
    }

    @Override
    public String getTsQueryResultWithParams(String query, Map<String, Object> params) {
        return tsPlugin.queryWithParams(query, params, ctx.getStub().getChannelId());
    }

    ;

    private void saveKeyValue(Point p) {
        saveKeyValue(p.lineProtocol());
    }

    private void saveKeyValue(String lineProtocol) {
        final StringBuilder builder = new StringBuilder();

        byte[] tsPrefix = {0x01, 0x02};
        builder.append(new String(tsPrefix, StandardCharsets.UTF_8));
        try {
            List<org.hyperledger.tempo.ts.dto.Point> parsedPoints = influxValidatePoint.fromLineProtocols(lineProtocol);
            if (parsedPoints != null) { // lineprotocol is valid
                MessageDigest digest = MessageDigest.getInstance(HASH_FUNCTION);
                byte[] hash = digest.digest(
                        lineProtocol.getBytes());
                builder.append(new String(hash, StandardCharsets.UTF_8));
            }
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }

        ctx.getStub().putState(builder.toString(), lineProtocol.getBytes());
    }

}
