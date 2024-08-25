package softeer.team_pineapple_be.domain.admin.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import softeer.team_pineapple_be.domain.admin.domain.EventDayInfo;
import softeer.team_pineapple_be.domain.admin.exception.AdminErrorCode;
import softeer.team_pineapple_be.domain.admin.repisotory.EventDayInfoRepository;
import softeer.team_pineapple_be.domain.admin.request.EventScheduleUpdateRequest;
import softeer.team_pineapple_be.domain.admin.response.EventScheduleResponse;
import softeer.team_pineapple_be.global.cloud.service.exception.S3ErrorCode;
import softeer.team_pineapple_be.global.exception.RestApiException;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

public class EventDayInfoServiceTest {

    private EventDayInfoService eventDayInfoService;

    @Mock
    private EventDayInfoRepository eventDayInfoRepository;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        eventDayInfoService = new EventDayInfoService(eventDayInfoRepository);
    }

    @Test
    @DisplayName("특정 이벤트 날짜를 정상적으로 조회할 수 있다.")
    void getEventDateOfEventDay() {
        // Given
        int day = 1;
        LocalDate eventDate = LocalDate.of(2024, 5, 1);
        EventDayInfo eventDayInfo = new EventDayInfo(day, eventDate);

        when(eventDayInfoRepository.findById(day)).thenReturn(Optional.of(eventDayInfo));

        // When
        LocalDate result = eventDayInfoService.getEventDateOfEventDay(day);

        // Then
        assertThat(result).isEqualTo(eventDate);
        verify(eventDayInfoRepository).findById(day);
    }

    @Test
    @DisplayName("존재하지 않는 이벤트 날짜 요청 시 예외가 발생한다.")
    void getEventDateOfEventDay_NotFound() {
        // Given
        int day = 1;

        when(eventDayInfoRepository.findById(day)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> eventDayInfoService.getEventDateOfEventDay(day))
                .isInstanceOf(RestApiException.class)
                .satisfies(exception -> {
                    RestApiException restApiException = (RestApiException) exception; // 캐스팅
                    assertThat(restApiException.getErrorCode()).isEqualTo(AdminErrorCode.NOT_EVENT_DAY);
                });
    }

    @Test
    @DisplayName("이벤트 스케줄을 정상적으로 가져올 수 있다.")
    void getEventSchedules() {
        // Given
        EventDayInfo eventDayInfo1 = new EventDayInfo(1, LocalDate.of(2024, 5, 1));
        EventDayInfo eventDayInfo2 = new EventDayInfo(2, LocalDate.of(2024, 5, 2));
        List<EventDayInfo> eventDayInfos = Arrays.asList(eventDayInfo1, eventDayInfo2);

        when(eventDayInfoRepository.findAll()).thenReturn(eventDayInfos);

        // When
        List<EventScheduleResponse> result = eventDayInfoService.getEventSchedules();

        // Then
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getDate()).isEqualTo(eventDayInfo1.getEventDate());
        assertThat(result.get(1).getDate()).isEqualTo(eventDayInfo2.getEventDate());
        verify(eventDayInfoRepository).findAll();
    }

    @Test
    @DisplayName("이벤트 스케줄을 정상적으로 수정할 수 있다.")
    void updateEventStartDay() {
        // Given
        LocalDate startDate = LocalDate.of(2024, 5, 1);
        EventScheduleUpdateRequest request = new EventScheduleUpdateRequest(startDate);

        // When
        eventDayInfoService.updateEventStartDay(request);

        // Then
        verify(eventDayInfoRepository).deleteAll();
        verify(eventDayInfoRepository).saveAll(anyList());

        // 추가적으로 저장된 이벤트 날짜를 확인
        for (int i = 0; i < 14; i++) {
            LocalDate expectedDate = startDate.plusDays(i);
            verify(eventDayInfoRepository).saveAll(argThat(eventDayInfos ->
                    // Iterable을 List로 변환하여 stream() 사용
                    ((List<EventDayInfo>) eventDayInfos).stream().anyMatch(eventDayInfo ->
                            eventDayInfo.getEventDate().isEqual(expectedDate)
                    )
            ));
        }
    }

}
