package org.hyperledger.tempo.external.chaincode.plugins;

import java.io.IOException;
import java.util.Set;

public interface Checkpointer {
    long UNSET_BLOCK_NUMBER = -1;

    long getBlockNumber();

    void setBlockNumber(long blockNumber) throws IOException;

    Set<String> getTransactionIds();

    void addTransactionId(String transactionId) throws IOException;

    void close() throws IOException;
}
