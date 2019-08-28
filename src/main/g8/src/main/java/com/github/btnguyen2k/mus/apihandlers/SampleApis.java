package com.github.btnguyen2k.mus.apihandlers;

import com.github.ddth.recipes.apiservice.*;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

import javax.ws.rs.Path;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;

public class SampleApis {
    static Cache<String, byte[]> cache = buildCache();

    static Cache<String, byte[]> buildCache() {
        return CacheBuilder.newBuilder().expireAfterWrite(1000, TimeUnit.MILLISECONDS).build();
    }

    static String format(double bytes, int digits) {
        String[] dictionary = { "bytes", "KB", "MB", "GB", "TB", "PB", "EB", "ZB", "YB" };
        int index = 0;
        for (index = 0; index < dictionary.length; index++) {
            if (bytes < 1024) {
                break;
            }
            bytes = bytes / 1024;
        }
        return String.format("%." + digits + "f", bytes) + " " + dictionary[index];
    }

    static Map<String, Object> memInfo() {
        Runtime runtime = Runtime.getRuntime();
        Map<String, Object> memInfo = new TreeMap<>();
        memInfo.put("3.free ", format(runtime.freeMemory(), 2));
        memInfo.put("2.total", format(runtime.totalMemory(), 2));
        memInfo.put("1.max  ", format(runtime.maxMemory(), 2));
        return memInfo;
    }

    public static class MemInfoApi implements IApiHandler {
        @Override
        public ApiResult handle(ApiContext context, ApiAuth auth, ApiParams params) {
            return new ApiResult(ApiResult.STATUS_OK, ApiResult.MSG_OK, memInfo());
        }
    }

    public static class MemAllocateApi implements IApiHandler {
        @Override
        public ApiResult handle(ApiContext context, ApiAuth auth, ApiParams params) {
            Map<String, Object> memInfo = memInfo();
            Integer requestMemMb = params.getParamOptional("size", Integer.class).orElse(0);
            memInfo.put("a.requested", format(requestMemMb * 1024 * 1024, 2));
            if (requestMemMb.intValue() > 0) {
                long start = System.currentTimeMillis();
                for (int i = 0; i < requestMemMb.intValue(); i++) {
                    cache.put(String.valueOf(start + i), new byte[1024 * 1024]);
                }
                memInfo.put("b.allocated", format(requestMemMb * 1024 * 1024, 2));
            }
            return new ApiResult(ApiResult.STATUS_OK, ApiResult.MSG_OK, memInfo);
        }
    }

    public static class MemClearApi implements IApiHandler {
        @Override
        public ApiResult handle(ApiContext context, ApiAuth auth, ApiParams params) {
            cache = buildCache();
            return new ApiResult(ApiResult.STATUS_OK, ApiResult.MSG_OK, memInfo());
        }
    }

    public static class MemFreeApi implements IApiHandler {
        @Override
        public ApiResult handle(ApiContext context, ApiAuth auth, ApiParams params) {
            cache = buildCache();
            System.gc();
            return new ApiResult(ApiResult.STATUS_OK, ApiResult.MSG_OK, memInfo());
        }
    }
}
