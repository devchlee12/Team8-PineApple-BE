package softeer.team_pineapple_be.domain.draw.request;

import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 일자별 응모 정보 수정 요청 객체
 */
@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
public class DrawDailyMessageModifyRequest {
  @NotNull(message = "{draw.win_message_required}")
  private String winMessage;
  @NotNull(message = "{draw.lose_message_required}")
  private String loseMessage;
  @NotNull(message = "{draw.lose_scenario_required}")
  private String loseScenario;
  @NotNull(message = "{draw.win_image_required}")
  private MultipartFile winImage;
  @NotNull(message = "{draw.lose_image_required}")
  private MultipartFile loseImage;
  @NotNull(message = "{draw.common_scenario_required}")
  private String commonScenario;
  @NotNull(message = "{draw.draw_date_required}")
  private LocalDate drawDate;
}
