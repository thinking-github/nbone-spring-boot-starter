package org.nbone.spring.boot.autoconfigure.validation;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.HashMap;
import java.util.Map;

/**
 *
 * hibernate.validator.messages-location = classpath:/config/validation-message.properties
    hibernate.validator.fail_fast = true
 *
 * @author chenyicheng
 * @version 1.0
 * @since 2018/4/10
 *
 * @see org.hibernate.validator.HibernateValidatorConfiguration
 *
 */
@ConfigurationProperties(prefix = HibernateValidatorProperties.SPRING_HV_PREFIX)
public class HibernateValidatorProperties {



    public static final String SPRING_HV_PREFIX = "spring.hv";

    public static final String HIBERNATE_VALIDATOR_PREFIX = "hibernate.validator";

    /**
     * 自定义提示消息文件位置
     */

    @Value("${spring.hv.hibernate.validator.messages-location:}")
    private String messagesLocation ;


    /**
     * Externalized properties for hibernate-validator configuration.
     */

    private Map<String, String> properties = new HashMap<String,String>();


    public String getMessagesLocation() {
        return messagesLocation;
    }

    public void setMessagesLocation(String messagesLocation) {
        this.messagesLocation = messagesLocation;
    }



    public Map<String, String> getProperties() {
        return properties;
    }

    public void setProperties(Map<String, String> properties) {
        this.properties = properties;
    }





    /**
     * hibernate.validator.fail_fast = true
     * @return
     */
    public Map<String, String> getHibernateValidatorProperties() {
        if(properties == null || properties.size() ==0){
            return properties;
        }


        Map<String,String> hvproperties =  new HashMap<String,String>();
        for (Map.Entry<String, String> stringEntry : properties.entrySet()) {
            if(!stringEntry.getKey().startsWith(HIBERNATE_VALIDATOR_PREFIX)){
                hvproperties.put(HIBERNATE_VALIDATOR_PREFIX + "." + stringEntry.getKey(),stringEntry.getValue());
            }

        }
        return  hvproperties;

    }

}
