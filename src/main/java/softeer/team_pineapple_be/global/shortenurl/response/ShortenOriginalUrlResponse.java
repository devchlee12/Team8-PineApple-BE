package softeer.team_pineapple_be.global.shortenurl.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * original url을 응답하는 클래스
 */
@Getter
@AllArgsConstructor
public class ShortenOriginalUrlResponse {
    private String originalUrl;
}
