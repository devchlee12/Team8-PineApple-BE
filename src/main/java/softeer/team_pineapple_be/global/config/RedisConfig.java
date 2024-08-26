package softeer.team_pineapple_be.global.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

/**
 * 레디스 설정
 */
@Configuration
public class RedisConfig {
  @Value("${redis.host}")
  private String host;
  @Value("${redis.port}")
  private int redisPort;

  @Bean("redisCacheManager")
  public CacheManager redisCacheManager() {
    return RedisCacheManager.RedisCacheManagerBuilder.fromConnectionFactory(redisConnectionFactory())
                                                     .cacheDefaults(defaultConfiguration())
                                                     .withInitialCacheConfigurations(configureMap())
                                                     .build();
  }

  @Bean
  public RedisConnectionFactory redisConnectionFactory() {
    return new LettuceConnectionFactory(host, redisPort);
  }

  @Bean
  public RedisTemplate<String, String> redisTemplate(RedisConnectionFactory redisConnectionFactory) {
    RedisTemplate<String, String> redisTemplate = new RedisTemplate<>();
    redisTemplate.setConnectionFactory(redisConnectionFactory);
    redisTemplate.setKeySerializer(new StringRedisSerializer());
    redisTemplate.setValueSerializer(new StringRedisSerializer());
    return redisTemplate;
  }

  private Map<String, RedisCacheConfiguration> configureMap() {
    Map<String, RedisCacheConfiguration> cacheConfigurationMap = new HashMap<>();
    cacheConfigurationMap.put("getRedisWithCacheManager", defaultConfiguration().entryTtl(Duration.ofMinutes(5)));
    cacheConfigurationMap.put("drawProbability", defaultConfiguration().entryTtl(Duration.ofDays(1)));
    cacheConfigurationMap.put("quizContent", defaultConfiguration().entryTtl(Duration.ofDays(1)));
    cacheConfigurationMap.put("quizInfo", defaultConfiguration().entryTtl(Duration.ofDays(1)));
    return cacheConfigurationMap;
  }

  private RedisCacheConfiguration defaultConfiguration() {
    return RedisCacheConfiguration.defaultCacheConfig()
                                  .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(
                                      new StringRedisSerializer()))
                                  .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(
                                      new GenericJackson2JsonRedisSerializer()))
                                  .entryTtl(Duration.ofMinutes(10));
  }
}
