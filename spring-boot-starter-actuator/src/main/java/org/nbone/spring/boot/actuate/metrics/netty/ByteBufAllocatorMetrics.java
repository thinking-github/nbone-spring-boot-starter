package org.nbone.spring.boot.actuate.metrics.netty;

import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.netty.buffer.ByteBufAllocatorMetric;
import io.netty.buffer.PooledByteBufAllocatorMetric;
import io.netty.util.internal.PlatformDependent;

import java.util.concurrent.ConcurrentMap;

import static org.nbone.spring.boot.actuate.metrics.netty.Metrics.*;

/**
 * @author thinking
 * @version 1.0
 * @since 2019-09-28
 */
public class ByteBufAllocatorMetrics {
    public static final ByteBufAllocatorMetrics INSTANCE = new ByteBufAllocatorMetrics();

    final ConcurrentMap<String, ByteBufAllocatorMetric> cache = PlatformDependent.newConcurrentHashMap();

    private ByteBufAllocatorMetrics() {
    }

    public void registerMetrics(String allocType, ByteBufAllocatorMetric metrics) {
        registerMetrics(allocType, metrics, io.micrometer.core.instrument.Metrics.globalRegistry);
    }

    // allocType : pooled/ unpooled
    public void registerMetrics(String allocType, ByteBufAllocatorMetric metrics, MeterRegistry meterRegistry) {
        cache.computeIfAbsent(metrics.hashCode() + "", key -> {
            String[] tags = new String[]{ID, key, TYPE, allocType};

            Gauge.builder(BYTE_BUF_ALLOCATOR_PREFIX + USED_HEAP_MEMORY, metrics, ByteBufAllocatorMetric::usedHeapMemory)
                    .description("The number of the bytes of the heap memory.")
                    .tags(tags)
                    .register(meterRegistry);

            Gauge.builder(BYTE_BUF_ALLOCATOR_PREFIX + USED_DIRECT_MEMORY, metrics, ByteBufAllocatorMetric::usedDirectMemory)
                    .description("The number of the bytes of the direct memory.")
                    .tags(tags)
                    .register(meterRegistry);

            if (metrics instanceof PooledByteBufAllocatorMetric) {
                PooledByteBufAllocatorMetric pooledMetrics = (PooledByteBufAllocatorMetric) metrics;

                Gauge.builder(BYTE_BUF_ALLOCATOR_PREFIX + HEAP_ARENAS, pooledMetrics, PooledByteBufAllocatorMetric::numHeapArenas)
                        .description("The number of heap arenas.")
                        .tags(tags)
                        .register(meterRegistry);

                Gauge.builder(BYTE_BUF_ALLOCATOR_PREFIX + DIRECT_ARENAS, pooledMetrics, PooledByteBufAllocatorMetric::numDirectArenas)
                        .description("The number of direct arenas.")
                        .tags(tags)
                        .register(meterRegistry);

                Gauge.builder(BYTE_BUF_ALLOCATOR_PREFIX + THREAD_LOCAL_CACHES, pooledMetrics, PooledByteBufAllocatorMetric::numThreadLocalCaches)
                        .description("The number of thread local caches.")
                        .tags(tags)
                        .register(meterRegistry);

                Gauge.builder(BYTE_BUF_ALLOCATOR_PREFIX + SMALL_CACHE_SIZE, pooledMetrics, PooledByteBufAllocatorMetric::smallCacheSize)
                        .description("The size of the small cache.")
                        .tags(tags)
                        .register(meterRegistry);

                Gauge.builder(BYTE_BUF_ALLOCATOR_PREFIX + NORMAL_CACHE_SIZE, pooledMetrics, PooledByteBufAllocatorMetric::normalCacheSize)
                        .description("The size of the normal cache.")
                        .tags(tags)
                        .register(meterRegistry);

                Gauge.builder(BYTE_BUF_ALLOCATOR_PREFIX + CHUNK_SIZE, pooledMetrics, PooledByteBufAllocatorMetric::chunkSize)
                        .description("The chunk size for an arena.")
                        .tags(tags)
                        .register(meterRegistry);
            }

            return metrics;
        });
    }
}
