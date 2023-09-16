package org.hyperledger.tempo.tscc;


import org.influxdb.dto.Point;

import java.util.Map;

public interface TsCCApi {
    /**
     * @param p -       point to be written to the key-value store
     */
    public void putTsState(Point p);

    /**
     * @param lp -       point serialized to line protocol format to be written to the key-value store
     */
    void putTsState(String lp);

    /**
     * @param query the time series database query
     * @return response  the response of the time series database
     */
    public String getTsQueryResult(String query);

    /**
     * @param query the time series database query
     * @param params  Set of bind parameters that will be replaced within query.
     * @return response  the response of the time series database
     */
    public String getTsQueryResultWithParams(String query, Map<String, Object> params);
}
