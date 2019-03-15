package com.github.btnguyen2k.mus;

import com.github.btnguyen2k.mus.utils.AppUtils;
import com.github.ddth.commons.utils.DPathUtils;
import com.github.ddth.commons.utils.TypesafeConfigUtils;
import com.github.ddth.recipes.apiservice.ApiRouter;
import com.typesafe.config.Config;
import io.undertow.Undertow;
import io.undertow.server.HttpHandler;
import io.undertow.server.handlers.PathTemplateHandler;
import io.undertow.util.Headers;
import org.apache.commons.lang3.StringUtils;
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

        PathTemplateHandler rootHandler = new PathTemplateHandler(exchange -> {
            exchange.setStatusCode(404).
                    getResponseHeaders().put(Headers.CONTENT_TYPE, "text/plain");
            exchange.getResponseSender().send("No handler defined for URI [" + exchange.getRequestURI() + "]!");
        }, true);

        Map<?, ?> apiRoutes = TypesafeConfigUtils.getObject(appConfig, "api.routes", Map.class);
        if (apiRoutes != null) {
            HttpHandler httpHandlerNoApiHandler = exchange -> {
                exchange.setStatusCode(404).
                        getResponseHeaders().put(Headers.CONTENT_TYPE, "text/plain");
                exchange.getResponseSender()
                        .send("No API handler registered for URI [" + exchange.getRequestURI() + "]!");
            };
            int maxRequestDataSize = TypesafeConfigUtils.getBytesOptional(appConfig, "api.max_request_size")
                    .orElse((long) AppUtils.DEFAULT_MAX_REQUEST_SIZE).intValue();
            int requestTimeout = TypesafeConfigUtils
                    .getDurationOptional(appConfig, "api" + ".request_timeout", TimeUnit.MILLISECONDS)
                    .orElse((long) AppUtils.DEFAULT_REQUEST_TIMEOUT).intValue();
            apiRoutes.forEach((uriTemplate, handlerConfig) -> {
                LOGGER.info("Setting up handler {}...", uriTemplate);
                String handler = DPathUtils.getValue(handlerConfig, "handler", String.class);
                if (StringUtils.isBlank(handler)) {
                    LOGGER.error("No handler defined for URI [" + uriTemplate + "]!");
                } else if (!apiRouter.getApiHandlers().containsKey(handler)) {
                    rootHandler.add(uriTemplate.toString(), httpHandlerNoApiHandler);
                    LOGGER.error("No API handler registered for URI [" + uriTemplate + "]!");
                } else {
                    rootHandler.add(uriTemplate.toString(),
                            AppUtils.buildHttpHandler(apiRouter, maxRequestDataSize, requestTimeout, handlerConfig));
                }
            });
        }

        Undertow undertowServer = builder.setHandler(rootHandler).build();
        Runtime.getRuntime().addShutdownHook(new Thread(() -> undertowServer.stop()));
        return undertowServer;
    }

    public static void main(String[] args) {
        /*
        Load configuration file, default location "conf/application.conf".
        System's property "config.file" can override the location value.
         */
        AppUtils.APP_CONFIG = AppUtils.loadConfig("conf/application.conf");

        /*
        Build API routing table from configurations.
         */
        ApiRouter apiRouter = AppUtils.buildApiRouter(AppUtils.APP_CONFIG);

        /*
        Build undertow server.
         */
        Undertow undertowServer = buildUndertowServer(AppUtils.APP_CONFIG, apiRouter);
        undertowServer.start();
    }
}
