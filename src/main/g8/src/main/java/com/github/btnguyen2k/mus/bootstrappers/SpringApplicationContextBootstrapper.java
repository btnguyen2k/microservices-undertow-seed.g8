package com.github.btnguyen2k.mus.bootstrappers;

import com.github.btnguyen2k.mus.utils.AppUtils;
import com.github.ddth.commons.utils.TypesafeConfigUtils;
import com.github.ddth.recipes.global.GlobalRegistry;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Bootstrapper that initialize a Spring's {@link org.springframework.context.ApplicationContext}.
 *
 * <p>This bootstrapper loads beans from configuration file specified by {@code spring.conf}</p>
 * <ul>
 *     <li>First, it looks for system property {@code spring.conf}.</li>
 *     <li>If no such exists, it then looks for configuration key {@code spring.conf} in {@code application.conf}.</li>
 *     <li>If bean configuration files are found, and {@link org.springframework.context.ApplicationContext} is created, initialized and stored in {@link GlobalRegistry} at key {@link AppUtils#GLOBAL_KEY_SPRING_APP_CONTEXT}.</li>
 * </ul>
 *
 * @author Thanh Nguyen <btnguyen2k@gmail.com>
 * @since template-v2.0.r3
 */
public class SpringApplicationContextBootstrapper implements Runnable {
    private final Logger LOGGER = LoggerFactory.getLogger(SpringApplicationContextBootstrapper.class);

    @Override
    public void run() {
        String strConfig = System.getProperty("spring.conf");
        if (StringUtils.isBlank(strConfig)) {
            LOGGER.info("No [spring.conf] found in system properties, looking up in application's configurations.");
            strConfig = TypesafeConfigUtils.getString(AppUtils.APP_CONFIG, "spring.conf");
            if (!StringUtils.isBlank(strConfig)) {
                LOGGER.info("Found [spring.conf] config: " + strConfig);
            }
        } else {
            LOGGER.info("Found [spring.conf] system properties: " + strConfig);
        }
        if (StringUtils.isBlank(strConfig)) {
            LOGGER.info("No [spring.conf] found, Spring's ApplicationContext will not be created.");
            return;
        }

        String[] configFiles = strConfig.trim().split("[,;\\s]+");
        List<String> configLocations = new ArrayList<>();
        for (String configFile : configFiles) {
            File f = new File(configFile);
            if (f.exists() && f.isFile() && f.canRead()) {
                configLocations.add("file:" + f.getAbsolutePath());
            } else {
                LOGGER.warn("Spring config file [" + f + "] not found or not readable.");
            }
        }
        if (configLocations.size() > 0) {
            LOGGER.info("Creating Spring ApplicationContext with configuration files: " + configLocations);
            AbstractApplicationContext applicationContext = new FileSystemXmlApplicationContext(
                    configLocations.toArray(ArrayUtils.EMPTY_STRING_ARRAY));
            applicationContext.start();
            Runtime.getRuntime().addShutdownHook(new Thread(() -> applicationContext.close()));
            GlobalRegistry.putToGlobalStorage(AppUtils.GLOBAL_KEY_SPRING_APP_CONTEXT, applicationContext);
        } else {
            LOGGER.warn("No valid Spring configuration file(s), skip creating ApplicationContext.");
        }
    }
}
