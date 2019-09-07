package com.github.btnguyen2k.mus.utils.perflogs;

import com.github.btnguyen2k.mus.utils.AppUtils;
import com.github.ddth.commons.utils.DPathUtils;
import com.github.ddth.commons.utils.SerializationUtils;
import com.github.ddth.commons.utils.TypesafeConfigUtils;
import com.github.ddth.queue.IQueue;
import com.github.ddth.queue.IQueueMessage;
import com.github.ddth.queue.impl.AbstractQueue;
import com.github.ddth.queue.impl.universal.idint.UniversalInmemQueue;
import com.github.ddth.queue.utils.QueueException;
import com.github.ddth.recipes.apiservice.logging.AbstractPerfApiLogger;
import com.typesafe.config.Config;
import okhttp3.OkHttpClient;
import org.apache.commons.lang3.StringUtils;
import org.influxdb.BatchOptions;
import org.influxdb.InfluxDB;
import org.influxdb.InfluxDBFactory;
import org.influxdb.dto.Point;
import org.influxdb.dto.Query;
import org.influxdb.dto.QueryResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Log API execution performance to InfluxDB 1.x.
 *
 * @author Thanh Nguyen <btnguyen2k@gmail.com>
 * @since template-v2.0.r4
 */
public class InfluxdbPerfLogger extends AbstractPerfApiLogger {
    private final static long DEFAULT_RETENTION = 7 * 24 * 3600 * 1000; //7 days
    private final static int DEFAULT_MAX_QUEUE_ITEMS = 1024 * 1024;

    private Logger LOGGER = LoggerFactory.getLogger(InfluxdbPerfLogger.class);

    private Config config;
    private String server, user, password, database;
    private String retentionPolicyName;
    private Long retentionPolicyDurationMs;
    private InfluxDB influxdb;
    private boolean inited = false;
    private ScheduledExecutorService executorService;
    private boolean myOwnExecutorService = false;
    private IQueue<Long, byte[]> queue;
    private String appName, appVersion;

    public InfluxdbPerfLogger(Config config) {
        this(null, config);
    }

    public InfluxdbPerfLogger(ScheduledExecutorService executorService, Config config) {
        this.config = config;

        this.executorService = executorService;
        if (executorService == null) {
            this.executorService = Executors.newSingleThreadScheduledExecutor();
            myOwnExecutorService = true;
        }
    }

    public InfluxdbPerfLogger init() throws Exception {
        appName = TypesafeConfigUtils.getString(AppUtils.APP_CONFIG, "app.name");
        appVersion = TypesafeConfigUtils.getString(AppUtils.APP_CONFIG, "app.version");

        server = TypesafeConfigUtils.getString(config, "influxdb.server");
        database = TypesafeConfigUtils.getString(config, "influxdb.database");
        user = TypesafeConfigUtils.getString(config, "influxdb.user");
        password = TypesafeConfigUtils.getString(config, "influxdb.password");
        retentionPolicyName = TypesafeConfigUtils.getString(config, "influxdb.retentionPolicyName");
        retentionPolicyDurationMs = TypesafeConfigUtils
                .getDurationOptional(config, "influxdb.retentionPolicyDuration", TimeUnit.MILLISECONDS)
                .orElse(DEFAULT_RETENTION);
        OkHttpClient.Builder okHttpClientBuilder = new OkHttpClient().newBuilder().connectTimeout(3, TimeUnit.SECONDS)
                .readTimeout(5, TimeUnit.SECONDS).writeTimeout(7, TimeUnit.SECONDS);
        BatchOptions batchOptions = BatchOptions.DEFAULTS.actions(10).bufferLimit(1024)
                .exceptionHandler((failedPoints, t) -> LOGGER.error(t.getMessage(), t));
        if (StringUtils.isBlank(user)) {
            influxdb = InfluxDBFactory.connect(server, user, password, okHttpClientBuilder).enableGzip()
                    .enableBatch(batchOptions);
        } else {
            influxdb = InfluxDBFactory.connect(server, okHttpClientBuilder).enableGzip().enableBatch(batchOptions);
        }

        String bufferType = TypesafeConfigUtils.getStringOptional(config, "buffer.type").orElse("memory");
        int queueBoundary = TypesafeConfigUtils.getIntegerOptional(config, "buffer.max_items")
                .orElse(DEFAULT_MAX_QUEUE_ITEMS);
        if (bufferType.equalsIgnoreCase("file")) {

        } else {
            //default is memory buffer
            AbstractQueue<Long, byte[]> queue = new UniversalInmemQueue(queueBoundary)
                    .setQueueName(this.getClass().getSimpleName() + "-" + System.currentTimeMillis());
            queue.init();
            this.queue = queue;
        }

        executorService.schedule(() -> loopSendLogs(), 1, TimeUnit.SECONDS);
        return this;
    }

    public void destroy() {
        if (executorService != null && myOwnExecutorService) {
            try {
                executorService.shutdown();
            } catch (Exception e) {
                LOGGER.error(e.getMessage(), e);
            } finally {
                executorService = null;
            }
        }

        if (queue != null) {
            try {
                if (queue instanceof AbstractQueue) {
                    ((AbstractQueue<?, byte[]>) queue).destroy();
                }
            } catch (Exception e) {
                LOGGER.error(e.getMessage(), e);
            } finally {
                queue = null;
            }
        }

        if (influxdb != null) {
            try {
                influxdb.close();
            } catch (Exception e) {
                LOGGER.error(e.getMessage(), e);
            } finally {
                influxdb = null;
            }
        }
    }

    private void initLogger() {
        if (influxdb == null || inited) {
            return;
        }
        LOGGER.info("Initializing API logger " + InfluxdbPerfLogger.class.getName() + "...");
        QueryResult qResult;

        //create database if not exist
        LOGGER.info("Creating InfluxDB database [" + database + "]...");
        qResult = influxdb.query(new Query("CREATE DATABASE " + database));
        if (qResult == null || qResult.hasError()) {
            LOGGER.warn("Has error while creating InfluxDB database [" + database + "]: " + qResult);
            return;
        }
        influxdb.setDatabase(database);

        //create retention policy
        if (!StringUtils.isBlank(retentionPolicyName)) {
            String retentionDuration = (retentionPolicyDurationMs / 1000 / 3600) + "h";
            LOGGER.info("Creating RetentionPolicy [" + database + "/" + retentionPolicyName + ":" + retentionDuration
                    + "] ");
            qResult = influxdb.query(new Query(
                    "CREATE RETENTION POLICY " + retentionPolicyName + " ON " + database + " DURATION "
                            + retentionDuration + " REPLICATION 1"));
            if (qResult == null || qResult.hasError()) {
                LOGGER.warn(
                        "Has error while creating InfluxDB retention policy [" + database + "/" + retentionPolicyName
                                + "]: " + qResult);
                return;
            }
            influxdb.setRetentionPolicy(retentionPolicyName);
        } else {
            LOGGER.info("Using default retention policy.");
        }

        inited = true;
    }

    private Map<String, Object> takeFromQueue() {
        IQueueMessage<Long, byte[]> queueMsg = queue.take();
        if (queueMsg != null) {
            queue.finish(queueMsg);
            String json = new String(queueMsg.getData(), StandardCharsets.UTF_8);
            return SerializationUtils.fromJsonString(json, Map.class);
        }
        return null;
    }

    private void loopSendLogs() {
        if (!inited) {
            try {
                initLogger();
            } catch (Exception e) {
                LOGGER.error(e.getMessage(), e);
            }
            if (executorService != null && !executorService.isShutdown() && !executorService.isTerminated()) {
                executorService.schedule(() -> loopSendLogs(), 5, TimeUnit.SECONDS);
            }
            return;
        }

        int counter = 0;
        while (true && queue != null && influxdb != null && counter < 100) {
            try {
                Map<String, Object> data = takeFromQueue();
                if (data != null) {
                    counter++;
                    String appName = DPathUtils.getValueOptional(data, "app_name", String.class).orElse("");
                    String appVersion = DPathUtils.getValueOptional(data, "app_version", String.class).orElse("");
                    String id = DPathUtils.getValueOptional(data, getFieldId(), String.class).orElse("");
                    String api = DPathUtils.getValueOptional(data, getFieldApiName(), String.class).orElse("");
                    String gw = DPathUtils.getValueOptional(data, getFieldGateway(), String.class).orElse("");
                    String stage = DPathUtils.getValueOptional(data, getFieldStage(), String.class).orElse("");
                    long timestamp = DPathUtils.getValueOptional(data, getFieldTimestampStart(), Long.class).orElse(0L);
                    int apiConcurrency = DPathUtils.getValueOptional(data, getFieldApiConcurrency(), Integer.class)
                            .orElse(0);
                    int totalConcurrency = DPathUtils.getValueOptional(data, getFieldTotalConcurrency(), Integer.class)
                            .orElse(0);
                    long duration = DPathUtils.getValueOptional(data, getFieldDuration(), Long.class).orElse(0L);

                    influxdb.write(
                            Point.measurement("api").time(timestamp, TimeUnit.MILLISECONDS).tag("app_name", appName)
                                    .tag("app_version", appVersion).tag("api", api).tag("gw", gw).tag("stage", stage)
                                    .addField("id", id).addField("api_concurrency", apiConcurrency)
                                    .addField("concurrency", totalConcurrency).addField("duration", duration).build());
                } else {
                    break;
                }
            } catch (Exception e) {
                LOGGER.error(e.getMessage(), e);
            }
        }
        if (executorService != null && !executorService.isShutdown() && !executorService.isTerminated()) {
            executorService.schedule(() -> loopSendLogs(), 1, TimeUnit.SECONDS);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void writeLog(Map<String, Object> data) {
        Map<String, Object> myData = new HashMap<>(data);
        myData.put("app_name", appName);
        myData.put("app_version", appVersion);
        String json = SerializationUtils.toJsonString(myData);
        IQueueMessage<Long, byte[]> queueMsg = queue.createMessage(json.getBytes(StandardCharsets.UTF_8));
        try {
            if (!queue.queue(queueMsg)) {
                LOGGER.error("Cannot queue log entry.");
            }
        } catch (QueueException.QueueIsFull e) {
            LOGGER.error("Cannot queue log entry because queue is full.");
        }
    }
}
