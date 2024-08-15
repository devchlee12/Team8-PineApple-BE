package softeer.team_pineapple_be.domain.quiz.domain;

import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import softeer.team_pineapple_be.domain.quiz.request.QuizModifyRequest;

/**
 * QuizContent의 엔티티 타입
 */
@Entity
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class QuizContent {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;

  @Column(nullable = false)
  private String quizDescription;

  @Column(nullable = false, name = "QUIZ_QUESTION_1")
  private String quizQuestion1;

  @Column(nullable = false, name = "QUIZ_QUESTION_2")
  private String quizQuestion2;

  @Column(nullable = false, name = "QUIZ_QUESTION_3")
  private String quizQuestion3;

  @Column(nullable = false, name = "QUIZ_QUESTION_4")
  private String quizQuestion4;

  @Column(nullable = false)
  private LocalDate quizDate;

  @Builder
  public QuizContent(String quizDescription, String quizQuestion1, String quizQuestion2, String quizQuestion3,
      String quizQuestion4, LocalDate quizDate) {
    this.quizDescription = quizDescription;
    this.quizQuestion1 = quizQuestion1;
    this.quizQuestion2 = quizQuestion2;
    this.quizQuestion3 = quizQuestion3;
    this.quizQuestion4 = quizQuestion4;
    this.quizDate = quizDate;
  }

  public void update(QuizModifyRequest quizModifyRequest) {
    this.quizDescription = quizModifyRequest.getQuizDescription();
    this.quizQuestion1 = quizModifyRequest.getQuizQuestions().get("1");
    this.quizQuestion2 = quizModifyRequest.getQuizQuestions().get("2");
    this.quizQuestion3 = quizModifyRequest.getQuizQuestions().get("3");
    this.quizQuestion4 = quizModifyRequest.getQuizQuestions().get("4");
  }
}
