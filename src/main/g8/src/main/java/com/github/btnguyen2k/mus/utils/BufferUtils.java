package com.github.btnguyen2k.mus.utils;

import com.github.ddth.commons.utils.TypesafeConfigUtils;
import com.github.ddth.queue.IQueue;
import com.github.ddth.queue.impl.AbstractQueue;
import com.github.ddth.queue.impl.universal.idint.UniversalInmemQueue;
import com.github.ddth.queue.impl.universal.idint.UniversalRocksDbQueue;
import com.typesafe.config.Config;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.Random;

/**
 * Buffer helper class.
 *
 * @author Thanh Nguyen <btnguyen2k@gmail.com>
 * @since template-v2.0.1.r4
 */
public class BufferUtils {
    private final static Logger LOGGER = LoggerFactory.getLogger(BufferUtils.class);

    /**
     * Try creating a file buffer if {@code buffer.type} is "file"
     *
     * @param bufferConfig
     * @return
     * @throws Exception
     */
    public static IQueue<Long, byte[]> tryCreateFileBuffer(Config bufferConfig) throws Exception {
        String bufferType = TypesafeConfigUtils.getStringOptional(bufferConfig, "buffer.type").orElse("memory");
        if (bufferType.equalsIgnoreCase("file")) {
            String directory = TypesafeConfigUtils.getString(bufferConfig, "buffer.directory");
            if (StringUtils.isBlank(directory)) {
                LOGGER.warn("Buffer type is [file] but directory is not configured at key [buffer.directory].");
                return null;
            }
            Random rand = new Random(System.currentTimeMillis());
            directory = StringUtils.replace(directory, "#{random}", String.valueOf(rand.nextInt(Integer.MAX_VALUE)));
            File dir = new File(directory);
            dir.mkdirs();
            if (!dir.isDirectory() || !dir.canWrite()) {
                LOGGER.warn("Directory [" + dir.getAbsolutePath() + "] is not valid or not writable.");
                return null;
            }
            Runtime.getRuntime().addShutdownHook(new Thread(() -> FileUtils.deleteQuietly(dir)));
            AbstractQueue<Long, byte[]> queue = new UniversalRocksDbQueue().setStorageDir(dir.getAbsolutePath())
                    .setQueueName("FileBuffer-" + System.currentTimeMillis());
            queue.init();
            LOGGER.info("Created file-buffer [" + queue.getQueueName() + "] at " + dir.getAbsolutePath());
            return queue;
        }
        return null;
    }

    /**
     * Create a memory buffer.
     *
     * @param bufferConfig
     * @return
     * @throws Exception
     */
    public static IQueue<Long, byte[]> createMemoryBuffer(Config bufferConfig, int defaultCapacity) throws Exception {
        int queueBoundary = TypesafeConfigUtils.getIntegerOptional(bufferConfig, "buffer.max_items")
                .orElse(defaultCapacity);
        AbstractQueue<Long, byte[]> queue = new UniversalInmemQueue(queueBoundary)
                .setQueueName("MemoryBuffer-" + System.currentTimeMillis());
        queue.init();
        LOGGER.info("Created memory-buffer [" + queue.getQueueName() + "] with capacity " + queueBoundary);
        return queue;
    }
}
