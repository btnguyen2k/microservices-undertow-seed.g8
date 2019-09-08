package com.github.btnguyen2k.mus.utils.requestlogs;

import com.github.ddth.commons.utils.SerializationUtils;
import com.typesafe.config.Config;

import java.io.PrintStream;
import java.util.Map;

/**
 * Log API request/response to a {@link PrintStream} in JSON format.
 *
 * @author Thanh Nguyen <btnguyen2k@gmail.com>
 * @since template-v2.0.r4
 */
public class PrintStreamRequestApiLogger extends AbstractRequestApiLogger {
    private PrintStream printStream = System.out;

    public PrintStreamRequestApiLogger(Config config) {
        super(config);
    }

    public PrintStream getPrintStream() {
        return printStream;
    }

    public PrintStreamRequestApiLogger setPrintStream(PrintStream printStream) {
        this.printStream = printStream;
        return this;
    }

    /**
     * {@@inheritDoc}
     */
    @Override
    protected void writeLog(Map<String, Object> data) {
        if (printStream != null) {
            printStream.println(SerializationUtils.toJsonString(data));
        }
    }
}
