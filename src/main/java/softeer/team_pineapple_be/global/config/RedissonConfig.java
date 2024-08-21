package softeer.team_pineapple_be.global.config;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import softeer.team_pineapple_be.domain.quiz.service.QuizRedisService;

/**
 * Redisson 설정
 */
@Configuration
public class RedissonConfig {
  private static final String REDISSON_HOST_PREFIX = "redis://";
  @Value("${redis.host}")
  private String host;
  @Value("${redis.port}")
  private int redisPort;

  @Bean
  public RedissonClient redissonClient(QuizRedisService quizRedisService) {
    RedissonClient redisson = null;
    Config config = new Config();
    config.useSingleServer().setAddress(REDISSON_HOST_PREFIX + host + ":" + redisPort);
    redisson = Redisson.create(config);
    return redisson;
  }
}
