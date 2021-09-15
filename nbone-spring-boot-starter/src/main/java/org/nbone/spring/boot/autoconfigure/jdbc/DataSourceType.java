package org.nbone.spring.boot.autoconfigure.jdbc;

import java.util.HashSet;
import java.util.Set;

/**
 * @author thinking
 * @version 1.0
 * @since 2019/7/5
 */
public enum DataSourceType {
    HIKARI("spring.datasource.hikari", "com.zaxxer.hikari.HikariDataSource", "hikariDataSource"),
    TOMCAT("spring.datasource.tomcat", "org.apache.tomcat.jdbc.pool.DataSource", "tomcatDataSource"),
    DRUID("spring.datasource.druid", "com.alibaba.druid.pool.DruidDataSource", "druidDataSource"),
    DBCP2("spring.datasource.dbcp2", "org.apache.commons.dbcp2.BasicDataSource", "dbcp2DataSource"),
    DBCP("spring.datasource.dbcp", "org.apache.commons.dbcp.BasicDataSource", "dbcpDataSource");


    final static Set<DataSourceType> DATA_SOURCE_TYPES = new HashSet<DataSourceType>();

    static {
        for (DataSourceType value : values()) {
            DATA_SOURCE_TYPES.add(value);
        }
    }

    private String prefix;

    private String name;

    private String className;

    DataSourceType(String prefix, String className, String name) {
        this.prefix = prefix;
        this.className = className;
        this.name = name;
    }


    public String getPrefix() {
        return prefix;
    }

    public String getClassName() {
        return className;
    }

    public String getName() {
        return name;
    }
}
