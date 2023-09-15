package org.hyperledger.tempo.external.chaincode.plugins;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.bouncycastle.asn1.pkcs.PrivateKeyInfo;
import org.bouncycastle.openssl.PEMKeyPair;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter;
import org.hyperledger.fabric.sdk.Enrollment;
import org.hyperledger.fabric.sdk.HFClient;
import org.hyperledger.fabric.sdk.exception.CryptoException;
import org.hyperledger.fabric.sdk.exception.InvalidArgumentException;
import org.hyperledger.fabric.sdk.identity.X509Enrollment;
import org.hyperledger.fabric.sdk.security.CryptoSuite;
import org.hyperledger.fabric.sdk.security.CryptoSuiteFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PreDestroy;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.PrivateKey;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

@Configuration
public class HFClientInitializer {
    private static final Log LOGGER = LogFactory.getLog(HFClientInitializer.class);

    private final ExternalExecutorConfig eeConfig;
    private ExecutorService executorService;


    public HFClientInitializer(ExternalExecutorConfig eeConfig) {
        this.eeConfig = eeConfig;
    }

    @Bean
    public HFClient getClient() throws InvalidArgumentException, IOException, CryptoException, ClassNotFoundException, InvocationTargetException, IllegalAccessException, InstantiationException, NoSuchMethodException {

        return createAndInitializeClient();
    }

    private HFClient createAndInitializeClient() throws IOException, ClassNotFoundException, InvocationTargetException, IllegalAccessException, InstantiationException, NoSuchMethodException, InvalidArgumentException, CryptoException {
        final Path certificatePem = Paths.get(eeConfig.getCertificationPath());
        final File skFile = new File(eeConfig.getPrivateKeyPath());
        final Path privateKey = skFile.toPath();
        final Enrollment enrollment = new X509Enrollment(getPrivateKey(Files.newBufferedReader(privateKey)), getCertificate(Files.newBufferedReader(certificatePem)));
        final UserContext userContext = new UserContext();
        userContext.setName(eeConfig.getUserName());
        userContext.setAffiliation(eeConfig.getOrgName());
        userContext.setMspId(eeConfig.getMspId());
        userContext.setEnrollment(enrollment);

        final HFClient client = HFClient.createNewInstance();

        final CryptoSuite cryptoSuite = CryptoSuiteFactory.getDefault().getCryptoSuite();
        client.setCryptoSuite(cryptoSuite);
        client.setUserContext(userContext);

        executorService = Executors.newFixedThreadPool(2);
        client.setExecutorService(executorService);

        return client;
    }


    String getCertificate(BufferedReader certReader) {
        String certStr = certReader.lines().collect(Collectors.joining("\n", "", "\n"));
        return certStr;
    }

    PrivateKey getPrivateKey(Reader pkReader) throws IOException {
        try (PEMParser parser = new PEMParser(pkReader)) {
            Object key = parser.readObject();
            JcaPEMKeyConverter converter = new JcaPEMKeyConverter();
            PrivateKey pk;
            if (key instanceof PrivateKeyInfo) {
                pk = converter.getPrivateKey((PrivateKeyInfo) key);
            } else {
                pk = converter.getPrivateKey(((PEMKeyPair) key).getPrivateKeyInfo());
            }

            return pk;
        }
    }

    @PreDestroy
    void stop() {
        executorService.shutdown();
    }
}
