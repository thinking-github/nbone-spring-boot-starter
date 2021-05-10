package org.nbone.spring.boot.autoconfigure.jdbc;

import org.apache.shardingsphere.shardingjdbc.jdbc.adapter.AbstractDataSourceAdapter;
import org.apache.shardingsphere.shardingjdbc.jdbc.core.context.AbstractRuntimeContext;
import org.apache.shardingsphere.underlying.common.config.properties.ConfigurationProperties;
import org.apache.shardingsphere.underlying.common.config.properties.ConfigurationPropertyKey;
import org.apache.shardingsphere.underlying.common.properties.TypedPropertyValue;
import org.apache.shardingsphere.underlying.common.properties.TypedPropertyValueException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.StringUtils;

import javax.sql.DataSource;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Map;

/**
 * @author thinking
 * @version 1.0
 * @since @since 2019/7/5
 */
public class ShardingSphereDatasource {

    private static final Logger logger = LoggerFactory.getLogger(ShardingSphereDatasource.class);

    public final static String CLASS_NAME = "org.apache.shardingsphere.shardingjdbc.jdbc.adapter.AbstractDataSourceAdapter";
    public final static String SHARDING_SPHERE_DATASOURCE_BEAN_NAME = "spring.shardingsphere.datasource.beanName";
    public final static String SHARDING_SPHERE_PROPS_PROFIX = "spring.shardingsphere.props.";

    String prefix = "spring.shardingsphere.datasource.";


    private ApplicationContext applicationContext;

    public ShardingSphereDatasource(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }


    public AbstractDataSourceAdapter getShardingSphereDatasource(String beanName) {
        if (StringUtils.hasLength(beanName)) {
            return applicationContext.getBean(beanName, AbstractDataSourceAdapter.class);
        }
        return applicationContext.getBean(AbstractDataSourceAdapter.class);
    }

    public Map<String, DataSource> getShardingDatasourceMap(String beanName) {
        return getShardingSphereDatasource(beanName).getDataSourceMap();
    }

    public DataSource getShardingSphereDatasource(String beanName, String name) {
        return getShardingDatasourceMap(beanName).get(name);
    }

    /**
     * https://shardingsphere.apache.org/document/current/cn/user-manual/shardingsphere-jdbc/configuration/props/
     */
    public void setShardingSphereProperties(String beanName, String changedKey, String oldValue, String newValue) {
        AbstractDataSourceAdapter abstractDataSource = getShardingSphereDatasource(beanName);
        Method method = ReflectionUtils.findMethod(abstractDataSource.getClass(), "getRuntimeContext");
        AbstractRuntimeContext runtimeContext = (AbstractRuntimeContext) ReflectionUtils.invokeMethod(method, abstractDataSource);
        ConfigurationProperties configurationProperties = runtimeContext.getProperties();
        Field cacheField = ReflectionUtils.findField(configurationProperties.getClass(), "cache", Map.class);

        ReflectionUtils.makeAccessible(cacheField);
        Map<Enum, TypedPropertyValue> cache = (Map<Enum, TypedPropertyValue>) ReflectionUtils.getField(cacheField, configurationProperties);


        String key = changedKey.substring(SHARDING_SPHERE_PROPS_PROFIX.length());
        ConfigurationPropertyKey propertyKey = getConfigurationPropertyKey(key);
        if (propertyKey == null) {
            logger.error(ConfigurationPropertyKey.class.getName() + " not found {} enum mapping.", key);
            return;
        }

        Object oValue = configurationProperties.getValue(propertyKey);
        TypedPropertyValue value = null;
        try {
            value = new TypedPropertyValue(propertyKey, newValue);
        } catch (TypedPropertyValueException e) {
            logger.error(e.getMessage(), e);
        }
        cache.put(propertyKey, value);
        logger.info("shardingSphere configuration properties {} changed : {} -> {}", key, oValue, newValue);
    }

    private ConfigurationPropertyKey getConfigurationPropertyKey(String key) {
        for (ConfigurationPropertyKey propertyKey : ConfigurationPropertyKey.values()) {
            if (propertyKey.getKey().equals(key)) {
                return propertyKey;
            }
        }
        return null;
    }


}
