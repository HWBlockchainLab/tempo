package dummy;

import org.hyperledger.tempo.external.chaincode.TransactionCommitter;
import org.hyperledger.tempo.external.chaincode.TxData;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@Component
public class DummyTransactionCommitterImpl implements TransactionCommitter {
    private final AtomicInteger counter = new AtomicInteger(0);

    @Override
    public boolean accepts(String txKey) {
        return txKey.startsWith("__dummy://");
    }

    @Override
    public void commit(String channelName, List<TxData> TxData) {
        for (TxData txData : TxData) {
            final String key = txData.getKey();
            final String value = new String(txData.getData());
            counter.incrementAndGet();
            System.out.println("Committing TxData: key=" + key + "value=" + value);
        }
    }

    @Override
    public void flush(String channelName) {
        System.out.println("Flushing bulk of " + counter.get() + " transactions");
        counter.set(0);
    }
}
