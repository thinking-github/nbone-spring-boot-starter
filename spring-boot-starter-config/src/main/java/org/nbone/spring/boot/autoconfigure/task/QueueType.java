package org.nbone.spring.boot.autoconfigure.task;

import java.util.HashSet;
import java.util.Set;

/**
 * @author thinking
 * @version 1.0
 * @since 2019/7/5
 */
public enum QueueType {

    LINKED_BLOCKING_QUEUE("LinkedBlockingQueue"),
    SYNCHRONOUS_QUEUE("SynchronousQueue"),
    ARRAY_BLOCKING_QUEUE("ArrayBlockingQueue"),
    DELAY_QUEUE("DelayQueue"),
    LINKED_TRANSFER_DEQUE("LinkedTransferQueue"),
    LINKED_BLOCKING_DEQUE("LinkedBlockingDeque"),
    PRIORITY_BLOCKING_QUEUE("PriorityBlockingQueue");

    private final static Set<QueueType> QUEUE_TYPES = new HashSet<QueueType>();

    static {
        for (QueueType value : values()) {
            QUEUE_TYPES.add(value);
        }
    }

    QueueType(String type) {
        this.type = type;
    }


    private String type;

    public String getType() {
        return type;
    }

    public static QueueType of(String queue) {
        for (QueueType queueType : QUEUE_TYPES) {
            if (queueType.type.equals(queue) || queueType.name().equals(queue)) {
                return queueType;
            }

        }
        return null;
    }


    public static boolean exists(String type) {
        for (QueueType queueType : QUEUE_TYPES) {
            if (queueType.getType().equals(type)) {
                return true;
            }
        }
        return false;
    }
}
