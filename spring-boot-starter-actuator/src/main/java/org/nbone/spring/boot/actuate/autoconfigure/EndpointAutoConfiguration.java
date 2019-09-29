package org.nbone.spring.boot.actuate.autoconfigure;

import org.springframework.boot.actuate.endpoint.EndpointProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 * @author thinking
 * @version 1.0
 * @since 2019-09-28
 */
@Configuration
@EnableConfigurationProperties(EndpointProperties.class)
@ComponentScan(basePackages = "org.nbone.spring.boot.actuate.endpoint")
public class EndpointAutoConfiguration {

}
