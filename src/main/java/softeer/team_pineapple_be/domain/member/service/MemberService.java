package softeer.team_pineapple_be.domain.member.service;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import softeer.team_pineapple_be.domain.member.domain.Member;
import softeer.team_pineapple_be.domain.member.exception.MemberErrorCode;
import softeer.team_pineapple_be.domain.member.repository.MemberRepository;
import softeer.team_pineapple_be.domain.member.response.MemberInfoResponse;
import softeer.team_pineapple_be.domain.quiz.service.QuizRedisService;
import softeer.team_pineapple_be.global.auth.service.AuthMemberService;
import softeer.team_pineapple_be.global.exception.RestApiException;

/**
 * 멤버 서비스
 */
@Service
@RequiredArgsConstructor
public class MemberService {
  private final MemberRepository memberRepository;
  private final AuthMemberService authMemberService;
  private final QuizRedisService quizRedisService;

  public MemberInfoResponse getMemberInfo() {
    Member member = memberRepository.findByPhoneNumber(authMemberService.getMemberPhoneNumber())
                                    .orElseThrow(() -> new RestApiException(MemberErrorCode.NO_MEMBER));
    return MemberInfoResponse.of(member, quizRedisService.wasParticipatedInQuiz(member.getPhoneNumber()));
  }
}
