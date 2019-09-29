package org.springframework.boot.autoconfigure.amqp;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;

/**
 * spring boot rabbit X
 *
 * @author thinking
 * @version 1.0
 * @see RabbitAutoConfiguration
 * @since 2019-07-31
 */
public class RabbitConfiguration extends RabbitAutoConfiguration {

    private static final Logger logger = LoggerFactory.getLogger(RabbitConfiguration.class);
    private static RabbitConnectionFactoryCreator creator;

    /**
     * create rabbitConnectionFactory
     *
     * @param config
     * @return
     * @see RabbitConnectionFactoryCreator#rabbitConnectionFactory(RabbitProperties)
     */
    public static CachingConnectionFactory rabbitConnectionFactory(RabbitProperties config) {
        if (creator == null) {
            creator = new RabbitConnectionFactoryCreator();
        }
        CachingConnectionFactory connectionFactory = null;
        try {
            connectionFactory = creator.rabbitConnectionFactory(config);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        return connectionFactory;
    }


    /**
     * create SimpleRabbitListenerContainerFactoryConfigurer
     *
     * @param config
     * @return
     * @see RabbitAnnotationDrivenConfiguration#rabbitListenerContainerFactoryConfigurer()
     */
    public static SimpleRabbitListenerContainerFactoryConfigurer rabbitListenerContainerFactoryConfigurer(RabbitProperties config) {
        SimpleRabbitListenerContainerFactoryConfigurer configurer = new SimpleRabbitListenerContainerFactoryConfigurer();
        configurer.setRabbitProperties(config);

        return configurer;
    }


}
