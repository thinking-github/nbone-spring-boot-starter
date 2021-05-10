package org.nbone.spring.boot.autoconfigure.task;

import lombok.Data;

import java.util.concurrent.TimeUnit;

/**
 * <pre>
 * spring.task.execution.pool.allow-core-thread-timeout=true
 * spring.task.execution.pool.core-size=8
 * spring.task.execution.pool.keep-alive=60s
 * spring.task.execution.pool.max-size=
 * spring.task.execution.pool.queue-capacity=
 * spring.task.execution.thread-name-prefix=task-
 * </pre>
 *
 * @author thinking
 * @version 1.0
 * @since 2019/7/5
 */
@Data
public class ThreadPoolProperties {

    /**
     * 线程池名称
     */
    private String threadPoolName = "ThreadPool";

    /**
     * 核心线程数
     */
    private int corePoolSize = 1;

    /**
     * 最大线程数, 默认值为CPU核心数量
     * Runtime.getRuntime().availableProcessors()
     */
    private int maximumPoolSize;

    /**
     * 队列最大数量
     */
    private int queueCapacity = 10000;

    /**
     * 队列类型
     *
     * @see QueueType
     */
    private String queueType = QueueType.LINKED_BLOCKING_QUEUE.getType();

    /**
     * SynchronousQueue 是否公平策略
     */
    private boolean fair;

    /**
     * 拒绝策略
     *
     * @see
     */
    private String rejectedExecutionType;

    /**
     * 空闲线程存活时间
     */
    private long keepAliveTime;

    /**
     * 空闲线程存活时间单位
     */
    private TimeUnit unit = TimeUnit.MILLISECONDS;

    /**
     * 队列容量阀值，超过此值告警
     */
    private int queueCapacityThreshold = queueCapacity;

}
