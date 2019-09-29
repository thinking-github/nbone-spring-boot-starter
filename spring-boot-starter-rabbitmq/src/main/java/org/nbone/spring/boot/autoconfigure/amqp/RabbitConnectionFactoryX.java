package org.nbone.spring.boot.autoconfigure.amqp;

import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.connection.RabbitConnectionFactoryBean;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.listener.RabbitListenerContainerFactory;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.boot.autoconfigure.amqp.SimpleRabbitListenerContainerFactoryConfigurer;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.nbone.spring.boot.autoconfigure.amqp.RabbitMultipleProperties.Address;


/**
 * @author thinking
 * @version 1.0
 * @since 2019-07-26
 * 继承BeanFactoryPostProcessor  存在@ConfigurationProperties 不起作用的问题
 */
@Data
public class RabbitConnectionFactoryX implements BeanFactoryAware {

    private static final Logger logger = LoggerFactory.getLogger(RabbitConnectionFactoryX.class);
    private ConfigurableBeanFactory beanFactory;
    /**
     * 由spring boot rabbit  RabbitAutoConfiguration  注入
     */
    @Resource(name = RabbitConfigUtils.RABBIT_T_BEAN_NAME)
    private RabbitTemplate defaultTemplate;

    @Resource(name = RabbitConfigUtils.RABBIT_ADMIN_BEAN_NAME)
    private AmqpAdmin defaultAdmin;

    @Resource(name = RabbitConfigUtils.RABBIT_LCF_BEAN_NAME)
    private RabbitListenerContainerFactory defaultContainerFactory;

    @Resource(name = RabbitConfigUtils.RABBIT_LCFC_BEAN_NAME)
    private SimpleRabbitListenerContainerFactoryConfigurer containerFactoryConfigurer;

    private RabbitMultipleProperties properties;

    private Map<String, ConnectionFactory> cacheMap = new HashMap<>();

    private Map<String, List<Address>> targetMap = new HashMap<>();


    public RabbitConnectionFactoryX(RabbitMultipleProperties properties) {
        this.properties = properties;
    }

    public RabbitConnectionFactoryX(RabbitMultipleProperties properties, Map<String,
            List<Address>> targetMap, Map<String, ConnectionFactory> cacheMap) {
        this.properties = properties;
        this.targetMap = targetMap;
        this.cacheMap = cacheMap;

    }


    public RabbitConnectionFactoryX addTargetMap(String key, List<Address> addresses) {
        targetMap.put(key, addresses);
        return this;
    }

    public void addTargetMap(Map<String, List<Address>> targetMap) {
        targetMap.putAll(targetMap);
    }

    /**
     * 初始化 RabbitTemplate 放入 spring beanFactory
     */
    protected void initRabbitTemplate() {
        for (String key : targetMap.keySet()) {
            try {
                RabbitTemplate rabbitTemplate = buildRabbitTemplate(key);
                beanFactory.registerSingleton(key, rabbitTemplate);
                if (!key.endsWith(RabbitConfigUtils.RABBIT_RT_BEAN_NAME_SUFFIX)) {
                    beanFactory.registerAlias(key, key + RabbitConfigUtils.RABBIT_RT_BEAN_NAME_SUFFIX);
                }

            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }

        }
    }

    /**
     * 初始化 RabbitListenerContainerFactory 放入 spring beanFactory
     *
     * @param properties
     */
    protected void initRabbitListenerContainerFactory(RabbitMultipleProperties properties) {
        for (String key : targetMap.keySet()) {
            try {
                RabbitListenerContainerFactory containerFactory = buildRabbitListenerContainerFactory(key);
                beanFactory.registerSingleton(key + RabbitConfigUtils.RABBIT_CF_BEAN_NAME_SUFFIX, containerFactory);

            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }

        }
    }

    /**
     * 根据名称获取 RabbitTemplate
     *
     * @param target 目标key 可为空，为空时返回springboot Rabbit
     * @return
     * @throws Exception
     */
    public RabbitTemplate getRabbitTemplate(String target) throws Exception {
        if (StringUtils.isEmpty(target))
            return defaultTemplate;

        List<Address> addr = targetMap.get(target);
        if (addr == null || addr.isEmpty())
            return defaultTemplate;
        return buildRabbitTemplate(target);
    }

    public AmqpAdmin getRabbitAdmin(String target) throws Exception {
        if (StringUtils.isEmpty(target))
            return defaultAdmin;

        List<Address> addr = targetMap.get(target);
        if (addr == null || addr.isEmpty())
            return defaultAdmin;

        return buildRabbitAdmin(target);
    }

    public RabbitListenerContainerFactory getRabbitListenerContainerFactory(String target) throws Exception {
        if (StringUtils.isEmpty(target))
            return defaultContainerFactory;

        List<Address> addr = targetMap.get(target);
        if (addr == null || addr.isEmpty())
            return defaultContainerFactory;

        return buildRabbitListenerContainerFactory(target);
    }

    private RabbitListenerContainerFactory buildRabbitListenerContainerFactory(String target) throws Exception {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        containerFactoryConfigurer.configure(factory, getTargetConnectionFactory(target));
        return factory;
    }

    private RabbitTemplate buildRabbitTemplate(String target) throws Exception {
        return new RabbitTemplate(getTargetConnectionFactory(target));
    }

    private AmqpAdmin buildRabbitAdmin(String target) throws Exception {
        return new RabbitAdmin(getTargetConnectionFactory(target));
    }


    private ConnectionFactory getTargetConnectionFactory(String target) throws Exception {
        if (!cacheMap.containsKey(target)) {
            List<Address> addr = targetMap.get(target);
            cacheMap.put(target, buildConnectionFactory(addr));
        }

        return cacheMap.get(target);
    }


    private ConnectionFactory buildConnectionFactory(List<Address> addr) throws Exception {

        return buildConnectionFactory(addr, properties);
    }

    protected static ConnectionFactory buildConnectionFactory(List<Address> addr, RabbitMultipleProperties properties) throws Exception {
        RabbitConnectionFactoryBean factory = new RabbitConnectionFactoryBean();
        factory.setHost(addr.get(0).getHost());
        factory.setPort(addr.get(0).getPort());
        factory.setUsername(addr.get(0).getUsername());
        factory.setPassword(addr.get(0).getPassword());
        factory.setVirtualHost(addr.get(0).getVirtualHost());
        factory.afterPropertiesSet();

        CachingConnectionFactory connectionFactory = new CachingConnectionFactory(factory.getObject());
        connectionFactory.setAddresses(properties.determineAddresses(addr));
        return connectionFactory;
    }


    @Override
    public void setBeanFactory(BeanFactory beanFactory) {
        if (beanFactory instanceof ConfigurableBeanFactory) {
            this.beanFactory = (ConfigurableBeanFactory) beanFactory;
        }
    }

    /*@Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
        this.beanFactory = (ConfigurableBeanFactory) beanFactory;
        //init RabbitTemplate
        initRabbitTemplate(properties);

        //init RabbitListenerContainerFactory
        initRabbitListenerContainerFactory(properties);
    }*/
}
