package com.github.btnguyen2k.mus;

import com.github.btnguyen2k.mus.utils.AppUtils;
import com.github.ddth.commons.utils.DPathUtils;
import com.github.ddth.commons.utils.TypesafeConfigUtils;
import com.github.ddth.recipes.apiservice.ApiRouter;
import com.github.ddth.recipes.apiservice.IApiHandler;
import com.github.ddth.recipes.apiservice.auth.AllowAllApiAuthenticator;
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
            exchange.getResponseSender()
                    .send("No handler defined for URI [" + exchange.getRequestURI() + "]!");
        }, true);

        Map<?, ?> apiRoutes = TypesafeConfigUtils.getObject(appConfig, "api.routes", Map.class);
        if (apiRoutes != null) {
            HttpHandler httpHandlerNoApiHandler = exchange -> {
                exchange.setStatusCode(404).
                        getResponseHeaders().put(Headers.CONTENT_TYPE, "text/plain");
                exchange.getResponseSender()
                        .send("No API handler registered for URI [" + exchange.getRequestURI()
                                + "]!");
            };
            int maxRequestDataSize = TypesafeConfigUtils
                    .getBytesOptional(appConfig, "api.max_request_size")
                    .orElse((long) AppUtils.DEFAULT_MAX_REQUEST_SIZE).intValue();
            int requestTimeout = TypesafeConfigUtils
                    .getDurationOptional(appConfig, "api" + ".request_timeout",
                            TimeUnit.MILLISECONDS).orElse((long) AppUtils.DEFAULT_REQUEST_TIMEOUT)
                    .intValue();
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
                            AppUtils.buildHttpHandler(apiRouter, maxRequestDataSize, requestTimeout,
                                    handlerConfig));
                }
            });
        }

        Undertow undertowServer = builder.setHandler(rootHandler).build();
        Runtime.getRuntime().addShutdownHook(new Thread(() -> undertowServer.stop()));
        return undertowServer;
    }

    private static ApiRouter buildApiRouter(Config appConfig) {
        ApiRouter apiRouter = new ApiRouter();
        apiRouter.setApiAuthenticator(AllowAllApiAuthenticator.instance);
        apiRouter.init();

        Map<String, Object> apiHandlerConfig = TypesafeConfigUtils
                .getObject(appConfig, "api.handlers", Map.class);
        if (apiHandlerConfig != null) {
            apiHandlerConfig.forEach((hName, hClazz) -> {
                IApiHandler apiHandler = AppUtils
                        .loadClassAndCreateObject(hClazz.toString(), IApiHandler.class);
                if (apiHandler != null) {
                    apiRouter.addApiHandler(hName, apiHandler);
                    LOGGER.info(
                            "Registered class [" + hClazz + "] for API handler [" + hName + "]!");
                } else {
                    LOGGER.warn("Cannot register API handler for [" + hName + "]!");
                }
            });
        }

        Runtime.getRuntime().addShutdownHook(new Thread(() -> apiRouter.destroy()));
        return apiRouter;
    }

    public static void main(String[] args) {
        Config appConfig = AppUtils.loadConfig("conf/application.conf");
        ApiRouter apiRouter = buildApiRouter(appConfig);
        Undertow undertowServer = buildUndertowServer(appConfig, apiRouter);
        undertowServer.start();
    }
}
