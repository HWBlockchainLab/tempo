package org.hyperledger.tempo.external.chaincode.plugins;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hyperledger.fabric.sdk.BlockEvent;
import org.hyperledger.fabric.sdk.Channel;
import org.hyperledger.fabric.sdk.HFClient;
import org.hyperledger.fabric.sdk.Peer;
import org.hyperledger.fabric.sdk.exception.InvalidArgumentException;
import org.hyperledger.fabric.sdk.exception.TransactionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.IOException;
import java.util.EnumSet;
import java.util.Optional;

@Component
@DependsOn("parentPeerInfoRetriever")
public class EventListener {
    private static final Log LOGGER = LogFactory.getLog(EventListener.class);

    private final TransactionHandler txHandler;

    private final ParentPeerInfo parentPeerInfo;

    private final HFClient hfCLient;

    private final ExternalExecutorConfig eeConfig;

    private final Checkpointer checkpointer;
    private Channel channel;

    @Autowired
    public EventListener(TransactionHandler txHandler, ParentPeerInfo parentPeerInfo, HFClient hfCLient, ExternalExecutorConfig eeConfig, Checkpointer checkpointer) {
        this.txHandler = txHandler;
        this.parentPeerInfo = parentPeerInfo;
        this.hfCLient = hfCLient;
        this.eeConfig = eeConfig;
        this.checkpointer = checkpointer;
    }

    @PostConstruct
    public void listen() throws InvalidArgumentException, TransactionException, IOException {
        final String channelId = eeConfig.getChannelId();
        channel = hfCLient.newChannel(channelId);

        final Channel.PeerOptions peerOptions = Channel.PeerOptions.createPeerOptions();

        final long blockNumber = checkpointer.getBlockNumber();
        LOGGER.info("Bookmark blockNumber=" + blockNumber);

        peerOptions.startEvents((blockNumber == -1) ? -1 : blockNumber + 1);
        peerOptions.setPeerRoles(EnumSet.of(Peer.PeerRole.EVENT_SOURCE));
        peerOptions.registerEventsForBlocks();
        channel.initialize();

        LOGGER.info("PeerOptions: " + peerOptions);

        LOGGER.info("Channel: +" + channel);
        registerListener(channel);

        final Peer peer = parentPeerInfo.getPeer();

        channel.addPeer(peer, peerOptions);
        peer.setPeerEventingServiceDisconnected(de -> {
            LOGGER.info("Lost connection to the peer. Retrying. Attempt #" + de.getReconnectCount());
            LOGGER.error("Reconnect Reason: " + de.getExceptionThrown());
            LOGGER.error(de.getExceptionThrown().getMessage());
            try {
                try {
                    Thread.sleep(1000);
                    final BlockEvent latestBLockReceived = de.getLatestBLockReceived();
                    de.reconnect(Optional.ofNullable(latestBLockReceived).map(BlockEvent::getBlockNumber).orElse(null));
                } catch (InterruptedException ignored) {
                    Thread.currentThread().interrupt();
                }
            } catch (TransactionException e) {
                LOGGER.error("Failed to reconnect to the peer:", e);
            }
        });

//        final List<Channel> channels = parentPeerInfo.getChannels();
//        for (Channel channel : channels) {
//
//
//            //channel.initialize();
//            LOGGER.info("PeerOptions: "+peerOptions);
//            channel.setPeer(peer, peerOptions);
//
//        peer.setPeerEventingServiceDisconnected(de -> {
//            try {
//                de.reconnect(de.getLatestBLockReceived().getBlockNumber() + 1);
//            } catch (TransactionException e) {
//                throw new RuntimeException(e);
//            }
//        });
//            registerListener(channel);
//        }
    }


    private void registerListener(Channel channel) throws InvalidArgumentException {
        LOGGER.info("Starting block listening for events on channel " + channel.getName());
        channel.registerBlockListener(blockEvent -> {
            try {
                txHandler.process(channel.getName(), blockEvent);
            } catch (IOException e) {
                LOGGER.error("Failed to register event block listener", e);
            }
        });
    }

    @PreDestroy
    void stop() {
        if (channel != null) {
            channel.shutdown(false);
        }
        LOGGER.debug("EventListener stopped");
    }
}
