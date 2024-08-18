package softeer.team_pineapple_be.domain.quiz.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * QuizHistory의 응답을 구성하는 클래스
 */
@AllArgsConstructor
@Getter
public class QuizHistoryResponse {

    private double[][] dayNRetentionAndDAU;
}
