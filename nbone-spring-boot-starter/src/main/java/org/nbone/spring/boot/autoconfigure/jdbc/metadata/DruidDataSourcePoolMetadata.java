package org.nbone.spring.boot.autoconfigure.jdbc.metadata;

import com.alibaba.druid.pool.DruidAbstractDataSource;
import org.springframework.boot.autoconfigure.jdbc.metadata.AbstractDataSourcePoolMetadata;

/**
 * @author thinking
 * @version 1.0
 * @since 2019-08-01
 */
public class DruidDataSourcePoolMetadata extends AbstractDataSourcePoolMetadata<DruidAbstractDataSource> {
    /**
     * Create an instance with the data source to use.
     *
     * @param dataSource the data source
     */
    protected DruidDataSourcePoolMetadata(DruidAbstractDataSource dataSource) {
        super(dataSource);
    }

    @Override
    public Integer getActive() {
        return getDataSource().getActiveCount();
    }

    @Override
    public Integer getMax() {
        return getDataSource().getMaxActive();
    }

    @Override
    public Integer getMin() {
        return getDataSource().getMinIdle();
    }

    @Override
    public String getValidationQuery() {
        return getDataSource().getValidationQuery();
    }
}
