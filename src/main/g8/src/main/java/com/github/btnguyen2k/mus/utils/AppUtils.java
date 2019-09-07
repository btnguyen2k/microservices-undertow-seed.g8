package com.github.btnguyen2k.mus.utils;

import com.fasterxml.jackson.databind.JsonNode;
import com.github.btnguyen2k.mus.utils.httphandlers.ParseApiAuthHttpHandler;
import com.github.btnguyen2k.mus.utils.perflogs.InfluxdbPerfLogger;
import com.github.ddth.commons.utils.DPathUtils;
import com.github.ddth.commons.utils.SerializationUtils;
import com.github.ddth.commons.utils.TypesafeConfigUtils;
import com.github.ddth.recipes.apiservice.*;
import com.github.ddth.recipes.apiservice.filters.LoggingFilter;
import com.github.ddth.recipes.apiservice.logging.PrintStreamPerfApiLogger;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import com.typesafe.config.ConfigResolveOptions;
import io.swagger.v3.oas.annotations.Operation;
import io.undertow.Undertow;
import io.undertow.UndertowOptions;
import io.undertow.io.Receiver;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.AttachmentKey;
import io.undertow.util.Headers;
import io.undertow.util.HttpString;
import io.undertow.util.StatusCodes;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * Application's utility class.
 *
 * @author Thanh Nguyen <btnguyen2k@gmail.com>
 * @since template-v2.0.r1
 */
public class AppUtils {
    /**
     * @since template-v2.0.r2
     */
    public final static AttachmentKey<ApiAuth> ATTKEY_API_AUTH = AttachmentKey.create(ApiAuth.class);

    /**
     * @since template-v2.0.r2
     */
    public static Config APP_CONFIG;
    /**
     * @since template-v2.0.r3
     */
    public static String GLOBAL_KEY_APP_CONFIG = "APP_CONFIG";

    /**
     * @since template-v2.0.r3
     */
    public static ApiRouter API_ROUTER;
    /**
     * @since template-v2.0.r3
     */
    public static String GLOBAL_KEY_API_ROUTER = "API_ROUTER";

    /**
     * @since template-v2.0.r3
     */
    public static String GLOBAL_KEY_SPRING_APP_CONTEXT = "SPRING_APP_CONTEXT";

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

    private static class MyPartialBytesCallback implements Receiver.PartialBytesCallback, Receiver.ErrorCallback {
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
                throw exception instanceof RuntimeException ?
                        (RuntimeException) exception :
                        new RuntimeException(exception);
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

    /**
     * Construct a {@link HttpHandler} to handle a HTTP request.
     *
     * @param apiRouter
     * @param maxRequestDataSize
     * @param requestTimeout
     * @param handlerConfigMap   mapping {http-method-name:api-spec}
     * @return
     */
    public static HttpHandler buildHttpHandler(ApiRouter apiRouter, int maxRequestDataSize, int requestTimeout,
            Map<String, ApiSpec> handlerConfigMap) {
        return buildHttpHandler(apiRouter, maxRequestDataSize, requestTimeout, handlerConfigMap, null);
    }

    /**
     * Construct a {@link HttpHandler} to handle a HTTP request.
     *
     * @param apiRouter
     * @param maxRequestDataSize
     * @param requestTimeout
     * @param handlerConfigMap   mapping {http-method-name:api-spec}
     * @param defaultHandler     default handler to invoked when no http-method matched
     * @return
     * @since template-v2.0.r3
     */
    public static HttpHandler buildHttpHandler(ApiRouter apiRouter, int maxRequestDataSize, int requestTimeout,
            Map<String, ApiSpec> handlerConfigMap, HttpHandler defaultHandler) {
        HttpHandler myDefaultHandler = defaultHandler != null ?
                defaultHandler :
                exchange -> exchange.setStatusCode(StatusCodes.NOT_IMPLEMENTED).endExchange();
        Map<String, String> myHandlerConfigMap = new HashMap<>();
        handlerConfigMap.forEach((k, v) -> myHandlerConfigMap.put(k.toUpperCase(), v.getHandlerName()));
        String catchAllHandlerName = myHandlerConfigMap.get("*");
        HttpHandler next = exchange -> {
            String handlerName = catchAllHandlerName != null ?
                    catchAllHandlerName :
                    myHandlerConfigMap.get(exchange.getRequestMethod().toString().toUpperCase());
            if (handlerName != null) {
                ApiResult apiResult;
                ApiContext context = ApiContext.newContext("HTTP", handlerName);
                ApiAuth auth = exchange.getAttachment(ATTKEY_API_AUTH);
                try {
                    apiResult = apiRouter
                            .callApi(context, auth, parseParams(exchange, maxRequestDataSize, requestTimeout));
                } catch (RequestSizeTooLargeException e) {
                    apiResult = new ApiResult(400, "Request size too large: " + e.size + " bytes.");
                } catch (RequestTimeoutException e) {
                    apiResult = new ApiResult(400,
                            "Request timed out while parsing request data: " + e.timeoutMs + " ms.");
                } catch (Exception e) {
                    apiResult = new ApiResult(500, e.getMessage());
                }
                exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "application/json")
                        .put(new HttpString("Access-Control-Allow-Origin"), "*");
                exchange.getResponseSender().send(apiResult.asJson().toString(), StandardCharsets.UTF_8);
            } else {
                myDefaultHandler.handleRequest(exchange);
            }
        };
        String httpHeaderAppId = TypesafeConfigUtils.getStringOptional(APP_CONFIG, "api.http_header_app_id")
                .orElse("X-App-Id");
        String httpHeaderAccessToken = TypesafeConfigUtils.getStringOptional(APP_CONFIG, "api.http_header_access_token")
                .orElse("X-Access-Token");
        return new ParseApiAuthHttpHandler(next, httpHeaderAppId, httpHeaderAccessToken);
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
        Undertow.Builder builder = Undertow.builder().setServerOption(UndertowOptions.ENABLE_HTTP2, true);
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
                LOGGER.error("Class [" + className + "] must implement/extends [" + clazz + "].");
            }
            return (T) objClass.newInstance();
        } catch (ClassNotFoundException e) {
            LOGGER.warn("Cannot find class [" + className + "].");
            return null;
        } catch (IllegalAccessException | InstantiationException e) {
            LOGGER.error(e.getMessage(), e);
            return null;
        }
    }

    /**
     * Load application's configuration from file.
     *
     * <p>Configurations are first load from system property "config.file". If the system property is not defined, "defaultConfigFile" is used.</p>
     *
     * @param defaultConfigFile
     * @return
     */
    public static Config loadConfig(String defaultConfigFile) {
        String cmdConfigFile = System.getProperty("config.file", defaultConfigFile);
        File configFile = new File(cmdConfigFile);
        if (!configFile.isFile() || !configFile.canRead()) {
            if (StringUtils.equals(cmdConfigFile, defaultConfigFile)) {
                String msg = "Cannot read from config file [" + configFile.getAbsolutePath() + "].";
                LOGGER.error(msg);
                throw new RuntimeException(msg);
            } else {
                LOGGER.warn("Configuration file [" + configFile.getAbsolutePath()
                        + "], is invalid or not readable, fallback to default.");
                configFile = new File(defaultConfigFile);
            }
        }
        LOGGER.info("Loading configuration from [" + configFile + "]...");
        if (!configFile.isFile() || !configFile.canRead()) {
            String msg = "Cannot read from config file [" + configFile.getAbsolutePath() + "].";
            LOGGER.error(msg);
            throw new RuntimeException(msg);
        }
        Config config = ConfigFactory.parseFile(configFile)
                .resolve(ConfigResolveOptions.defaults().setUseSystemEnvironment(true));
        LOGGER.info("Application config: {}", config);
        return config;
    }

    private static Collection<ReflectionUtils.ApiHandlerWithAnnotations<Operation>> cachedAnnotatedHandlers;

    private static Collection<ReflectionUtils.ApiHandlerWithAnnotations<Operation>> scanAnnotatedHandlers(
            Config appConfig) {
        if (cachedAnnotatedHandlers == null) {
            List<String> scanPackages = TypesafeConfigUtils.getStringList(appConfig, "api.scan_packages");
            if (scanPackages != null && scanPackages.size() > 0) {
                cachedAnnotatedHandlers = ReflectionUtils.findApiHandlerAnnotatedWith(Operation.class,
                        scanPackages.toArray(ArrayUtils.EMPTY_STRING_ARRAY));
            } else {
                cachedAnnotatedHandlers = new HashSet<>();
            }
        }
        return cachedAnnotatedHandlers;
    }

    private static Map<String, ApiSpec> cachedApiSpecs = new HashMap<>();

    private static ApiRouter cachedApiRouter = null;

    private static IApiLogger buildPerfLogger(Config config) throws Exception {
        String destination = TypesafeConfigUtils.getStringOptional(config, "api.perf_log.destination").orElse("");
        if (destination.equalsIgnoreCase("console")) {
            return PrintStreamPerfApiLogger.STDOUT_LOGGER;
        }
        if (destination.equalsIgnoreCase("influx") || destination.equalsIgnoreCase("influxdb")) {
            String server = TypesafeConfigUtils.getString(config, "api.perf_log.influxdb.server");
            if (StringUtils.isBlank(server)) {
                throw new RuntimeException("Performance log destination is [" + destination
                        + "], but no InfluxDB server configured at key [api.perf_log.influxdb.server].");
            }
            String database = TypesafeConfigUtils.getString(config, "api.perf_log.influxdb.database");
            if (StringUtils.isBlank(database)) {
                throw new RuntimeException("Performance log destination is [" + destination
                        + "], but no InfluxDB database configured at key [api.perf_log.influxdb.database].");
            }
            InfluxdbPerfLogger apiLogger = new InfluxdbPerfLogger(config.getConfig("api.perf_log"));
            Runtime.getRuntime().addShutdownHook(new Thread(() -> apiLogger.destroy()));
            apiLogger.init();
            return apiLogger;
        }
        return null;
    }

    /**
     * Build {@link ApiRouter} from configurations.
     *
     * @param appConfig
     * @return
     */
    public static ApiRouter buildApiRouter(Config appConfig) throws Exception {
        if (cachedApiRouter == null) {
            cachedApiRouter = new ApiRouter();
            Runtime.getRuntime().addShutdownHook(new Thread(() -> cachedApiRouter.destroy()));
            {
                IApiLogger perfLogger = buildPerfLogger(appConfig);
                if (perfLogger != null) {
                    cachedApiRouter.setApiFilter(new LoggingFilter(cachedApiRouter, perfLogger));
                }
            }
            cachedApiRouter.init();

            //collect handlers from annotation
            Collection<ReflectionUtils.ApiHandlerWithAnnotations<Operation>> annotatedHandlers = scanAnnotatedHandlers(
                    appConfig);
            annotatedHandlers.forEach(entry -> {
                Operation operation = entry.annotations.iterator().next();
                ApiSpec apiSpec = ApiSpec.newInstance(operation);
                String hName = apiSpec.getHandlerName();
                if (!StringUtils.isBlank(hName)) {
                    cachedApiSpecs.put(hName, apiSpec);
                    IApiHandler existing = cachedApiRouter.getApiHandlers().get(hName);
                    if (existing == null) {
                        cachedApiRouter.addApiHandler(hName, entry.apiHandler);
                        LOGGER.info("Registered API handler [" + hName + ":" + entry.apiHandler.getClass().getName()
                                + "] from annotation.");
                    } else {
                        LOGGER.warn(
                                "API handler [" + hName + "] has already registered to class [" + existing.getClass()
                                        .getName() + "], cannot register to class [" + entry.apiHandler.getClass()
                                        .getName() + "] from annotation.");
                    }
                }
            });

            //collect handlers from application configurations
            Map<String, Object> apiHandlerConfig = TypesafeConfigUtils.getObject(appConfig, "api.handlers", Map.class);
            if (apiHandlerConfig != null) {
                apiHandlerConfig.forEach((hName, hClazz) -> {
                    IApiHandler apiHandler = AppUtils.loadClassAndCreateObject(hClazz.toString(), IApiHandler.class);
                    if (apiHandler != null) {
                        cachedApiRouter.addApiHandler(hName, apiHandler);
                        LOGGER.info("Registered API handler [" + hName + ":" + hClazz + "] from app-config.");
                    } else {
                        LOGGER.warn("Cannot register API handler for [" + hName + ":" + hClazz + "].");
                    }
                });
            }
        }

        return cachedApiRouter;
    }

    private static Map<String, Map<String, ApiSpec>> cachedEnpoints;

    /**
     * @param appConfig
     * @return
     * @since template-v2.0.r3
     */
    public static Map<String, Map<String, ApiSpec>> buildEndpoints(Config appConfig) {
        if (cachedEnpoints == null) {
            cachedEnpoints = new TreeMap<>();

            //build endpoints from annotation
            Collection<ReflectionUtils.ApiHandlerWithAnnotations<Operation>> annotatedHandlers = scanAnnotatedHandlers(
                    appConfig);
            annotatedHandlers.forEach(entry -> {
                Operation operation = entry.annotations.iterator().next();
                ApiSpec apiSpec = ApiSpec.newInstance(operation);
                String hName = apiSpec.getHandlerName();
                if (!StringUtils.isBlank(hName)) {
                    cachedApiSpecs.put(hName, apiSpec);
                    //method and uri are encoded in Operation.method with format <method>:<uri>
                    String[] tokens = operation.method().split(":");
                    if (tokens != null && tokens.length > 1) {
                        String method = tokens[0];
                        String uri = tokens[1];
                        Map<String, ApiSpec> handlerMappings = cachedEnpoints.getOrDefault(uri, new TreeMap<>());
                        cachedEnpoints.put(uri, handlerMappings);
                        handlerMappings.put(method, apiSpec);
                        LOGGER.info("Endpoint [" + tokens[0] + "]" + tokens[1] + " from annotation.");
                    }
                }
            });

            //build endpoints from application configurations
            Map<?, ?> apiEndpoints = TypesafeConfigUtils.getObject(appConfig, "api.endpoints", Map.class);
            if (apiEndpoints != null) {
                apiEndpoints.forEach((uriTemplate, _handlerConfig) -> {
                    if (!(_handlerConfig instanceof Map)) {
                        LOGGER.warn("Invalid handler configurations. Expecting a map, but received " + _handlerConfig
                                .getClass() + " / " + _handlerConfig);
                    } else {
                        Map<String, String> myHandlerConfigMap = new HashMap<>();
                        ((Map<?, ?>) _handlerConfig)
                                .forEach((k, v) -> myHandlerConfigMap.put(k.toString().toUpperCase(), v.toString()));
                        String catchAllHandlerName = myHandlerConfigMap.get("*");
                        if (!StringUtils.isBlank(catchAllHandlerName)) {
                            Map<String, ApiSpec> handlerMappings = cachedEnpoints
                                    .getOrDefault(uriTemplate.toString(), new TreeMap<>());
                            cachedEnpoints.put(uriTemplate.toString(), handlerMappings);
                            //default is GET
                            handlerMappings.put("GET",
                                    cachedApiSpecs.getOrDefault(catchAllHandlerName, new ApiSpec(catchAllHandlerName)));
                            LOGGER.info("Endpoint [GET]" + uriTemplate + " from app-config.");
                        } else {
                            Map<String, ApiSpec> handlerMappings = cachedEnpoints
                                    .getOrDefault(uriTemplate.toString(), new TreeMap<>());
                            cachedEnpoints.put(uriTemplate.toString(), handlerMappings);
                            myHandlerConfigMap.forEach((method, handlerName) -> handlerMappings
                                    .put(method, cachedApiSpecs.getOrDefault(handlerName, new ApiSpec(handlerName))));
                            LOGGER.info("Endpoint " + handlerMappings.keySet() + uriTemplate.toString()
                                    + " from app-config.");
                        }
                    }
                });
            }
        }

        return cachedEnpoints;
    }
}
