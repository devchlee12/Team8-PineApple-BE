package softeer.team_pineapple_be.global.shortenurl.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 단축 url 엔티티
 */
@Entity
@Getter
@NoArgsConstructor
public class ShortenUrl {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String shortenUrl;
    private String originalUrl;

    public ShortenUrl(String shortenUrl, String originalUrl) {
        this.shortenUrl = shortenUrl;
        this.originalUrl = originalUrl;
    }
}
