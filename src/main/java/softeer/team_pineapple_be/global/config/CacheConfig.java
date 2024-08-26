package softeer.team_pineapple_be.global.config;

import com.github.benmanes.caffeine.cache.Caffeine;

import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCache;
import org.springframework.cache.support.SimpleCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import softeer.team_pineapple_be.global.common.domain.enums.CacheType;

/**
 * 캐시 설정
 */
@Configuration
public class CacheConfig {
  @Bean
  @Primary
  public CacheManager cacheManager() {
    SimpleCacheManager cacheManager = new SimpleCacheManager();
    List<CaffeineCache> caches = Arrays.stream(CacheType.values())
                                       .map(cache -> new CaffeineCache(cache.getCacheName(), Caffeine.newBuilder()
                                                                                                     .expireAfterWrite(
                                                                                                         cache.getExpiredAfterWrite(),
                                                                                                         TimeUnit.SECONDS)
                                                                                                     .maximumSize(
                                                                                                         cache.getMaximumSize())
                                                                                                     .build()))
                                       .collect(Collectors.toList());
    cacheManager.setCaches(caches);
    return cacheManager;
  }
}
