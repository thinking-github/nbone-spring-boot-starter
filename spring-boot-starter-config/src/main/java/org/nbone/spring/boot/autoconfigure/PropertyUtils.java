package org.nbone.spring.boot.autoconfigure;

import com.google.common.base.CaseFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.SimpleTypeConverter;
import org.springframework.beans.TypeConverter;
import org.springframework.beans.TypeConverterSupport;
import org.springframework.core.MethodParameter;
import org.springframework.core.convert.support.DefaultConversionService;

import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * @author thinking
 * @version 1.0
 * @since 2019/7/5
 */
public class PropertyUtils {

    private static final Logger logger = LoggerFactory.getLogger(PropertyUtils.class);

    private static final TypeConverterSupport typeConverter = new SimpleTypeConverter();

    static {
        typeConverter.setConversionService(DefaultConversionService.getSharedInstance());
    }

    public static void set(Object bean, String changedKey, String oldValue, String newValue) {
        PropertyUtils.set(bean, changedKey, oldValue, newValue, typeConverter);
    }

    public static void set(Object bean, String changedKey, String oldValue, String newValue, TypeConverter converter) {

        // maxActive ->maxActive
        // max-active ->maxActive
        String name = changedKey.substring(changedKey.lastIndexOf('.') + 1);
        if (name.indexOf("initial") > -1) {
            logger.warn("initial property not refresh {}:{}", changedKey, newValue);
            return;
        }
        if (name.indexOf('-') > 0) {
            name = CaseFormat.LOWER_HYPHEN.to(CaseFormat.LOWER_CAMEL, name);
        }
        PropertyDescriptor propertyDescriptor = BeanUtils.getPropertyDescriptor(bean.getClass(), name);
        if (propertyDescriptor == null) {
            logger.error("{} property not found class {}", name, bean.getClass().getName());
            return;
        }
        Object oValue = null;
        try {
            oValue = propertyDescriptor.getReadMethod().invoke(bean);
        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
            logger.error("bean property read error, class={}, property={}", bean.getClass().getName(), name);
        }

        try {
            Method method = propertyDescriptor.getWriteMethod();
            Object value = convertIfNecessary(method, newValue, converter);
            method.invoke(bean, value);
            logger.info("bean datasource {} changed : {} -> {}", name, oValue, newValue);
        } catch (IllegalAccessException | InvocationTargetException e) {
            logger.error("bean property write error, class={}, property={}", bean.getClass().getName(), name, e);
        }

        //ReflectionUtils.findMethod(dataSource.getClass(), name);
    }


    //TypeConverter converter = beanFactory.getTypeConverter();
    public static Object convertIfNecessary(Method method, Object value, TypeConverter converter) {
        Class<?>[] paramTypes = method.getParameterTypes();
        Object[] arguments = new Object[paramTypes.length];

        if (converter == null) {
            converter = typeConverter;
        }

        if (arguments.length == 1) {
            return converter.convertIfNecessary(value, paramTypes[0],
                    new MethodParameter(method, 0));
        }

        for (int i = 0; i < arguments.length; i++) {
            arguments[i] = converter.convertIfNecessary(value, paramTypes[i],
                    new MethodParameter(method, i));
        }

        return arguments;
    }

}
