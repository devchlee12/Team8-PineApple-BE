package softeer.team_pineapple_be.domain.admin.request;

import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 이벤트
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class EventScheduleUpdateRequest {
  private LocalDate startDate;
}
