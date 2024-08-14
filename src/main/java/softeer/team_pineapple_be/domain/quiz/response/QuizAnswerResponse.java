package softeer.team_pineapple_be.domain.quiz.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import softeer.team_pineapple_be.domain.quiz.domain.QuizInfo;

/**
 * 퀴즈 답변 응답
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class QuizAnswerResponse {
  private Byte answerNum;
  private String quizImage;

  public static QuizAnswerResponse of(QuizInfo quizInfo) {
    return new QuizAnswerResponse(quizInfo.getAnswerNum(), quizInfo.getQuizImage());
  }
}
