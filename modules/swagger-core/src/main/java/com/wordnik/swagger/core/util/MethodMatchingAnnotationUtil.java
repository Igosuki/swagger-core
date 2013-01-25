package com.wordnik.swagger.core.util;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

public class MethodMatchingAnnotationUtil extends AnnotationUtil {


    private static final Map<Class<?>, Boolean> annotatedInterfaceCache = new WeakHashMap<Class<?>, Boolean>();

    public static <A extends Annotation> List<Method> findAnnotatedMethods(Method method, Class<A> annotationType) {
        List<Method> methods = new ArrayList<Method>();
        A annotation = getAnnotation(method, annotationType);
        Class<?> cl = method.getDeclaringClass();
        if (annotation != null) {
            methods.add(method);
        }
        Method baseMethod = searchOnInterfaces(method, annotationType, cl.getInterfaces());
        if(baseMethod != null) {
            methods.add(baseMethod);
        }
        while (cl.getSuperclass() != null || cl.getInterfaces().length > 0) {
            cl = cl.getSuperclass();
            if (cl == null || cl == Object.class) {
                break;
            }
            try {
                Method equivalentMethod = cl.getDeclaredMethod(method.getName(), method.getParameterTypes());
                annotation = getAnnotation(equivalentMethod, annotationType);
                if (annotation != null) {
                    methods.add(equivalentMethod);
                }
                Method ifMethod = searchOnInterfaces(method, annotationType, cl.getInterfaces());
                if(ifMethod != null) {
                    methods.add(ifMethod);
                }
            }
            catch (NoSuchMethodException ex) {
                // We're done...
            }
        }
        return methods;
    }

    private static <A extends Annotation> Method searchOnInterfaces(Method method, Class<A> annotationType, Class<?>[] ifcs) {
        A annotation = null;
        for (Class<?> iface : ifcs) {
            if (isInterfaceWithAnnotatedMethods(iface)) {
                Method equivalentMethod = null;
                try {
                    equivalentMethod = iface.getMethod(method.getName(), method.getParameterTypes());
                    annotation = getAnnotation(equivalentMethod, annotationType);
                }
                catch (NoSuchMethodException ex) {
                    // Skip this interface - it doesn't have the method...
                }
                if (annotation != null) {
                    return equivalentMethod;
                }
            }
        }
        return null;
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
}