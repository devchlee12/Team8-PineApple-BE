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
  NOT_EVENT_DAY(HttpStatus.BAD_REQUEST, "이벤트 기간이 아닙니다."),
  NOT_ZIP_FILE(HttpStatus.BAD_REQUEST, "집 파일 형식이 아닙니다."),
  SAVE_FAILURE(HttpStatus.BAD_REQUEST, "저장 중 에러가 발생했습니다"),
  CAN_NOT_DRAW_TOP_PRIZE_WINNER(HttpStatus.BAD_REQUEST, "1등 추첨 가능 날짜가 아닙니다.");

  private final HttpStatus httpStatus;
  private final String message;
}
