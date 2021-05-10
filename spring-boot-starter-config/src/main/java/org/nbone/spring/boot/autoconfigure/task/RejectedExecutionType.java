package org.nbone.spring.boot.autoconfigure.task;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.util.ClassUtils;
import org.springframework.util.StringUtils;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * @author thinking
 * @version 1.0
 * @since 2019/7/5
 */
public enum RejectedExecutionType {

    UNKNOWN("UNKNOWN"),
    ABORT("AbortPolicy"),
    CALLER_RUNS("CallerRunsPolicy"),
    DISCARD("DiscardPolicy"),
    DISCARD_OLDEST("DiscardOldestPolicy");

    private static final Logger logger = LoggerFactory.getLogger(RejectedExecutionType.class);

    private String type;

    private final static Set<RejectedExecutionType> REJECTED_EXECUTIONS = new HashSet<RejectedExecutionType>();

    static {
        for (RejectedExecutionType value : values()) {
            REJECTED_EXECUTIONS.add(value);
        }
    }

    RejectedExecutionType(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }

    public static RejectedExecutionType of(String rejectionPolicy) {
        for (RejectedExecutionType rejectedExecution : REJECTED_EXECUTIONS) {
            if (rejectedExecution.name().equals(rejectionPolicy) || rejectedExecution.type.equals(rejectionPolicy)) {
                return rejectedExecution;
            }

        }
        return UNKNOWN;
    }

    // @see ExecutorBeanDefinitionParser
    public static void configureRejectionPolicy(ThreadPoolExecutor threadPoolExecutor, String rejectionPolicy) {
        // element.getAttribute("rejection-policy");
        if (!StringUtils.hasText(rejectionPolicy)) {
            return;
        }
        String prefix = "java.util.concurrent.ThreadPoolExecutor.";
        String policyClassName;

        RejectedExecutionType rejectedExecutionType = of(rejectionPolicy);

        switch (rejectedExecutionType) {
            case ABORT:
            case CALLER_RUNS:
            case DISCARD:
            case DISCARD_OLDEST:
                policyClassName = prefix + rejectedExecutionType.type;
                break;
            default:
                policyClassName = rejectionPolicy;
                break;
        }

        RejectedExecutionHandler handler = null;
        try {
           Class<?> policyType =  ClassUtils.forName(policyClassName, ThreadPoolExecutor.class.getClassLoader());
            handler = (RejectedExecutionHandler) BeanUtils.instantiate(policyType);
        } catch (ClassNotFoundException e) {
            logger.error(e.getMessage(), e);
        }
        if (handler == null) {
            return;
        }
        // builder.addPropertyValue("rejectedExecutionHandler", new RootBeanDefinition(policyClassName));
        threadPoolExecutor.setRejectedExecutionHandler(handler);
    }

}
