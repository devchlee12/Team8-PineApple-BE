package softeer.team_pineapple_be.domain.admin.exception;

import org.springframework.http.HttpStatus;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import softeer.team_pineapple_be.global.exception.ErrorCode;

/**
 * 어드민 에러코드
 */
@Getter
@RequiredArgsConstructor
public enum AdminErrorCode implements ErrorCode {
  NOT_EVENT_DAY(HttpStatus.BAD_REQUEST, "이벤트 기간이 아닙니다.");

  private final HttpStatus httpStatus;
  private final String message;
}
