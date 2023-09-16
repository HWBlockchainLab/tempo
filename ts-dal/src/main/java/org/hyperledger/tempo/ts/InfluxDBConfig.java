package org.hyperledger.tempo.ts;

import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

@Component
public class InfluxDBConfig {
    private String host;
    private int port;
    private String username;
    private String password;
    private Boolean isTlsEnabled;
    private int maxRetries;
    private String connectionDbName;
    private int maxResponseSize;
    private String certificate;

    public InfluxDBConfig() {
        super();
    }

    @PostConstruct
    public void init() {
        this.host = System.getenv("INFLUX_HOST");
        this.port = Integer.parseInt(System.getenv("INFLUX_PORT"));
        this.username = System.getenv("INFLUX_USERNAME");
        this.password = System.getenv("INFLUX_PASSWORD");
        this.isTlsEnabled = Boolean.parseBoolean(System.getenv("INFLUX_IS_SSL"));
        this.maxRetries = Integer.parseInt(System.getenv("INFLUX_MAX_RETRY"));
        this.certificate = System.getenv("INFLUX_CERTIFICATE_BASE64");
        this.maxResponseSize = Integer.parseInt(System.getenv("MAX_RESPONSE_SIZE"));
        this.connectionDbName = "db";
    }

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public Boolean getTlsEnabled() {
        return isTlsEnabled;
    }

    public int getMaxRetries() {
        return maxRetries;
    }

    public String getConnectionDbName() {
        return connectionDbName;
    }

    public String getCertificate() {
        return certificate;
    }

    public int getMaxResponseSize() {
        return maxResponseSize;
    }
}