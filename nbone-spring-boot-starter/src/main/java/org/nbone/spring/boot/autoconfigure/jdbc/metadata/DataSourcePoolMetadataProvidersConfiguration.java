package org.nbone.spring.boot.autoconfigure.jdbc.metadata;

import com.alibaba.druid.pool.DruidAbstractDataSource;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.jdbc.metadata.DataSourcePoolMetadata;
import org.springframework.boot.autoconfigure.jdbc.metadata.DataSourcePoolMetadataProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

/**
 * @author thinking
 * @version 1.0
 * @since 2019-08-01
 */
@Configuration
public class DataSourcePoolMetadataProvidersConfiguration {

    @Configuration
    @ConditionalOnClass(DruidAbstractDataSource.class)
    static class DruidDataSourceMetadataProviderConfiguration {

        @Bean
        public DataSourcePoolMetadataProvider druidPoolDataSourceMetadataProvider() {
            return new DataSourcePoolMetadataProvider() {
                @Override
                public DataSourcePoolMetadata getDataSourcePoolMetadata(
                        DataSource dataSource) {
                    if (dataSource instanceof DruidAbstractDataSource) {
                        return new DruidDataSourcePoolMetadata((DruidAbstractDataSource) dataSource);
                    }
                    return null;
                }
            };
        }

    }
}
