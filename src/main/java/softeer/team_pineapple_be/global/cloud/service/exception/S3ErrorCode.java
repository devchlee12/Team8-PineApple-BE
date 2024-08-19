package softeer.team_pineapple_be.global.cloud.service.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;
import softeer.team_pineapple_be.global.exception.ErrorCode;

/**
 *
 */
@Getter
@AllArgsConstructor
public enum S3ErrorCode implements ErrorCode {

    IMAGE_FAILURE(HttpStatus.BAD_REQUEST, "이미지가 정상적으로 저장되지 않았습니다.");

    private final HttpStatus httpStatus;
    private final String message;
}
