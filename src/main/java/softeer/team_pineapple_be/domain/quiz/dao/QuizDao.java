package softeer.team_pineapple_be.domain.quiz.dao;

import com.querydsl.jpa.impl.JPAQueryFactory;

import org.springframework.stereotype.Repository;

import java.time.LocalDate;

import lombok.RequiredArgsConstructor;
import softeer.team_pineapple_be.domain.quiz.domain.QuizInfo;

import static softeer.team_pineapple_be.domain.quiz.domain.QQuizContent.quizContent;
import static softeer.team_pineapple_be.domain.quiz.domain.QQuizInfo.quizInfo;

/**
 * QuizQuerydsl Dao
 */
@RequiredArgsConstructor
@Repository
public class QuizDao {
  private final JPAQueryFactory queryFactory;

  public QuizInfo getQuizInfoByDate(LocalDate date) {
    return queryFactory.selectFrom(quizInfo)
                       .join(quizContent)
                       .on(quizInfo.id.eq(quizContent.id))
                       .where(quizContent.quizDate.eq(date))
                       .fetchOne();
  }
}
