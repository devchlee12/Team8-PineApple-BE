package softeer.team_pineapple_be.global.lock.aop;

import org.aspectj.lang.ProceedingJoinPoint;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * AOP 트랜잭션 분리
 */
@Component
public class AopForTransaction {
  @Transactional(propagation = Propagation.REQUIRES_NEW)
  public Object proceed(final ProceedingJoinPoint joinPoint) throws Throwable {
    return joinPoint.proceed();
  }
}
