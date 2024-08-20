package softeer.team_pineapple_be.global.shortenurl.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import softeer.team_pineapple_be.global.exception.ErrorCode;
import org.springframework.http.HttpStatus;

/**
 * 단축 url 에러 코드
 */
@Getter
@RequiredArgsConstructor
public enum ShortenUrlErrorCode implements ErrorCode {

    CAN_NOT_GENERATE_SHORTEN_URL(HttpStatus.CONFLICT, "단축 URL 생성에 실패하였습니다."),
    NOT_EXISTS(HttpStatus.BAD_REQUEST, "존재하지 않는 url입니다");

    private final HttpStatus httpStatus;
    private final String message;
}

