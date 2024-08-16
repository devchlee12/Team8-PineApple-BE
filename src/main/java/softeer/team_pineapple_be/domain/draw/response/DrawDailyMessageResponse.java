package softeer.team_pineapple_be.domain.draw.response;

import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 일자별 응모 정보 응답 객체
 */
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class DrawDailyMessageResponse {
  private String winMessage;
  private String loseMessage;
  private String loseScenario;
  private String winImage;
  private String loseImage;
  private String commonScenario;
  private LocalDate drawDate;
}
