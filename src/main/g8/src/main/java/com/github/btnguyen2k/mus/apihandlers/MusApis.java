package com.github.btnguyen2k.mus.apihandlers;

import com.github.btnguyen2k.mus.utils.AppUtils;
import com.github.ddth.commons.utils.TypesafeConfigUtils;
import com.github.ddth.recipes.apiservice.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Some built-in APIs.
 *
 * @author Thanh Nguyen <btnguye2k@gmail.com>
 * @since template-v2.0.r3
 */
public class MusApis {
    public static class Info implements IApiHandler {
        @Override
        public ApiResult handle(ApiContext context, ApiAuth auth, ApiParams params) {
            Map<String, Object> data = new HashMap<>();

            Runtime runtime = Runtime.getRuntime();
            Map<String, Object> memInfo = new HashMap<>();
            memInfo.put("free", runtime.freeMemory());
            memInfo.put("max", runtime.maxMemory());
            memInfo.put("total", runtime.totalMemory());
            data.put("mem", memInfo);

            data.put("cpu", runtime.availableProcessors());

            data.put("app", TypesafeConfigUtils.getObject(AppUtils.APP_CONFIG, "app"));

            return ApiResult.resultOk(data);
        }
    }
}
