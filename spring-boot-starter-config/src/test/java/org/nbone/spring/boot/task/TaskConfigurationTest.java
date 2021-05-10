package org.nbone.spring.boot.task;

import org.junit.Test;
import org.nbone.spring.boot.autoconfigure.task.DynamicThreadPoolManager;

/**
 * @author thinking
 * @version 1.0
 * @since 1/22/21
 */
public class TaskConfigurationTest {


    public static void main(String[] args) {

        System.out.println("spring.task.executors.genthread.coreSize".substring(DynamicThreadPoolManager.THREAD_POOLS_PREFIX.length() + 1));
    }

    @Test
    public void dd() {

    }
}
