package org.nbone.spring.boot.autoconfigure.cache;

import com.google.common.cache.CacheBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationPropertiesBindingPostProcessor;
import org.springframework.cache.guava.GuavaCacheManagerX;
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
public class GuavaMultipleConfiguration implements EnvironmentAware {

    @Resource
    private ConfigurationPropertiesBindingPostProcessor bindingPostProcessor;

    @Value("${spring.cache.guava.spec:}")
    private String specification;

    private GuavaCacheProperties properties = new GuavaCacheProperties();
    private Map<String, String> multiple;


    @Override
    public void setEnvironment(Environment environment) {
        bindingPostProcessor.postProcessBeforeInitialization(properties, "guavaCacheProperties");
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
    public GuavaCacheManagerX createCacheManager() {
        GuavaCacheManagerX cacheManager = new GuavaCacheManagerX();
        if (StringUtils.hasText(specification)) {
            cacheManager.setCacheSpecification(specification);
        }
   /*     cacheManager.setCacheBuilder(
                CacheBuilder.newBuilder().
                        expireAfterWrite(10, TimeUnit.MINUTES).
                        maximumSize(1000));

        cacheManager.addCacheBuilder("classify",
                CacheBuilder.newBuilder()
                        .expireAfterWrite(10, TimeUnit.MINUTES)
                        .maximumSize(1000));
        */
        processMultiple(cacheManager);
        return cacheManager;
    }


    public void processMultiple(GuavaCacheManagerX cacheManager) {
        if (multiple == null) {
            return;
        }
        for (Map.Entry<String, String> entry : multiple.entrySet()) {
            cacheManager.addCacheBuilder(entry.getKey(), CacheBuilder.from(entry.getValue()));
        }
    }
}
