package org.hyperledger.tempo.external.chaincode.plugins;


import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.channels.OverlappingFileLockException;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

public final class FileCheckpointer implements Checkpointer {
    private static final Set<OpenOption> OPEN_OPTIONS = Collections.unmodifiableSet(EnumSet.of(
            StandardOpenOption.CREATE,
            StandardOpenOption.READ,
            StandardOpenOption.WRITE
    ));
    private static final int VERSION = 1;
    private static final String CONFIG_KEY_VERSION = "version";

    static class BlockInfoV1 {
        public int version;
        public long block;
        public Collection<String> transactions;

        public long getBlock() {
            return block;
        }

        public void setBlock(long block) {
            this.block = block;
        }

        public Collection<String> getTransactions() {
            return transactions;
        }

        public void setTransactions(Collection<String> transactions) {
            this.transactions = transactions;
        }

        public void setVersion(int version) {
            this.version = version;
        }

        public int getVersion() {
            return version;
        }
    }

    private final Path filePath;
    private final FileChannel fileChannel;
    private final Reader fileReader;
    private final Writer fileWriter;
    private final AtomicLong blockNumber = new AtomicLong(Checkpointer.UNSET_BLOCK_NUMBER);
    private final Set<String> transactionIds = new HashSet<>();

    public FileCheckpointer(final Path checkpointFile) throws IOException {
        boolean isFileAlreadyPresent = Files.exists(checkpointFile);

        filePath = checkpointFile;
        fileChannel = FileChannel.open(filePath, OPEN_OPTIONS);
        lockFile();

        CharsetDecoder utf8Decoder = StandardCharsets.UTF_8.newDecoder();
        fileReader = Channels.newReader(fileChannel, utf8Decoder, -1);

        CharsetEncoder utf8Encoder = StandardCharsets.UTF_8.newEncoder();
        fileWriter = Channels.newWriter(fileChannel, utf8Encoder, -1);

        if (isFileAlreadyPresent) {
            load();
        } else {
            save();
        }
    }

    private void lockFile() throws IOException {
        final FileLock fileLock;
        try {
            fileLock = fileChannel.tryLock();
        } catch (OverlappingFileLockException e) {
            throw new IOException("File is already locked: " + filePath, e);
        }
        if (fileLock == null) {
            throw new IOException("Another process holds an overlapping lock for file: " + filePath);
        }
    }

    private synchronized void load() throws IOException {
        fileChannel.position(0);
        JsonObject savedData = loadJson();
        final int version = savedData.getAsJsonPrimitive(CONFIG_KEY_VERSION).getAsInt();
        if (version == VERSION) {
            parseDataV1(savedData);
        } else {
            throw new IOException("Unsupported checkpoint data version " + version + " from file: " + filePath);
        }
    }

    private JsonObject loadJson() {
        return new Gson().fromJson(fileReader, JsonObject.class);
    }

    private void parseDataV1(final JsonObject json) throws IOException {
        final BlockInfoV1 blockInfoV1 = new Gson().fromJson(json.toString(), BlockInfoV1.class);
        blockNumber.set(blockInfoV1.getBlock());
        transactionIds.clear();
        transactionIds.addAll(blockInfoV1.getTransactions());
    }

    private synchronized void save() throws IOException {
        fileChannel.position(0);

        final BlockInfoV1 blockInfoV1 = new BlockInfoV1();
        blockInfoV1.setVersion(VERSION);
        blockInfoV1.setBlock(blockNumber.get());
        blockInfoV1.setTransactions(transactionIds);
        new GsonBuilder()
                .serializeNulls()
                .create()
                .toJson(blockInfoV1, fileWriter);
        fileWriter.flush();
        fileChannel.truncate(fileChannel.position());
    }

    @Override
    public long getBlockNumber() {
        return blockNumber.get();
    }

    @Override
    public synchronized void setBlockNumber(final long blockNumber) throws IOException {
        this.blockNumber.set(blockNumber);
        transactionIds.clear();
        save();
    }

    @Override
    public Set<String> getTransactionIds() {
        return Collections.unmodifiableSet(transactionIds);
    }

    @Override
    public synchronized void addTransactionId(final String transactionId) throws IOException {
        transactionIds.add(transactionId);
        save();
    }

    @Override
    public void close() throws IOException {
        fileChannel.close(); // Also releases lock
    }

    @Override
    public String toString() {
        return "FileCheckpointer{" +
                "filePath=" + filePath +
                ", blockNumber=" + blockNumber +
                ", transactionIds=" + transactionIds +
                '}';
    }
}
