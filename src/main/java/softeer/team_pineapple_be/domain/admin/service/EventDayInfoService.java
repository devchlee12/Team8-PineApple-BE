package softeer.team_pineapple_be.domain.admin.service;

import org.springframework.stereotype.Service;

import java.time.LocalDate;

import lombok.RequiredArgsConstructor;
import softeer.team_pineapple_be.domain.admin.domain.EventDayInfo;
import softeer.team_pineapple_be.domain.admin.exception.AdminErrorCode;
import softeer.team_pineapple_be.domain.admin.repisotory.EventDayInfoRepository;
import softeer.team_pineapple_be.global.exception.RestApiException;

/**
 * 날짜와 이벤트
 */
@Service
@RequiredArgsConstructor
public class EventDayInfoService {
  private final EventDayInfoRepository eventDayInfoRepository;

  public LocalDate getEventDateOfEventDay(int day) {
    EventDayInfo eventDayInfo =
        eventDayInfoRepository.findById(day).orElseThrow(() -> new RestApiException(AdminErrorCode.NOT_EVENT_DAY));
    return eventDayInfo.getEventDate();
  }
}
