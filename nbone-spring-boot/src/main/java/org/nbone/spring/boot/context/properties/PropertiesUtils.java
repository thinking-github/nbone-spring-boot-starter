package org.nbone.spring.boot.context.properties;

import lombok.SneakyThrows;
import org.springframework.core.env.Environment;

import java.lang.reflect.Method;

/**
 * @author thinking
 * @version 1.0
 * @since 5/25/21
 * see org.apache.shardingsphere.spring.boot.util.PropertyUtil
 */
public class PropertiesUtils {

    private static int springBootVersion = 1;

    static {
        try {
            Class.forName("org.springframework.boot.bind.RelaxedPropertyResolver");
        } catch (ClassNotFoundException ignored) {
            springBootVersion = 2;
        }
    }


    @SneakyThrows
    private static Object v2(final Environment environment, final String prefix, final Class<?> targetClass) {
        Class<?> binderClass = Class.forName("org.springframework.boot.context.properties.bind.Binder");
        Method getMethod = binderClass.getDeclaredMethod("get", Environment.class);
        Method bindMethod = binderClass.getDeclaredMethod("bind", String.class, Class.class);
        Object binderObject = getMethod.invoke(null, environment);
        String prefixParam = prefix.endsWith(".") ? prefix.substring(0, prefix.length() - 1) : prefix;
        Object bindResultObject = bindMethod.invoke(binderObject, prefixParam, targetClass);
        Method resultGetMethod = bindResultObject.getClass().getDeclaredMethod("get");
        return resultGetMethod.invoke(bindResultObject);
    }

}
