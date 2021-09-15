package org.nbone.spring.boot.autoconfigure.jdbc;

import com.alibaba.druid.pool.DruidDataSource;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.jdbc.DataSourceBuilder;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DatabaseDriver;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.StringUtils;

import javax.sql.DataSource;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

/**
 * support multiple datasource configuration
 *
 * @author thinking
 * @version 1.0
 * @since 2020-07-29
 * see org.springframework.boot.autoconfigure.jdbc.DataSourceConfiguration
 */
public class DataSourceConfiguration {

    protected static final Map<String, String> DATA_SOURCE_TYPE_NAMES = new HashMap<String, String>();

    static {
        DATA_SOURCE_TYPE_NAMES.put("org.apache.tomcat.jdbc.pool.DataSource", "spring.datasource.tomcat");
        DATA_SOURCE_TYPE_NAMES.put("com.zaxxer.hikari.HikariDataSource", "spring.datasource.hikari");
        DATA_SOURCE_TYPE_NAMES.put("org.apache.commons.dbcp.BasicDataSource", "spring.datasource.dbcp");
        DATA_SOURCE_TYPE_NAMES.put("org.apache.commons.dbcp2.BasicDataSource", "spring.datasource.dbcp2");
        DATA_SOURCE_TYPE_NAMES.put("com.alibaba.druid.pool.DruidDataSource", "spring.datasource.druid");
    }

    @SuppressWarnings("unchecked")
    public static <T> T createDataSource(DataSourceProperties properties,
                                         Class<? extends DataSource> type) {

        String prefix = DATA_SOURCE_TYPE_NAMES.get(type.getName());
        if (StringUtils.isEmpty(prefix)) {
            prefix = "spring.datasource";
        }
        Field environmentField = ReflectionUtils.findField(DataSourceProperties.class, "environment");
        ReflectionUtils.makeAccessible(environmentField);
        Environment environment = (Environment) ReflectionUtils.getField(environmentField, properties);

        String driverClassName = environment.getProperty(prefix + ".driver-class-name");
        if (StringUtils.isEmpty(driverClassName)) {
            driverClassName = properties.determineDriverClassName();
        }
        String url = environment.getProperty(prefix + ".url");
        String username = environment.getProperty(prefix + ".username");
        String password = environment.getProperty(prefix + ".password");


        DataSourceBuilder builder = DataSourceBuilder.create(properties.getClassLoader()).type(properties.getType())
                .driverClassName(driverClassName).url(url)
                .username(username).password(password);


        return (T) builder.type(type).build();
    }

    /**
     * Tomcat Pool DataSource configuration.
     */
    @ConditionalOnClass(org.apache.tomcat.jdbc.pool.DataSource.class)
    @ConditionalOnProperty(name = "spring.datasource.type", havingValue = "org.apache.tomcat.jdbc.pool.DataSource",
            matchIfMissing = true)
    static class Tomcat {

        @Bean
        @ConfigurationProperties(prefix = "spring.datasource.tomcat")
        public org.apache.tomcat.jdbc.pool.DataSource tomcatDataSource(DataSourceProperties properties) {
            org.apache.tomcat.jdbc.pool.DataSource dataSource = createDataSource(properties,
                    org.apache.tomcat.jdbc.pool.DataSource.class);
            DatabaseDriver databaseDriver = DatabaseDriver.fromJdbcUrl(properties.determineUrl());
            String validationQuery = databaseDriver.getValidationQuery();
            if (validationQuery != null) {
                dataSource.setTestOnBorrow(true);
                dataSource.setValidationQuery(validationQuery);
            }
            return dataSource;
        }

    }
    /**
     * Hikari DataSource configuration.
     */
    @ConditionalOnClass(HikariDataSource.class)
    @ConditionalOnProperty(name = "spring.datasource.type", havingValue = "com.zaxxer.hikari.HikariDataSource",
            matchIfMissing = true)
    public static class Hikari {

        @Bean
        @ConfigurationProperties(prefix = "spring.datasource.hikari")
        public HikariDataSource hikariDataSource(DataSourceProperties properties) {
            return createDataSource(properties, HikariDataSource.class);
        }

    }

    /**
     * Druid DataSource configuration.
     */
    @ConditionalOnClass(DruidDataSource.class)
    @ConditionalOnProperty(name = "spring.datasource.type", havingValue = "com.alibaba.druid.pool.DruidDataSource",
            matchIfMissing = true)
    public static class Druid {

        @Bean
        @ConfigurationProperties(prefix = "spring.datasource.druid")
        public DruidDataSource druidDataSource(DataSourceProperties properties) {
            return createDataSource(properties, DruidDataSource.class);
        }

    }



    @ConditionalOnClass(org.apache.commons.dbcp2.BasicDataSource.class)
    //@ConditionalOnMissingBean(DataSource.class)
    @ConditionalOnProperty(name = "spring.datasource.type", havingValue = "org.apache.commons.dbcp2.BasicDataSource", matchIfMissing = true)
    public static class Dbcp2 {

        @Bean
        @ConfigurationProperties(prefix = "spring.datasource.dbcp2")
        public org.apache.commons.dbcp2.BasicDataSource dbcp2DataSource(DataSourceProperties properties) {
            return createDataSource(properties, org.apache.commons.dbcp2.BasicDataSource.class);
        }

    }
}


