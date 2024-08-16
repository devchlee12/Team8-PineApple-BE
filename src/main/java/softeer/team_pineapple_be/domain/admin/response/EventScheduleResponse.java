package softeer.team_pineapple_be.domain.admin.response;

import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import softeer.team_pineapple_be.domain.admin.domain.EventDayInfo;

/**
 * 이벤트 스케줄 응답
 */
@AllArgsConstructor
@Getter
@Setter
public class EventScheduleResponse {
  private LocalDate date;
  private Integer day;

  public static EventScheduleResponse of(EventDayInfo eventDayInfo) {
    return new EventScheduleResponse(eventDayInfo.getEventDate(), eventDayInfo.getEventDay());
  }
}
