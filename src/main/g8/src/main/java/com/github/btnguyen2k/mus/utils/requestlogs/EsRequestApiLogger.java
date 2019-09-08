package com.github.btnguyen2k.mus.utils.requestlogs;

import com.github.btnguyen2k.mus.utils.AppUtils;
import com.github.btnguyen2k.mus.utils.BufferUtils;
import com.github.ddth.commons.utils.DPathUtils;
import com.github.ddth.commons.utils.DateFormatUtils;
import com.github.ddth.commons.utils.TypesafeConfigUtils;
import com.github.ddth.queue.IQueue;
import com.github.ddth.queue.utils.QueueException;
import com.typesafe.config.Config;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.elasticsearch.client.RestHighLevelClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Log API request/response to ElasticSearch.
 *
 * @author Thanh Nguyen <btnguyen2k@gmail.com>
 * @since template-v2.0.r4
 */
public class EsRequestApiLogger extends AbstractRequestApiLogger {
    private final static int DEFAULT_MAX_QUEUE_ITEMS = 16 * 1024;

    private Logger LOGGER = LoggerFactory.getLogger(EsRequestApiLogger.class);

    private boolean inited = false;
    private ScheduledExecutorService executorService;
    private boolean myOwnExecutorService = false;

    private RequestOptions requestOptions;
    private RestHighLevelClient es;
    private String server, user, password, index;
    private IQueue<Long, byte[]> queue;

    public EsRequestApiLogger(Config config) {
        this(null, config);
    }

    public EsRequestApiLogger(ScheduledExecutorService executorService, Config config) {
        super(config);

        this.executorService = executorService;
        if (executorService == null) {
            this.executorService = Executors.newSingleThreadScheduledExecutor();
            myOwnExecutorService = true;
        }
    }

    public EsRequestApiLogger init() throws Exception {
        super.init();

        requestOptions = RequestOptions.DEFAULT.toBuilder().build();

        server = TypesafeConfigUtils.getString(getConfig(), "elasticsearch.server");
        user = TypesafeConfigUtils.getString(getConfig(), "elasticsearch.user");
        password = TypesafeConfigUtils.getString(getConfig(), "elasticsearch.password");
        index = TypesafeConfigUtils.getString(getConfig(), "elasticsearch.index");
        RestClientBuilder builder = RestClient.builder(HttpHost.create(server))
                .setRequestConfigCallback(rcb -> rcb.setConnectTimeout(3000).setSocketTimeout(60000));
        if (!StringUtils.isBlank(user)) {
            CredentialsProvider cp = new BasicCredentialsProvider();
            cp.setCredentials(AuthScope.ANY, new UsernamePasswordCredentials(user, password));
            builder.setHttpClientConfigCallback(hcp -> hcp.setDefaultCredentialsProvider(cp));
        }
        es = new RestHighLevelClient(builder);

        queue = BufferUtils.tryCreateFileBuffer(getConfig());
        if (queue == null) {
            queue = BufferUtils.createMemoryBuffer(getConfig(), DEFAULT_MAX_QUEUE_ITEMS);
        }

        executorService.schedule(() -> loopSendLogs(), 1, TimeUnit.SECONDS);
        return this;
    }

    public void destroy() {
        AppUtils.close(executorService, myOwnExecutorService);
        executorService = null;

        AppUtils.close(queue, true);
        queue = null;

        AppUtils.close(es, true);
        es = null;
    }

    private void initLogger() {
        if (es == null || inited) {
            return;
        }
        LOGGER.info("Initializing API logger " + EsRequestApiLogger.class.getName() + "...");

        try {
            if (!es.ping(requestOptions)) {
                return;
            }
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
            return;
        }

        LOGGER.info("API logger " + EsRequestApiLogger.class.getName() + " initialized.");
        inited = true;
    }

    private static Pattern pattern = Pattern.compile("#\\{([^\\}].*?)\\}");

    private String calcIndexName(Date timestamp) {
        Matcher m = pattern.matcher(index);
        StringBuffer result = new StringBuffer();
        while (m.find()) {
            m.appendReplacement(result, DateFormatUtils.toString(timestamp, m.group(1)));
        }
        m.appendTail(result);
        return result.toString();
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
        while (true && queue != null && es != null && counter < 100) {
            try {
                Map<String, Object> data = AppUtils.takeFromQueue(queue);
                if (data != null) {
                    counter++;
                    String id = DPathUtils.getValue(data, getFieldId(), String.class);
                    String stage = DPathUtils.getValue(data, getFieldStage(), String.class);
                    long timestamp = DPathUtils.getValueOptional(data, getFieldTimestampStart(), Long.class).orElse(0L);
                    Date date = new Date(timestamp);
                    data.put(getFieldTimestampStart(), date);
                    IndexRequest request = new IndexRequest(calcIndexName(date)).id(id + "-" + stage).source(data);
                    es.index(request, requestOptions);
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
        try {
            if (!AppUtils.write(queue, data)) {
                LOGGER.error("Cannot queue log entry.");
            }
        } catch (QueueException.QueueIsFull e) {
            LOGGER.error("Cannot queue log entry because queue is full.");
        }
    }
}
