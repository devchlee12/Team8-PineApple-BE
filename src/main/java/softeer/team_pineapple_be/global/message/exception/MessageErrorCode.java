package softeer.team_pineapple_be.global.message.exception;


import org.springframework.http.HttpStatus;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import softeer.team_pineapple_be.global.exception.ErrorCode;

/**
 * 메시지 에러코드
 */
@Getter
@RequiredArgsConstructor
public enum MessageErrorCode implements ErrorCode {
  MESSAGE_SEND_FAILED(HttpStatus.BAD_REQUEST, "메시지 전송에 실패했습니다.");

  private final HttpStatus httpStatus;
  private final String message;
}
