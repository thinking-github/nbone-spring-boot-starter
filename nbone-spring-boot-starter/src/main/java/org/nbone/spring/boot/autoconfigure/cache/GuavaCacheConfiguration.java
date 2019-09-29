package org.nbone.spring.boot.autoconfigure.cache;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheBuilderSpec;
import com.google.common.cache.CacheLoader;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.cache.CacheProperties;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.CacheManager;
import org.springframework.cache.guava.GuavaCacheManager;
import org.springframework.cache.guava.GuavaCacheManagerX;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

import java.util.Map;

/**
 * 支持多个cache 个性化配置
 *
 * <li> spring.cache.guava.multiple.classify = expireAfterWrite=10m,maximumSize=1000
 * <li> spring.cache.guava.multiple.user     = expireAfterWrite=10m,maximumSize=1000
 *
 * @author thinking
 * @version 1.0
 * @since 2019-08-06
 */
@Configuration
@ConditionalOnClass({CacheBuilder.class, GuavaCacheManager.class})
@EnableConfigurationProperties({GuavaCacheProperties.class,CacheProperties.class})
//@ConditionalOnProperty(prefix = "spring.cache.guava.multiple", name = "enabled",havingValue = "true")
@ConditionalOnExpression("#{T(org.springframework.boot.context.config.ConfigUtils).containsPrefix(environment,'spring.cache.guava.multiple')}")
public class GuavaCacheConfiguration {

    private final CacheProperties cacheProperties;

    private final GuavaCacheProperties guavaCacheProperties;

    private final CacheBuilder<Object, Object> cacheBuilder;

    private final CacheBuilderSpec cacheBuilderSpec;

    private final CacheLoader<Object, Object> cacheLoader;

    /**
     * spring boot 2.0 remove guava
     */
    @Value("${spring.cache.guava.spec:}")
    private String specification;

    GuavaCacheConfiguration(CacheProperties cacheProperties,
                            GuavaCacheProperties guavaCacheProperties,
                            ObjectProvider<CacheBuilder<Object, Object>> cacheBuilder,
                            ObjectProvider<CacheBuilderSpec> cacheBuilderSpec,
                            ObjectProvider<CacheLoader<Object, Object>> cacheLoader) {
        this.cacheProperties = cacheProperties;
        this.guavaCacheProperties = guavaCacheProperties;
        this.cacheBuilder = cacheBuilder.getIfAvailable();
        this.cacheBuilderSpec = cacheBuilderSpec.getIfAvailable();
        this.cacheLoader = cacheLoader.getIfAvailable();
    }

    @Bean
    @ConditionalOnMissingBean
    public CacheManager cacheManager() {
        GuavaCacheManagerX cacheManager = new GuavaCacheManagerX();

        //Primary
        setCacheBuilder(cacheManager);
        if (this.cacheLoader != null) {
            cacheManager.setCacheLoader(this.cacheLoader);
        }

        //multiple
        processMultiple(cacheManager);

        return cacheManager;
    }

    private void setCacheBuilder(GuavaCacheManager cacheManager) {
        if (StringUtils.hasText(specification)) {
            cacheManager.setCacheSpecification(specification);
        } else if (this.cacheBuilderSpec != null) {
            cacheManager.setCacheBuilderSpec(this.cacheBuilderSpec);
        } else if (this.cacheBuilder != null) {
            cacheManager.setCacheBuilder(this.cacheBuilder);
        }
    }

    private void processMultiple(GuavaCacheManagerX guavaCacheManager) {
        Map<String, String> multiple = guavaCacheProperties.getMultiple();
        if (multiple == null) {
            return;
        }
        for (Map.Entry<String, String> entry : multiple.entrySet()) {
            guavaCacheManager.addCacheBuilder(entry.getKey(), CacheBuilder.from(entry.getValue()));
        }
    }


}
