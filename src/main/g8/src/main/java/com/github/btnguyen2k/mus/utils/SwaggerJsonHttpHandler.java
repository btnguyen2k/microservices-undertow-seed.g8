package com.github.btnguyen2k.mus.utils;

import com.github.ddth.commons.utils.MapUtils;
import com.github.ddth.commons.utils.SerializationUtils;
import com.github.ddth.commons.utils.TypesafeConfigUtils;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.Headers;
import io.undertow.util.HttpString;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Map;
import java.util.TreeMap;

public class SwaggerJsonHttpHandler implements HttpHandler {
    public static SwaggerJsonHttpHandler instance = new SwaggerJsonHttpHandler();

    private Map<String, Object> baseRoot = new TreeMap<>();

    public SwaggerJsonHttpHandler() {
        baseRoot.put("swagger", "2.0");
        baseRoot.put("info",
                MapUtils.createMap("title", TypesafeConfigUtils.getString(AppUtils.APP_CONFIG, "app.name"), "version",
                        TypesafeConfigUtils.getString(AppUtils.APP_CONFIG, "app.version"), "description",
                        TypesafeConfigUtils.getString(AppUtils.APP_CONFIG, "app.desc")));
        baseRoot.put("securityDefinitions", MapUtils.createMap("AppIdHeader",
                MapUtils.createMap("type", "apiKey", "in", "header", "name",
                        TypesafeConfigUtils.getString(AppUtils.APP_CONFIG, "api.http_header_app_id")),
                "AccessTokenHeader", MapUtils.createMap("type", "apiKey", "in", "header", "name",
                        TypesafeConfigUtils.getString(AppUtils.APP_CONFIG, "api.http_header_access_token"))));
        baseRoot.put("security", Arrays.asList(MapUtils.createMap("AppIdHeader", Arrays.asList()),
                MapUtils.createMap("AccessTokenHeader", Arrays.asList())));
        baseRoot.put("produces", Arrays.asList("application/json"));
        baseRoot.put("consumes", Arrays.asList("application/json"));

        Map<String, Object> paths = new TreeMap<>();
        baseRoot.put("paths", paths);
        Map<String, Map<String, ApiSpec>> endpoints = AppUtils.buildEndpoints(AppUtils.APP_CONFIG);
        endpoints.forEach((epUri, epConfig) -> {
            Map<String, Object> endpoint = new TreeMap<>();
            paths.put(epUri, endpoint);
            epConfig.forEach((method, spec) -> {
                Map<String, Object> epSpec = new TreeMap<>();
                endpoint.put(method.equals("*") ? "GET" : method.toLowerCase(), epSpec);
                epSpec.put("operationId", spec.getHandlerName());
            });
        });
    }

    @Override
    public void handleRequest(HttpServerExchange exchange) throws Exception {
        Map<String, Object> root = new TreeMap<>(baseRoot);
        root.put("host", exchange.getRequestScheme() + "://" + exchange.getHostAndPort());

        exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "application/json")
                .put(new HttpString("Access-Control-Allow-Origin"), "*");
        exchange.getResponseSender().send(SerializationUtils.toJsonString(root), StandardCharsets.UTF_8);
    }
}
