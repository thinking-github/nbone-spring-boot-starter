package org.springframework.boot.context.config;

import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.env.PropertySource;

/**
 * @author thinking
 * @version 1.0
 * @since 2019-08-08
 */
public class ConfigUtils {

    public static final String SOURCE_NAME = ConfigFileApplicationListener.APPLICATION_CONFIGURATION_PROPERTY_SOURCE_NAME;

    /**
     * 判断  application Configuration Properties 中是否含有某个前缀
     *
     * @param environment
     * @param prefix
     * @return
     */
    public static boolean containsPrefix(ConfigurableEnvironment environment, String prefix) {
        MutablePropertySources mutablePropertySources = environment.getPropertySources();

        PropertySource<?> source = mutablePropertySources.get(SOURCE_NAME);
        if (source instanceof ConfigFileApplicationListener.ConfigurationPropertySources) {

            String[] names = ((ConfigFileApplicationListener.ConfigurationPropertySources) source).getPropertyNames();
            return containsPrefix(names, prefix);
        }

        return false;
    }


    private static boolean containsPrefix(String[] names, String prefix) {
        if (names == null) {
            return false;
        }
        for (String name : names) {
            if (name.startsWith(prefix)) {
                return true;
            }
        }
        return false;
    }
}
