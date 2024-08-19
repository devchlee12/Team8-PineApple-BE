package softeer.team_pineapple_be.global.shortenurl.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 단축 url 생성 및 응답을 위한 Response 클래스
 */
@Getter
@AllArgsConstructor
public class ShortenUrlResponse {
    private String shortenUrl;
}
