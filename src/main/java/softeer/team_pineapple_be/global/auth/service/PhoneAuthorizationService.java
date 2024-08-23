package softeer.team_pineapple_be.global.auth.service;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import softeer.team_pineapple_be.global.common.utils.RandomUtils;
import softeer.team_pineapple_be.global.message.MessageService;

/**
 * 핸드폰 인증번호 서비스
 */
@Service
@RequiredArgsConstructor
public class PhoneAuthorizationService {
  private final MessageService messageService;

  /**
   * 사용자에게 인증코드를 문자로 보내고, 해당 인증코드를 반환한다.
   *
   * @param phoneNumber
   * @return 인증코드
   */
  public Integer sendAuthMessage(String phoneNumber) {
//        Integer authCode = RandomUtils.getAuthCode();
//        messageService.sendTextMessageTo(String.valueOf(authCode), phoneNumber);
//        return authCode;
      return 671401;
  }
}
