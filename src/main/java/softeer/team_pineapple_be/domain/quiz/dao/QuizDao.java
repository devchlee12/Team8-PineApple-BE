package softeer.team_pineapple_be.domain.quiz.dao;

import com.querydsl.jpa.impl.JPAQueryFactory;

import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;

import lombok.RequiredArgsConstructor;
import softeer.team_pineapple_be.domain.quiz.domain.QQuizInfo;
import softeer.team_pineapple_be.domain.quiz.domain.QuizInfo;

import static softeer.team_pineapple_be.domain.quiz.domain.QQuizContent.quizContent;

/**
 * QuizQuerydsl Dao
 */
@RequiredArgsConstructor
@Repository
public class QuizDao {
  private final JPAQueryFactory queryFactory;

  public Optional<QuizInfo> getQuizInfoByDate(LocalDate date) {
    QuizInfo quizInfo = queryFactory.selectFrom(QQuizInfo.quizInfo)
                                    .join(quizContent)
                                    .on(QQuizInfo.quizInfo.id.eq(quizContent.id))
                                    .where(quizContent.quizDate.eq(date))
                                    .fetchOne();
    return Optional.ofNullable(quizInfo);
  }
}
