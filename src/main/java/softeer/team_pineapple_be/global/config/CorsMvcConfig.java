package softeer.team_pineapple_be.global.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * cors 오류를 해결 설정
 */
@Configuration
public class CorsMvcConfig implements WebMvcConfigurer {

  @Override
  public void addCorsMappings(CorsRegistry corsRegistry) {

    corsRegistry.addMapping("/**")
                .allowedOrigins("http://localhost:5173", "https://casper-event.store", "https://hyundai-admin.store")
                .allowCredentials(true)
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*");
  }
}