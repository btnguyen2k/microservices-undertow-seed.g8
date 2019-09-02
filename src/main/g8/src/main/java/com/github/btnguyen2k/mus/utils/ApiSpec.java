package com.github.btnguyen2k.mus.utils;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.*;

/**
 * API specifications.
 *
 * @author Thanh Nguyen <btnguyen2k@gmail.com>
 * @since template-v2.0.r3
 */
public class ApiSpec {
    /**
     * Build {@link ApiSpec} instance from annotation.
     *
     * @param annotation
     * @return
     */
    public static ApiSpec newInstance(Operation annotation) {
        ApiSpec apiSpec = new ApiSpec(annotation.operationId());
        apiSpec.setSummary(annotation.summary()).setDescription(annotation.summary());
        apiSpec.addTags(annotation.tags());
        apiSpec.addParameters(ApiParameter.newInstances(annotation.parameters()));
        return apiSpec;
    }

    public static class ApiResponse {
    }

    public static class ApiParameter {
        private final static ApiParameter[] EMPTY_ARRAY = new ApiParameter[0];

        public static ApiParameter[] newInstances(Parameter... params) {
            List<ApiParameter> apiParams = new ArrayList<>();
            if (params != null) {
                for (Parameter param : params) {
                    apiParams.add(newInstance(param));
                }
            }
            return apiParams.toArray(EMPTY_ARRAY);
        }

        public static ApiParameter newInstance(Parameter param) {
            ApiParameter apiParam = new ApiParameter(param.name(), param.required(), param.in());
            apiParam.setDescription(param.description());
            Schema schema = param.schema();
            if (schema != null) {
                apiParam.setType(schema.type());
                apiParam.setFormat(schema.format());
                apiParam.addAllowedValues(schema.allowableValues());
            }
            return apiParam;
        }

        private String name, description, type, format;
        private boolean required;
        private String in;
        private Set<String> allowedValues = new HashSet<>();

        public ApiParameter(String name, boolean required, ParameterIn in) {
            setName(name);
            setRequired(required);
            setIn(in);
        }

        public String getName() {
            return name;
        }

        public ApiParameter setName(String name) {
            this.name = name;
            return this;
        }

        public String getDescription() {
            return description;
        }

        public ApiParameter setDescription(String description) {
            this.description = description;
            return this;
        }

        public String getType() {
            return type;
        }

        public ApiParameter setType(String type) {
            this.type = type;
            return this;
        }

        public String getFormat() {
            return format;
        }

        public ApiParameter setFormat(String format) {
            this.format = format;
            return this;
        }

        public boolean isRequired() {
            return required;
        }

        public ApiParameter setRequired(boolean required) {
            this.required = required;
            return this;
        }

        public String getIn() {
            return in;
        }

        public ApiParameter setIn(ParameterIn in) {
            switch (in) {
            case COOKIE:
                this.in = "cookie";
                break;
            case HEADER:
                this.in = "header";
                break;
            case PATH:
                this.in = "path";
                break;
            case QUERY:
                this.in = "query";
                break;
            default:
                this.in = "body";
            }
            return this;
        }

        public ApiParameter addAllowedValue(String allowedValue) {
            this.allowedValues.add(allowedValue);
            return this;
        }

        public ApiParameter addAllowedValues(String... allowedValues) {
            if (allowedValues != null) {
                for (String allowedValue : allowedValues) {
                    this.allowedValues.add(allowedValue);
                }
            }
            return this;
        }

        public ApiParameter setAllowedValues(Collection<String> allowedValues) {
            this.allowedValues.clear();
            if (allowedValues != null) {
                this.allowedValues.addAll(allowedValues);
            }
            return this;
        }

        public Set<String> getAllowedValues() {
            return new HashSet<>(allowedValues);
        }
    }

    private String handlerName;
    private String description, summary;
    private Set<String> tags = new HashSet<>();
    private List<ApiResponse> responses = new ArrayList<>();
    private List<ApiParameter> parameters = new ArrayList<>();

    public ApiSpec(String handlerName) {
        this.handlerName = handlerName;
    }

    public String getHandlerName() {
        return handlerName;
    }

    public ApiSpec setHandlerName(String handlerName) {
        this.handlerName = handlerName;
        return this;
    }

    public String getDescription() {
        return description;
    }

    public ApiSpec setDescription(String description) {
        this.description = description;
        return this;
    }

    public String getSummary() {
        return summary;
    }

    public ApiSpec setSummary(String summary) {
        this.summary = summary;
        return this;
    }

    public ApiSpec addTag(String tag) {
        this.tags.add(tag);
        return this;
    }

    public ApiSpec addTags(String... tags) {
        if (tags != null) {
            for (String tag : tags) {
                this.tags.add(tag);
            }
        }
        return this;
    }

    public Set<String> getTags() {
        return new HashSet<>(tags);
    }

    public ApiSpec addResponse(ApiResponse apiResponse) {
        this.responses.add(apiResponse);
        return this;
    }

    public ApiSpec addResponses(ApiResponse... apiResponses) {
        if (apiResponses != null) {
            for (ApiResponse apiResponse : apiResponses) {
                this.responses.add(apiResponse);
            }
        }
        return this;
    }

    public List<ApiResponse> getResponses() {
        return new ArrayList<>(responses);
    }

    public ApiSpec addParameter(ApiParameter apiParameter) {
        this.parameters.add(apiParameter);
        return this;
    }

    public ApiSpec addParameters(ApiParameter... apiParameters) {
        if (apiParameters != null) {
            for (ApiParameter apiParameter : apiParameters) {
                this.parameters.add(apiParameter);
            }
        }
        return this;
    }

    public List<ApiParameter> getParameters() {
        return new ArrayList<>(parameters);
    }
}
