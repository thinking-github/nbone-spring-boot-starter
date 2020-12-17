package org.nbone.spring.boot.actuate.metrics.redis;

import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tag;
import io.micrometer.core.instrument.Tags;
import io.micrometer.core.instrument.binder.BaseUnits;
import io.micrometer.core.instrument.binder.MeterBinder;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.util.ReflectionUtils;
import redis.clients.jedis.Jedis;
import redis.clients.util.Pool;

import java.lang.reflect.Field;

/**
 * @author thinking
 * @version 1.0
 * @see io.micrometer.core.instrument.binder.jvm.ExecutorServiceMetrics
 * @since 2019-09-28
 */
public class JedisPoolMetrics implements MeterBinder {

    private final Iterable<Tag> tags;
    private final String metricPrefix = "redis";
    private final static String NAME = "jedisPool";

    @Autowired(required = false)
    private RedisConnectionFactory redisConnectionFactory;

    private Pool<Jedis> pool;


    public JedisPoolMetrics(RedisConnectionFactory redisConnectionFactory, Iterable<Tag> tags) {
        this.redisConnectionFactory = redisConnectionFactory;
        this.tags = Tags.concat(tags, "name", NAME);
    }

    public JedisPoolMetrics(Iterable<Tag> tags) {
        this.tags = Tags.concat(tags, "name", NAME);
    }

    @Override
    public void bindTo(MeterRegistry registry) {
        getRedisPool();
        if (pool == null) {
            return;
        }
        Field internalPoolField = ReflectionUtils.findField(Pool.class, "internalPool");
        ReflectionUtils.makeAccessible(internalPoolField);
        GenericObjectPool<Jedis> internalPool = (GenericObjectPool<Jedis>) ReflectionUtils.getField(internalPoolField, this.pool);

        Gauge.builder("redis.pool.active", internalPool, GenericObjectPool::getNumActive)
                .tags(tags)
                .description("Active redis connection")
                .baseUnit(BaseUnits.CONNECTIONS)
                .register(registry);

        Gauge.builder("redis.pool.min", internalPool, GenericObjectPool::getMinIdle)
                .tags(tags)
                .description("min redis connection")
                .baseUnit(BaseUnits.CONNECTIONS)
                .register(registry);

        Gauge.builder("redis.pool.total", internalPool, GenericObjectPool::getMaxTotal)
                .tags(tags)
                .description("MaxTotal redis connection")
                .baseUnit(BaseUnits.CONNECTIONS)
                .register(registry);

        Gauge.builder("redis.pool.idle", internalPool, GenericObjectPool::getNumIdle)
                .tags(tags)
                .description("Idle redis connection")
                .baseUnit(BaseUnits.CONNECTIONS)
                .register(registry);

        Gauge.builder("redis.pool.waiters", internalPool, GenericObjectPool::getNumWaiters)
                .tags(tags)
                .description(
                        "The estimate of the number of threads currently blocked waiting for an object from the pool")
                .baseUnit(BaseUnits.TASKS)
                .register(registry);


        Gauge.builder("redis.pool.borrowed", internalPool, GenericObjectPool::getBorrowedCount)
                .tags(tags)
                .description("The total number of objects successfully borrowed from this pool")
                .register(registry);

        Gauge.builder("redis.pool.returned", internalPool, GenericObjectPool::getReturnedCount)
                .tags(tags)
                .description("The total number of objects returned to this pool over the lifetime of the pool.")
                .register(registry);

        Gauge.builder("redis.pool.created", internalPool, GenericObjectPool::getCreatedCount)
                .tags(tags)
                .description("The total number of objects created for this pool ")
                .register(registry);

        Gauge.builder("redis.pool.destroyed", internalPool, GenericObjectPool::getDestroyedCount)
                .tags(tags)
                .description("The total number of objects destroyed by this pool")
                .register(registry);

    }


    private Pool<Jedis> getRedisPool() {
        if (pool == null) {
            if (redisConnectionFactory == null) {
                return null;
            }
            if (redisConnectionFactory instanceof JedisConnectionFactory) {
                Field field = ReflectionUtils.findField(redisConnectionFactory.getClass(), "pool");
                ReflectionUtils.makeAccessible(field);
                pool = (Pool<Jedis>) ReflectionUtils.getField(field, redisConnectionFactory);
            }
        }
        return pool;
    }
}
