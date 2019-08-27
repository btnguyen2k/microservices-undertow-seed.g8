package com.github.btnguyen2k.mus;

import com.github.btnguyen2k.mus.utils.AppUtils;
import com.github.ddth.commons.utils.TypesafeConfigUtils;
import com.github.ddth.recipes.apiservice.ApiResult;
import com.github.ddth.recipes.apiservice.ApiRouter;
import com.typesafe.config.Config;
import io.undertow.Undertow;
import io.undertow.server.HttpHandler;
import io.undertow.server.handlers.PathTemplateHandler;
import io.undertow.util.Headers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Application's bootstrap class.
 *
 * @author Thanh Nguyen <btnguyen2k@gmail.com>
 * @since template-v2.0.r1
 */
public class Bootstrap {

    private static Logger LOGGER = LoggerFactory.getLogger(Bootstrap.class);

    private static Undertow buildUndertowServer(Config appConfig, ApiRouter apiRouter) {
        Undertow.Builder builder = AppUtils.buildUndertowServer(appConfig);
        if (builder == null) {
            throw new RuntimeException("Cannot build HTTP server!");
        }

        // default "no-api" handler
        HttpHandler noApiHttpHandler = exchange -> {
            exchange.setStatusCode(ApiResult.STATUS_NOT_IMPLEMENTED).
                    getResponseHeaders().put(Headers.CONTENT_TYPE, "text/plain");
            exchange.getResponseSender()
                    .send("No API handler for [" + exchange.getRequestMethod() + ":" + exchange.getRequestURI() + "]");
        };
        PathTemplateHandler rootHandler = new PathTemplateHandler(noApiHttpHandler, true);

        Map<?, ?> apiRoutes = TypesafeConfigUtils.getObject(appConfig, "api.routes", Map.class);
        if (apiRoutes != null) {
            int maxRequestDataSize = TypesafeConfigUtils.getBytesOptional(appConfig, "api.max_request_size")
                    .orElse((long) AppUtils.DEFAULT_MAX_REQUEST_SIZE).intValue();
            int requestTimeout = TypesafeConfigUtils
                    .getDurationOptional(appConfig, "api" + ".request_timeout", TimeUnit.MILLISECONDS)
                    .orElse((long) AppUtils.DEFAULT_REQUEST_TIMEOUT).intValue();
            apiRoutes.forEach((uriTemplate, handlerConfig) -> {
                LOGGER.info("Setting up handler {}...", uriTemplate);
                if (!(handlerConfig instanceof Map)) {
                    LOGGER.warn(
                            "Invalid handler configurations. Expecting a map, but received " + handlerConfig.getClass()
                                    + " / " + handlerConfig);
                } else {
                    Map<?, ?> handlerConfigMap = (Map<?, ?>) handlerConfig;
                    rootHandler.add(uriTemplate.toString(),
                            AppUtils.buildHttpHandler(apiRouter, maxRequestDataSize, requestTimeout, handlerConfigMap,
                                    noApiHttpHandler));
                }
            });
        }

        Undertow undertowServer = builder.setHandler(rootHandler).build();
        Runtime.getRuntime().addShutdownHook(new Thread(() -> undertowServer.stop()));
        return undertowServer;
    }

    public static void main(String[] args) {
        /*
         * Load configuration file, default location "conf/application.conf".
         * System's property "config.file" can override the location value.
         */
        AppUtils.APP_CONFIG = AppUtils.loadConfig("conf/application.conf");

        /*
         * Build API routing table from configurations.
         */
        ApiRouter apiRouter = AppUtils.buildApiRouter(AppUtils.APP_CONFIG);

        /*
         * Build undertow server.
         */
        Undertow undertowServer = buildUndertowServer(AppUtils.APP_CONFIG, apiRouter);
        undertowServer.start();
    }
}
