package softeer.team_pineapple_be.global.config;

import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * S3 설정
 */
@Configuration
public class S3Config {
  @Value("${cloud.aws.region.static}")
  private String region;

  @Bean
  public AmazonS3 s3Client() {
    return AmazonS3ClientBuilder.standard()
                                .withCredentials(new DefaultAWSCredentialsProviderChain())
                                .withRegion(region)
                                .build();
  }
}
