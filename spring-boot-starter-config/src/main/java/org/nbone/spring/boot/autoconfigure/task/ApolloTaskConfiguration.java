package org.nbone.spring.boot.autoconfigure.task;

import com.ctrip.framework.apollo.core.ConfigConsts;
import com.ctrip.framework.apollo.model.ConfigChange;
import com.ctrip.framework.apollo.model.ConfigChangeEvent;
import com.ctrip.framework.apollo.spring.annotation.ApolloConfigChangeListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;


/**
 * @author thinking
 * @since 2019/7/5
 */
public class ApolloTaskConfiguration {
    private static final Logger logger = LoggerFactory.getLogger(ApolloTaskConfiguration.class);
    //private static final String LOGGER_TAG = "spring.task.execution";

    private DynamicThreadPoolManager dynamicThreadPoolManager;


    public ApolloTaskConfiguration(DynamicThreadPoolManager dynamicThreadPoolManager) {
        this.dynamicThreadPoolManager = dynamicThreadPoolManager;
    }

    @ApolloConfigChangeListener(value = {ConfigConsts.NAMESPACE_APPLICATION},
            interestedKeyPrefixes = DynamicThreadPoolManager.THREAD_POOLS_PREFIX)
    private void onChange(ConfigChangeEvent changeEvent) {
        refreshExecutorsConfig(changeEvent);
    }


    private void refreshExecutorsConfig(ConfigChangeEvent changeEvent) {
        Set<String> changedKeys = changeEvent.changedKeys();
        for (String changedKey : changedKeys) {
            ConfigChange configChange = changeEvent.getChange(changedKey);
            String newValue = configChange.getNewValue();
            dynamicThreadPoolManager.refreshExecutorsConfig(changedKey, newValue);
            logger.info("{}:{}", changedKey, newValue);
        }

    }


}
