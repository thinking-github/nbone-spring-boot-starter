package org.nbone.spring.boot.autoconfigure.cache;

import org.nbone.spring.boot.context.properties.MultipleProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author thinking
 * @version 1.0
 * @since 2019-08-06
 */
@Configuration
@ConfigurationProperties(prefix = "spring.cache.caffeine")
public class CaffeineCacheProperties implements MultipleProperties {

    private final Map<String, String> multiple = new HashMap<>();

    public Map<String, String> getMultiple() {
        return multiple;
    }


}
