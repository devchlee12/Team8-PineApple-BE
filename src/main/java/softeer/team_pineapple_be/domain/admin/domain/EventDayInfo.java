package softeer.team_pineapple_be.domain.admin.domain;

import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 해당 날짜가 이벤트 몇번째 날인지 기록
 */
@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class EventDayInfo {
  @Id
  @Column(nullable = false)
  private Integer eventDay;

  @Column(nullable = false)
  private LocalDate eventDate;
}
