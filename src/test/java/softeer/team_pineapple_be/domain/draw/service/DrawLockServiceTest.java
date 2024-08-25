package softeer.team_pineapple_be.domain.draw.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import softeer.team_pineapple_be.domain.comment.exception.CommentErrorCode;
import softeer.team_pineapple_be.domain.comment.repository.CommentRepository;
import softeer.team_pineapple_be.domain.draw.domain.DrawDailyMessageInfo;
import softeer.team_pineapple_be.domain.draw.domain.DrawHistory;
import softeer.team_pineapple_be.domain.draw.domain.DrawPrize;
import softeer.team_pineapple_be.domain.draw.domain.DrawRewardInfo;
import softeer.team_pineapple_be.domain.draw.exception.DrawErrorCode;
import softeer.team_pineapple_be.domain.draw.repository.DrawDailyMessageInfoRepository;
import softeer.team_pineapple_be.domain.draw.repository.DrawHistoryRepository;
import softeer.team_pineapple_be.domain.draw.repository.DrawPrizeRepository;
import softeer.team_pineapple_be.domain.draw.repository.DrawRewardInfoRepository;
import softeer.team_pineapple_be.domain.draw.response.DrawLoseResponse;
import softeer.team_pineapple_be.domain.draw.response.DrawResponse;
import softeer.team_pineapple_be.domain.draw.response.DrawWinningResponse;
import softeer.team_pineapple_be.domain.member.domain.Member;
import softeer.team_pineapple_be.global.exception.RestApiException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Optional;

public class DrawLockServiceTest {

    @InjectMocks
    private DrawLockService drawLockService;

    @Mock
    private DrawRewardInfoRepository drawRewardInfoRepository;

    @Mock
    private DrawDailyMessageInfoRepository drawDailyMessageInfoRepository;

    @Mock
    private DrawHistoryRepository drawHistoryRepository;

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private DrawPrizeRepository drawPrizeRepository;


    @Mock
    private DrawDailyMessageInfo dailyMessageInfo;

    @Mock
    private Member member;

    @Mock
    private DrawPrize drawPrize;

    private String memberPhoneNumber;
    private DrawRewardInfo drawRewardInfo;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        memberPhoneNumber = "010-1234-5678";
        drawRewardInfo = new DrawRewardInfo((byte)2, "Prize", 1 ,"image",null);
    }

    @Test
    @DisplayName("추첨에 당첨 되었을 경우 - SuccessCase")
    public void testDisposeDrawPrize_Win_SuccessCase() {
        // Given
        Byte prizeRank = 2;
        when(drawRewardInfoRepository.findById(prizeRank)).thenReturn(Optional.of(drawRewardInfo));
        when(drawPrizeRepository.findFirstByDrawRewardInfoAndValid(drawRewardInfo, true)).thenReturn(Optional.of(drawPrize));
        when(dailyMessageInfo.getWinMessage()).thenReturn("축하합니다!");
        when(dailyMessageInfo.getWinImage()).thenReturn("win_image.png");
        when(member.isCar()).thenReturn(false);
        when(member.getToolBoxCnt()).thenReturn(0);

        // When
        DrawResponse response = drawLockService.disposeDrawPrize(memberPhoneNumber, dailyMessageInfo, prizeRank, member);

        // Then
        assertThat(response).isInstanceOf(DrawWinningResponse.class);
        DrawWinningResponse winningResponse = (DrawWinningResponse) response;
        assertThat(winningResponse.getDailyWinningMessage()).isEqualTo("축하합니다!");
        assertThat(winningResponse.getImage()).isEqualTo("win_image.png");
        assertThat(winningResponse.getPrizeId()).isNotNull();
        verify(drawHistoryRepository).save(any(DrawHistory.class));
        verify(drawPrize).isNowOwnedBy(memberPhoneNumber);
        verify(drawPrize).invalidate();
    }

    @Test
    @DisplayName("추첨에 당첨되지 못했을 경우 - SuccessCase")
    public void testDisposeDrawPrize_lose_SuccessCase() {
        // Given
        Byte prizeRank = 1;
        drawRewardInfo = new DrawRewardInfo((byte)0, "Prize", 1 ,"image",null);
        when(drawRewardInfoRepository.findById(prizeRank)).thenReturn(Optional.of(drawRewardInfo));
        when(drawPrizeRepository.findFirstByDrawRewardInfoAndValid(drawRewardInfo, true)).thenReturn(Optional.of(drawPrize));
        when(dailyMessageInfo.getLoseMessage()).thenReturn("아쉽습니다!");
        when(dailyMessageInfo.getLoseImage()).thenReturn("lose_image.png");
        when(member.isCar()).thenReturn(false);
        when(member.getToolBoxCnt()).thenReturn(0);

        // When
        DrawResponse response = drawLockService.disposeDrawPrize(memberPhoneNumber, dailyMessageInfo, prizeRank, member);

        // Then
        assertThat(response).isInstanceOf(DrawLoseResponse.class);
        DrawLoseResponse loseResponse = (DrawLoseResponse) response;
        assertThat(loseResponse.getDailyLoseMessage()).isEqualTo("아쉽습니다!");
        assertThat(loseResponse.getImage()).isEqualTo("lose_image.png");
        verify(drawHistoryRepository).save(any(DrawHistory.class));
    }

    @Test
    @DisplayName("상품이 존재하지 않는 경우 - FailureCase")
    public void testDisposeDrawPrize_NoPrizeFound_FailureCase() {
        // Given
        String memberPhoneNumber = "010-1234-5678";
        Byte prizeRank = 2;
        when(drawRewardInfoRepository.findById(prizeRank)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> drawLockService.disposeDrawPrize(memberPhoneNumber, dailyMessageInfo, prizeRank, member)).isInstanceOf(
                RestApiException.class).satisfies(exception -> {
            RestApiException restApiException = (RestApiException) exception;
            assertThat(restApiException.getErrorCode()).isEqualTo(DrawErrorCode.NO_PRIZE);
        });
    }

    @Test
    @DisplayName("유효한 상품이 존재하지 않는 경우 - FailureCase")
    public void testDisposeDrawPrize_NoValidPrize_FailureCase() {
        // Given
        String memberPhoneNumber = "010-1234-5678";
        Byte prizeRank = DrawLockService.DRAW_FIRST_PRIZE;
        when(drawRewardInfoRepository.findById(prizeRank)).thenReturn(Optional.of(drawRewardInfo));
        when(drawPrizeRepository.findFirstByDrawRewardInfoAndValid(drawRewardInfo, true)).thenReturn(Optional.empty());

        // When
        DrawResponse response = drawLockService.disposeDrawPrize(memberPhoneNumber, dailyMessageInfo, prizeRank, member);

        // Then
        assertThat(response).isInstanceOf(DrawLoseResponse.class);
        verify(drawHistoryRepository).save(any(DrawHistory.class));
    }

    @Test
    @DisplayName("추첨에 당첨 되지 못하고 성공적으로 저장되는 경우 - SuccessCase")
    public void testDisposeDrawPrize_StockDepleted_SuccessCase() {
        // Given
        String memberPhoneNumber = "010-1234-5678";
        Byte prizeRank = DrawLockService.DRAW_FIRST_PRIZE;
        when(drawRewardInfoRepository.findById(prizeRank)).thenReturn(Optional.of(drawRewardInfo));

        // Wheb
        DrawResponse response = drawLockService.disposeDrawPrize(memberPhoneNumber, dailyMessageInfo, prizeRank, member);

        // Then
        assertThat(response).isInstanceOf(DrawLoseResponse.class);
        verify(drawHistoryRepository).save(any(DrawHistory.class));
    }
}
