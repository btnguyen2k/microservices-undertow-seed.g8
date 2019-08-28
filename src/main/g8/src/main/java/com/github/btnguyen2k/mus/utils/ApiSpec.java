package com.github.btnguyen2k.mus.utils;

/**
 * API specifications.
 *
 * @author Thanh Nguyen <btnguyen2k@gmail.com>
 * @since template-v2.0.r3
 */
public class ApiSpec {
    private String handlerName;

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
}
