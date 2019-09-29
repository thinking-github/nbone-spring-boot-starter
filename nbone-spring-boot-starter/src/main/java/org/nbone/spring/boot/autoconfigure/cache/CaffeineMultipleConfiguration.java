package org.nbone.spring.boot.autoconfigure.cache;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationPropertiesBindingPostProcessor;
import org.springframework.cache.caffeine.CaffeineCacheManagerX;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.util.Map;

/**
 * @author thinking
 * @version 1.0
 * @since 2019-08-06
 */
@SuppressWarnings("unused")
@Configuration
public class CaffeineMultipleConfiguration implements EnvironmentAware {

    @Resource
    private ConfigurationPropertiesBindingPostProcessor bindingPostProcessor;

    @Value("${spring.cache.caffeine.spec:}")
    private String specification;

    private CaffeineCacheProperties properties = new CaffeineCacheProperties();
    private Map<String, String> multiple;


    @Override
    public void setEnvironment(Environment environment) {
        bindingPostProcessor.postProcessBeforeInitialization(properties, "caffeineCacheProperties");
        this.multiple = properties.getMultiple();
    }


    /**
     * <ol>
     * <li> createCacheManager
     * <li> if primary config exist, load Primary cache config
     * <li> if multiple config exist,load Multiple cache config
     * </ol>
     *
     * @return
     */
    public CaffeineCacheManagerX createCacheManager() {
        CaffeineCacheManagerX cacheManager = new CaffeineCacheManagerX();
        if (StringUtils.hasText(specification)) {
            cacheManager.setCacheSpecification(specification);
        }

        processMultiple(cacheManager);
        return cacheManager;
    }


    public void processMultiple(CaffeineCacheManagerX cacheManager) {
        if (multiple == null) {
            return;
        }
        for (Map.Entry<String, String> entry : multiple.entrySet()) {
            cacheManager.addCacheBuilder(entry.getKey(), Caffeine.from(entry.getValue()));
        }
    }
}
