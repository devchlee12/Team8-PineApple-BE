package softeer.team_pineapple_be.domain.quiz.request;

import org.hibernate.validator.constraints.Range;
import org.springframework.web.multipart.MultipartFile;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 퀴즈 정답 바꾸기 요청
 */
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class QuizInfoModifyRequest {
  @NotNull(message = "{quiz.answer_required}")
  @Range(min = 1, max = 4, message = "{quiz.answer_num_range}")
  private Byte answerNum;
  @NotNull(message = "{quiz.image_required}")
  private MultipartFile quizImage;
}
