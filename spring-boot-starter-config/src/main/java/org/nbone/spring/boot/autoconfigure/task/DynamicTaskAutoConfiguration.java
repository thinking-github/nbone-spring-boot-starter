package org.nbone.spring.boot.autoconfigure.task;

import com.alibaba.nacos.client.config.NacosConfigService;
import com.ctrip.framework.apollo.Config;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.Resource;


/**
 * @author thinking
 * DataSourcePoolMetadataProvidersConfiguration
 * @since 2019/7/5
 */
@Configuration
public class DynamicTaskAutoConfiguration {


    @Bean
    @ConditionalOnMissingBean
    public static DynamicThreadPoolManager dynamicThreadPoolManager() {
        return new DynamicThreadPoolManager();
    }

    @Configuration
    @ConditionalOnClass(value = Config.class)
    @ConditionalOnProperty(prefix = "spring.task.executors.apollo", name = "enabled", havingValue = "true", matchIfMissing = true)
    static class ApolloProviderConfiguration {
        @Resource
        private DynamicThreadPoolManager dynamicThreadPoolManager;

        @Bean
        public ApolloTaskConfiguration apolloTaskConfiguration() {
            return new ApolloTaskConfiguration(dynamicThreadPoolManager);
        }
    }


    @Configuration
    @ConditionalOnClass(value = NacosConfigService.class)
    @ConditionalOnProperty(prefix = "spring.task.executors.nacos", name = "enabled", havingValue = "true", matchIfMissing = true)
    static class NacosProviderConfiguration {
        @Resource
        private DynamicThreadPoolManager dynamicThreadPoolManager;

        @Bean
        public NacosTaskConfiguration nacosTaskConfiguration() {
            return new NacosTaskConfiguration(dynamicThreadPoolManager);
        }
    }


}
