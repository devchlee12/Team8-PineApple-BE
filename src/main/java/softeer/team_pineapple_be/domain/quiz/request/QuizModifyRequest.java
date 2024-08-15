package softeer.team_pineapple_be.domain.quiz.request;

import java.util.Map;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 퀴즈 수정 요청
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class QuizModifyRequest {
  @NotNull(message = "{quiz.description_required}")
  private String quizDescription;
  @NotNull(message = "{quiz.questions_required}")
  private Map<String, String> quizQuestions;
}
