package org.nbone.spring.boot.autoconfigure.amqp;

import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.autoconfigure.amqp.SimpleRabbitListenerContainerFactoryConfigurer;

/**
 * @author thinking
 * @version 1.0
 * @since 2019-07-31
 */
public class RabbitListenerContainerFactoryX extends SimpleRabbitListenerContainerFactory implements InitializingBean {

    private SimpleRabbitListenerContainerFactoryConfigurer configurer;
    private ConnectionFactory connectionFactory;


    public void setConfigurer(SimpleRabbitListenerContainerFactoryConfigurer configurer) {
        this.configurer = configurer;
    }

    @Override
    public void setConnectionFactory(ConnectionFactory connectionFactory) {
        this.connectionFactory = connectionFactory;
    }


    @Override
    public void afterPropertiesSet() throws Exception {
        configurer.configure(this, connectionFactory);
    }


}
