package com.github.btnguyen2k.mus.utils.requestlogs;

import com.github.btnguyen2k.mus.utils.AppUtils;
import com.github.ddth.commons.utils.MapUtils;
import com.github.ddth.commons.utils.TypesafeConfigUtils;
import com.github.ddth.recipes.apiservice.*;
import com.typesafe.config.Config;

import java.util.HashMap;
import java.util.Map;

/**
 * This {@link IApiLogger} is base class to implement API request/response logger.
 *
 * @author Thanh Nguyen <btnguyen2k@gmail.com>
 * @since template-v2.0.r4
 */
public abstract class AbstractRequestApiLogger implements IApiLogger {
    private String fieldAppName = "app_name";
    private String fieldAppVersion = "app_version";

    private String fieldId = "id";
    private String fieldApiName = "api";
    private String fieldTimestampStart = "t";
    private String fieldStage = "s";
    private String fieldGateway = "gw";
    private String fieldDuration = "d";
    private String fieldTotalConcurrency = "c_total";
    private String fieldApiConcurrency = "c_api";

    private String fieldContext = "context";
    private String fieldAuth = "auth";
    private String fieldParams = "params";
    private String fieldResult = "result";

    private Config config;
    private String appName, appVersion;

    public AbstractRequestApiLogger(Config config) {
        this.config = config;
    }

    public String getFieldId() {
        return fieldId;
    }

    public AbstractRequestApiLogger setFieldId(String fieldId) {
        this.fieldId = fieldId;
        return this;
    }

    public String getFieldAppName() {
        return fieldAppName;
    }

    public AbstractRequestApiLogger setFieldAppName(String fieldAppName) {
        this.fieldAppName = fieldAppName;
        return this;
    }

    public String getFieldAppVersion() {
        return fieldAppVersion;
    }

    public AbstractRequestApiLogger setFieldAppVersion(String fieldAppVersion) {
        this.fieldAppVersion = fieldAppVersion;
        return this;
    }

    public String getFieldStage() {
        return fieldStage;
    }

    public AbstractRequestApiLogger setFieldStage(String fieldStage) {
        this.fieldStage = fieldStage;
        return this;
    }

    public String getFieldApiName() {
        return fieldApiName;
    }

    public AbstractRequestApiLogger setFieldApiName(String fieldApiName) {
        this.fieldApiName = fieldApiName;
        return this;
    }

    public String getFieldGateway() {
        return fieldGateway;
    }

    public AbstractRequestApiLogger setFieldGateway(String fieldGateway) {
        this.fieldGateway = fieldGateway;
        return this;
    }

    public String getFieldTimestampStart() {
        return fieldTimestampStart;
    }

    public AbstractRequestApiLogger setFieldTimestampStart(String fieldTimestampStart) {
        this.fieldTimestampStart = fieldTimestampStart;
        return this;
    }

    public String getFieldDuration() {
        return fieldDuration;
    }

    public AbstractRequestApiLogger setFieldDuration(String fieldDuration) {
        this.fieldDuration = fieldDuration;
        return this;
    }

    public String getFieldTotalConcurrency() {
        return fieldTotalConcurrency;
    }

    public AbstractRequestApiLogger setFieldTotalConcurrency(String fieldTotalConcurrency) {
        this.fieldTotalConcurrency = fieldTotalConcurrency;
        return this;
    }

    public String getFieldApiConcurrency() {
        return fieldApiConcurrency;
    }

    public AbstractRequestApiLogger setFieldApiConcurrency(String fieldApiConcurrency) {
        this.fieldApiConcurrency = fieldApiConcurrency;
        return this;
    }

    protected Config getConfig() {
        return config;
    }

    public AbstractRequestApiLogger init() throws Exception {
        appName = TypesafeConfigUtils.getString(AppUtils.APP_CONFIG, "app.name");
        appVersion = TypesafeConfigUtils.getString(AppUtils.APP_CONFIG, "app.version");
        return this;
    }

    /**
     * Sub-class implements this method to write log data.
     *
     * @param data
     */
    protected abstract void writeLog(Map<String, Object> data);

    private Map<String, Object> buildAuthMap(ApiAuth auth) {
        Map<String, Object> authMap = new HashMap<>();
        String accessToken = auth.getAccessToken();
        if (accessToken == null || accessToken.length() <= 3) {
            accessToken = "***";
        } else {
            accessToken = "***" + accessToken.substring(-3);
        }
        authMap.put("app_id", auth.getAppId());
        authMap.put("access_token", accessToken);
        return authMap;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void preApiCall(long totalConcurrency, long apiConcurrency, ApiContext context, ApiAuth auth,
            ApiParams params) {
        Map<String, Object> data = MapUtils
                .createMap(fieldId, context.getId(), fieldAppName, appName, fieldAppVersion, appVersion, fieldStage,
                        "START", fieldApiName, context.getApiName(), fieldGateway, context.getGateway(),
                        fieldTimestampStart, context.getTimestamp().getTime(), fieldTotalConcurrency, totalConcurrency,
                        fieldApiConcurrency, apiConcurrency, fieldContext, context.getAllContextFields(), fieldAuth,
                        buildAuthMap(auth), fieldParams, params.getAllParamsAsMap());
        writeLog(data);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void postApiCall(long durationMs, long totalConcurrency, long apiConcurrency, ApiContext context,
            ApiAuth auth, ApiParams params, ApiResult result) {
        Map<String, Object> data = MapUtils
                .createMap(fieldId, context.getId(), fieldAppName, appName, fieldAppVersion, appVersion, fieldStage,
                        "END", fieldApiName, context.getApiName(), fieldGateway, context.getGateway(),
                        fieldTimestampStart, context.getTimestamp().getTime(), fieldDuration, durationMs,
                        fieldTotalConcurrency, totalConcurrency, fieldApiConcurrency, apiConcurrency, fieldContext,
                        context.getAllContextFields(), fieldAuth, buildAuthMap(auth), fieldParams,
                        params.getAllParamsAsMap(), fieldResult, result.asMap());
        writeLog(data);
    }
}
