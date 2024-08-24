package softeer.team_pineapple_be.domain.quiz.service;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SetOperations;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

/**
 * 퀴즈 레디스 서비스
 */
@Service
@RequiredArgsConstructor
public class QuizRedisService {
  private static final String PARTICIPATED_KEY = "participated";
  private static final String REWARDED_KEY = "rewarded";
  private final RedisTemplate<String, String> redisTemplate;

  /**
   * 일자별 퀴즈 참여 정보, 상품 받은 정보 초기화
   */
  public void deleteParticipateInfo() {
    redisTemplate.delete(PARTICIPATED_KEY);
    redisTemplate.delete(REWARDED_KEY);
  }

  /**
   * 퀴즈 참여 등록
   *
   * @param memberPhoneNumber
   */
  public void participate(String memberPhoneNumber) {
    SetOperations<String, String> setOps = redisTemplate.opsForSet();
    setOps.add(PARTICIPATED_KEY, memberPhoneNumber);
  }

  /**
   * 당일 선착순 경품 당첨 여부 저장
   *
   * @param memberPhoneNumber
   */
  public void saveRewardWin(String memberPhoneNumber) {
    redisTemplate.opsForSet().add(REWARDED_KEY, memberPhoneNumber);
  }

  /**
   * 유저가 당일 선착순 상품을 받았는지 확인
   *
   * @param memberPhoneNumber
   * @return
   */
  public Boolean wasMemberWinRewardToday(String memberPhoneNumber) {
    return redisTemplate.opsForSet().isMember(REWARDED_KEY, memberPhoneNumber);
  }

  /**
   * 퀴즈 참여 여부 반환하는 메서드
   *
   * @param memberPhoneNumber
   * @return 오늘 퀴즈 참여 여부
   */
  public Boolean wasParticipatedInQuiz(String memberPhoneNumber) {
    SetOperations<String, String> setOps = redisTemplate.opsForSet();
    return setOps.isMember(PARTICIPATED_KEY, memberPhoneNumber);
  }
}
