package com.github.btnguyen2k.mus.samples.apihandlers;

import com.github.ddth.commons.utils.MapUtils;
import com.github.ddth.recipes.apiservice.*;

import java.lang.management.ManagementFactory;
import java.util.HashMap;
import java.util.Map;

/**
 * Sample API that returns server's info to client.
 *
 * @author Thanh Nguyen <btnguyen2k@gmail.com>
 * @since template-v2.0.r1
 */
public class InfoApi implements IApiHandler {
    @Override
    public ApiResult handle(ApiContext context, ApiAuth auth, ApiParams params) {
        Map<Object, Object> infoJava = new HashMap<>();
        System.getProperties().forEach((k, v) -> {
            if (k.toString().startsWith("java.")) {
                infoJava.put(k, v);
            }
        });

        Map<Object, Object> infoMemory = MapUtils
                .createMap("memory_heap", ManagementFactory.getMemoryMXBean().getHeapMemoryUsage(),
                        "memory_non_heap",
                        ManagementFactory.getMemoryMXBean().getNonHeapMemoryUsage());

        Map<Object, Object> osInfo = MapUtils
                .createMap("arch", ManagementFactory.getOperatingSystemMXBean().getArch(),
                        "processors",
                        ManagementFactory.getOperatingSystemMXBean().getAvailableProcessors(),
                        "name", ManagementFactory.getOperatingSystemMXBean().getName(), "version",
                        ManagementFactory.getOperatingSystemMXBean().getVersion(), "system_load",
                        ManagementFactory.getOperatingSystemMXBean().getSystemLoadAverage());

        Map<String, Object> data = MapUtils
                .createMap("java", infoJava, "memory", infoMemory, "os", osInfo);
        return new ApiResult(ApiResult.STATUS_OK, ApiResult.MSG_OK, data);
    }
}
