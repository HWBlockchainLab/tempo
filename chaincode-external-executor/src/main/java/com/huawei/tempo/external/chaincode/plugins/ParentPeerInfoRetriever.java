package org.hyperledger.tempo.external.chaincode.plugins;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hyperledger.fabric.sdk.HFClient;
import org.hyperledger.fabric.sdk.Peer;
import org.hyperledger.fabric.sdk.exception.InvalidArgumentException;
import org.hyperledger.fabric.sdk.exception.TransactionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.Properties;

@Component
public class ParentPeerInfoRetriever {

    private static final Log LOGGER = LogFactory.getLog(ParentPeerInfoRetriever.class);

    private final HFClient hfCLient;

    private final ParentPeerInfo parentPeerInfo;

    private final ExternalExecutorConfig eeConfig;


    @Autowired
    public ParentPeerInfoRetriever(HFClient hfCLient, ParentPeerInfo parentPeerInfo, ExternalExecutorConfig eeConfig) {
        this.hfCLient = hfCLient;
        this.parentPeerInfo = parentPeerInfo;
        this.eeConfig = eeConfig;
    }

    @PostConstruct
    public void open() throws InvalidArgumentException, TransactionException {

        Properties peer_properties = new Properties();
        peer_properties.setProperty("pemFile", eeConfig.getTlsCACertificatePath());
        peer_properties.setProperty("sslProvider", "openSSL");
        peer_properties.setProperty("negotiationType", "TLS");
        peer_properties.setProperty("trustServerCertificate ", "true");

        final Peer peer = hfCLient.newPeer(eeConfig.getPeerName(), eeConfig.getPeerURL(), peer_properties);
//        final Set<String> channelNames = hfCLient.queryChannels(peer);
//        LOGGER.info("queryChannels returned " + channelNames.size() + " entries");
//        for (String channelName : channelNames) {
//            LOGGER.info("Adding channel:" + channelName);
//            final Channel channel = hfCLient.newChannel(channelName);
//            channel.addPeer(peer);
//            channel.initialize();
//            LOGGER.info("Channel: +" + channel);
//
//            final Collection<Peer> peers1 = channel.getPeers();
//            LOGGER.info("channel.getPeers() returned " + peers1.size() + " entries");
//            for (Peer peer1 : peers1) {
//                LOGGER.info("Peer " + peer1.getName() + " is part of channel " + channel.getName());
//            }
//            final Collection<Peer> peers = channel.getPeers(EnumSet.of(Peer.PeerRole.EVENT_SOURCE));
//            for (Peer peer1 : peers) {
//                LOGGER.info("Peer " + peer1.getName() + " is marked as event source");
//            }

//            final LifecycleQueryInstalledChaincodesRequest lifecycleQueryInstalledChaincodesRequest = hfCLient.newLifecycleQueryInstalledChaincodesRequest();
//            final Collection<LifecycleQueryInstalledChaincodesProposalResponse> lifecycleQueryInstalledChaincodesProposalResponses = hfCLient.sendLifecycleQueryInstalledChaincodes(lifecycleQueryInstalledChaincodesRequest, Collections.singleton(peer));
//
//            for (LifecycleQueryInstalledChaincodesProposalResponse proposalResponse : lifecycleQueryInstalledChaincodesProposalResponses) {
//                final Collection<LifecycleQueryInstalledChaincodesProposalResponse.LifecycleQueryInstalledChaincodesResult> lifecycleQueryInstalledChaincodesResult = proposalResponse.getLifecycleQueryInstalledChaincodesResult();
//                for (LifecycleQueryInstalledChaincodesProposalResponse.LifecycleQueryInstalledChaincodesResult queryInstalledChaincodesResult : lifecycleQueryInstalledChaincodesResult) {
//                   LOGGER.info("package:"+queryInstalledChaincodesResult.getPackageId()+" label:"+queryInstalledChaincodesResult.getLabel());
//                }
//            }
//        }

        parentPeerInfo.setPeer(peer);
    }
}
