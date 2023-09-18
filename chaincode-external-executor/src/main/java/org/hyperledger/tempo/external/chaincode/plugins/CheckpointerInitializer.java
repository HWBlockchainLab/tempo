package org.hyperledger.tempo.external.chaincode.plugins;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

@Configuration
public class CheckpointerInitializer {
    private final ExternalExecutorConfig config;

    @Autowired
    public CheckpointerInitializer(ExternalExecutorConfig config) {
        this.config = config;
    }

    @Bean
    Checkpointer createCheckPointer() throws IOException {
        final Path path = Paths.get(config.getBookmarkPath());
        final String dir = path.getParent().toString();
        final String fileName = path.getFileName().toString();
        final Path finalDir = Paths.get(dir, config.getPeerName(), config.getChannelId(), config.getChainCodeName(), config.getChainCodeVersion());
        final Path finalPath = finalDir.resolve(fileName);

        final File f = finalDir.toFile();
        if (!f.exists()) {
            if (!f.mkdirs()) {
                throw new IOException("Can't create directory " + finalDir);
            }
        }
        return new FileCheckpointer(finalPath);
    }
}
