package org.nbone.spring.boot.autoconfigure.cache;

import com.github.benmanes.caffeine.cache.CacheLoader;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.CaffeineSpec;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.cache.CacheProperties;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.cache.caffeine.CaffeineCacheManagerX;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

import java.util.Map;

/**
 * 支持多个cache 个性化配置
 *
 * <li> spring.cache.caffeine.multiple.classify = expireAfterWrite=10m,maximumSize=1000
 * <li> spring.cache.caffeine.multiple.user     = expireAfterWrite=10m,maximumSize=1000
 *
 * @author thinking
 * @version 1.0
 * @since 2019-08-08
 */
@Configuration
@ConditionalOnClass({Caffeine.class, CaffeineCacheManager.class})
@EnableConfigurationProperties({CaffeineCacheProperties.class, CacheProperties.class})
@ConditionalOnExpression("#{T(org.springframework.boot.context.config.ConfigUtils).containsPrefix(environment,'spring.cache.caffeine.multiple')}")
public class CaffeineCacheConfiguration {

    private final CacheProperties cacheProperties;

    private final CaffeineCacheProperties caffeineCacheProperties;

    private final Caffeine<Object, Object> caffeine;

    private final CaffeineSpec caffeineSpec;

    private final CacheLoader<Object, Object> cacheLoader;


    CaffeineCacheConfiguration(CacheProperties cacheProperties,
                               CaffeineCacheProperties caffeineCacheProperties,
                               ObjectProvider<Caffeine<Object, Object>> caffeine,
                               ObjectProvider<CaffeineSpec> caffeineSpec,
                               ObjectProvider<CacheLoader<Object, Object>> cacheLoader) {
        this.cacheProperties = cacheProperties;
        this.caffeineCacheProperties = caffeineCacheProperties;
        this.caffeine = caffeine.getIfAvailable();
        this.caffeineSpec = caffeineSpec.getIfAvailable();
        this.cacheLoader = cacheLoader.getIfAvailable();
    }


    @Bean
    @ConditionalOnMissingBean
    public CacheManager cacheManager() {
        CaffeineCacheManagerX cacheManager = new CaffeineCacheManagerX();

        //Primary
        setCacheBuilder(cacheManager);
        if (this.cacheLoader != null) {
            cacheManager.setCacheLoader(this.cacheLoader);
        }

        //multiple
        processMultiple(cacheManager);

        return cacheManager;
    }

    private void setCacheBuilder(CaffeineCacheManager cacheManager) {
        String specification = this.cacheProperties.getCaffeine().getSpec();
        if (StringUtils.hasText(specification)) {
            cacheManager.setCacheSpecification(specification);
        } else if (this.caffeineSpec != null) {
            cacheManager.setCaffeineSpec(this.caffeineSpec);
        } else if (this.caffeine != null) {
            cacheManager.setCaffeine(this.caffeine);
        }
    }


    private void processMultiple(CaffeineCacheManagerX caffeineCacheManagerX) {
        Map<String, String> multiple = caffeineCacheProperties.getMultiple();
        if (multiple == null) {
            return;
        }
        for (Map.Entry<String, String> entry : multiple.entrySet()) {
            caffeineCacheManagerX.addCacheBuilder(entry.getKey(), Caffeine.from(entry.getValue()));
        }
    }

}
