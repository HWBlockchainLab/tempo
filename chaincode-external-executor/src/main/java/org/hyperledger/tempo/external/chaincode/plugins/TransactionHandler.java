package org.hyperledger.tempo.external.chaincode.plugins;

import com.google.protobuf.InvalidProtocolBufferException;
import org.hyperledger.tempo.external.chaincode.TransactionCommitter;
import org.hyperledger.tempo.external.chaincode.TxData;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hyperledger.fabric.protos.ledger.rwset.kvrwset.KvRwset;
import org.hyperledger.fabric.sdk.BlockEvent;
import org.hyperledger.fabric.sdk.BlockInfo;
import org.hyperledger.fabric.sdk.TxReadWriteSetInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class TransactionHandler {
    private static final Log LOGGER = LogFactory.getLog(TransactionHandler.class);

    private final List<TransactionCommitter> committers;
    private final String chaincodeName;
    private final String chaincodeVersion;

    private final Checkpointer checkpointer;

    @Autowired
    public TransactionHandler(List<TransactionCommitter> committers, ExternalExecutorConfig eeConfig, Checkpointer checkpointer) {
        this.committers = committers;
        this.chaincodeName = eeConfig.getChainCodeName();
        this.chaincodeVersion = eeConfig.getChainCodeVersion();

        this.checkpointer = checkpointer;
    }

    void process(String channelName, BlockEvent blockEvent) throws IOException {
        LOGGER.info(Thread.currentThread().getName() + ": Event block " + blockEvent.getBlock().getHeader().getNumber() + " was received for channel:" + channelName);
        final Map<TransactionCommitter, List<TxData>> usedCommitters = new HashMap<>();

        final Iterable<BlockEvent.TransactionEvent> transactionEvents = blockEvent.getTransactionEvents();
        for (BlockEvent.TransactionEvent transactionEvent : transactionEvents) {
            final Iterable<BlockInfo.TransactionEnvelopeInfo.TransactionActionInfo> transactionActionInfos = transactionEvent.getTransactionActionInfos();

            for (BlockInfo.TransactionEnvelopeInfo.TransactionActionInfo transactionActionInfo : transactionActionInfos) {
                final String chaincodeIDVersion = transactionActionInfo.getChaincodeIDVersion();
                LOGGER.debug("transactionActionInfo.chaincodeIDVersion()=" + chaincodeIDVersion);
                final String chaincodeIDName = transactionActionInfo.getChaincodeIDName();
                LOGGER.debug("transactionActionInfo.getChaincodeIDName()=" + chaincodeIDName);
                // if transaction came from another chaincode let's skip it
                if (!chaincodeIDName.equals(chaincodeName) || !chaincodeIDVersion.equals(chaincodeVersion)) {
                    LOGGER.debug(chaincodeIDName + "(" + chaincodeIDVersion + ") doesn't match " + chaincodeName + "(" + chaincodeVersion + ")");
                    continue;
                }

                final TxReadWriteSetInfo txReadWriteSet = transactionActionInfo.getTxReadWriteSet();
                final Iterable<TxReadWriteSetInfo.NsRwsetInfo> nsRwsetInfos = txReadWriteSet.getNsRwsetInfos();
                for (TxReadWriteSetInfo.NsRwsetInfo nsRwsetInfo : nsRwsetInfos) {
                    try {
                        final KvRwset.KVRWSet rwset = nsRwsetInfo.getRwset();

                        final List<KvRwset.KVWrite> writesList = rwset.getWritesList();
                        for (KvRwset.KVWrite kvWrite : writesList) {
                            final String key = kvWrite.getKey();
                            final byte[] value = kvWrite.getValue().toByteArray();

                            for (TransactionCommitter committer : committers) {
                                if (committer.accepts(key)) {
                                    final List<TxData> kvWrites = usedCommitters.computeIfAbsent(committer, k -> new ArrayList<>());
                                    kvWrites.add(new TxData(key, value));
                                    break;
                                }
                            }
                        }
                    } catch (InvalidProtocolBufferException e) {
                        //TODO handle properly the exception
                        throw new RuntimeException(e);
                    }
                }
            }
        }


        LOGGER.debug("usedCommitters.entrySet().size(): " + usedCommitters.entrySet().size());
        for (Map.Entry<TransactionCommitter, List<TxData>> usedCommittersEntry : usedCommitters.entrySet()) {
            usedCommittersEntry.getKey().commit(channelName, usedCommittersEntry.getValue());
        }

        checkpointer.setBlockNumber(blockEvent.getBlock().getHeader().getNumber());
    }
}
