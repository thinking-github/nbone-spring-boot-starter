package org.nbone.spring.boot.actuate.autoconfigure.metrics.jdbc;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tag;
import io.micrometer.spring.jdbc.DataSourcePoolMetrics;
import org.apache.shardingsphere.shardingjdbc.jdbc.adapter.AbstractDataSourceAdapter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.metadata.DataSourcePoolMetadataProvider;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

import javax.sql.DataSource;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;

/**
 * @author thinking
 * @version 1.0
 * @since 2019-09-28
 * <p>
 * see DataSourcePoolMetricsAutoConfiguration
 */
@Configuration
@AutoConfigureAfter({DataSourceAutoConfiguration.class})
@ConditionalOnClass({DataSource.class, MeterRegistry.class,AbstractDataSourceAdapter.class})
@ConditionalOnBean({DataSource.class, MeterRegistry.class})
public class ShardingDataSourceMetricsAutoConfiguration {

    private static int springBootVersion = 1;

    static {
        try {
            Class.forName("org.springframework.boot.bind.RelaxedPropertyResolver");
        } catch (ClassNotFoundException ignored) {
            springBootVersion = 2;
        }
    }


    @Configuration
    @ConditionalOnBean(DataSourcePoolMetadataProvider.class)
    static class DataSourcePoolMetadataMetricsConfiguration {

        private static final String DATASOURCE_SUFFIX = "dataSource";

        private final MeterRegistry registry;

        private final Collection<DataSourcePoolMetadataProvider> metadataProviders;

        DataSourcePoolMetadataMetricsConfiguration(MeterRegistry registry,
                                                   Collection<DataSourcePoolMetadataProvider> metadataProviders) {
            this.registry = registry;
            this.metadataProviders = metadataProviders;
        }

        @Autowired
        public void bindDataSourcesToRegistry(Map<String, AbstractDataSourceAdapter> dataSources) {
            dataSources.forEach(this::bindDataSourceToRegistry);
        }

        private void bindDataSourceToRegistry(String beanName, AbstractDataSourceAdapter dataSource) {
            String dataSourceName = getDataSourceName(beanName);
            switch (springBootVersion) {
                case 1:
                    v1(dataSourceName, dataSource);
                    break;

                case 2:

                    break;
                default:
                    break;
            }
        }

        private void v1(String dataSourceName, AbstractDataSourceAdapter dataSource) {
            for (Map.Entry<String, DataSource> source : dataSource.getDataSourceMap().entrySet()) {
                Tag tag = Tag.of("sub_name", source.getKey());
                DataSource actualDataSource  = source.getValue();
                new DataSourcePoolMetrics(actualDataSource, this.metadataProviders, dataSourceName, Arrays.asList(tag))
                        .bindTo(this.registry);
            }
        }

        /**
         * Get the name of a DataSource based on its {@code beanName}.
         *
         * @param beanName the name of the data source bean
         * @return a name for the given data source
         */
        private String getDataSourceName(String beanName) {
            if (beanName.length() > DATASOURCE_SUFFIX.length()
                    && StringUtils.endsWithIgnoreCase(beanName, DATASOURCE_SUFFIX)) {
                return beanName.substring(0, beanName.length() - DATASOURCE_SUFFIX.length());
            }
            return beanName;
        }

    }

}
