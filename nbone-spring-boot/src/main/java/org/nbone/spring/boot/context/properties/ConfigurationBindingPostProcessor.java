package org.nbone.spring.boot.context.properties;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.boot.bind.PropertiesConfigurationFactory;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.EnvironmentAware;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.convert.support.DefaultConversionService;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.Environment;
import org.springframework.core.env.PropertySources;
import org.springframework.util.ClassUtils;
import org.springframework.util.StringUtils;
import org.springframework.validation.Validator;

/**
 * 将spring配置文件装换成实体类,支持覆盖 properties prefix
 *
 * @author thinking
 * @version 1.0
 * @see org.springframework.boot.context.properties.ConfigurationPropertiesBindingPostProcessor
 * @see org.springframework.boot.context.properties.ConfigurationPropertiesBindingPostProcessorRegistrar
 * @since 2019-07-29
 */
@SuppressWarnings("unused")
public class ConfigurationBindingPostProcessor implements BeanFactoryAware, EnvironmentAware, ApplicationContextAware {

    private PropertySources propertySources;

    private Validator validator;

    private ConversionService conversionService;
    private DefaultConversionService defaultConversionService;

    private BeanFactory beanFactory;

    private Environment environment;
    private ApplicationContext applicationContext;

    /**
     * Set the property sources to bind.
     *
     * @param propertySources the property sources
     */
    public void setPropertySources(PropertySources propertySources) {
        this.propertySources = propertySources;
    }

    /**
     * Set the bean validator used to validate property fields.
     *
     * @param validator the validator
     */
    public void setValidator(Validator validator) {
        this.validator = validator;
    }


    /**
     * Set the conversion service used to convert property values.
     *
     * @param conversionService the conversion service
     */
    public void setConversionService(ConversionService conversionService) {
        this.conversionService = conversionService;
    }

    @Override
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        this.beanFactory = beanFactory;
    }

    @Override
    public void setEnvironment(Environment environment) {
        this.environment = environment;
        if (environment instanceof ConfigurableEnvironment) {
            ConfigurableEnvironment env = (ConfigurableEnvironment) environment;
            if (this.conversionService == null) {
                this.conversionService = env.getConversionService();
            }
            if (this.propertySources == null) {
                this.propertySources = env.getPropertySources();
            }
        }
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
        if (this.environment == null) {
            this.environment = applicationContext.getEnvironment();
        }
    }

    /**
     * new add function
     *
     * @param environment
     * @param clazz  Configuration Properties target class
     * @param prefix  properties prefix
     * @param <T>
     * @return
     * @throws BeansException
     */
    public <T> T postProcessBind(final Environment environment, Class<T> clazz, String prefix) throws BeansException {
        // TODO:
        throw new UnsupportedOperationException();
    }
    /**
     * new add function
     * @param clazz  Configuration Properties target class
     * @param prefix properties prefix
     * @param <T>
     * @return
     * @throws BeansException
     */
    public <T> T postProcessBind(Class<T> clazz,String prefix) throws BeansException {
       T bean = BeanUtils.instantiate(clazz);
        return (T) postProcessBind(bean,prefix);
    }
    // new add
    public Object postProcessBind(Object bean,String prefix) throws BeansException {
        ConfigurationProperties annotation = AnnotationUtils.findAnnotation(bean.getClass(), ConfigurationProperties.class);
        if (annotation != null) {
            postProcessBeforeInitialization(bean, prefix, annotation,prefix);
        }
        return bean;
    }



    public Object postProcessBeforeInitialization(Object bean, String beanName)
            throws BeansException {
        ConfigurationProperties annotation = AnnotationUtils.findAnnotation(bean.getClass(), ConfigurationProperties.class);
        if (annotation != null) {
            postProcessBeforeInitialization(bean, beanName, annotation);
        }
        return bean;
    }

    @SuppressWarnings("deprecation")
    private void postProcessBeforeInitialization(Object bean, String beanName, ConfigurationProperties annotation){
        postProcessBeforeInitialization(bean,beanName,annotation,null);
    }
    @SuppressWarnings("deprecation")
    private void postProcessBeforeInitialization(Object bean, String beanName,
                                                 ConfigurationProperties annotation,String prefix) {
        Object target = bean;
        PropertiesConfigurationFactory<Object> factory = new PropertiesConfigurationFactory<Object>(target);
        factory.setPropertySources(this.propertySources);
        factory.setApplicationContext(this.applicationContext);
        //factory.setValidator(determineValidator(bean));
        // If no explicit conversion service is provided we add one so that (at least)
        // comma-separated arrays of convertibles can be bound automatically
        factory.setConversionService((this.conversionService != null) ? this.conversionService : getDefaultConversionService());
        if (annotation != null) {
            factory.setIgnoreInvalidFields(annotation.ignoreInvalidFields());
            factory.setIgnoreUnknownFields(annotation.ignoreUnknownFields());
            factory.setExceptionIfInvalid(annotation.exceptionIfInvalid());
            factory.setIgnoreNestedProperties(annotation.ignoreNestedProperties());
            //如果传入前缀则覆盖注解元素中定义的前缀名称
            if(StringUtils.hasLength(prefix)){
                factory.setTargetName(prefix);
            }else {
                if (StringUtils.hasLength(annotation.prefix())) {
                    factory.setTargetName(annotation.prefix());
                }
            }
        }
        try {
            factory.bindPropertiesToTarget();
        } catch (Exception ex) {
            String targetClass = ClassUtils.getShortName(target.getClass());
            throw new BeanCreationException(beanName, "Could not bind properties to " + targetClass + " (" + getAnnotationDetails(annotation) + ")", ex);
        }
    }

    private String getAnnotationDetails(ConfigurationProperties annotation) {
        if (annotation == null) {
            return "";
        }
        StringBuilder details = new StringBuilder();
        details.append("prefix=").append(annotation.prefix());
        details.append(", ignoreInvalidFields=").append(annotation.ignoreInvalidFields());
        details.append(", ignoreUnknownFields=").append(annotation.ignoreUnknownFields());
        details.append(", ignoreNestedProperties=")
                .append(annotation.ignoreNestedProperties());
        return details.toString();
    }

    private ConversionService getDefaultConversionService() {
        if (this.defaultConversionService == null) {
            DefaultConversionService conversionService = new DefaultConversionService();

            this.defaultConversionService = conversionService;
        }
        return this.defaultConversionService;
    }


}
