package org.nbone.spring.boot.autoconfigure.amqp;

/**
 * @author thinking
 * @version 1.0
 * @since 2019-07-30
 */
public abstract class RabbitConfigUtils {

    /**
     * RabbitTemplate bean id 后缀
     */
    public static final String RABBIT_RT_BEAN_NAME_SUFFIX = "RabbitTemplate";
    /**
     * ContainerFactory bean id 后缀
     */
    public static final String RABBIT_CF_BEAN_NAME_SUFFIX = "ContainerFactory";

    /**
     * spring boot def name  rabbitListenerContainerFactoryConfigurer
     */
    public static final String RABBIT_LCFC_BEAN_NAME = "rabbitListenerContainerFactoryConfigurer";

    /**
     * spring boot def name  rabbitListenerContainerFactoryConfigurer
     */
    public static final String RABBIT_LCF_BEAN_NAME = "rabbitListenerContainerFactory";

    /**
     * spring boot def name  rabbitTemplate
     */
    public static final String RABBIT_T_BEAN_NAME = "rabbitTemplate";

    /**
     * spring boot def name  amqpAdmin
     */
    public static final String RABBIT_ADMIN_BEAN_NAME = "amqpAdmin";

    /**
     * 支持多个rabbits 多个以逗号隔开（namespace模式）
     */
    public static final String RABBITS_NAMESPACE = "spring.rabbitmq.namespace";
    /**
     * 支持多个rabbits 多个以逗号隔开（namespace模式）
     */
    public static final String RABBITS_NAMESPACES = "spring.rabbitmq.namespaces";

    /**
     * 支持多个rabbits multiple （multiple模式）
     */
    public static final String RABBITS_MULTIPLE_PREFIX = "spring.rabbitmq.multiple";
}
