package org.nbone.spring.boot.actuate.metrics.kafka;

import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerInterceptor;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.common.TopicPartition;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author thinking
 * @version 1.0
 * @since 2019-09-28
 */
public class ConsumerMetricsInterceptor implements ConsumerInterceptor<String, Object> {

    public final static String METER_REGISTRY_BEAN = "io.meter.registry.bean";

    private MeterRegistry meterRegistry;

    private String group;

    private String clientId;


    @Override
    public ConsumerRecords onConsume(ConsumerRecords consumerRecords) {
        if (meterRegistry == null || consumerRecords == null || consumerRecords.isEmpty()) {
            return consumerRecords;
        }

        Set<TopicPartition> topicPartitions = consumerRecords.partitions();
        for (TopicPartition topicPartition : topicPartitions) {
            List<ConsumerRecord<String, ?>> records = consumerRecords.records(topicPartition);
            if (!CollectionUtils.isEmpty(records)) {
                ConsumerRecord consumerRecord = records.get(0);
                long timestamp = consumerRecord.timestamp();
                String timestampType = consumerRecord.timestampType().name;
                String topic = consumerRecord.topic();
                int partition = consumerRecord.partition();
                long end = System.currentTimeMillis();

                Gauge.builder("kafka.consumer.records.latency", () -> (end - timestamp))
                        .description("The latest latency of the partition")
                        .tag("group", group)
                        .tag("topic", topic)
                        .tag("partition", partition + "")
                        .tag("client.id", clientId)
                        .register(meterRegistry);
                //TODO:
                //meterRegistry.gauge("kafka.consumer.");
            }
        }
        return consumerRecords;
    }

    @Override
    public void close() {

    }

    @Override
    public void onCommit(Map map) {

    }

    @Override
    public void configure(Map<String, ?> configs) {
        this.meterRegistry = (MeterRegistry) configs.get(METER_REGISTRY_BEAN);
        this.group = (String) configs.get(ConsumerConfig.GROUP_ID_CONFIG);
        this.clientId = (String) configs.get(ConsumerConfig.CLIENT_ID_CONFIG);
    }
}
