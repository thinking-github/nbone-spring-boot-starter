package org.nbone.spring.boot.autoconfigure.log;

import com.ctrip.framework.apollo.Config;
import com.ctrip.framework.apollo.core.ConfigConsts;
import com.ctrip.framework.apollo.model.ConfigChange;
import com.ctrip.framework.apollo.model.ConfigChangeEvent;
import com.ctrip.framework.apollo.spring.annotation.ApolloConfig;
import com.ctrip.framework.apollo.spring.annotation.ApolloConfigChangeListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.logging.LogLevel;
import org.springframework.boot.logging.LoggingSystem;

import javax.annotation.PostConstruct;
import java.util.Set;


/**
 * @author thinking
 * @since 2019/7/5
 */
public class ApolloLoggerConfiguration {
    private static final Logger logger = LoggerFactory.getLogger(ApolloLoggerConfiguration.class);
    private static final String LOGGER_TAG = "logging.level.";

    @Autowired
    private LoggingSystem loggingSystem;

    @ApolloConfig(value = "log")
    private Config config;

    @ApolloConfigChangeListener(value = {"log", ConfigConsts.NAMESPACE_APPLICATION}, interestedKeyPrefixes = LOGGER_TAG)
    private void onChange(ConfigChangeEvent changeEvent) {
        refreshLoggingLevels(changeEvent);
    }

    @PostConstruct
    private void refreshLoggingLevels() {
        Set<String> keyNames = config.getPropertyNames();
        for (String key : keyNames) {
            if (containsIgnoreCase(key, LOGGER_TAG)) {
                String strLevel = config.getProperty(key, "info");
                LogLevel level = LogLevel.valueOf(strLevel.toUpperCase());
                loggingSystem.setLogLevel(key.replace(LOGGER_TAG, ""), level);
                logger.info("{}:{}", key, strLevel);
            }
        }
    }

    private void refreshLoggingLevels(ConfigChangeEvent changeEvent) {
        Set<String> changedKeys = changeEvent.changedKeys();
        for (String changedKey : changedKeys) {
            ConfigChange configChange = changeEvent.getChange(changedKey);
            String newLevel = configChange.getNewValue();
            LogLevel level = LogLevel.valueOf(newLevel.toUpperCase());
            loggingSystem.setLogLevel(changedKey.replace(LOGGER_TAG, ""), level);
            logger.info("{}:{}", changedKey, newLevel);
        }

    }

    private static boolean containsIgnoreCase(String str, String searchStr) {
        if (str == null || searchStr == null) {
            return false;
        }
        int len = searchStr.length();
        int max = str.length() - len;
        for (int i = 0; i <= max; i++) {
            if (str.regionMatches(true, i, searchStr, 0, len)) {
                return true;
            }
        }
        return false;
    }
}
