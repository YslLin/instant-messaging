package com.ysl.im.redis;

import com.ysl.im.Constants;
import io.netty.handler.codec.redis.RedisMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.cache.RedisCacheWriter;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.listener.adapter.MessageListenerAdapter;
import org.springframework.data.redis.serializer.GenericToStringSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;

@Configuration
public class RedisConfiguration {

    @Value("${spring.redis.host}")
    private String redisHost;

    @Value("${spring.redis.port}")
    private int redisPort;

    @Autowired
    private NewMessageListener newMessageListener;

    @Bean
    JedisConnectionFactory jedisConnectionFactory(){
        JedisConnectionFactory factory = new JedisConnectionFactory();
        factory.setHostName(redisHost);
        factory.setPort(redisPort);
        factory.setUsePool(true);
        return factory;
    }

    @Bean
    RedisTemplate<Object, Object> redisTemplate(){
        RedisTemplate<Object, Object> redisTemplate = new RedisTemplate<>();
        redisTemplate.setConnectionFactory(jedisConnectionFactory());
        GenericToStringSerializer genericToStringSerializer = new GenericToStringSerializer(Object.class);

        redisTemplate.setValueSerializer(genericToStringSerializer);
        redisTemplate.setKeySerializer(new StringRedisSerializer());

        redisTemplate.setHashValueSerializer(genericToStringSerializer);
        redisTemplate.setHashKeySerializer(genericToStringSerializer);

        redisTemplate.afterPropertiesSet();

        return redisTemplate;
    }

    @Bean
    RedisCacheManager cacheManager(){
//        RedisCacheManager redisCacheManager = new RedisCacheManager(redisTemplate());
//        return redisCacheManager;
        RedisCacheConfiguration redisCacheConfiguration = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofHours(1)); // 设置缓存有效期一小时
        return RedisCacheManager
                .builder(RedisCacheWriter.nonLockingRedisCacheWriter(jedisConnectionFactory()))
                .cacheDefaults(redisCacheConfiguration).build();
    }

    @Bean
    RedisMessageListenerContainer redisContainer(){
        final RedisMessageListenerContainer container = new RedisMessageListenerContainer();

        container.setConnectionFactory(jedisConnectionFactory());
        container.addMessageListener(new MessageListenerAdapter(newMessageListener), new ChannelTopic(Constants.WEBSOCKET_MSG_TOPIC));

        return container;
    }
}
