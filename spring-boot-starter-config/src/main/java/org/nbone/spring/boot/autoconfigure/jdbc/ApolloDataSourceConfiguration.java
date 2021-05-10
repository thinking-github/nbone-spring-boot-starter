package org.nbone.spring.boot.autoconfigure.jdbc;

import com.ctrip.framework.apollo.Config;
import com.ctrip.framework.apollo.ConfigChangeListener;
import com.ctrip.framework.apollo.ConfigService;
import com.ctrip.framework.apollo.core.ConfigConsts;
import com.ctrip.framework.apollo.model.ConfigChange;
import com.ctrip.framework.apollo.model.ConfigChangeEvent;
import com.google.common.collect.Sets;
import org.nbone.framework.spring.dao.config.JdbcComponentConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.config.ConfigUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.EnvironmentAware;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.Environment;
import org.springframework.util.ClassUtils;
import org.springframework.util.StringUtils;

import javax.annotation.PostConstruct;
import javax.sql.DataSource;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import static org.nbone.spring.boot.autoconfigure.jdbc.DataSourceType.DATA_SOURCE_TYPES;
import static org.nbone.spring.boot.autoconfigure.jdbc.ShardingSphereDatasource.SHARDING_SPHERE_DATASOURCE_BEAN_NAME;
import static org.nbone.spring.boot.autoconfigure.jdbc.ShardingSphereDatasource.SHARDING_SPHERE_PROPS_PROFIX;

/**
 * @author thinking
 * @version 1.0
 * @since 2019/7/5
 */
public class ApolloDataSourceConfiguration implements EnvironmentAware, ApplicationContextAware {

    private static final Logger logger = LoggerFactory.getLogger(ApolloDataSourceConfiguration.class);

    private Environment environment;

    private ApplicationContext applicationContext;

    private DynamicDataSourceManager dynamicDataSourceManager;

    @Value("${spring.shardingsphere.namespace:}")
    private String shardingNamespace;


    public ApolloDataSourceConfiguration(DynamicDataSourceManager dynamicDataSourceManager) {
        this.dynamicDataSourceManager = dynamicDataSourceManager;
    }

    @PostConstruct
    private void addConfigChangeListener() {
        Config config = ConfigService.getConfig(ConfigConsts.NAMESPACE_APPLICATION);
        for (DataSourceType type : DATA_SOURCE_TYPES) {
            String prefix = type.getPrefix() + ".";
            if (ConfigUtils.containsPrefix((ConfigurableEnvironment) environment, prefix)) {
                ConfigChangeListener configChangeListener = getConfigChangeListener(type);
                if (configChangeListener == null) {
                    throw new UnsupportedOperationException("not implemented " + type);
                }
                config.addChangeListener(configChangeListener, null, Sets.newHashSet(prefix));
            }

        }

        String nbonePrefix = "nbone.jdbc.props.";
        String className = "org.nbone.framework.spring.dao.config.JdbcComponentConfig";
        if (ClassUtils.isPresent(className, DataSource.class.getClassLoader())) {
            Map<String, JdbcComponentConfig> componentMap = applicationContext.getBeansOfType(JdbcComponentConfig.class);
            if (componentMap != null && componentMap.size() == 1) {
                Map.Entry<String, JdbcComponentConfig> entry = componentMap.entrySet().iterator().next();
                config.addChangeListener(new NboneJdbcConfigChangeListener(entry.getValue()), null, Sets.newHashSet(nbonePrefix));
            }
        }


        //org.apache.shardingsphere.shardingjdbc.spring.boot.SpringBootConfiguration#setEnvironment
        String prefix = "spring.shardingsphere.datasource.";
        // ShardingSphereConfigChangeListener
        if (ConfigUtils.containsPrefix((ConfigurableEnvironment) environment, prefix)) {
            ConfigChangeListener configChangeListener = new ShardingSphereConfigChangeListener();
            ConfigChangeListener propertiesChangeListener = new ShardingSpherePropertiesChangeListener();
            String beanName = environment.getProperty(SHARDING_SPHERE_DATASOURCE_BEAN_NAME);
            ShardingSphereDatasource shardingSphere = dynamicDataSourceManager.getShardingSphereDatasource();
            Map<String, DataSource> dataSourceMap = shardingSphere.getShardingDatasourceMap(beanName);
            Set<String> interestedKeyPrefixes = new HashSet<>();
            for (String dataSourceName : dataSourceMap.keySet()) {
                interestedKeyPrefixes.add(prefix + dataSourceName.trim());
            }
            // NAMESPACE_APPLICATION
            config.addChangeListener(configChangeListener, null, interestedKeyPrefixes);
            config.addChangeListener(propertiesChangeListener, null, Sets.newHashSet(SHARDING_SPHERE_PROPS_PROFIX));
            // custom namespace
            if (StringUtils.hasLength(shardingNamespace)) {
                Config shardingConfig = ConfigService.getConfig(shardingNamespace);
                shardingConfig.addChangeListener(configChangeListener, null, interestedKeyPrefixes);
                shardingConfig.addChangeListener(propertiesChangeListener, null, Sets.newHashSet(SHARDING_SPHERE_PROPS_PROFIX));
            }
        }

    }


    public ConfigChangeListener getConfigChangeListener(DataSourceType dataSourceType) {
        switch (dataSourceType) {
            case HIKARI:
                return new HikariConfigChangeListener();

            case DRUID:
                return new DruidConfigChangeListener();

            case DBCP2:
                return new Dbcp2ConfigChangeListener();

            case TOMCAT:
                return new TomcatConfigChangeListener();
            case DBCP:
                return new DbcpConfigChangeListener();
            default:
                break;
        }
        return null;

    }


    class HikariConfigChangeListener implements ConfigChangeListener {
        @Override
        public void onChange(ConfigChangeEvent changeEvent) {
            String beanName = environment.getProperty("spring.datasource.hikari.name");
            refreshDataSource(changeEvent, DataSourceType.HIKARI, beanName);
        }
    }

    class DruidConfigChangeListener implements ConfigChangeListener {
        @Override
        public void onChange(ConfigChangeEvent changeEvent) {
            String beanName = environment.getProperty("spring.datasource.druid.name");
            refreshDataSource(changeEvent, DataSourceType.DRUID, beanName);
        }
    }

    class Dbcp2ConfigChangeListener implements ConfigChangeListener {
        @Override
        public void onChange(ConfigChangeEvent changeEvent) {
            String beanName = environment.getProperty("spring.datasource.dbcp2.name");
            refreshDataSource(changeEvent, DataSourceType.DBCP2, beanName);
        }
    }

    class DbcpConfigChangeListener implements ConfigChangeListener {
        @Override
        public void onChange(ConfigChangeEvent changeEvent) {
            String beanName = environment.getProperty("spring.datasource.dbcp.name");
            refreshDataSource(changeEvent, DataSourceType.DBCP, beanName);
        }
    }

    class TomcatConfigChangeListener implements ConfigChangeListener {
        @Override
        public void onChange(ConfigChangeEvent changeEvent) {
            String beanName = environment.getProperty("spring.datasource.tomcat.name");
            refreshDataSource(changeEvent, DataSourceType.TOMCAT, beanName);
        }
    }


    class NboneJdbcConfigChangeListener implements ConfigChangeListener {
        JdbcComponentConfig jdbcComponentConfig;

        public NboneJdbcConfigChangeListener(JdbcComponentConfig jdbcComponentConfig) {
            this.jdbcComponentConfig = jdbcComponentConfig;
        }

        @Override
        public void onChange(ConfigChangeEvent changeEvent) {
            refreshBean(changeEvent,jdbcComponentConfig);
        }
    }

    class ShardingSphereConfigChangeListener implements ConfigChangeListener {
        String prefix = "spring.shardingsphere.datasource.";

        @Override
        public void onChange(ConfigChangeEvent changeEvent) {
            //spring.shardingsphere.datasource.names
            //shardingDataSource
            String beanName = environment.getProperty(SHARDING_SPHERE_DATASOURCE_BEAN_NAME);
            Iterator<String> changedKeys = changeEvent.changedKeys().iterator();
            if (changedKeys.hasNext()) {
                String first = changedKeys.next();
                String datasource = first.substring(prefix.length());
                String datasourceName = datasource.substring(0, datasource.indexOf('.'));
                String type = environment.getProperty(prefix + datasourceName + ".type");
                refreshShardingSphereDatasource(changeEvent, type, beanName, datasourceName);
            }
        }
    }

    class ShardingSpherePropertiesChangeListener implements ConfigChangeListener {
        @Override
        public void onChange(ConfigChangeEvent changeEvent) {
            String beanName = environment.getProperty(SHARDING_SPHERE_DATASOURCE_BEAN_NAME);
            for (String changedKey : changeEvent.changedKeys()) {
                ConfigChange configChange = changeEvent.getChange(changedKey);
                String newValue = configChange.getNewValue();
                ShardingSphereDatasource shardingSphere = dynamicDataSourceManager.getShardingSphereDatasource();
                shardingSphere.setShardingSphereProperties(beanName, changedKey, configChange.getOldValue(), newValue);
                logger.info("{}:{}", changedKey, newValue);
            }
        }
    }


    private void refreshDataSource(ConfigChangeEvent changeEvent, DataSourceType dataSourceType, String beanName) {

        DataSource dataSource = dynamicDataSourceManager.getDataSource(beanName);
        if (dataSource == null) {
            logger.error("{} bean not exist. beanName={}", dataSourceType.getPrefix(), beanName);
            return;
        }
        refreshBean(changeEvent, dataSource);
    }

    private void refreshShardingSphereDatasource(ConfigChangeEvent changeEvent, String type, String beanName, String dataSourceName) {
        ShardingSphereDatasource shardingSphere = dynamicDataSourceManager.getShardingSphereDatasource();
        DataSource dataSource = shardingSphere.getShardingSphereDatasource(beanName, dataSourceName);
        if (dataSource == null) {
            logger.error("bean not exist.type={},beanName={},dataSourceName={}", type, beanName, dataSourceName);
            return;
        }
        refreshBean(changeEvent, dataSource);
    }

    private void refreshBean(ConfigChangeEvent changeEvent, Object bean) {
        Set<String> changedKeys = changeEvent.changedKeys();
        for (String changedKey : changedKeys) {
            ConfigChange configChange = changeEvent.getChange(changedKey);
            String newValue = configChange.getNewValue();
            dynamicDataSourceManager.set(bean, changedKey, configChange.getOldValue(), newValue);
            logger.info("{}:{}", changedKey, newValue);
        }
    }


    @Override
    public void setEnvironment(Environment environment) {
        this.environment = environment;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
}
