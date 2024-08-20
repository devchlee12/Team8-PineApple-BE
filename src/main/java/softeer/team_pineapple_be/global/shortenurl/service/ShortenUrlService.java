package softeer.team_pineapple_be.global.shortenurl.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import lombok.RequiredArgsConstructor;
import softeer.team_pineapple_be.domain.comment.domain.Comment;
import softeer.team_pineapple_be.domain.comment.exception.CommentErrorCode;
import softeer.team_pineapple_be.domain.comment.repository.CommentRepository;
import softeer.team_pineapple_be.global.auth.service.AuthMemberService;
import softeer.team_pineapple_be.global.common.utils.RandomUtils;
import softeer.team_pineapple_be.global.exception.RestApiException;
import softeer.team_pineapple_be.global.shortenurl.domain.ShortenUrl;
import softeer.team_pineapple_be.global.shortenurl.exception.ShortenUrlErrorCode;
import softeer.team_pineapple_be.global.shortenurl.repository.ShortenUrlRepository;
import softeer.team_pineapple_be.global.shortenurl.response.ShortenOriginalUrlResponse;
import softeer.team_pineapple_be.global.shortenurl.response.ShortenUrlResponse;

/**
 * 단축 url 처리를 담당하는 클래스
 */
@Service
@RequiredArgsConstructor
public class ShortenUrlService {

  private static final String BASE_56_CHARACTERS = "23456789ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnpqrstuvwxyz";
  private static final int SHORTEN_URL_LENGTH = 8;
  private static final int MAX_TRY_COUNT = 5;

  private final ShortenUrlRepository shortenUrlRepository;
  private final CommentRepository commentRepository;
  private final AuthMemberService authMemberService;

  /**
   * 단축 url을 생성해주는 메서드
   * @return 생성된 단축 url
   */
  @Transactional
  public ShortenUrlResponse getShortenUrl() {
    Comment comment = findTodayComment();
    String originalUrl = buildOriginalUrl(comment.getId());

    return shortenUrlRepository.findByOriginalUrl(originalUrl)
                               .map(shortenUrl -> new ShortenUrlResponse(shortenUrl.getShortenUrl()))
                               .orElseGet(() -> new ShortenUrlResponse(generateAndSaveShortenUrl(originalUrl)));
  }

  /**
   * 단축 url이 들어왔을 때 원본 url을 반환하는 메서드
   * @param shortenUrl 요청된 단축 url
   * @return 반환하고자 하는 url
   */
  @Transactional
  public ShortenOriginalUrlResponse redirectUrl(String shortenUrl) {
    ShortenUrl shortenUrlEntity = shortenUrlRepository.findByShortenUrl(shortenUrl)
                                                      .orElseThrow(
                                                          () -> new RestApiException(ShortenUrlErrorCode.NOT_EXISTS));
    return new ShortenOriginalUrlResponse(shortenUrlEntity.getOriginalUrl());
  }

  private String buildOriginalUrl(Long commentId) {
    return "https://casper-event.store/event/comments/commentId/" + commentId;
  }

  private Comment findTodayComment() {
    LocalDate today = LocalDate.now();
    LocalDateTime startOfDay = today.atStartOfDay();
    LocalDateTime endOfDay = today.atTime(LocalTime.MAX);
    String memberPhoneNumber = authMemberService.getMemberPhoneNumber();

    return commentRepository.findByPhoneNumberAndPostTimeBetween(memberPhoneNumber, startOfDay, endOfDay)
                            .orElseThrow(() -> new RestApiException(CommentErrorCode.NO_COMMENT));
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

  private String generateShortenUrl() {
    StringBuilder shortenUrl = new StringBuilder(SHORTEN_URL_LENGTH);

    for (int count = 0; count < SHORTEN_URL_LENGTH; count++) {
      int index = RandomUtils.getBase56Index();
      char character = BASE_56_CHARACTERS.charAt(index);
      shortenUrl.append(character);
    }

    return shortenUrl.toString();
  }

  private void saveShortenUrl(String shortenUrl, String originalUrl) {
    ShortenUrl shortenUrlEntity = new ShortenUrl(shortenUrl, originalUrl);
    shortenUrlRepository.save(shortenUrlEntity);
  }
}
