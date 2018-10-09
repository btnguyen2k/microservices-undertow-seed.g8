package com.github.btnguyen2k.mus.utils;

import com.fasterxml.jackson.databind.JsonNode;
import com.github.ddth.commons.utils.DPathUtils;
import com.github.ddth.commons.utils.SerializationUtils;
import com.github.ddth.recipes.apiservice.*;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import com.typesafe.config.ConfigResolveOptions;
import io.undertow.Undertow;
import io.undertow.UndertowOptions;
import io.undertow.io.Receiver;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.Headers;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 * Application's utility class.
 *
 * @author Thanh Nguyen <btnguyen2k@gmail.com>
 * @since template-v2.0.r1
 */
public class AppUtils {

    public final static int DEFAULT_MAX_REQUEST_SIZE = 64 * 1024;
    public final static int DEFAULT_REQUEST_TIMEOUT = 10000;

    private static Logger LOGGER = LoggerFactory.getLogger(AppUtils.class);

    private static Set<String> buildAllowedMethods(Object handlerConfig) {
        List<String> methods = DPathUtils.getValue(handlerConfig, "allowed_methods", List.class);
        Set<String> allowedMethods = new HashSet<>();
        methods.forEach(m -> allowedMethods.add(m.trim().toUpperCase()));
        return allowedMethods;
    }

    /**
     * Thrown to indicate that the size of request data is too large.
     */
    public static class RequestSizeTooLargeException extends RuntimeException {
        private int maxSize, size;

        public RequestSizeTooLargeException(int size) {
            this(size, 0);
        }

        public RequestSizeTooLargeException(int size, int maxSize) {
            this.size = size;
            this.maxSize = maxSize;
        }
    }

    /**
     * Thrown to indicate that timeout occurred while parsing the request data.
     */
    public static class RequestTimeoutException extends RuntimeException {
        private int maxTimeoutMs, timeoutMs;

        public RequestTimeoutException(int timeoutMs) {
            this(timeoutMs, 0);
        }

        public RequestTimeoutException(int timeoutMs, int maxTimeoutMs) {
            this.timeoutMs = timeoutMs;
            this.maxTimeoutMs = maxTimeoutMs;
        }
    }

    private static class MyPartialBytesCallback
            implements Receiver.PartialBytesCallback, Receiver.ErrorCallback {
        private int maxSize = 1024, timeoutMs = 10000;
        private int dataSize = 0, dataTime = 0;
        private long timestamp = System.currentTimeMillis();

        private ByteArrayOutputStream data = new ByteArrayOutputStream();
        private Exception exception;

        public MyPartialBytesCallback(int maxSize, int timeoutMs) {
            this.maxSize = maxSize;
            this.timeoutMs = timeoutMs;
        }

        @Override
        public void handle(HttpServerExchange exchange, byte[] message, boolean last) {
            if (exception != null) {
                throw exception instanceof RuntimeException
                        ? (RuntimeException) exception
                        : new RuntimeException(exception);
            }

            dataSize += message.length;
            if (dataSize > maxSize) {
                throw new RequestSizeTooLargeException(dataSize, maxSize);
            }

            try {
                data.write(message);
            } catch (IOException e) {
                LOGGER.error(e.getMessage(), e);
            }

            dataTime += System.currentTimeMillis() - timestamp;
            if (dataTime > timeoutMs) {
                throw new RequestTimeoutException(dataTime, timeoutMs);
            }
            timestamp = System.currentTimeMillis();
        }

        @Override
        public void error(HttpServerExchange exchange, IOException e) {
            this.exception = e;
        }
    }

    private static ApiParams parseParams(HttpServerExchange exchange, int maxSize, int timeoutMs) {
        MyPartialBytesCallback callback = new MyPartialBytesCallback(maxSize, timeoutMs);
        exchange.getRequestReceiver().receivePartialBytes(callback, callback);
        JsonNode dataNode = SerializationUtils.readJson(callback.data.toByteArray());
        ApiParams params = new ApiParams(dataNode);
        exchange.getQueryParameters().forEach((k, v) -> {
            List<String> paramList = new LinkedList<>();
            v.forEach(paramList::add);
            params.addParam(k, paramList.size() > 1 ? paramList : v.getFirst());
        });
        return params;
    }

    public static HttpHandler buildHttpHandler(ApiRouter apiRouter, int maxRequestDataSize,
            int requestTimeout, Object handlerConfig) {
        String handler = DPathUtils.getValue(handlerConfig, "handler", String.class);
        Set<String> allowedMethods = buildAllowedMethods(handlerConfig);
        boolean allowAllMethods = allowedMethods.size() == 0 || allowedMethods.contains("*");
        return exchange -> {
            ApiResult apiResult;
            String method = exchange.getRequestMethod().toString();
            if (allowAllMethods || allowedMethods.contains(method)) {
                ApiContext context = ApiContext.newContext("HTTP", handler);
                ApiAuth auth = ApiAuth.NULL_API_AUTH;
                try {
                    apiResult = apiRouter.callApi(context, auth,
                            parseParams(exchange, maxRequestDataSize, requestTimeout));
                } catch (RequestSizeTooLargeException e) {
                    apiResult = new ApiResult(400, "Request size too large: " + e.size + " bytes.");
                } catch (RequestTimeoutException e) {
                    apiResult = new ApiResult(400,
                            "Request timed out while parsing request data: " + e.timeoutMs
                                    + " ms.");
                } catch (Exception e) {
                    apiResult = new ApiResult(500, e.getMessage());
                }
            } else {
                apiResult = new ApiResult(403, "Method [" + method + "] is not allowed!");
            }
            exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "application/json");
            exchange.getResponseSender()
                    .send(apiResult.asJson().toString(), StandardCharsets.UTF_8);
        };
    }

    /**
     * Build Undertow server using specified configurations.
     *
     * @param appConfig
     * @return
     */
    public static Undertow.Builder buildUndertowServer(Config appConfig) {
        int httpPort = ConfigUtils.getConfigAsInt(appConfig, "api.http.port", 0);
        if (httpPort <= 0) {
            return null;
        }
        String httpBind = ConfigUtils.getConfigAsString(appConfig, "api.http.address");
        httpBind = httpBind != null ? httpBind : "localhost";
        Undertow.Builder builder = Undertow.builder()
                .setServerOption(UndertowOptions.ENABLE_HTTP2, true);
        {
            LOGGER.info("Starting HTTP server on [" + httpBind + ":" + httpPort + "]...");
            builder.addHttpListener(httpPort, httpBind);
        }
        return builder;
    }

    /**
     * Load a class and create an object of that class.
     *
     * @param className
     * @param clazz
     * @param <T>
     * @return
     */
    public static <T> T loadClassAndCreateObject(String className, Class<T> clazz) {
        try {
            Class<?> objClass = Class.forName(className);
            if (!clazz.isAssignableFrom(objClass)) {
                LOGGER.error("Class [" + className + "] must implement/extends [" + clazz + "]!");
            }
            return (T) objClass.newInstance();
        } catch (ClassNotFoundException e) {
            LOGGER.warn("Cannot find class [" + className + "]!");
            return null;
        } catch (IllegalAccessException | InstantiationException e) {
            LOGGER.error(e.getMessage(), e);
            return null;
        }
    }

    /**
     * Load application's config.
     *
     * @param defaultConfigFile
     * @return
     */
    public static Config loadConfig(String defaultConfigFile) {
        String cmdConfigFile = System.getProperty("config.file", defaultConfigFile);
        File configFile = new File(cmdConfigFile);
        if (!configFile.isFile() || !configFile.canRead()) {
            if (StringUtils.equals(cmdConfigFile, defaultConfigFile)) {
                String msg = "Cannot read from config file [" + configFile.getAbsolutePath() + "]!";
                LOGGER.error(msg);
                throw new RuntimeException(msg);
            } else {
                LOGGER.warn("Configuration file [" + configFile.getAbsolutePath()
                        + "], is invalid or not readable, fallback to default!");
                configFile = new File(defaultConfigFile);
            }
        }
        LOGGER.info("Loading configuration from [" + configFile + "]...");
        if (!configFile.isFile() || !configFile.canRead()) {
            String msg = "Cannot read from config file [" + configFile.getAbsolutePath() + "]!";
            LOGGER.error(msg);
            throw new RuntimeException(msg);
        }
        Config config = ConfigFactory.parseFile(configFile)
                .resolve(ConfigResolveOptions.defaults().setUseSystemEnvironment(true));
        LOGGER.info("Application config: {}", config);
        return config;
    }
}
