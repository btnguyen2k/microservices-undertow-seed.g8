package com.github.btnguyen2k.mus.utils;

import com.github.ddth.recipes.global.GlobalRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.context.ApplicationContext;

/**
 * Spring beans utility class.
 *
 * @author Thanh Nguyen <btnguye2k@gmail.com>
 * @since template-v2.0.r3
 */
public class SpringBeanUtils {
    private static Logger LOGGER = LoggerFactory.getLogger(SpringBeanUtils.class);

    /**
     * Get the {@link ApplicationContext} instance from global registry.
     *
     * @return
     */
    public static ApplicationContext getApplicationContext() {
        return GlobalRegistry.getFromGlobalStorage(AppUtils.GLOBAL_KEY_SPRING_APP_CONTEXT, ApplicationContext.class);
    }

    /**
     * Get a Spring bean by type. This method returns {@code null} if no such bean found.
     *
     * @param type
     * @param <T>
     * @return
     */
    public static <T> T getBean(Class<T> type) {
        ApplicationContext appContext = getApplicationContext();
        if (appContext == null) {
            LOGGER.warn("Spring's ApplicationContext is not found.");
            return null;
        }
        try {
            return appContext.getBean(type);
        } catch (NoSuchBeanDefinitionException e) {
            return null;
        }
    }

    /**
     * Get a Spring bean by name and type. This method returns {@code null} if no such bean found.
     *
     * @param name
     * @param type
     * @param <T>
     * @return
     */
    public static <T> T getBean(String name, Class<T> type) {
        ApplicationContext appContext = getApplicationContext();
        if (appContext == null) {
            LOGGER.warn("Spring's ApplicationContext is not found.");
            return null;
        }
        try {
            return appContext.getBean(name, type);
        } catch (NoSuchBeanDefinitionException e) {
            return null;
        }
    }
}
