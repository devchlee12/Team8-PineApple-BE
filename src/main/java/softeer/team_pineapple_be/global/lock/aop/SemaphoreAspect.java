package softeer.team_pineapple_be.global.lock.aop;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

import java.util.concurrent.Semaphore;

/**
 * hikariCP 데드락 방지 세마포어 AOP
 */
@Aspect
@Component
public class SemaphoreAspect {
  private final int MAX_CONNECTION_SIZE_ON_ONE_THREAD = 2;
  private final int hikariMaxPoolSize = 5;
  //pool size = Tn x (Cm - 1) + 1
  private final int MAX_THEAD_COUNT = (hikariMaxPoolSize - 1) / (MAX_CONNECTION_SIZE_ON_ONE_THREAD - 1);
  private final Semaphore semaphore = new Semaphore(MAX_THEAD_COUNT);

  @Around("@annotation(softeer.team_pineapple_be.global.lock.annotation.SemaphoreGuarded)")
  public Object controlConcurrency(ProceedingJoinPoint joinPoint) throws Throwable {
    try {
      semaphore.acquire();
      return joinPoint.proceed();
    } finally {
      semaphore.release();
    }
  }
}
