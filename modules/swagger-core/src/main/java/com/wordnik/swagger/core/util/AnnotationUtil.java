package com.wordnik.swagger.core.util;

import java.lang.annotation.Annotation;

public class AnnotationUtil {

    public static Class<?> findAnnotationDeclaringClass(Class<? extends Annotation> annotationType, Class<?> classToFind) {
        if(hasAnnotation(annotationType, classToFind)) {
            return classToFind;
        }
        Class clazz = classToFind;
        while(clazz.getSuperclass() != null || clazz.getInterfaces().length > 0) {
            if(hasAnnotation(annotationType, clazz.getSuperclass())) {
                return clazz.getSuperclass();
            }
            Class<?>[] interfaces = clazz.getInterfaces();
            if(interfaces.length > 0) {
                for(Class declaringInterface : interfaces) {
                    if(hasAnnotation(annotationType, declaringInterface)) {
                        return declaringInterface;
                    }
                }
            }
            clazz = clazz.getSuperclass();
        }
        return null;
    }

    private static boolean hasAnnotation(Class<?> annotationType, Class clazz) {
        return (clazz.getAnnotation(annotationType) != null);
    }
}
