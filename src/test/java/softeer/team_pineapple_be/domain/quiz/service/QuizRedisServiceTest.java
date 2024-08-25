package softeer.team_pineapple_be.domain.quiz.service;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SetOperations;

import static org.mockito.Mockito.*;
import static org.assertj.core.api.Assertions.assertThat;

public class QuizRedisServiceTest {

    private QuizRedisService quizRedisService;

    @Mock
    private RedisTemplate<String, String> redisTemplate;

    @Mock
    private SetOperations<String, String> setOps;

    private final String memberPhoneNumber = "010-1234-5678";

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        quizRedisService = new QuizRedisService(redisTemplate);
        when(redisTemplate.opsForSet()).thenReturn(setOps);
    }

    @Test
    @DisplayName("퀴즈 참여 정보 및 상품 정보 초기화")
    void deleteParticipateInfo() {
        // When
        quizRedisService.deleteParticipateInfo();

        // Then
        verify(redisTemplate).delete("participated");
        verify(redisTemplate).delete("rewarded");
    }

    @Test
    @DisplayName("퀴즈 참여 등록")
    void participate() {
        // When
        quizRedisService.participate(memberPhoneNumber);

        // Then
        verify(setOps).add("participated", memberPhoneNumber);
    }

    @Test
    @DisplayName("당일 선착순 경품 당첨 여부 저장")
    void saveRewardWin() {
        // When
        quizRedisService.saveRewardWin(memberPhoneNumber);

        // Then
        verify(setOps).add("rewarded", memberPhoneNumber);
    }

    @Test
    @DisplayName("유저가 당일 선착순 상품을 받았는지 확인")
    void wasMemberWinRewardToday() {
        // Given
        when(setOps.isMember("rewarded", memberPhoneNumber)).thenReturn(true);

        // When
        Boolean result = quizRedisService.wasMemberWinRewardToday(memberPhoneNumber);

        // Then
        verify(setOps).isMember("rewarded", memberPhoneNumber);
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("퀴즈 참여 여부 확인")
    void wasParticipatedInQuiz() {
        // Given
        when(setOps.isMember("participated", memberPhoneNumber)).thenReturn(true);

        // When
        Boolean result = quizRedisService.wasParticipatedInQuiz(memberPhoneNumber);

        // Then
        verify(setOps).isMember("participated", memberPhoneNumber);
        assertThat(result).isTrue();
    }
}
