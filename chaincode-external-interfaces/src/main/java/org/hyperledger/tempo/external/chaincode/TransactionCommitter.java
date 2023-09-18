package org.hyperledger.tempo.external.chaincode;


import java.util.List;

public interface TransactionCommitter {

    boolean accepts(String txKey);

    void commit(String channel, List<TxData> txDataList);

    void flush(String channel);
}
