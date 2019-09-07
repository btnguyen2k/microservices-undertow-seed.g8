package com.github.btnguyen2k.mus.utils.httphandlers;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.Headers;
import io.undertow.util.StatusCodes;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.webjars.NotFoundException;
import org.webjars.WebJarAssetLocator;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.ByteBuffer;
import java.util.Deque;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;

/**
 * Serve WebJar assets.
 *
 * @author Thanh Nguyen <btnguyen2k@gmail.com>
 * @since template-v2.0.r3
 */
public class WebJarAssetsHttpHandler implements HttpHandler {
    public static WebJarAssetsHttpHandler instance = new WebJarAssetsHttpHandler();

    public final static String DEFAULT_PREFIX = "/webjar";
    private String prefix = DEFAULT_PREFIX;
    private WebJarAssetLocator locator;
    private Deque<String> emptyQueue = new LinkedBlockingDeque<>();

    public WebJarAssetsHttpHandler() {
        this(DEFAULT_PREFIX);
    }

    public WebJarAssetsHttpHandler(String prefix) {
        this.prefix = prefix;
        locator = new WebJarAssetLocator();
    }

    private class FileContentAndType {
        public final String type;
        public final byte[] content;

        public FileContentAndType(String type, byte[] content) {
            this.type = type;
            this.content = content;
        }
    }

    private FileContentAndType loadResource(String resourcePath) throws IOException {
        String type = URLConnection.guessContentTypeFromName(resourcePath);
        URL resource = getClass().getResource("/" + resourcePath);
        byte[] content = IOUtils.toByteArray(resource);
        return new FileContentAndType(type, content);
    }

    private Cache<String, FileContentAndType> cachedFile = CacheBuilder.newBuilder()
            .expireAfterAccess(3600, TimeUnit.MILLISECONDS).build();

    private FileContentAndType locateResource(HttpServerExchange exchange) throws IOException {
        String requestPath = exchange.getRequestPath();
        FileContentAndType fileContentAndType = cachedFile.getIfPresent(requestPath);
        if (fileContentAndType == null) {
            String bundle = exchange.getQueryParameters().getOrDefault("bundle", emptyQueue).peekFirst();
            String myPrefix = prefix + "/" + bundle;
            if (!StringUtils.isBlank(bundle) && requestPath.startsWith(myPrefix)) {
                try {
                    String resourcePath = locator.getFullPath(bundle, requestPath.substring(myPrefix.length()));
                    fileContentAndType = loadResource(resourcePath);
                    if (fileContentAndType != null) {
                        cachedFile.put(requestPath, fileContentAndType);
                    }
                } catch (NotFoundException e) {
                    return null;
                }
            }
        }
        return fileContentAndType;
    }

    @Override
    public void handleRequest(HttpServerExchange exchange) throws Exception {
        FileContentAndType fileContentAndType = locateResource(exchange);
        if (fileContentAndType == null) {
            exchange.setStatusCode(StatusCodes.NOT_FOUND);
            exchange.endExchange();
        } else {
            exchange.
                    getResponseHeaders().put(Headers.CONTENT_TYPE, fileContentAndType.type);
            exchange.getResponseSender().send(ByteBuffer.wrap(fileContentAndType.content));
        }
    }
}