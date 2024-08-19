package softeer.team_pineapple_be.global.shortenurl.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import softeer.team_pineapple_be.domain.comment.domain.Comment;
import softeer.team_pineapple_be.domain.comment.exception.CommentErrorCode;
import softeer.team_pineapple_be.domain.comment.repository.CommentRepository;
import softeer.team_pineapple_be.global.auth.service.AuthMemberService;
import softeer.team_pineapple_be.global.common.utils.RandomUtils;
import softeer.team_pineapple_be.global.exception.RestApiException;
import softeer.team_pineapple_be.global.shortenurl.domain.ShortenUrl;
import softeer.team_pineapple_be.global.shortenurl.exception.ShortenUrlErrorCode;
import softeer.team_pineapple_be.global.shortenurl.repository.ShortenUrlRepository;
import softeer.team_pineapple_be.global.shortenurl.response.ShortenUrlResponse;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Service
@RequiredArgsConstructor
public class ShortenUrlService {

    private static final String BASE_56_CHARACTERS = "23456789ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnpqrstuvwxyz";
    private static final int SHORTEN_URL_LENGTH = 8;
    private static final int MAX_TRY_COUNT = 5;

    private final ShortenUrlRepository shortenUrlRepository;
    private final CommentRepository commentRepository;
    private final AuthMemberService authMemberService;

    @Transactional
    public ShortenUrlResponse getShortenUrl() {
        Comment comment = findTodayComment();
        String originalUrl = buildOriginalUrl(comment.getId());

        return shortenUrlRepository.findByOriginalUrl(originalUrl)
                .map(shortenUrl -> new ShortenUrlResponse(shortenUrl.getShortenUrl()))
                .orElseGet(() -> new ShortenUrlResponse(generateAndSaveShortenUrl(originalUrl)));
    }

    @Transactional
    public String redirectUrl(String shortenUrl) {
        ShortenUrl shortenUrlEntity = shortenUrlRepository.findByShortenUrl(shortenUrl)
                .orElseThrow(() -> new RestApiException(ShortenUrlErrorCode.NOT_EXISTS));
        return shortenUrlEntity.getOriginalUrl();
    }

    private Comment findTodayComment() {
        LocalDate today = LocalDate.now();
        LocalDateTime startOfDay = today.atStartOfDay();
        LocalDateTime endOfDay = today.atTime(LocalTime.MAX);
        String memberPhoneNumber = authMemberService.getMemberPhoneNumber();

        return commentRepository.findByPhoneNumberAndPostTimeBetween(memberPhoneNumber, startOfDay, endOfDay)
                .orElseThrow(() -> new RestApiException(CommentErrorCode.NO_COMMENT));
    }

    private String buildOriginalUrl(Long commentId) {
        return "/comments/commentId?id=" + commentId;
    }

    private String generateAndSaveShortenUrl(String originalUrl) {
        for (int tryCount = 0; tryCount < MAX_TRY_COUNT; tryCount++) {
            String shortenUrl = generateShortenUrl();
            if (shortenUrlRepository.findByShortenUrl(shortenUrl).isEmpty()) {
                saveShortenUrl(shortenUrl, originalUrl);
                return shortenUrl;
            }
        }
        throw new RestApiException(ShortenUrlErrorCode.CAN_NOT_GENERATE_SHORTEN_URL);
    }

    private void saveShortenUrl(String shortenUrl, String originalUrl) {
        ShortenUrl shortenUrlEntity = new ShortenUrl(shortenUrl, originalUrl);
        shortenUrlRepository.save(shortenUrlEntity);
    }

    private String generateShortenUrl() {
        StringBuilder shortenUrl = new StringBuilder(SHORTEN_URL_LENGTH);

        for (int count = 0; count < SHORTEN_URL_LENGTH; count++) {
            int index = RandomUtils.getBase56Index();
            char character = BASE_56_CHARACTERS.charAt(index);
            shortenUrl.append(character);
        }

        return shortenUrl.toString();
    }
}
