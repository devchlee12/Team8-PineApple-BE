package softeer.team_pineapple_be.domain.fcfs.batch;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import softeer.team_pineapple_be.domain.fcfs.service.FcfsService;

/**
 * 선착순 기능 관련 배치 처리
 */
@Component
@RequiredArgsConstructor
public class FcfsDailyBatch {
  private final FcfsService fcfsService;

  /**
   * 매일 12시에 선착순 큐 초기화
   */
  @Scheduled(cron = "0 0 12 * * *")
  public void initializesQueue() {
    fcfsService.clearFcfsQueue();
  }
}
