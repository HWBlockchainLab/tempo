package org.hyperledger.tempo.tx.impl;

import org.hyperledger.tempo.Constants;
import org.hyperledger.tempo.external.chaincode.TransactionCommitter;
import org.hyperledger.tempo.external.chaincode.TxData;
import org.hyperledger.tempo.ts.TsDalPlugin;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class TransactionCommitterImpl implements TransactionCommitter {
    private static final Log LOGGER = LogFactory.getLog(TransactionCommitterImpl.class);

    private final TsDalPlugin tsDalPlugin;

    @Autowired
    public TransactionCommitterImpl(TsDalPlugin tsDalPlugin) {
        LOGGER.debug("TransactionCommitterImpl :: constructor");
        this.tsDalPlugin = tsDalPlugin;
    }

    public boolean accepts(String txKey) {
        LOGGER.debug("TransactionCommitterImpl :: accepts :: txKey: [" + txKey + "]");
        boolean isStartsWith = txKey.startsWith(new String(Constants.TS_PREFIX));
        LOGGER.debug("isStartsWith:" + isStartsWith);
        return isStartsWith;
    }

    public void commit(String channel, List<TxData> txDataList) {
        LOGGER.info("TransactionCommitterImpl :: commit :: channel: [" + channel + "]");
        List<String> lineProtocols = new ArrayList<String>(txDataList.size());
        for (TxData txData : txDataList) {
            byte[] data = txData.getData();
            String dataStr = new String(data);
            LOGGER.info("key:" + txData.getKey() + "data: " + dataStr);
            // TODO: validate line protocol
            lineProtocols.add(dataStr);
        }
        tsDalPlugin.insert(lineProtocols, channel);
    }

    public void flush(String channel) {
        // TODO
    }
}
