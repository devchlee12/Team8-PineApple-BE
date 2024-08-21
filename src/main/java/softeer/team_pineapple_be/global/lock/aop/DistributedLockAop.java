package softeer.team_pineapple_be.global.lock.aop;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import softeer.team_pineapple_be.global.lock.annotation.DistributedLock;
import softeer.team_pineapple_be.global.lock.util.CustomSpringELParser;

/**
 * 분산락 어노테이션 선언시 수행되는 AOP
 */
@Aspect
@Component
@RequiredArgsConstructor
@Slf4j
public class DistributedLockAop {
  private static final String REDISSON_LOCK_PREFIX = "LOCK:";

  private final RedissonClient redissonClient;
  private final AopForTransaction aopForTransaction;

  @Around("@annotation(softeer.team_pineapple_be.global.lock.annotation.DistributedLock)")
  public Object lock(final ProceedingJoinPoint joinPoint) throws Throwable {
    MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();
    DistributedLock distributedLock = methodSignature.getMethod().getAnnotation(DistributedLock.class);

    String key = REDISSON_LOCK_PREFIX +
        CustomSpringELParser.getDynamicValue(methodSignature.getParameterNames(), joinPoint.getArgs(),
            distributedLock.key());
    RLock rLock = redissonClient.getLock(key);
    try {
      boolean available =
          rLock.tryLock(distributedLock.waitTime(), distributedLock.leaseTime(), distributedLock.timeUnit());
      if (!available) {
        return false;
      }
      return aopForTransaction.proceed(joinPoint);
    } catch (InterruptedException e) {
      throw new InterruptedException();
    } finally {
      try {
        rLock.unlock();
      } catch (IllegalMonitorStateException e) {
        log.info("Redisson Lock Already Unlocked");
      }
    }
  }
}
