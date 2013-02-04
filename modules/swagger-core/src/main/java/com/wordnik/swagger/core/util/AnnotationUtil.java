package com.wordnik.swagger.core.util;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.WeakHashMap;

public class AnnotationUtil {

    private static final Map<Class<?>, Boolean> annotatedInterfaceCache = new WeakHashMap<Class<?>, Boolean>();

    public static Class<?> findAnnotationDeclaringClass(Class<? extends Annotation> annotationType,
            Class<?> classToFind) {
        if (hasAnnotation(annotationType, classToFind)) {
            return classToFind;
        }
        Class clazz = classToFind;
        while (clazz != null && (clazz.getSuperclass() != null || clazz.getInterfaces().length > 0)) {
            if (hasAnnotation(annotationType, clazz.getSuperclass())) {
                return clazz.getSuperclass();
            }
            Class<?>[] interfaces = clazz.getInterfaces();
            if (interfaces.length > 0) {
                for (Class declaringInterface : interfaces) {
                    if (hasAnnotation(annotationType, declaringInterface)) {
                        return declaringInterface;
                    }
                }
            }
            clazz = clazz.getSuperclass();
        }
        return null;
    }

    private static boolean hasAnnotation(Class annotationType, Class clazz) {
        return (clazz != null && getAnnotation(clazz, annotationType) != null);
        //        return (clazz != null && clazz.getAnnotation(annotationType) != null);
    }

    //Code copied over from Spring
    public static <A extends Annotation> A findAnnotation(Class clazz, Class<A> annotationType) {
        A annotation = getAnnotation(clazz, annotationType);
        Class<?> cl = clazz;
        if (annotation == null) {
            annotation = searchOnInterfaces(clazz, annotationType, cl.getInterfaces());
        }
        while (annotation == null) {
            cl = cl.getSuperclass();
            if (cl == null || cl == Object.class) {
                break;
            }
            annotation = getAnnotation(cl, annotationType);
            if (annotation == null) {
                annotation = searchOnInterfaces(clazz, annotationType, cl.getInterfaces());
            }
        }
        return annotation;
    }

    //Code copied over from Spring
    public static <A extends Annotation> A findAnnotation(Method method, Class<A> annotationType) {
        A annotation = getAnnotation(method, annotationType);
        Class<?> cl = method.getDeclaringClass();
        if (annotation == null) {
            annotation = searchOnInterfaces(method, annotationType, cl.getInterfaces());
        }
        while (annotation == null) {
            cl = cl.getSuperclass();
            if (cl == null || cl == Object.class) {
                break;
            }
            try {
                Method equivalentMethod = cl.getDeclaredMethod(method.getName(), method.getParameterTypes());
                annotation = getAnnotation(equivalentMethod, annotationType);
                if (annotation == null) {
                    annotation = searchOnInterfaces(method, annotationType, cl.getInterfaces());
                }
            } catch (NoSuchMethodException ex) {
                // We're done...
            }
        }
        return annotation;
    }

    private static <A extends Annotation> A searchOnInterfaces(Method method, Class<A> annotationType, Class<?>[] ifcs) {
        A annotation = null;
        for (Class<?> iface : ifcs) {
            if (isInterfaceWithAnnotatedMethods(iface)) {
                try {
                    Method equivalentMethod = iface.getMethod(method.getName(), method.getParameterTypes());
                    annotation = getAnnotation(equivalentMethod, annotationType);
                } catch (NoSuchMethodException ex) {
                    // Skip this interface - it doesn't have the method...
                }
                if (annotation != null) {
                    break;
                }
            }
        }
        return annotation;
    }

    private static <A extends Annotation> A searchOnInterfaces(Class clazz, Class<A> annotationType, Class<?>[] ifcs) {
        A annotation = null;
        for (Class<?> iface : ifcs) {
            annotation = getAnnotation(iface, annotationType);
            if (annotation != null) {
                break;
            }
        }
        return annotation;
    }

    private static boolean isInterfaceWithAnnotatedMethods(Class<?> iface) {
        synchronized (annotatedInterfaceCache) {
            Boolean flag = annotatedInterfaceCache.get(iface);
            if (flag != null) {
                return flag;
            }
            boolean found = false;
            for (Method ifcMethod : iface.getMethods()) {
                if (ifcMethod.getAnnotations().length > 0) {
                    found = true;
                    break;
                }
            }
            annotatedInterfaceCache.put(iface, found);
            return found;
        }
    }

    public static <T extends Annotation> T getAnnotation(AnnotatedElement ae, Class<T> annotationType) {
        T ann = ae.getAnnotation(annotationType);
        if (ann == null) {
            for (Annotation metaAnn : ae.getAnnotations()) {
                if (annotationType.getClassLoader() != metaAnn.annotationType().getClassLoader()
                        && metaAnn.annotationType().getName().equals(annotationType.getName())) {
                    ann = (T) metaAnn;
                } else {
                    ann = metaAnn.annotationType().getAnnotation(annotationType);
                }
                if (ann != null) {
                    break;
                }
            }
        }
        return ann;
    }

}
