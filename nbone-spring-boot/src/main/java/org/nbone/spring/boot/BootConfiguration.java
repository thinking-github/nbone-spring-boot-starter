package org.nbone.spring.boot;

/**
 * @author thinking
 * @version 1.0
 * @since 2019-07-31
 */

public class BootConfiguration {

    /**
     * 多rabbit服务支持
     */
    private String[] rabbitNamespace;
    /**
     * 多数据源支持
     */
    private String[] dataSourceNamespace;



    public String[] getRabbitNamespace() {
        return rabbitNamespace;
    }

    public BootConfiguration setRabbitNamespace(String ...rabbitNamespace) {
        this.rabbitNamespace = rabbitNamespace;
        return this;
    }

    public String[] getDataSourceNamespace() {
        return dataSourceNamespace;
    }

    public BootConfiguration setDataSourceNamespace(String... dataSourceNamespace) {
        this.dataSourceNamespace = dataSourceNamespace;
        return this;
    }
}
