package softeer.team_pineapple_be.global.shortenurl.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import softeer.team_pineapple_be.global.shortenurl.domain.ShortenUrl;

import java.util.Optional;

/**
 * 단축 url 관련 정보를 담당하는 레포지토리
 */
public interface ShortenUrlRepository extends JpaRepository<ShortenUrl, Long> {
    Optional<ShortenUrl> findByShortenUrl(String shortenUrl);
    Optional<ShortenUrl> findByOriginalUrl(String originalUrl);
}
