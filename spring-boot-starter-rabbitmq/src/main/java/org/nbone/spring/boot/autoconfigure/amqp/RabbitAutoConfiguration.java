package org.nbone.spring.boot.autoconfigure.amqp;

import org.nbone.spring.boot.context.properties.ConfigurationBindingPostProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.AutoConfigureOrder;
import org.springframework.boot.autoconfigure.amqp.RabbitConfiguration;
import org.springframework.boot.autoconfigure.amqp.RabbitProperties;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.context.properties.ConfigurationPropertiesBindingPostProcessor;
import org.springframework.boot.context.properties.ConfigurationPropertiesBindingPostProcessorRegistrar;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.Ordered;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.Environment;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.nbone.spring.boot.autoconfigure.amqp.RabbitMultipleProperties.Address;

/**
 * 支持多个Rabbit 配置  <br/>
 * multiple 模式 spring.rabbitmq.multiple is not null .thinking <br/>
 * 命名空间模式   spring.rabbitmq.chen.host=host<br/>
 * spring.rabbitmq.chen.port=port
 *
 * @author thinking
 * @version 1.0
 * @since 2019-07-26
 */

@Configuration
@EnableConfigurationProperties(value = {RabbitMultipleProperties.class})
@ConditionalOnClass({RabbitTemplate.class})
@AutoConfigureOrder(Ordered.HIGHEST_PRECEDENCE)
@AutoConfigureAfter(org.springframework.boot.autoconfigure.amqp.RabbitAutoConfiguration.class)
@Import({RabbitAutoConfiguration.RabbitRegistrar.class})
public class RabbitAutoConfiguration {

    private static final Logger logger = LoggerFactory.getLogger(RabbitAutoConfiguration.class);

    private static Map<String, ConnectionFactory> cacheMap = new HashMap<>();
    private static Map<String, List<Address>> targetMap = new HashMap<>();

    /**
     * register BeanDefinition
     */
    public static class RabbitRegistrar implements ImportBeanDefinitionRegistrar, BeanFactoryAware, EnvironmentAware {

        public static final String CF_BEAN_NAME = "rabbitConnectionFactoryX";
        public static final String BIND_BEAN_NAME = ConfigurationPropertiesBindingPostProcessorRegistrar.BINDER_BEAN_NAME;

        private static ConfigurationBindingPostProcessor bind = new ConfigurationBindingPostProcessor();
        private static RabbitMultipleProperties properties = new RabbitMultipleProperties();

        private ConfigurableListableBeanFactory beanFactory;
        private ConfigurableEnvironment environment;

        private ConfigurationPropertiesBindingPostProcessor binding;

        private Map<String, RabbitProperties> rabbitPropertiesMap = new HashMap<>();


        @Override
        public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata, BeanDefinitionRegistry registry) {
            if (!registry.containsBeanDefinition(CF_BEAN_NAME) && targetMap.size() > 0) {
                BeanDefinitionBuilder bean = BeanDefinitionBuilder.genericBeanDefinition(RabbitConnectionFactoryX.class);
                bean.addConstructorArgValue(properties);

                //spring RabbitAutoConfiguration rabbitTemplate
                bean.addPropertyReference("defaultTemplate", RabbitConfigUtils.RABBIT_T_BEAN_NAME);
                bean.addPropertyReference("defaultAdmin", RabbitConfigUtils.RABBIT_ADMIN_BEAN_NAME);
                bean.addPropertyReference("defaultContainerFactory", RabbitConfigUtils.RABBIT_LCF_BEAN_NAME);
                bean.addPropertyReference("containerFactoryConfigurer", RabbitConfigUtils.RABBIT_LCFC_BEAN_NAME);

                bean.addPropertyValue("targetMap", targetMap);
                bean.addPropertyValue("cacheMap", cacheMap);
                registry.registerBeanDefinition(CF_BEAN_NAME, bean.getBeanDefinition());

            }

            //namespace RabbitTemplate 模式配置加载
            registerRabbitTemplateNamespace(registry);
            //namespace RabbitListenerContainerFactory 模式配置加载
            registerRabbitListenerContainerFactoryNamespace(registry);


            //register RabbitTemplate
            registerRabbitTemplate(registry);
            //register RabbitListenerContainerFactory
            registerRabbitListenerContainerFactory(registry);

        }

        @Override
        public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
            if (beanFactory instanceof ConfigurableListableBeanFactory) {
                this.beanFactory = (ConfigurableListableBeanFactory) beanFactory;
                //使用系统内部的绑定配置实现
                binding = beanFactory.getBean(BIND_BEAN_NAME, ConfigurationPropertiesBindingPostProcessor.class);

            }
        }

        @Override
        public void setEnvironment(Environment environment) {
            if (environment instanceof ConfigurableEnvironment) {
                this.environment = (ConfigurableEnvironment) environment;
                bind.setBeanFactory(beanFactory);
                bind.setEnvironment(environment);
                bind.setConversionService((this.environment).getConversionService());
                bind.setPropertySources((this.environment).getPropertySources());
                // config to bean ConfigurationProperties
                // 提前加载
                bind.postProcessBeforeInitialization(properties, "rabbitMultipleProperties");

                for (Map.Entry<String, String> entry : properties.getMultiple().entrySet()) {
                    if (!StringUtils.isEmpty(entry.getValue()))
                        targetMap.put(entry.getKey(), properties.parseAddresses(entry.getValue()));
                }

                //namespace 模式
                rabbitNamespaceProperties();
            }
        }


        /**
         * 首先使用 namespace获取， namespaces 次之
         */
        private void rabbitNamespaceProperties() {
            String namespace = environment.getProperty(RabbitConfigUtils.RABBITS_NAMESPACE);
            if (StringUtils.isEmpty(namespace)) {
                namespace = environment.getProperty(RabbitConfigUtils.RABBITS_NAMESPACES);
            }
            String[] namespaces = StringUtils.commaDelimitedListToStringArray(namespace);
            for (String prefix : namespaces) {

                String key = prefix.substring(prefix.lastIndexOf(".") + 1);
                RabbitProperties properties = bind.postProcessBind(RabbitProperties.class, prefix);
                rabbitPropertiesMap.put(key, properties);
            }
        }

        /**
         * 注册 RabbitTemplate 放入 spring BeanDefinitionRegistry
         *
         * @param registry
         */
        private void registerRabbitTemplate(BeanDefinitionRegistry registry) {
            for (String key : targetMap.keySet()) {
                if (!registry.containsBeanDefinition(key)) {
                    try {
                        BeanDefinitionBuilder bean = BeanDefinitionBuilder.genericBeanDefinition(RabbitTemplate.class);
                        ConnectionFactory connectionFactory = getTargetConnectionFactory(key);
                        bean.addPropertyValue("connectionFactory", connectionFactory);
                        registry.registerBeanDefinition(key, bean.getBeanDefinition());
                        //alias name  set
                        if (!key.endsWith(RabbitConfigUtils.RABBIT_RT_BEAN_NAME_SUFFIX)) {
                            registry.registerAlias(key, key + RabbitConfigUtils.RABBIT_RT_BEAN_NAME_SUFFIX);
                        }


                    } catch (Exception e) {
                        logger.error(e.getMessage(), e);
                    }
                }
            }
        }

        /**
         * namespace模式 注册 RabbitTemplate 放入 spring BeanDefinitionRegistry
         *
         * @param registry
         */
        private void registerRabbitTemplateNamespace(BeanDefinitionRegistry registry) {
            for (Map.Entry<String, RabbitProperties> entry : rabbitPropertiesMap.entrySet()) {
                String key = entry.getKey();
                if (!registry.containsBeanDefinition(key)) {
                    try {
                        BeanDefinitionBuilder bean = BeanDefinitionBuilder.genericBeanDefinition(RabbitTemplate.class);
                        ConnectionFactory connectionFactory = getTargetConnectionFactoryNameSpace(key, entry.getValue());
                        bean.addPropertyValue("connectionFactory", connectionFactory);
                        registry.registerBeanDefinition(key, bean.getBeanDefinition());
                        //alias name  set
                        if (!key.endsWith(RabbitConfigUtils.RABBIT_RT_BEAN_NAME_SUFFIX)) {
                            registry.registerAlias(key, key + RabbitConfigUtils.RABBIT_RT_BEAN_NAME_SUFFIX);
                        }


                    } catch (Exception e) {
                        logger.error(e.getMessage(), e);
                    }
                }
            }
        }

        /**
         * 监听时使用， 可以指定目标监听
         * 注册 RabbitListenerContainerFactory 放入 spring BeanDefinitionRegistry
         *
         * @param registry
         */
        private void registerRabbitListenerContainerFactory(BeanDefinitionRegistry registry) {
            for (String key : targetMap.keySet()) {
                String beanName = key + RabbitConfigUtils.RABBIT_CF_BEAN_NAME_SUFFIX;
                if (!registry.containsBeanDefinition(beanName)) {
                    try {
                        BeanDefinitionBuilder bean = BeanDefinitionBuilder.genericBeanDefinition(RabbitListenerContainerFactoryX.class);
                        ConnectionFactory connectionFactory = getTargetConnectionFactory(key);
                        bean.addPropertyValue("connectionFactory", connectionFactory);
                        bean.addPropertyReference("configurer", RabbitConfigUtils.RABBIT_LCFC_BEAN_NAME);

                        registry.registerBeanDefinition(beanName, bean.getBeanDefinition());

                    } catch (Exception e) {
                        logger.error(e.getMessage(), e);
                    }
                }
            }
        }

        /**
         * namespace模式 监听时使用， 可以指定目标监听
         * 注册 RabbitListenerContainerFactory 放入 spring BeanDefinitionRegistry
         *
         * @param registry
         */
        private void registerRabbitListenerContainerFactoryNamespace(BeanDefinitionRegistry registry) {
            for (Map.Entry<String, RabbitProperties> entry : rabbitPropertiesMap.entrySet()) {
                String key = entry.getKey();
                String beanName = key + RabbitConfigUtils.RABBIT_CF_BEAN_NAME_SUFFIX;
                if (!registry.containsBeanDefinition(beanName)) {
                    try {
                        RabbitProperties properties = entry.getValue();
                        BeanDefinitionBuilder bean = BeanDefinitionBuilder.genericBeanDefinition(RabbitListenerContainerFactoryX.class);
                        ConnectionFactory connectionFactory = getTargetConnectionFactoryNameSpace(key, properties);
                        bean.addPropertyValue("connectionFactory", connectionFactory);
                        bean.addPropertyValue("configurer", RabbitConfiguration.rabbitListenerContainerFactoryConfigurer(properties));

                        registry.registerBeanDefinition(beanName, bean.getBeanDefinition());

                    } catch (Exception e) {
                        logger.error(e.getMessage(), e);
                    }
                }
            }
        }


        private ConnectionFactory getTargetConnectionFactory(String target) throws Exception {
            if (!cacheMap.containsKey(target)) {
                List<Address> addr = targetMap.get(target);
                ConnectionFactory connectionFactory = RabbitConnectionFactoryX.buildConnectionFactory(addr, properties);
                cacheMap.put(target, connectionFactory);
            }

            return cacheMap.get(target);
        }


        /**
         * 从缓存里面取 ConnectionFactory,不存在则创建
         *
         * @param target
         * @param properties
         * @return
         * @throws Exception
         */
        private ConnectionFactory getTargetConnectionFactoryNameSpace(String target, RabbitProperties properties) throws Exception {
            if (!cacheMap.containsKey(target)) {
                ConnectionFactory connectionFactory = RabbitConfiguration.rabbitConnectionFactory(properties);
                cacheMap.put(target, connectionFactory);
            }

            return cacheMap.get(target);
        }
    }


}
