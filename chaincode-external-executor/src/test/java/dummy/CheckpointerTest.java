package dummy;

import org.hyperledger.tempo.external.chaincode.plugins.FileCheckpointer;

import java.io.IOException;
import java.nio.file.Paths;

public class CheckpointerTest {
    public static void main(String[] args) throws IOException {
        FileCheckpointer ckp = new FileCheckpointer(Paths.get("/tmp/bookmark.json"));
        ckp.close();

        ckp = new FileCheckpointer(Paths.get("/tmp/bookmark.json"));
        long blockNumber = ckp.getBlockNumber();
        assert blockNumber == -1;
        ckp.close();

        ckp = new FileCheckpointer(Paths.get("/tmp/bookmark.json"));
        ckp.setBlockNumber(100);
        ckp.close();

        ckp = new FileCheckpointer(Paths.get("/tmp/bookmark.json"));
        blockNumber = ckp.getBlockNumber();
        assert blockNumber == 100;
        ckp.close();
    }
}
