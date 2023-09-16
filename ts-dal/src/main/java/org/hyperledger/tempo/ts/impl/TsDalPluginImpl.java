package org.hyperledger.tempo.ts.impl;


import org.hyperledger.tempo.ts.InfluxDBConfig;
import org.hyperledger.tempo.ts.TsDalPlugin;
import org.hyperledger.tempo.ts.TsResultValidator;
import com.influxdb.client.InfluxDBClient;
import com.influxdb.client.InfluxDBClientFactory;
import com.influxdb.client.InfluxDBClientOptions;
import com.influxdb.client.WriteApiBlocking;
import com.influxdb.client.domain.Query;
import com.influxdb.client.domain.WritePrecision;
import okhttp3.*;
import okhttp3.tls.Certificates;
import okhttp3.tls.HandshakeCertificates;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.logging.log4j.core.lookup.StrSubstitutor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.retry.backoff.FixedBackOffPolicy;
import org.springframework.retry.policy.SimpleRetryPolicy;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Component
public class TsDalPluginImpl implements TsDalPlugin {
    private static final Log LOGGER = LogFactory.getLog(TsDalPluginImpl.class);

    private final InfluxDBClient influxClient;
    private final InfluxDBConfig influxDBConfig;
    private final String influxUrl;
    private final OkHttpClient okHttpClient;
    private final ArrayList<TsResultValidator> tsResultValidator = new ArrayList<>();

    @Autowired
    public TsDalPluginImpl(InfluxDBConfig influxConfig, TSResultSizeValidatorImpl tsResultSizeValidator) throws InterruptedException {
        LOGGER.debug("TsDalPluginImpl :: constructor");
        influxDBConfig = influxConfig;
        this.tsResultValidator.add(tsResultSizeValidator);
        String host = influxDBConfig.getHost();
        LOGGER.debug("host: " + host);
        int port = influxDBConfig.getPort();
        LOGGER.debug("port: " + port);
        Boolean isTlsEnabled = influxDBConfig.getTlsEnabled();
        LOGGER.debug("isTlsEnabled: " + isTlsEnabled);
        String urlPrefix = isTlsEnabled ? "https" : "http";

        String influxUrlTemplate = "${urlPrefix}://${host}:${port}";
        Map<String, String> data = new HashMap<>();
        data.put("urlPrefix", urlPrefix);
        data.put("host", host);
        data.put("port", String.valueOf(port));

        influxUrl = StrSubstitutor.replace(influxUrlTemplate, data);
        LOGGER.debug("formatted influxdb URL: " + influxUrl);

        OkHttpClient.Builder okClient = new OkHttpClient.Builder()
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .connectTimeout(30, TimeUnit.SECONDS);

        if (isTlsEnabled) {
            handleCertificate(okClient);
        }

        interceptRetryMechanism(okClient);
        this.okHttpClient = okClient.build();

        InfluxDBClientOptions options = InfluxDBClientOptions.builder().url(influxUrl).org("org").okHttpClient(okClient).build();
        InfluxDBClient clientV2 = InfluxDBClientFactory.create(options);
        influxClient = clientV2;

        boolean isInfluxPing = clientV2.ping();
        long currentAttempt = 0;
        int maxRetries = influxDBConfig.getMaxRetries();
        LOGGER.debug("initialized influxdb client :: gonna test ping request || maximum attempts: " + maxRetries);
        while (!isInfluxPing && currentAttempt < maxRetries) {
            Thread.sleep((currentAttempt + 1) * 1000);
            isInfluxPing = clientV2.ping();
            currentAttempt++;
        }
        LOGGER.debug("isInfluxPing: [" + isInfluxPing + "]");
        if (!isInfluxPing) {
            throw new RuntimeException("Could not establish valid connection with influxdb || influxURL: [" + influxUrl + "]");
        }
    }

    private void handleCertificate(OkHttpClient.Builder okClient) {
        X509Certificate certificate = null;
        byte[] decodedCertificate = Base64.decodeBase64(influxDBConfig.getCertificate());
        try {
            String x509String = new String(decodedCertificate, "UTF-8") + "\n";
            LOGGER.debug("Certificate: " + "\n" + x509String);
            certificate = Certificates.decodeCertificatePem(x509String);
        } catch (Exception err) {
            LOGGER.debug("ERROR Parsing Certificate !!!!! " + err.getMessage());
            throw new RuntimeException("Could not parse certificate [" + decodedCertificate + "]");
        }

        HandshakeCertificates certificates = new HandshakeCertificates.Builder()
                .addTrustedCertificate(certificate)
                .build();
        okClient.sslSocketFactory(certificates.sslSocketFactory(), certificates.trustManager());
    }

    private void interceptRetryMechanism(OkHttpClient.Builder okClient) {
        okClient.interceptors().add(new Interceptor() {
            @Override
            public Response intercept(Chain chain) throws IOException {
                Request request = chain.request();
                Response response = chain.proceed(request); // try the request

                int tryCount = 0;
                int maxLimit = influxDBConfig.getMaxRetries();

                // error code 404 used to detect 'database not found' error
                while ((!response.isSuccessful() && response.code() != 404) && tryCount < maxLimit) {
                    response.close();
                    LOGGER.debug("Request intercept || Request failed - " + tryCount);
                    tryCount++;
                    response = chain.proceed(request); // retry the request
                }

                return response; // otherwise just pass the original response on
            }
        });
    }

    public Error createDatabase(String dbname) {
        LOGGER.debug("TsDalPluginImpl :: createDatabase() || dbname: [" + dbname + "]");
        try {
            SimpleRetryPolicy retryPolicy = new SimpleRetryPolicy();
            int maxRetries = influxDBConfig.getMaxRetries();
            retryPolicy.setMaxAttempts(maxRetries);

            FixedBackOffPolicy backOffPolicy = new FixedBackOffPolicy();
            backOffPolicy.setBackOffPeriod(1000);

            RetryTemplate retryTemplate = new RetryTemplate();
            retryTemplate.setRetryPolicy(retryPolicy);
            retryTemplate.setBackOffPolicy(backOffPolicy);

            RequestBody formBody = new FormBody.Builder()
                    .add("q", "CREATE DATABASE " + dbname)
                    .build();
            Request requestt = new Request.Builder()
                    .url(influxUrl + "/query")
                    .post(formBody)
                    .build();

            retryTemplate.execute(context -> {
                Response response = okHttpClient.newCall(requestt).execute();
                LOGGER.debug(response);
                if (response.isSuccessful()) {
                    LOGGER.debug("create database request succeeded");
                    return true;
                }
                return false;
            });
        } catch (Exception e) {
            LOGGER.debug("ERROR CREATING DATABASE: " + e.getMessage());
            return new Error("Could not create Database");
        }
        return null;
    }

    public void insert(List<String> points, String channel) {
        LOGGER.debug("TsDalPluginImpl :: insert :: channel: [" + channel + "] || point: [" + points.toString() + "]");
        try {
            WriteApiBlocking writeApi = influxClient.getWriteApiBlocking();
            writeApi.writeRecords(channel, channel, WritePrecision.NS, points);
        } catch (Exception e) {
            LOGGER.debug("Insert ERROR: " + e.getMessage());
            if (e.getMessage() != null && e.getMessage().contains("database not found")) {
                Error err = createDatabase(channel);
                if (err == null) {
                    insert(points, channel);
                } else {
                    LOGGER.debug("ERROR CREATING DATABASE " + channel + " !!!!! " + err.getMessage());
                    throw new RuntimeException("Could not create Database [" + channel + "]");
                }
            } else {
                LOGGER.debug("ERROR DATA INSERTION TO DATABASE " + channel + " !!!!! ");
                throw new RuntimeException("Could not insert data to Database [" + channel + "]");
            }
        }
    }

    public String query(String query, String channel) {
        LOGGER.debug("TsDalPluginImpl :: query :: channel: [" + channel + "] || query: [" + query + "]");
        try {
            Query q = new Query();
            String template = "from(bucket:\"${channel}\") ${query}";

            Map<String, String> data = new HashMap<String, String>();
            data.put("channel", channel);
            data.put("query", query);

            String formattedQuery = StrSubstitutor.replace(template, data);
            LOGGER.debug("formattedQuery: [" + formattedQuery + "]");

            q.setQuery(formattedQuery);
            String queryRaw = influxClient.getQueryApi().queryRaw(q);

            for (TsResultValidator resultValidator: tsResultValidator) {
                Error err = resultValidator.validate(queryRaw);
                if (err != null) {
                    throw new Exception(err);
                }
            }

            return queryRaw;
        } catch (Exception e) {
            LOGGER.debug("ERROR: " + e.getMessage());
            return "";
        }
    }

    public String queryWithParams(String query, Map<String, Object> params, String channel) {
        LOGGER.debug("TsDalPluginImpl :: queryWithParams :: channel: [" + channel + "] || query: [" + query + "]");
        try {
            String replacedQuery = replaceQueryParameters(query, params);
            LOGGER.debug("queryWithParams :: replacedQuery: [" + replacedQuery + "]");
            return query(replacedQuery, channel);
        } catch (Exception e) {
            LOGGER.debug("ERROR: " + e.getMessage());
            return "";
        }
    }

    private String replaceQueryParameters(String query, Map<String, Object> params) throws Exception {
        LOGGER.debug("replaceQueryParameters :: query: [" + query + "] || params: [" + params + "]");
        for (Map.Entry<String, Object> entry : params.entrySet()) {
            LOGGER.debug(entry.getKey() + "/" + entry.getValue());
            String replaceKey = String.format("$%s", entry.getKey());
            LOGGER.debug("replaceKey: [" + replaceKey + "]");
            String value = String.valueOf(entry.getValue());
            if (entry.getValue().getClass() == String.class) {
                LOGGER.debug("DETECTED String || value [" + value + "]");
            } else if (entry.getValue().getClass() == Double.class) {
                LOGGER.debug("DETECTED Double || value [" + value + "]");
            }  else if (entry.getValue().getClass() == Float.class) {
                LOGGER.debug("DETECTED Float || value [" + value + "]");
            } else if (entry.getValue().getClass() == Long.class) {
                LOGGER.debug("DETECTED Long || value [" + value + "]");
            } else if (entry.getValue().getClass() == Integer.class) {
                LOGGER.debug("DETECTED Integer || value [" + value + "]");
            } else if (entry.getValue().getClass() == Boolean.class) {
                LOGGER.debug("DETECTED Boolean || value [" + value + "]");
            } else {
                throw new Exception("Caught unsupported parameter || key: [" + entry.getKey() + "] || value: [" + entry.getValue() + "]");
            }
            query = query.replace(replaceKey, value);
        }
        return query;
    }
}