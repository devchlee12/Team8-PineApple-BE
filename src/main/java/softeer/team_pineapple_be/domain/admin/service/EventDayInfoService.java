package softeer.team_pineapple_be.domain.admin.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import lombok.RequiredArgsConstructor;
import softeer.team_pineapple_be.domain.admin.domain.EventDayInfo;
import softeer.team_pineapple_be.domain.admin.exception.AdminErrorCode;
import softeer.team_pineapple_be.domain.admin.repisotory.EventDayInfoRepository;
import softeer.team_pineapple_be.domain.admin.request.EventScheduleUpdateRequest;
import softeer.team_pineapple_be.domain.admin.response.EventScheduleResponse;
import softeer.team_pineapple_be.global.exception.RestApiException;

/**
 * 날짜와 이벤트
 */
@Service
@RequiredArgsConstructor
public class EventDayInfoService {
  private final int SCHEDULE_LENGTH = 14;
  private final EventDayInfoRepository eventDayInfoRepository;

  @Transactional(readOnly = true)
  public LocalDate getEventDateOfEventDay(int day) {
    EventDayInfo eventDayInfo =
        eventDayInfoRepository.findById(day).orElseThrow(() -> new RestApiException(AdminErrorCode.NOT_EVENT_DAY));
    return eventDayInfo.getEventDate();
  }

  /**
   * 이벤트 스케줄 가져오기 기능
   *
   * @return
   */
  @Transactional(readOnly = true)
  public List<EventScheduleResponse> getEventSchedules() {
    List<EventDayInfo> allEventSchedules = eventDayInfoRepository.findAll();
    return allEventSchedules.stream().map(EventScheduleResponse::of).toList();
  }

  /**
   * 이벤트 스케줄 수정 기능
   *
   * @param eventScheduleUpdateRequest
   */
  @Transactional
  public void updateEventStartDay(EventScheduleUpdateRequest eventScheduleUpdateRequest) {
    List<EventDayInfo> eventSchedules = new ArrayList<>();
    eventDayInfoRepository.deleteAll();
    LocalDate date = eventScheduleUpdateRequest.getStartDate();
    for (int i = 0; i < SCHEDULE_LENGTH; i++) {
      EventDayInfo eventDayInfo = new EventDayInfo(i + 1, date.plusDays(i));
      eventSchedules.add(eventDayInfo);
    }
    eventDayInfoRepository.saveAll(eventSchedules);
  }
}
