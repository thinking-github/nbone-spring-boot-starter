package org.nbone.spring.boot.autoconfigure.jdbc;

import org.nbone.spring.boot.autoconfigure.PropertyUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.TypeConverter;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.util.ClassUtils;
import org.springframework.util.StringUtils;

import javax.sql.DataSource;
import java.util.Map;

/**
 * @author thinking
 * @version 1.0
 * @since 2019/7/5
 */
public class DynamicDataSourceManager implements BeanFactoryAware, ApplicationContextAware {

    private static final Logger logger = LoggerFactory.getLogger(DynamicDataSourceManager.class);

    private ConfigurableBeanFactory beanFactory;

    private ApplicationContext applicationContext;

    private TypeConverter typeConverter;

    private ShardingSphereDatasource shardingSphereDatasource;


    @Override
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        if (!(beanFactory instanceof ConfigurableBeanFactory)) {
            throw new IllegalArgumentException("ApolloDataSourceConfiguration requires a ConfigurableBeanFactory");
        }
        this.beanFactory = (ConfigurableBeanFactory) beanFactory;
        this.typeConverter = this.beanFactory.getTypeConverter();
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
        if (ClassUtils.isPresent(ShardingSphereDatasource.CLASS_NAME, DataSource.class.getClassLoader())) {
            this.shardingSphereDatasource = new ShardingSphereDatasource(applicationContext);
        }
    }

    public ShardingSphereDatasource getShardingSphereDatasource() {
        return shardingSphereDatasource;
    }

    public DataSource getDataSource(String name) {
        if (StringUtils.hasLength(name)) {
            return applicationContext.getBean(name, DataSource.class);
        } else {
            Map<String, DataSource> dataSourceMap = applicationContext.getBeansOfType(DataSource.class);
            if (dataSourceMap != null && dataSourceMap.size() == 1) {
                for (DataSource value : dataSourceMap.values()) {
                    return value;
                }
            }

            if (dataSourceMap != null && dataSourceMap.size() > 1) {
                logger.error("multiple data sources bean names={}, must be set datasource name", dataSourceMap.keySet());
            }

        }
        return null;
    }


    public void set(Object dataSource, String changedKey, String oldValue, String newValue) {
        PropertyUtils.set(dataSource, changedKey, oldValue, newValue,typeConverter);
    }


}
