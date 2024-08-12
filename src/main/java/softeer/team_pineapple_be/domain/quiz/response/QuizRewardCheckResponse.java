package softeer.team_pineapple_be.domain.quiz.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 퀴즈 경품 수령 여부 응답
 */
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class QuizRewardCheckResponse {
  private Boolean rewarded;
}
