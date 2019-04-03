package org.nbone.spring.boot.autoconfigure.cors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.CollectionUtils;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

/**
 * Created by chenyicheng on 2018/4/04.
 *
 * @author chenyicheng
 * @version 1.0
 * @since 2018-04-04
 */

@Configuration
@EnableConfigurationProperties(WebCorsProperties.class)
public class WebCorsAutoConfiguration {


    private final  WebCorsProperties properties;

    /**
     * 支持默认设置模式
     */
    @Value("${spring.web.cors.cors-default:false}")
    private  boolean corsDefault;

    @Value("${spring.web.cors.mapping:/**}")
    private  String mapping;

    @Value("${spring.web.cors.path:/**}")
    private  String path;



    public WebCorsAutoConfiguration(WebCorsProperties properties) {
        this.properties = properties;
        System.out.println("=========================WebCorsAutoConfiguration init.");
    }


    private CorsConfiguration buildConfig() {
        CorsConfiguration corsConfiguration = new CorsConfiguration();
        corsConfiguration.addAllowedOrigin("*"); // 1
        corsConfiguration.addAllowedHeader("*"); // 2
        corsConfiguration.addAllowedMethod("*"); // 3
        corsConfiguration.setMaxAge(1800L);
        return corsConfiguration;
    }


    private CorsConfiguration getCorsConfiguration() {
        if (CollectionUtils.isEmpty(properties.getAllowedOrigins())) {
            return null;
        }
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(properties.getAllowedOrigins());

        if (!CollectionUtils.isEmpty(properties.getAllowedHeaders())) {
            configuration.setAllowedHeaders(properties.getAllowedHeaders());
        }
        if (!CollectionUtils.isEmpty(properties.getAllowedMethods())) {
            configuration.setAllowedMethods(properties.getAllowedMethods());
        }

        if (!CollectionUtils.isEmpty(properties.getExposedHeaders())) {
            configuration.setExposedHeaders(properties.getExposedHeaders());
        }
        if (properties.getMaxAge() != null) {
            configuration.setMaxAge(properties.getMaxAge());
        }
        if (properties.getAllowCredentials() != null) {
            configuration.setAllowCredentials(properties.getAllowCredentials());
        }

        return configuration;
    }




    @Bean
    @ConditionalOnMissingBean(CorsFilter.class)
    public CorsFilter corsFilter() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        if(corsDefault){
            source.registerCorsConfiguration("/**", buildConfig()); // 4

        }else {
            source.registerCorsConfiguration(this.mapping, getCorsConfiguration());
        }
        return new CorsFilter(source);
    }




    public String getMapping() {
        return mapping;
    }

    public void setMapping(String mapping) {
        this.mapping = mapping;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }
}
