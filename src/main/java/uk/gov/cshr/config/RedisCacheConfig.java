package uk.gov.cshr.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisPassword;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

public class RedisCacheConfig {

    @Value("${registry.cache.allowlistTTLSeconds}")
    private int learnerRecordCacheTTlSeconds;
    @Value("spring.redis.port")
    private int redisPort;
    @Value("spring.redis.host")
    private String redisHost;
    @Value("spring.redis.password")
    private String redisPassword;

    @Bean
    public LettuceConnectionFactory redisConnectionFactory() {
        RedisStandaloneConfiguration redisConf = new RedisStandaloneConfiguration();
        redisConf.setHostName(redisHost);
        redisConf.setPort(redisPort);
        redisConf.setPassword(RedisPassword.of(redisPassword));
        return new LettuceConnectionFactory(redisConf);
    }

    @Bean
    public RedisCacheManager redisCacheManager(LettuceConnectionFactory redisConnectionFactory) {
        Map<String, RedisCacheConfiguration> configMap = new HashMap<>();
        RedisCacheConfiguration defaultCacheConfig = RedisCacheConfiguration.defaultCacheConfig().disableCachingNullValues();
        configMap.put("allowlist", RedisCacheConfiguration.defaultCacheConfig().entryTtl(Duration.ofSeconds(learnerRecordCacheTTlSeconds)));
        return RedisCacheManager.RedisCacheManagerBuilder.fromConnectionFactory(redisConnectionFactory)
                .cacheDefaults(defaultCacheConfig)
                .withInitialCacheConfigurations(configMap)
                .build();
    }
}
