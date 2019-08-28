package com.github.btnguyen2k.mus;

import com.github.btnguyen2k.mus.utils.ApiSpec;
import com.github.btnguyen2k.mus.utils.AppUtils;
import com.github.btnguyen2k.mus.utils.SwaggerJsonHttpHandler;
import com.github.ddth.commons.utils.TypesafeConfigUtils;
import com.github.ddth.recipes.apiservice.ApiResult;
import com.github.ddth.recipes.apiservice.ApiRouter;
import com.github.ddth.recipes.global.GlobalRegistry;
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

        // return the Swagger API spec file in JSON
        rootHandler.add("/swagger.json", SwaggerJsonHttpHandler.instance);

        Map<String, Map<String, ApiSpec>> endpoints = AppUtils.buildEndpoints(appConfig);
        int maxRequestDataSize = TypesafeConfigUtils.getBytesOptional(appConfig, "api.max_request_size")
                .orElse((long) AppUtils.DEFAULT_MAX_REQUEST_SIZE).intValue();
        int requestTimeout = TypesafeConfigUtils
                .getDurationOptional(appConfig, "api" + ".request_timeout", TimeUnit.MILLISECONDS)
                .orElse((long) AppUtils.DEFAULT_REQUEST_TIMEOUT).intValue();
        endpoints.forEach((uriTemplate, handlerConfig) -> {
            rootHandler.add(uriTemplate,
                    AppUtils.buildHttpHandler(apiRouter, maxRequestDataSize, requestTimeout, handlerConfig,
                            noApiHttpHandler));
        });

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
        GlobalRegistry.putToGlobalStorage(AppUtils.GLOBAL_KEY_APP_CONFIG, AppUtils.APP_CONFIG);

        /*
         * Build API routing table from configurations.
         */
        AppUtils.API_ROUTER = AppUtils.buildApiRouter(AppUtils.APP_CONFIG);
        GlobalRegistry.putToGlobalStorage(AppUtils.GLOBAL_KEY_API_ROUTER, AppUtils.API_ROUTER);

        /*
         * Build undertow server.
         */
        Undertow undertowServer = buildUndertowServer(AppUtils.APP_CONFIG, AppUtils.API_ROUTER);
        undertowServer.start();
    }
}
