package org.nbone.spring.boot.autoconfigure.validation;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnResource;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.validation.MessageInterpolatorFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Role;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.util.StringUtils;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import javax.validation.Validator;
import javax.validation.executable.ExecutableValidator;
import java.util.Map;


/**
 * add Validation Config <br>
 *
 * {@link org.hibernate.validator.HibernateValidatorConfiguration } eg: hibernate.validator.fail_fast=true
 *
 * @author chenyicheng
 * @version 1.0
 * @since 2018/4/10
 *
 * @see  MessageInterpolatorFactory
 * @see  org.springframework.boot.autoconfigure.validation.ValidationAutoConfiguration
 * @see  org.hibernate.validator.HibernateValidatorConfiguration
 */

@Configuration
@ConditionalOnClass(ExecutableValidator.class)
@ConditionalOnResource(resources = "classpath:META-INF/services/javax.validation.spi.ValidationProvider")
@EnableConfigurationProperties(HibernateValidatorProperties.class)
public class ValidationAutoConfiguration {

    private static final String PROPERTIES_SUFFIX = ".properties";

    private static final String XML_SUFFIX = ".xml";


    private final HibernateValidatorProperties properties ;

    public ValidationAutoConfiguration(HibernateValidatorProperties properties) {
        this.properties = properties;
        System.out.println("=========================ValidationAutoConfiguration init.");
    }


    @Bean
    @Role(BeanDefinition.ROLE_INFRASTRUCTURE)
    @ConditionalOnMissingBean(Validator.class)
    public  LocalValidatorFactoryBean nboneDefaultValidator() {

        LocalValidatorFactoryBean factoryBean = new LocalValidatorFactoryBean();
        // MessageInterpolatorFactory interpolatorFactory = new MessageInterpolatorFactory();

        // add custom messages  validation
        if(StringUtils.hasText(properties.getMessagesLocation())){

            ReloadableResourceBundleMessageSource messageSource = new ReloadableResourceBundleMessageSource();

            String messagePath = properties.getMessagesLocation();
            String defaultPath = "classpath:org/hibernate/validator/ValidationMessages";

            if(messagePath.endsWith(PROPERTIES_SUFFIX)){
                messagePath = messagePath.substring(0,messagePath.indexOf(PROPERTIES_SUFFIX));
            }

            messageSource.setBasenames(messagePath,defaultPath);
            messageSource.setDefaultEncoding("UTF-8");

            factoryBean.setValidationMessageSource(messageSource);

        }


        //factoryBean.setMessageInterpolator(interpolatorFactory.getObject());
        //Map<String, String> validationProperties = new HashMap<>();
        //validationProperties.put("hibernate.validator.fail_fast","true");

        Map<String,String>  map = properties.getProperties();
        factoryBean.setValidationPropertyMap(map);


        return factoryBean;
    }









}
