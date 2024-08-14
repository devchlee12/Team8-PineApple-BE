package softeer.team_pineapple_be.domain.quiz.request;

import java.util.Map;

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
  private int quizId;
  private String quizDescription;
  private Map<String, String> quizQuestions;
}
