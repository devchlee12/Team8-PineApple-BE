package softeer.team_pineapple_be.domain.admin.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import softeer.team_pineapple_be.domain.admin.domain.EventDayInfo;
import softeer.team_pineapple_be.domain.admin.exception.AdminErrorCode;
import softeer.team_pineapple_be.domain.admin.repisotory.EventDayInfoRepository;
import softeer.team_pineapple_be.domain.admin.response.TopPrizeWinnerResponse;
import softeer.team_pineapple_be.domain.draw.domain.DrawHistory;
import softeer.team_pineapple_be.domain.draw.domain.DrawRewardInfo;
import softeer.team_pineapple_be.domain.draw.exception.DrawErrorCode;
import softeer.team_pineapple_be.domain.draw.repository.DrawHistoryRepository;
import softeer.team_pineapple_be.domain.draw.repository.DrawRewardInfoRepository;
import softeer.team_pineapple_be.global.exception.RestApiException;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TopPrizeWinnerServiceTest {

    @Mock
    private EventDayInfoRepository eventDayInfoRepository;

    @Mock
    private DrawHistoryRepository drawHistoryRepository;

    @Mock
    private DrawRewardInfoRepository drawRewardInfoRepository;

    @InjectMocks
    private TopPrizeWinnerService topPrizeWinnerService;

    private EventDayInfo eventDayInfo;
    private DrawHistory drawHistory;
    private DrawRewardInfo drawRewardInfo;
    private static final int SCHEDULE_LENGTH = 14;

    @BeforeEach
    void setUp() {
        eventDayInfo = new EventDayInfo(14, LocalDate.now().minusDays(1));
        drawHistory = new DrawHistory((byte)1, "010-1234-5678");

        drawRewardInfo = new DrawRewardInfo((byte) 1, "Prize 1", 10, "image1.jpg");
    }

    @Test
    @DisplayName("성공적으로 1등 추첨에 성공한 경우 - SuccessCase")
    void testGetTopPrizeWinner() {
        // given
        when(eventDayInfoRepository.findById(SCHEDULE_LENGTH)).thenReturn(Optional.of(eventDayInfo));
        when(drawHistoryRepository.count()).thenReturn(100L);
        when(drawHistoryRepository.findById(anyLong())).thenReturn(Optional.of(drawHistory));
        when(drawRewardInfoRepository.findById(anyByte())).thenReturn(Optional.of(drawRewardInfo));

        // when
        TopPrizeWinnerResponse response = topPrizeWinnerService.getTopPrizeWinner();

        // then
        assertThat(response).isNotNull();
        assertThat(response.getPhoneNumber()).isEqualTo(drawHistory.getPhoneNumber());

        // verify
        verify(drawHistoryRepository, times(1)).count();
        verify(drawHistoryRepository, times(1)).findById(anyLong());
        verify(drawRewardInfoRepository, times(1)).findById(anyByte());
        verify(drawRewardInfoRepository, times(1)).save(drawRewardInfo);
        verify(drawHistoryRepository, times(1)).save(any(DrawHistory.class));
    }

    @Test
    @DisplayName("이벤트 진행 중에 1등을 추첨하려고 하는 경우 - FailureCase")
    void testValidateDrawDate_BeforeEventDay_shouldThrowException() {
        // given
        eventDayInfo = new EventDayInfo(14, LocalDate.now().plusDays(1));
        when(eventDayInfoRepository.findById(SCHEDULE_LENGTH)).thenReturn(Optional.of(eventDayInfo));

        // when & then
        verify(drawHistoryRepository, never()).count();
        assertThatThrownBy(() -> topPrizeWinnerService.getTopPrizeWinner()).isInstanceOf(RestApiException.class)
                .satisfies(exception -> {
                    RestApiException restApiException =
                            (RestApiException) exception; // 캐스팅
                    assertThat(restApiException.getErrorCode()).isEqualTo(
                            AdminErrorCode.CAN_NOT_DRAW_TOP_PRIZE_WINNER);
                });
    }

    @Test
    @DisplayName("이벤트 진행 날짜가 조회되지 않는 경우 - FailureCase")
    void testValidateDrawDate_EventDayNotExists_shouldThrowException() {
        // given
        eventDayInfo = new EventDayInfo(14, LocalDate.now().plusDays(1));
        when(eventDayInfoRepository.findById(SCHEDULE_LENGTH)).thenReturn(Optional.empty());

        // when & then
        verify(drawHistoryRepository, never()).count();
        assertThatThrownBy(() -> topPrizeWinnerService.getTopPrizeWinner()).isInstanceOf(RestApiException.class)
                .satisfies(exception -> {
                    RestApiException restApiException =
                            (RestApiException) exception; // 캐스팅
                    assertThat(restApiException.getErrorCode()).isEqualTo(
                            AdminErrorCode.NOT_EVENT_DAY);
                });
    }

    @Test
    @DisplayName("재고가 없어서 1등 추첨이 불가능한 경우 ")
    void testProcessReward_noStock_shouldThrowException() {
        // given
        drawRewardInfo = new DrawRewardInfo((byte) 1, "Prize 1", 0, "image1.jpg");
        when(eventDayInfoRepository.findById(SCHEDULE_LENGTH)).thenReturn(Optional.of(eventDayInfo));
        when(drawHistoryRepository.count()).thenReturn(100L);
        when(drawHistoryRepository.findById(anyLong())).thenReturn(Optional.of(drawHistory));
        when(drawRewardInfoRepository.findById(anyByte())).thenReturn(Optional.of(drawRewardInfo));

        // when & then
        assertThatThrownBy(() -> topPrizeWinnerService.getTopPrizeWinner()).isInstanceOf(RestApiException.class)
                .satisfies(exception -> {
                    RestApiException restApiException =
                            (RestApiException) exception; // 캐스팅
                    assertThat(restApiException.getErrorCode()).isEqualTo(
                            DrawErrorCode.NO_PRIZE);
                });
    }

    @Test
    @DisplayName("재고가 없어 1등 추첨에 실패한 경우 - FailureCase")
    void testGetTopPrizeWinner_notDrawRewardInfo_ThrowRestApiException() {
        // given
        when(eventDayInfoRepository.findById(SCHEDULE_LENGTH)).thenReturn(Optional.of(eventDayInfo));
        when(drawHistoryRepository.count()).thenReturn(100L);
        when(drawHistoryRepository.findById(anyLong())).thenReturn(Optional.of(drawHistory));
        when(drawRewardInfoRepository.findById(anyByte())).thenReturn(Optional.empty());

        // when
        assertThatThrownBy(() -> topPrizeWinnerService.getTopPrizeWinner()).isInstanceOf(RestApiException.class)
                .satisfies(exception -> {
                    RestApiException restApiException =
                            (RestApiException) exception; // 캐스팅
                    assertThat(restApiException.getErrorCode()).isEqualTo(
                            DrawErrorCode.NO_PRIZE);
                });
    }

    @Test
    @DisplayName("유효한 1등 당첨자가 아닌 경우 - FailureCase")
    void testGetTopPrizeWinner_DrawHistoryNotExists_ThrowRestApiException() {
        // given
        when(eventDayInfoRepository.findById(SCHEDULE_LENGTH)).thenReturn(Optional.of(eventDayInfo));
        when(drawHistoryRepository.count()).thenReturn(100L);
        when(drawHistoryRepository.findById(anyLong())).thenReturn(Optional.empty());

        // when
        assertThatThrownBy(() -> topPrizeWinnerService.getTopPrizeWinner()).isInstanceOf(RestApiException.class)
                .satisfies(exception -> {
                    RestApiException restApiException =
                            (RestApiException) exception; // 캐스팅
                    assertThat(restApiException.getErrorCode()).isEqualTo(
                            DrawErrorCode.NOT_VALID_WINNER);
                });
    }
}
