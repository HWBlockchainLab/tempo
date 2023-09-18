package org.hyperledger.tempo.external.chaincode.plugins;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

@Component
public class ExternalExecutorConfig {
    private static final Log LOGGER = LogFactory.getLog(ExternalExecutorConfig.class);

    private String channelId;
    private String chaincodeId;
    private String peerName;
    private String peerURL;
    private String chainCodeName;
    private String chainCodeVersion;

    private String userName;
    //org1
    private String orgName;
    ///etc/ssl/ee.pem
    private String certificationPath;
    ///etc/ssl/ee.key
    private String privateKeyPath;

    private String tlsCACertificatePath;
    //Org1MSP
    private String mspId;
    private String bookmarkPath;

    @PostConstruct
    void init() {
        chaincodeId = System.getenv("EE_PACKAGE_ID");
        peerName = System.getenv("CORE_PEER_ID");
        peerURL = System.getenv("EE_RELATED_PEER_GRPC_URL");

        userName = System.getenv("EE_USER_NAME");
        orgName = System.getenv("CORE_PEER_ORG_NAME");
        mspId = System.getenv("CORE_PEER_LOCALMSPID");
        certificationPath = System.getenv("EE_CERT_FILE");
        privateKeyPath = System.getenv("EE_KEY_FILE");
        tlsCACertificatePath = System.getenv("ROOT_ORG_TLSCA_CERT_FILE");
        channelId = System.getenv("EE_CHANNEL_ID");
        bookmarkPath = System.getenv("BOOKMARK_PATH");
        validateParameters();

        final String nameAndVersion = chaincodeId.split(":")[0];
        final String[] parts = nameAndVersion.split("-");
        chainCodeName = parts[0];
        chainCodeVersion = parts[1];


        LOGGER.debug(this.toString());
    }

    private void validateParameters() {
        if (peerName == null || peerName.isEmpty()) {
            throw new IllegalArgumentException("peer's name is not set");
        }

        if (peerURL == null || peerURL.isEmpty()) {
            throw new IllegalArgumentException("peer URL is not set");
        }
        if (userName == null || userName.isEmpty()) {
            throw new IllegalArgumentException("user name is not set");
        }

        if (orgName == null || orgName.isEmpty()) {
            throw new IllegalArgumentException("organization's name is not set");
        }

        if (certificationPath == null || certificationPath.isEmpty()) {
            throw new IllegalArgumentException("certificate path is not set");
        }

        if (privateKeyPath == null || privateKeyPath.isEmpty()) {
            throw new IllegalArgumentException("private key is not set");
        }

        if (mspId == null || mspId.isEmpty()) {
            throw new IllegalArgumentException("MSPID is not set");
        }

        if (tlsCACertificatePath == null || tlsCACertificatePath.isEmpty()) {
            throw new IllegalArgumentException("TLS CA certificate is not set");
        }

        if (channelId == null || channelId.isEmpty()) {
            throw new IllegalArgumentException("channel Id is not set");
        }

        if (bookmarkPath == null || bookmarkPath.isEmpty()) {
            throw new IllegalArgumentException("bookmark path is not set");
        }

    }

    public String getChaincodeId() {
        return chaincodeId;
    }

    public String getPeerName() {
        return peerName;
    }

    public String getPeerURL() {
        return peerURL;
    }

    public String getChainCodeName() {
        return chainCodeName;
    }

    public String getChainCodeVersion() {
        return chainCodeVersion;
    }

    public String getCertificationPath() {
        return this.certificationPath;
    }

    public String getPrivateKeyPath() {
        return this.privateKeyPath;
    }

    public String getMspId() {
        return this.mspId;
    }

    public String getUserName() {
        return this.userName;
    }

    public String getOrgName() {
        return this.orgName;
    }

    public String getTlsCACertificatePath() {
        return tlsCACertificatePath;
    }

    public String getChannelId() {
        return channelId;
    }

    public String getBookmarkPath() {
        return bookmarkPath;
    }

    @Override
    public String toString() {
        return "ExternalExecutorConfig{" +
                "channelId='" + channelId + '\'' +
                ", chaincodeId='" + chaincodeId + '\'' +
                ", peerName='" + peerName + '\'' +
                ", peerURL='" + peerURL + '\'' +
                ", chainCodeName='" + chainCodeName + '\'' +
                ", chainCodeVersion='" + chainCodeVersion + '\'' +
                ", userName='" + userName + '\'' +
                ", orgName='" + orgName + '\'' +
                ", certificationPath='" + certificationPath + '\'' +
                ", privateKeyPath='" + privateKeyPath + '\'' +
                ", tlsCACertificatePath='" + tlsCACertificatePath + '\'' +
                ", mspId='" + mspId + '\'' +
                ", bookmarkPath='" + bookmarkPath + '\'' +
                '}';
    }
}
