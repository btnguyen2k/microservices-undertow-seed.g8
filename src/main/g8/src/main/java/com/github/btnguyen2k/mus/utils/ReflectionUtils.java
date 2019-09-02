package com.github.btnguyen2k.mus.utils;

import com.github.ddth.recipes.apiservice.ApiAuth;
import com.github.ddth.recipes.apiservice.ApiContext;
import com.github.ddth.recipes.apiservice.ApiParams;
import com.github.ddth.recipes.apiservice.IApiHandler;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AssignableTypeFilter;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;

/**
 * Reflection utility class.
 *
 * @author Thanh Nguyen <btnguye2k@gmail.com>
 * @since template-v2.0.r3
 */
public class ReflectionUtils {
    public static class ApiHandlerWithAnnotations<A extends Annotation> {
        public IApiHandler apiHandler;
        public Collection<A> annotations;
    }

    public static <A extends Annotation> Collection<ApiHandlerWithAnnotations<A>> findApiHandlerAnnotatedWith(
            Class<A> annotation, String... scanPackages) {
        Collection<ApiHandlerWithAnnotations<A>> result = new ArrayList<>();

        ClassPathScanningCandidateComponentProvider scanner = new ClassPathScanningCandidateComponentProvider(false);
        scanner.addIncludeFilter(new AssignableTypeFilter(IApiHandler.class));
        for (String scanPackage : scanPackages) {
            for (BeanDefinition bd : scanner.findCandidateComponents(scanPackage)) {
                ApiHandlerWithAnnotations<A> entry = new ApiHandlerWithAnnotations<>();
                entry.apiHandler = AppUtils.loadClassAndCreateObject(bd.getBeanClassName(), IApiHandler.class);
                entry.annotations = new HashSet<>();

                Class<?> clazz = entry.apiHandler.getClass();
                A[] cAnnoList = clazz.getAnnotationsByType(annotation);
                for (A anno : cAnnoList) {
                    entry.annotations.add(anno);
                }

                Method method;
                try {
                    method = clazz.getMethod("handle", ApiContext.class, ApiAuth.class, ApiParams.class);
                } catch (NoSuchMethodException e) {
                    method = null;
                }
                if (method != null) {
                    A[] mAnnoList = method.getAnnotationsByType(annotation);
                    for (A anno : mAnnoList) {
                        entry.annotations.add(anno);
                    }
                }

                if (entry.annotations.size() > 0) {
                    result.add(entry);
                }
            }
        }

        return result;
    }
}
