package org.nbone.spring.boot.autoconfigure.jdbc;

import java.util.HashSet;
import java.util.Set;

/**
 * @author thinking
 * @version 1.0
 * @since 2019/7/5
 */
public enum DataSourceType {
    HIKARI("spring.datasource.hikari", "com.zaxxer.hikari.HikariDataSource"),
    TOMCAT("spring.datasource.tomcat", "org.apache.tomcat.jdbc.pool.DataSource"),
    DRUID("spring.datasource.druid", "com.alibaba.druid.pool.DruidDataSource"),
    DBCP2("spring.datasource.dbcp2", "org.apache.commons.dbcp2.BasicDataSource"),
    DBCP("spring.datasource.dbcp", "org.apache.commons.dbcp.BasicDataSource");


    final static Set<DataSourceType> DATA_SOURCE_TYPES = new HashSet<DataSourceType>();

    static {
        for (DataSourceType value : values()) {
            DATA_SOURCE_TYPES.add(value);
        }
    }

    private String prefix;

    private String className;

    DataSourceType(String prefix, String className) {
        this.prefix = prefix;
        this.className = className;
    }


    public String getPrefix() {
        return prefix;
    }

    public String getClassName() {
        return className;
    }
}
