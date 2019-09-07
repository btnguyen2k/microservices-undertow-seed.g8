package com.github.btnguyen2k.mus.utils.httphandlers;

import com.github.btnguyen2k.mus.utils.ApiSpec;
import com.github.btnguyen2k.mus.utils.AppUtils;
import com.github.ddth.commons.utils.MapUtils;
import com.github.ddth.commons.utils.SerializationUtils;
import com.github.ddth.commons.utils.TypesafeConfigUtils;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.Headers;
import io.undertow.util.HttpString;
import org.apache.commons.lang3.StringUtils;

import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * Return the Swagger API spec file in JSON.
 *
 * @author Thanh Nguyen <btnguyen2k@gmail.com>
 * @since template-v2.0.r3
 */
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

        Map<String, Object> responsesDef = new TreeMap<>();
        baseRoot.put("responses", responsesDef);
        responsesDef.put("ApiResponse",
                MapUtils.createMap("type", "object", "description", "Generic API response", "properties",
                        MapUtils.createMap("status", MapUtils.createMap("type", "integer", "description",
                                "Status code, usually '200' means 'successful'"), "msg",
                                MapUtils.createMap("type", "string", "description",
                                        "Message that further describes API status code"), "data",
                                MapUtils.createMap("type", "object", "description",
                                        "(optional) Output returned from API"))));
        responsesDef.put("Success",
                MapUtils.createMap("type", "object", "description", "API call finished successful", "properties",
                        MapUtils.createMap("status", MapUtils.createMap("type", "integer", "enum", Arrays.asList(200)),
                                "data", MapUtils.createMap("type", "object", "description",
                                        "(optional) Output returned from API"))));
        responsesDef.put("ErrorClient", MapUtils.createMap("type", "object", "description",
                "There was error at client side (e.g. invalid parameters)", "properties",
                MapUtils.createMap("status", MapUtils.createMap("type", "integer", "enum", Arrays.asList(400)))));
        responsesDef.put("AccessDenied",
                MapUtils.createMap("type", "object", "description", "Client is not authorized to call the API",
                        "properties", MapUtils.createMap("status",
                                MapUtils.createMap("type", "integer", "enum", Arrays.asList(403)))));
        responsesDef.put("NotFound",
                MapUtils.createMap("type", "object", "description", "The requested item was not found", "properties",
                        MapUtils.createMap("status",
                                MapUtils.createMap("type", "integer", "enum", Arrays.asList(404)))));
        responsesDef.put("ErrorServer",
                MapUtils.createMap("type", "object", "description", "There was error at server side (e.g. exception)",
                        "properties", MapUtils.createMap("status",
                                MapUtils.createMap("type", "integer", "enum", Arrays.asList(500)))));
        responsesDef.put("NotImplemented",
                MapUtils.createMap("type", "object", "description", "This API/function has not been implemented yet",
                        "properties", MapUtils.createMap("status",
                                MapUtils.createMap("type", "integer", "enum", Arrays.asList(501)))));

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
                Set<String> tags = spec.getTags();
                if (tags != null && tags.size() > 0) {
                    epSpec.put("tags", tags);
                }
                if (!StringUtils.isBlank(spec.getDescription())) {
                    epSpec.put("description", spec.getDescription());
                }
                if (!StringUtils.isBlank(spec.getSummary())) {
                    epSpec.put("summary", spec.getSummary());
                }
                List<ApiSpec.ApiResponse> apiResponses = spec.getResponses();
                if (apiResponses == null || apiResponses.size() == 0) {
                    epSpec.put("responses", MapUtils.createMap("successful",
                            MapUtils.createMap("schema", MapUtils.createMap("$ref", "#/responses/Success"))));
                }

                List<ApiSpec.ApiParameter> apiParameters = spec.getParameters();
                List<Map<String, Object>> parameters = new ArrayList<>();
                epSpec.put("parameters", parameters);
                Map<String, Map<String, Object>> bodyParameters = new HashMap<>();
                if (apiParameters != null && apiParameters.size() > 0) {
                    List<String> requiredBBodyParameters = new ArrayList<>();
                    apiParameters.forEach(apiParam -> {
                        if (apiParam.getIn().equalsIgnoreCase("body")) {
                            bodyParameters.put(apiParam.getName(),
                                    MapUtils.createMap("description", apiParam.getDescription(), "type",
                                            apiParam.getType(), "format", apiParam.getFormat()));
                            if (apiParam.getAllowedValues().size() > 0) {
                                bodyParameters.get(apiParam.getName()).put("enum", apiParam.getAllowedValues());
                            }
                            if (apiParam.isRequired()) {
                                requiredBBodyParameters.add(apiParam.getName());
                            }
                        } else {
                            Map<String, Object> paramObj = MapUtils
                                    .createMap("in", apiParam.getIn(), "name", apiParam.getName(), "description",
                                            apiParam.getDescription(), "required", apiParam.isRequired(), "type",
                                            apiParam.getType(), "format", apiParam.getFormat());
                            if (apiParam.getAllowedValues().size() > 0) {
                                paramObj.put("enum", apiParam.getAllowedValues());
                            }
                            parameters.add(paramObj);
                        }
                    });
                    if (bodyParameters.size() > 0) {
                        Map<String, Object> bodyParam = MapUtils
                                .createMap("in", "body", "name", "_body_", "description",
                                        "Parameters passed via request body.", "schema",
                                        MapUtils.createMap("type", "object", "required", requiredBBodyParameters,
                                                "properties", bodyParameters));
                        parameters.add(bodyParam);
                    }
                }
                if (bodyParameters.size() == 0 && (method.equalsIgnoreCase("POST") || method.equalsIgnoreCase("PUT"))) {
                    //special case: if POST or PUT and body parameters are not defined: create a default body parameter
                    Map<String, Object> bodyParam = MapUtils.createMap("in", "body", "name", "_body_", "description",
                            "Parameters passed via request body.", "schema", MapUtils.createMap("type", "object"));
                    parameters.add(bodyParam);
                }
            });
        });
    }

    @Override
    public void handleRequest(HttpServerExchange exchange) {
        Map<String, Object> root = new TreeMap<>(baseRoot);
        root.put("host", exchange.getHostAndPort());

        exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "application/json")
                .put(new HttpString("Access-Control-Allow-Origin"), "*");
        exchange.getResponseSender().send(SerializationUtils.toJsonString(root), StandardCharsets.UTF_8);
    }
}
