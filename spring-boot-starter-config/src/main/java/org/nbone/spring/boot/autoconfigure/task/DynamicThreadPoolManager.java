package org.nbone.spring.boot.autoconfigure.task;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.util.StringUtils;

import java.util.Map;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author thinking
 * @version 1.0
 * @since 2019/7/5
 */
public class DynamicThreadPoolManager implements ApplicationContextAware {

    private static final Logger logger = LoggerFactory.getLogger(DynamicThreadPoolManager.class);

    public static final String THREAD_POOLS_PREFIX = "spring.task.executors";
    //public static final String EXECUTORS_PREFIX = "spring.executors";


    private String executorsPrefix = THREAD_POOLS_PREFIX;

    private Map<String, ThreadPoolExecutor> threadPoolMap;

    private ApplicationContext applicationContext;


    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    public void setExecutorsPrefix(String executorsPrefix) {
        this.executorsPrefix = executorsPrefix;
    }


    public Map<String, ThreadPoolExecutor> getThreadPoolExecutorMap() {
        if (threadPoolMap == null) {
            threadPoolMap = applicationContext.getBeansOfType(ThreadPoolExecutor.class);
        }
        return threadPoolMap;
    }

    public void setCoreSize(ThreadPoolExecutor threadPoolExecutor, int coreSize) {
        if (threadPoolExecutor == null) {
            return;
        }
        int maxSize = threadPoolExecutor.getMaximumPoolSize();
        int oldCore = threadPoolExecutor.getCorePoolSize();
        if (coreSize <= maxSize) {
            threadPoolExecutor.setCorePoolSize(coreSize);
            if (logger.isInfoEnabled()) {
                logger.info("threadPoolExecutor coreSize changed: {} -> {},setValue={}", oldCore, coreSize, threadPoolExecutor.getCorePoolSize());
            }
        } else {
            logger.error("configuration threadPoolExecutor coreSize, coreSize > maxSize, coreSize={},maxSize={}", coreSize, maxSize);
        }
    }

    public void setMaxSize(ThreadPoolExecutor threadPoolExecutor, int maxSize) {
        if (threadPoolExecutor == null) {
            return;
        }
        int oldMax = threadPoolExecutor.getMaximumPoolSize();
        threadPoolExecutor.setMaximumPoolSize(maxSize);
        if (logger.isInfoEnabled()) {
            logger.info("threadPoolExecutor maxSize changed: {} -> {},setValue={}", oldMax, maxSize, threadPoolExecutor.getMaximumPoolSize());
        }
    }

    public void setKeepAliveTime(ThreadPoolExecutor threadPoolExecutor, int keepAlive) {
        if (threadPoolExecutor == null) {
            return;
        }
        long oldKeepAlive = threadPoolExecutor.getKeepAliveTime(TimeUnit.SECONDS);
        threadPoolExecutor.setKeepAliveTime(keepAlive, TimeUnit.SECONDS);
        if (logger.isInfoEnabled()) {
            logger.info("threadPoolExecutor keepAlive changed: {}s -> {}s,setValue={}s", oldKeepAlive, keepAlive, threadPoolExecutor.getKeepAliveTime(TimeUnit.SECONDS));
        }
    }

    public void allowCoreThreadTimeOut(ThreadPoolExecutor threadPoolExecutor, boolean value) {
        if (threadPoolExecutor == null) {
            return;
        }
        boolean old = threadPoolExecutor.allowsCoreThreadTimeOut();
        threadPoolExecutor.allowCoreThreadTimeOut(value);
        if (logger.isInfoEnabled()) {
            logger.info("threadPoolExecutor allowCoreThreadTimeOut changed: {} -> {},setValue={}", old, value, threadPoolExecutor.allowsCoreThreadTimeOut());
        }
    }

    public void setRejectedExecutionHandler(ThreadPoolExecutor threadPoolExecutor, String rejectedExecutionHandler) {
        if (threadPoolExecutor == null) {
            return;
        }

        String old = threadPoolExecutor.getRejectedExecutionHandler().getClass().getSimpleName();
        RejectedExecutionType.configureRejectionPolicy(threadPoolExecutor, rejectedExecutionHandler);
        if (logger.isInfoEnabled()) {
            String newName = threadPoolExecutor.getRejectedExecutionHandler().getClass().getSimpleName();
            logger.info("threadPoolExecutor rejectionPolicy changed: {} -> {},setValue={}", old, rejectedExecutionHandler, newName);
        }

    }


    public Tuples getProperty(String key, String value) {
        int prefixLength = executorsPrefix.length() + 1;
        if (key.startsWith(executorsPrefix) && key.length() > prefixLength) {
            String name = key.substring(prefixLength);
            int index = name.indexOf(".");
            int lastIndex = name.lastIndexOf(".");
            if (index > 0 && lastIndex > 0) {
                String threadPoolName = name.substring(0, index);
                String property = name.substring(lastIndex + 1);
                Tuples tuples = new Tuples(threadPoolName, property, value);
                return tuples;
            }
        }
        return null;
    }

    public void refreshExecutorsConfig(String key, String value) {
        Tuples tuples = getProperty(key, value);
        ThreadPoolExecutor threadPoolExecutor = getThreadPoolExecutorMap().get(tuples.getName());

        String property = tuples.getProperty();
        if (StringUtils.isEmpty(property)) {
            return;
        }

        if (property.equals("coreSize") || property.equals("core-size")) {
            setCoreSize(threadPoolExecutor, Integer.valueOf(value));
        } else if (property.equals("maxSize") || property.equals("max-size")) {
            setMaxSize(threadPoolExecutor, Integer.valueOf(value));
        } else if (property.equals("keepAlive") || property.equals("keep-alive")) {
            setKeepAliveTime(threadPoolExecutor, Integer.valueOf(value));
        } else if (property.equals("allowCoreThreadTimeout") || property.equals("allow-core-thread-timeout")) {
            allowCoreThreadTimeOut(threadPoolExecutor, Boolean.valueOf(value));
        } else if (property.equals("rejectionPolicy") || property.equals("rejection-policy")) {
            setRejectedExecutionHandler(threadPoolExecutor, value);
        } else {
            logger.error("not find {} property {}={}", tuples.getName(), property, value);
        }
    }

}
