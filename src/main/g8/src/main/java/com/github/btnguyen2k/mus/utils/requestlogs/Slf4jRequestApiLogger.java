package com.github.btnguyen2k.mus.utils.requestlogs;

import com.github.ddth.commons.utils.SerializationUtils;
import com.typesafe.config.Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * Log API request/response to a {@link Logger} in JSON format.
 *
 * @author Thanh Nguyen <btnguyen2k@gmail.com>
 * @since template-v2.0.r4
 */
public class Slf4jRequestApiLogger extends AbstractRequestApiLogger {
    private Logger logger = LoggerFactory.getLogger(Slf4jRequestApiLogger.class);

    public Slf4jRequestApiLogger(Config config) {
        super(config);
    }

    public Logger getLogger() {
        return logger;
    }

    public Slf4jRequestApiLogger setLogger(Logger logger) {
        this.logger = logger;
        return this;
    }

    /**
     * {@@inheritDoc}
     */
    @Override
    protected void writeLog(Map<String, Object> data) {
        if (logger != null && logger.isInfoEnabled()) {
            logger.info(SerializationUtils.toJsonString(data));
        }
    }
}
