package softeer.team_pineapple_be.global.shortenurl.service;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import softeer.team_pineapple_be.domain.comment.domain.Comment;
import softeer.team_pineapple_be.domain.comment.repository.CommentRepository;
import softeer.team_pineapple_be.global.auth.service.AuthMemberService;
import softeer.team_pineapple_be.global.exception.RestApiException;
import softeer.team_pineapple_be.global.shortenurl.domain.ShortenUrl;
import softeer.team_pineapple_be.global.shortenurl.exception.ShortenUrlErrorCode;
import softeer.team_pineapple_be.global.shortenurl.repository.ShortenUrlRepository;
import softeer.team_pineapple_be.global.shortenurl.response.ShortenOriginalUrlResponse;
import softeer.team_pineapple_be.global.shortenurl.response.ShortenUrlResponse;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class ShortenUrlServiceTest {

    @Mock
    private ShortenUrlRepository shortenUrlRepository;

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private AuthMemberService authMemberService;

    @InjectMocks
    private ShortenUrlService shortenUrlService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    @DisplayName("단축 URL이 이미 존재하는 경우 - 성공 케이스")
    void testGetShortenUrlWhenAlreadyExists() {
        // Given
        Long commentId = 1L;
        String originalUrl = "event/comments/commentId/" + commentId;
        String shortenUrl = "abcd1234";
        Comment comment = mock(Comment.class);
        when(comment.getId()).thenReturn(commentId);
        when(authMemberService.getMemberPhoneNumber()).thenReturn("01012345678");
        when(commentRepository.findByPhoneNumberAndPostTimeBetween(any(), any(), any())).thenReturn(Optional.of(comment));
        when(shortenUrlRepository.findByOriginalUrl(originalUrl)).thenReturn(Optional.of(new ShortenUrl(shortenUrl, originalUrl)));

        // When
        ShortenUrlResponse response = shortenUrlService.getShortenUrl();

        // Then
        assertThat(response.getShortenUrl()).isEqualTo(shortenUrl);
        verify(shortenUrlRepository, never()).save(any(ShortenUrl.class));
    }

    @Test
    @DisplayName("단축 URL을 새로 생성하는 경우 - 성공 케이스")
    void testGetShortenUrlWhenNotExists() {
        // Given
        Long commentId = 1L;
        String originalUrl = "event/comments/commentId/" + commentId;
        Comment comment = mock(Comment.class);
        when(comment.getId()).thenReturn(commentId);
        when(authMemberService.getMemberPhoneNumber()).thenReturn("01012345678");
        when(commentRepository.findByPhoneNumberAndPostTimeBetween(any(), any(), any())).thenReturn(Optional.of(comment));
        when(shortenUrlRepository.findByOriginalUrl(originalUrl)).thenReturn(Optional.empty());
        when(shortenUrlRepository.findByShortenUrl(any())).thenReturn(Optional.empty());

        // When
        ShortenUrlResponse response = shortenUrlService.getShortenUrl();

        // Then
        assertThat(response.getShortenUrl()).hasSize(8);
        verify(shortenUrlRepository, times(1)).save(any(ShortenUrl.class));
    }

    @Test
    @DisplayName("단축 URL이 존재하지 않을 경우 - 예외 처리")
    void testRedirectUrlWhenShortenUrlNotExists() {
        // Given
        String shortenUrl = "abcd1234";
        when(shortenUrlRepository.findByShortenUrl(shortenUrl)).thenReturn(Optional.empty());

        // When / Then
        assertThatThrownBy(() -> shortenUrlService.redirectUrl(shortenUrl))
                .isInstanceOf(RestApiException.class)
                .satisfies(exception -> {
                    RestApiException restApiException = (RestApiException) exception; // 캐스팅
                    Assertions.assertThat(restApiException.getErrorCode()).isEqualTo(ShortenUrlErrorCode.NOT_EXISTS);
                });
    }

    @Test
    @DisplayName("단축 URL이 존재할 경우 - 성공 케이스")
    void testRedirectUrlWhenShortenUrlExists() {
        // Given
        String shortenUrl = "abcd1234";
        String originalUrl = "event/comments/commentId/1";
        ShortenUrl shortenUrlEntity = new ShortenUrl(shortenUrl, originalUrl);
        when(shortenUrlRepository.findByShortenUrl(shortenUrl)).thenReturn(Optional.of(shortenUrlEntity));

        // When
        ShortenOriginalUrlResponse response = shortenUrlService.redirectUrl(shortenUrl);

        // Then
        assertThat(response.getOriginalUrl()).isEqualTo(originalUrl);
        verify(shortenUrlRepository, times(1)).findByShortenUrl(shortenUrl);
    }
}
