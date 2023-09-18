package org.hyperledger.tempo.external.chaincode.server;

import org.hyperledger.tempo.external.chaincode.plugins.ExternalExecutorConfig;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hyperledger.fabric.contract.ContractRouter;
import org.hyperledger.fabric.shim.ChaincodeServerProperties;
import org.hyperledger.fabric.shim.NettyChaincodeServer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.IOException;


@Component
public class ChaincodeServer {
    private static final Log LOGGER = LogFactory.getLog(ChaincodeServer.class);

    private final static int PORT_NUMBER = 20002;
    private final String chaincodeID;
    private final String tlsClientKeyFile;
    private final String tlsClientCertFile;
    private Thread thread;
    private ContractRouter contractRouter;
    private org.hyperledger.fabric.shim.ChaincodeServer chaincodeServer;

    @Autowired
    public ChaincodeServer(ExternalExecutorConfig eeConfig) {
        this.chaincodeID = eeConfig.getChaincodeId();
        this.tlsClientCertFile = eeConfig.getCertificationPath();
        this.tlsClientKeyFile = eeConfig.getPrivateKeyPath();
    }

    @PostConstruct
    public void createChaincodeServer() throws IOException {
        ChaincodeServerProperties chaincodeServerProperties = new ChaincodeServerProperties();

        // set values on the server properties
        chaincodeServerProperties.setTlsEnabled(false);
        chaincodeServerProperties.setKeyFile(tlsClientKeyFile);
        chaincodeServerProperties.setKeyCertChainFile(tlsClientCertFile);

        LOGGER.info("GOING TO BIND PORT: " + PORT_NUMBER);
        chaincodeServerProperties.setPortChaincodeServer(PORT_NUMBER);

        contractRouter = new ContractRouter(new String[]{"-i", chaincodeID});
        chaincodeServer = new NettyChaincodeServer(contractRouter, chaincodeServerProperties);

        thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    contractRouter.startRouterWithChaincodeServer(chaincodeServer);
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    LOGGER.info("ContractRouter stopped");
                }
            }
        });
        thread.start();
    }

    @PreDestroy
    void closeRouter() {
        chaincodeServer.stop();
        //thread.interrupt();
    }
}
