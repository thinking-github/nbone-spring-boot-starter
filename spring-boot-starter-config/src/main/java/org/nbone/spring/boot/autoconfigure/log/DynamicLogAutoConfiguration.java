package org.nbone.spring.boot.autoconfigure.log;

import com.ctrip.framework.apollo.Config;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


/**
 * @author thinking
 * DataSourcePoolMetadataProvidersConfiguration
 * @since 2019/7/5
 */
@Configuration
public class DynamicLogAutoConfiguration {


    @Configuration
    @ConditionalOnClass(value = Config.class)
    static class ApolloProviderLogConfiguration {
        @Bean
        public ApolloLoggerConfiguration apolloLoggerConfiguration() {
            return new ApolloLoggerConfiguration();
        }
    }


}
