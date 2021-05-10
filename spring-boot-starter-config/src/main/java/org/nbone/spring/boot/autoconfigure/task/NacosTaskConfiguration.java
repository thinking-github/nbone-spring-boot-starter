package org.nbone.spring.boot.autoconfigure.task;

import com.alibaba.nacos.api.annotation.NacosInjected;
import com.alibaba.nacos.api.config.ConfigChangeEvent;
import com.alibaba.nacos.api.config.ConfigChangeItem;
import com.alibaba.nacos.api.config.ConfigService;
import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.client.config.listener.impl.AbstractConfigChangeListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import javax.annotation.PostConstruct;
import java.util.Collection;

import static com.alibaba.nacos.api.common.Constants.DEFAULT_GROUP;


/**
 * @author thinking
 * @since 2019/7/5
 */
public class NacosTaskConfiguration {
    private static final Logger logger = LoggerFactory.getLogger(NacosTaskConfiguration.class);
    //private static final String LOGGER_TAG = "spring.task.execution";

    private DynamicThreadPoolManager dynamicThreadPoolManager;

    private String dataId = "application";
    private String group;

    @NacosInjected
    private ConfigService configService;

    public NacosTaskConfiguration(DynamicThreadPoolManager dynamicThreadPoolManager) {
        this.dynamicThreadPoolManager = dynamicThreadPoolManager;
    }

    @PostConstruct
    public void init() throws NacosException {
        AbstractConfigChangeListener changeListener = new AbstractConfigChangeListener() {
            @Override
            public void receiveConfigChange(ConfigChangeEvent event) {
                refreshExecutorsConfig(event);
            }
        };

        if (StringUtils.isEmpty(group)) {
            group = DEFAULT_GROUP;
        }
        configService.addListener(dataId, group, changeListener);
    }

    private void refreshExecutorsConfig(ConfigChangeEvent changeEvent) {
        Collection<ConfigChangeItem> changeItems = changeEvent.getChangeItems();
        for (ConfigChangeItem changeItem : changeItems) {
            String changedKey = changeItem.getKey();
            if (changedKey.startsWith(DynamicThreadPoolManager.THREAD_POOLS_PREFIX)) {
                String newValue = changeItem.getNewValue();
                dynamicThreadPoolManager.refreshExecutorsConfig(changedKey, newValue);
                logger.info("{}:{}", changedKey, newValue);
            }

        }

    }


}
