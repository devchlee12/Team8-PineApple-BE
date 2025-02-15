package softeer.team_pineapple_be.domain.fcfs.service;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.UUID;

import lombok.RequiredArgsConstructor;
import softeer.team_pineapple_be.domain.fcfs.domain.FcfsInfoEntity;
import softeer.team_pineapple_be.domain.fcfs.dto.FcfsInfo;
import softeer.team_pineapple_be.domain.fcfs.exception.FcfsErrorCode;
import softeer.team_pineapple_be.domain.fcfs.repository.FcfsInfoRepository;
import softeer.team_pineapple_be.global.exception.RestApiException;

/**
 * 선착순 서비스
 */
@Service
@RequiredArgsConstructor
public class FcfsService {
  private final static String FCFS_KEY = "fcfs_queue";
  private final static Long FCFS_LIMIT = 500L;
  private final RedisTemplate<String, String> redisTemplate;
  private final FcfsInfoRepository fcfsInfoRepository;

  /**
   * 선착순 큐 초기화
   */
  public void clearFcfsQueue() {
    redisTemplate.delete(FCFS_KEY);
  }

  /**
   * 선착순 등록하고 순서 알려주는 메서드 응답
   *
   * @return 0 -> 선착순 등수 안에 들지 못함// 나머지 양수 -> 경품 받을 수 있는 선착순에 들었고 등수가 몇등인지
   */
  public FcfsInfo getFirstComeFirstServe() {
    String uuid = UUID.randomUUID().toString();
    Long order = redisTemplate.opsForValue().increment(FCFS_KEY);
    if (order > FCFS_LIMIT) {
      order = 0L;
    }
    if (order > 0) {
      fcfsInfoRepository.save(new FcfsInfoEntity(uuid, order.intValue()));
    }
    return new FcfsInfo(uuid, order);
  }

  /**
   * 참가자의 ID로 선착순 등수를 알려주는 메서드
   *
   * @param participantId
   * @return 참가자의 등수
   */
  public Integer getParticipantOrder(String participantId) {
    FcfsInfoEntity fcfsInfoEntity = fcfsInfoRepository.findByParticipantId(participantId)
                                                      .orElseThrow(
                                                          () -> new RestApiException(FcfsErrorCode.NOT_FOR_REWARD));
    return fcfsInfoEntity.getSuccessOrder();
  }
}
