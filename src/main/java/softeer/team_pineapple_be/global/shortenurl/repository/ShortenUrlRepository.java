package softeer.team_pineapple_be.global.shortenurl.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import softeer.team_pineapple_be.global.shortenurl.domain.ShortenUrl;

import java.util.Optional;

public interface ShortenUrlRepository extends JpaRepository<ShortenUrl, Long> {
    Optional<ShortenUrl> findByShortenUrl(String shortenUrl);
    Optional<ShortenUrl> findByOriginalUrl(String originalUrl);
}
