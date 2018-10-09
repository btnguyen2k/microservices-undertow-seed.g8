package com.github.btnguyen2k.mus.samples.apihandlers;

import com.github.ddth.recipes.apiservice.*;

/**
 * Sample API that echoes request parameters back to client.
 *
 * @author Thanh Nguyen <btnguyen2k@gmail.com>
 * @since template-v2.0.r1
 */
public class EchoApi implements IApiHandler {
    @Override
    public ApiResult handle(ApiContext context, ApiAuth auth, ApiParams params) {
        return new ApiResult(ApiResult.STATUS_OK, ApiResult.MSG_OK, params.getAllParams());
    }
}
