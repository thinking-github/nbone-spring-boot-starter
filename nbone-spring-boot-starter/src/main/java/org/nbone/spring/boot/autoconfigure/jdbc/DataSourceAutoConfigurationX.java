package org.nbone.spring.boot.autoconfigure.jdbc;

import org.nbone.spring.boot.autoconfigure.jdbc.metadata.DataSourcePoolMetadataProvidersConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;

import javax.sql.DataSource;

/**
 * 支持多个数据源 DataSource 配置  <br/>
 *
 * @author thinking
 * @version 1.0
 * @see org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration
 * @since 2019-08-01
 */
@Configuration
@ConditionalOnClass({DataSource.class, EmbeddedDatabaseType.class})
@EnableConfigurationProperties(DataSourceProperties.class)
@Import({DataSourcePoolMetadataProvidersConfiguration.class})
public class DataSourceAutoConfigurationX {


}
