package org.hyperledger.tempo.ts;

import java.util.List;
import java.util.Map;

public interface TsDalPlugin {
    /**
     * Puts the specified Points into time-series db.
     *
     * @param point   set of serialied Point objects
     * @param channel The name of the channel to which the data will be entered.
     */
    void insert(List<String> point, String channel);

    /**
     * Performs a "rich" query against a time series database.
     * <p>
     *
     * @param query   Arguments to pass on to the called chaincode.
     * @param channel The name of the channel which the data will be retrieved.
     * @return {@link String} query result raw data.
     */
    String query(String query, String channel);

    /**
     * Performs a "rich" query against a time series database.
     * <p>
     *
     * @param query   Arguments to pass on to the called chaincode.
     * @param params  Set of bind parameters that will be replaced within query.
     * @param channel The name of the channel which the data will be retrieved.
     * @return {@link String} query result raw data.
     */
    String queryWithParams(String query, Map<String, Object> params, String channel);

    // TODO: void Flush();
}
