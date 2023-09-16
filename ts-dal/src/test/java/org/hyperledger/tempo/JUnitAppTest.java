package org.hyperledger.tempo;

import org.hyperledger.tempo.external.chaincode.TxData;
import org.hyperledger.tempo.ts.InfluxDBConfig;
import org.hyperledger.tempo.ts.dto.Point;
import org.hyperledger.tempo.ts.impl.TSResultSizeValidatorImpl;
import org.hyperledger.tempo.ts.impl.TsDalPluginImpl;
import org.hyperledger.tempo.tx.impl.TransactionCommitterImpl;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.EnvironmentVariables;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.AnnotationConfigContextLoader;
import org.testcontainers.containers.InfluxDBContainer;
import org.testcontainers.utility.DockerImageName;

import java.util.*;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(loader = AnnotationConfigContextLoader.class,
        classes={InfluxDBConfig.class, TSResultSizeValidatorImpl.class, TsDalPluginImpl.class, TransactionCommitterImpl.class})
public class JUnitAppTest {
    private static final Log LOGGER = LogFactory.getLog(JUnitAppTest.class);

    final static InfluxDBContainer<?> influxdb = new InfluxDBContainer<>(
            DockerImageName.parse("influxdb:1.8.6-alpine")
    );

    @Autowired(required = true)
    private TsDalPluginImpl tsDalPlugin;

    @Autowired(required = true)
    private TransactionCommitterImpl txCommitter;

    @Rule
    public final EnvironmentVariables environmentVariables = new EnvironmentVariables();

    public JUnitAppTest() {
        influxdb.addEnv("INFLUXDB_HTTP_FLUX_ENABLED", "true");
        influxdb.withAuthEnabled(false);
//        influxdb.addEnv("INFLUXDB_ADMIN_USER", "admin");
//        influxdb.addEnv("INFLUXDB_ADMIN_PASSWORD", "password");
//        influxdb.addEnv("INFLUXDB_HTTP_AUTH_ENABLED", "false");
//        influxdb.withAdmin("admin");
//        influxdb.withAdmin("password");

        influxdb.start();
        Integer mappedPort  = influxdb.getMappedPort(8086);
        LOGGER.debug("mappedPort: " + mappedPort);
        environmentVariables.set("INFLUX_PORT", mappedPort.toString());
        environmentVariables.set("INFLUX_HOST", "localhost");
        environmentVariables.set("INFLUX_USERNAME", "admin");
        environmentVariables.set("INFLUX_PASSWORD", "password");
        environmentVariables.set("INFLUX_IS_SSL", "false");
        environmentVariables.set("INFLUX_MAX_RETRY", "10");
        environmentVariables.set("MAX_RESPONSE_SIZE", "104857600");

        LOGGER.debug("InfluxDB Docker Started !!!");
    }

    @BeforeClass
    public static void setUp() {
        LOGGER.debug("-----> SETUP <-----");
    }

    @Test
    public void tsDalTest() {
        LOGGER.debug("-----> TS DAL TEST <-----");
        StringBuilder builder = new StringBuilder();
        builder.append(Arrays.toString(Constants.TS_PREFIX));
        Boolean accepts = txCommitter.accepts(new String(Constants.TS_PREFIX));
        LOGGER.debug("accepts [" + accepts + "]");

        List<TxData> txDataList = new ArrayList<>();
        txDataList.add(new TxData(builder.toString(), "temperature,location=west value=60.0 10".getBytes()));
        txDataList.add(new TxData(builder.toString(), "temperature,location=east value=61.0 20".getBytes()));
        txDataList.add(new TxData(builder.toString(), "temperature,location=east value=62.0 30".getBytes()));

        txCommitter.commit("channel1", txDataList);
        String result = tsDalPlugin.query("|> range(start: 0, stop: now()) |> filter(fn: (r) => r._measurement == \"temperature\")", "channel1");
        LOGGER.debug("result [" + result + "]");
        assertTrue(result.contains("61,value,temperature,east"));

        Map<String, Object> params = new HashMap<>();
        params.put("Start", 0);
        params.put("Stop", 1465839830100400200L);
        params.put("Measurement", "temperature");
        params.put("PriceValue", 61.3);
        String result2 = tsDalPlugin.queryWithParams("|> range(start: -$Start, stop: $Stop) |> filter(fn: (r) => r._measurement == \"$Measurement\" and r._value > $PriceValue)", params, "channel1");
        LOGGER.debug("result2 [" + result2 + "]");
        assertTrue(result2.contains("62,value,temperature,east"));
    }

    @Test
    public void pointTest() {
        LOGGER.debug("-----> POINT TEST <-----");

        Point p1 = Point.fromLineProtocol("123temperature,location=west value=60.0 120");
        assertNotNull(p1);
        Point p2 = Point.fromLineProtocol("TTTTemperature,location=east value=62.0 30000");
        assertNotNull(p2);
        Point p3 = Point.fromLineProtocol("temperature1,location=east value=62.0");
        assertNotNull(p3);

        List<Point> singlePoint = Point.fromLineProtocols("temperature12,location=west value=60.0 1");
        assertTrue(singlePoint.size() == 1);

        List<Point> points = Point.fromLineProtocols("temperature12,location=west value=60.0 1\ntemperature12,location=west value=61.0 2\ntemperature12,location=west value=62.0 3\ntemperature12,location=west value=63.0 4\ntemperature12,location=west value=64.0 5");
        assertTrue(points.size() == 5);
    }

    @AfterClass
    public static void afterTest() {
        LOGGER.debug("-----> DESTROY <-----");
        influxdb.stop();
        LOGGER.debug("-----> InfluxDB DESTROYED <-----");
    }
}
