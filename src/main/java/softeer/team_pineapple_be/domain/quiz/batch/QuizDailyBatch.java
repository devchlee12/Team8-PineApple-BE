package softeer.team_pineapple_be.domain.quiz.batch;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import softeer.team_pineapple_be.domain.quiz.service.QuizRedisService;

/**
 * 일자별 퀴즈 참여 정보 초기화 처리하는 클래스
 */
@Component
@RequiredArgsConstructor
public class QuizDailyBatch {
  private final QuizRedisService quizRedisService;

  /**
   * 매일 12시에 퀴즈 참여 정보 초기화
   */
  @Scheduled(cron = "0 0 12 * * *")
  public void quizDailyBatch() {
    quizRedisService.deleteParticipateInfo();
  }
}
