package org.nbone.spring.boot.actuate.endpoint;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.http.MediaType;
import org.springframework.util.ReflectionUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.JedisShardInfo;
import redis.clients.util.Pool;

import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Field;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author thinking
 * @version 1.0
 * @see org.springframework.boot.actuate.endpoint.Endpoint
 * @since 2019-09-28
 */
@RestController
@RequestMapping(value = "${management.context-path:}", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
public class RedisEndpoint implements InitializingBean {

    private static final Logger logger = LoggerFactory.getLogger(RedisEndpoint.class);
    @Autowired(required = false)
    private RedisConnectionFactory redisConnectionFactory;

    private Pool<Jedis> pool;
    private JedisPoolConfig jedisPoolConfig;
    private JedisShardInfo jedisShardInfo;
    private Map<String, Object> hash;


    @RequestMapping(value = "redis", method = RequestMethod.GET)
    public Map<String, Object> getRedisPool(HttpServletRequest request) {
        if (hash == null) {
            hash = new LinkedHashMap<>();
        }
        if (pool == null) {
            if (redisConnectionFactory == null) {
                return hash;
            }
            if (redisConnectionFactory instanceof JedisConnectionFactory) {
                Field field = ReflectionUtils.findField(redisConnectionFactory.getClass(), "pool");
                ReflectionUtils.makeAccessible(field);
                pool = (Pool<Jedis>) ReflectionUtils.getField(field, redisConnectionFactory);

                jedisPoolConfig = ((JedisConnectionFactory) redisConnectionFactory).getPoolConfig();
                jedisShardInfo = ((JedisConnectionFactory) redisConnectionFactory).getShardInfo();

            }
        }

        hash.put("remoteHost", request.getRemoteHost() + ":" + request.getRemotePort());
        hash.put("remoteAddr", request.getRemoteAddr() + ":" + request.getRemotePort());
        hash.put("serverName", request.getServerName() + ":" + request.getServerPort());
        hash.put("localAddr", request.getLocalAddr() + ":" + request.getLocalPort());
        hash.put("localName", request.getLocalName() + ":" + request.getLocalPort());

        hash.put("numActive", pool.getNumActive());
        hash.put("numIdle", pool.getNumIdle());
        hash.put("numWaiters", pool.getNumWaiters());
        hash.put("poolConfig", jedisPoolConfig);
        hash.put("jedisShardInfo", jedisShardInfo);
        return hash;

    }

    @Override
    public void afterPropertiesSet() throws Exception {
        logger.info("actuate redisEndpoint initialize ...");
    }
}