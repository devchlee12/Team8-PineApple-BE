package softeer.team_pineapple_be.global.cloud.service;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import lombok.RequiredArgsConstructor;
import softeer.team_pineapple_be.global.exception.RestApiException;
import softeer.team_pineapple_be.global.message.exception.MessageErrorCode;

/**
 * S3에서 파일 다운로드하는 서비스
 */
@Service
@RequiredArgsConstructor
public class S3DownloadService {
  private final RestClient restClient;

  public byte[] getS3ImageBytes(String imageUrl) {
    ResponseEntity<byte[]> entity = restClient.get().uri(imageUrl).retrieve().toEntity(byte[].class);
    if (entity.getStatusCode().is2xxSuccessful()) {
      return entity.getBody();
    } else {
      throw new RestApiException(MessageErrorCode.MESSAGE_SEND_FAILED);
    }
  }
}
