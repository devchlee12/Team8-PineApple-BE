package softeer.team_pineapple_be.global.message;

import net.nurigo.sdk.NurigoApp;
import net.nurigo.sdk.message.exception.NurigoMessageNotReceivedException;
import net.nurigo.sdk.message.model.Message;
import net.nurigo.sdk.message.model.StorageType;
import net.nurigo.sdk.message.service.DefaultMessageService;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import softeer.team_pineapple_be.global.auth.service.AuthMemberService;
import softeer.team_pineapple_be.global.cloud.service.S3DownloadService;
import softeer.team_pineapple_be.global.exception.RestApiException;
import softeer.team_pineapple_be.global.message.exception.MessageErrorCode;

/**
 * 메시지 서비스 인터페이스
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class MessageService {
  private static final String PRIZE_TEXT = "축하합니다!";
  private final AuthMemberService authMemberService;
  private final S3DownloadService s3DownloadService;

  @Value("${sms.api-key}")
  private String apiKey;

  @Value("${sms.api-secret}")
  private String apiSecret;

  @Value("${sms.from-number}")
  private String fromNumber;

  @Value("${sms.url}")
  private String url;

  private DefaultMessageService messageService;

  /**
   * 경품 이미지 발송
   *
   * @param image
   * @return
   */
  public void sendPrizeImage(String image) {
    String memberPhoneNumber = authMemberService.getMemberPhoneNumber();
    sendImageMessageTo(image, memberPhoneNumber, PRIZE_TEXT);
  }

  /**
   * 텍스트 메시지를 전송하는 메서드
   *
   * @param message
   * @param phoneNumber
   * @return
   */
  public void sendTextMessageTo(String message, String phoneNumber) {
    phoneNumber = phoneNumber.replaceAll("-", "");
    Message smsMessage = new Message();
    smsMessage.setFrom(fromNumber);
    smsMessage.setTo(phoneNumber);
    smsMessage.setText(message);
    sendMessage(smsMessage);
  }

  /**
   * 임시 파일 생성해서 반환하는 메서드
   *
   * @param imageUrl
   * @return 임시파일
   */
  private File createTempFile(String imageUrl) {
    String extension = extractSuffix(imageUrl);
    String tempFileName = UUID.randomUUID().toString();
    try {
      byte[] s3ImageBytes = s3DownloadService.getS3ImageBytes(imageUrl);
      Path tempFile = Files.createTempFile(tempFileName, extension);
      Files.write(tempFile, s3ImageBytes);
      return tempFile.toFile();
    } catch (IOException e) {
      throw new RestApiException(MessageErrorCode.MESSAGE_SEND_FAILED);
    }
  }

  /**
   * 확장자 반환하는 메서드
   *
   * @param originalFileName
   * @return 확장자
   */
  private String extractSuffix(String originalFileName) {
    return originalFileName.substring(originalFileName.lastIndexOf("."));
  }

  /**
   * 메시지 시스템 초기화
   */
  @PostConstruct
  private void init() {
    this.messageService = NurigoApp.INSTANCE.initialize(apiKey, apiSecret, url);
  }

  /**
   * 이미지 전송하는 메서드 (이미지 사이즈 200kb 이내여야함)
   *
   * @param phoneNumber
   */
  private void sendImageMessageTo(String image, String phoneNumber, String text) {
    File tempFile = createTempFile(image);
    String imageId = messageService.uploadFile(tempFile, StorageType.MMS, null);
    Message message = new Message();
    message.setFrom(fromNumber);
    message.setTo(phoneNumber);
    message.setText(text);
    message.setImageId(imageId);
    sendMessage(message);
  }

  /**
   * 메시지 전송
   *
   * @param message
   */
  private void sendMessage(Message message) {
    try {
      messageService.send(message);
    } catch (NurigoMessageNotReceivedException exception) {
      log.info(exception.getFailedMessageList().toString());
      log.info(exception.getMessage());
      throw new RestApiException(MessageErrorCode.MESSAGE_SEND_FAILED);
    } catch (Exception exception) {
      log.info(exception.getMessage());
      throw new RestApiException(MessageErrorCode.MESSAGE_SEND_FAILED);
    }
  }
}
