package org.nbone.spring.boot.autoconfigure.jdbc;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.config.ConfigUtils;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.Environment;
import org.springframework.util.StringUtils;

import javax.sql.DataSource;

import static org.nbone.spring.boot.autoconfigure.jdbc.DataSourceBeanFactoryPostProcessor.PRIMARY_NAME;
import static org.nbone.spring.boot.autoconfigure.jdbc.DataSourceType.DATA_SOURCE_TYPES;


/**
 * <li>spring.datasource.primary: shardingDataSource </li>
 * <li>spring.datasource.primary: dataSource </li>
 *
 * @author thinking
 * @version 1.0
 * @since 1/26/21
 */
@ConditionalOnProperty(name = PRIMARY_NAME)
@ConditionalOnBean(DataSource.class)
@Configuration
public class DataSourceBeanFactoryPostProcessor implements EnvironmentAware, BeanFactoryPostProcessor {

    private static final Logger logger = LoggerFactory.getLogger(DataSourceBeanFactoryPostProcessor.class);

    // spring.datasource.primary: shardingDataSource
    public final static String PRIMARY_NAME = "spring.datasource.primary";
    private Environment environment;

    @Override
    public void setEnvironment(Environment environment) {
        this.environment = environment;
    }

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
        if (environment == null) {
            environment = beanFactory.getBean(ConfigurableApplicationContext.ENVIRONMENT_BEAN_NAME, Environment.class);
        }
        setAlias(beanFactory, environment);
        String name = environment.getProperty(PRIMARY_NAME);
        if (StringUtils.isEmpty(name)) {
            String[] beanNames = beanFactory.getBeanNamesForType(DataSource.class);

            return;
        }

        //shardingDataSource,dataSource
        BeanDefinition definition = beanFactory.getBeanDefinition(name);
        logger.info("dataSource name '{}' isPrimary= {}", name, definition.isPrimary());
        definition.setPrimary(true);

    }

    private void setAlias(ConfigurableListableBeanFactory beanFactory, Environment environment) {
        for (DataSourceType type : DATA_SOURCE_TYPES) {
            String prefix = type.getPrefix() + ".";
            if (ConfigUtils.containsPrefix((ConfigurableEnvironment) environment, prefix)) {
                String beanName = environment.getProperty(prefix + "name");
                if (StringUtils.hasLength(beanName)) {
                    if (!beanFactory.containsBeanDefinition(type.getName())) {
                        logger.warn("dataSource name '{}' not exist. alias name '{}'", type.getName(), beanName);
                        continue;
                    }
                    beanFactory.registerAlias(type.getName(), beanName);
                    if (logger.isInfoEnabled()) {
                        logger.info("dataSource name '{}' alias name '{}'", type.getName(), beanName);
                    }
                }
            }

        }
    }


}
