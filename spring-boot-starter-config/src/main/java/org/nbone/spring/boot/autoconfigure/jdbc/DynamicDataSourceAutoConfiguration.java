package org.nbone.spring.boot.autoconfigure.jdbc;

import com.ctrip.framework.apollo.Config;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.Resource;


/**
 * @author thinking
 * DataSourcePoolMetadataProvidersConfiguration
 * @since 2019/7/5
 */
@Configuration
public class DynamicDataSourceAutoConfiguration {


    @Bean
    @ConditionalOnMissingBean
    public static DynamicDataSourceManager dynamicDataSourceManager() {
        return new DynamicDataSourceManager();
    }

    @Configuration
    @ConditionalOnClass(value = Config.class)
    static class ApolloProviderConfiguration {
        @Resource
        private DynamicDataSourceManager dynamicDataSourceManager;

        @Bean
        public ApolloDataSourceConfiguration apolloDataSourceConfiguration() {
            return new ApolloDataSourceConfiguration(dynamicDataSourceManager);
        }
    }


}
