package softeer.team_pineapple_be.global.shortenurl.request;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 단축 url -> 원본 url을 위한 Request 클래스
 */
@Getter
@Setter
@NoArgsConstructor
public class ShortenUrlRequest {
    private String shortenUrl;
}
