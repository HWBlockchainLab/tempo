package org.hyperledger.tempo.external.chaincode;

public class TxData {
    private String key;
    private byte[] data;

    public TxData(String key, byte[] data) {
        this.key = key;
        this.data = data;
    }

    public String getKey() {
        return key;
    }

    public byte[] getData() {
        return data;
    }
}
