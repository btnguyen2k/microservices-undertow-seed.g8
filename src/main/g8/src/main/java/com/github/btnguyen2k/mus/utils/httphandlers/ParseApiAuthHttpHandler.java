package com.github.btnguyen2k.mus.utils.httphandlers;

import com.github.btnguyen2k.mus.utils.AppUtils;
import com.github.ddth.recipes.apiservice.ApiAuth;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;

import java.util.NoSuchElementException;

/**
 * {@link HttpHandler} implementation that extracts {@link com.github.ddth.recipes.apiservice.ApiAuth} from HTTP request header
 * and passes it to the next handler.
 *
 * @author Thanh Nguyen <btnguyen2k@gmail.com>
 * @since template-v2.0.r2
 */
public class ParseApiAuthHttpHandler implements HttpHandler {
    private final HttpHandler next;
    private final String httpHeaderAppId, httpHeaderAccessToken;

    public ParseApiAuthHttpHandler(HttpHandler next, String httpHeaderAppId, String httpHeaderAccessToken) {
        this.next = next;
        this.httpHeaderAppId = httpHeaderAppId;
        this.httpHeaderAccessToken = httpHeaderAccessToken;
    }

    private String getHeader(HttpServerExchange exchange, String key) {
        try {
            return exchange.getRequestHeaders().getFirst(key);
        } catch (NoSuchElementException e) {
            return null;
        }
    }

    @Override
    public void handleRequest(HttpServerExchange exchange) throws Exception {
        String appId = getHeader(exchange, httpHeaderAppId);
        String accessToken = getHeader(exchange, httpHeaderAccessToken);
        ApiAuth apiAuth = new ApiAuth(appId, accessToken);
        exchange.putAttachment(AppUtils.ATTKEY_API_AUTH, apiAuth);
        next.handleRequest(exchange);
    }
}
