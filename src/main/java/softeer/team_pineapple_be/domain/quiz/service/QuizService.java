package softeer.team_pineapple_be.domain.quiz.service;

import org.springdoc.core.parsers.ReturnTypeParser;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;

import lombok.RequiredArgsConstructor;
import softeer.team_pineapple_be.domain.fcfs.dto.FcfsInfo;
import softeer.team_pineapple_be.domain.fcfs.service.FcfsService;
import softeer.team_pineapple_be.domain.member.domain.Member;
import softeer.team_pineapple_be.domain.member.exception.MemberErrorCode;
import softeer.team_pineapple_be.domain.member.repository.MemberRepository;
import softeer.team_pineapple_be.domain.member.response.MemberInfoResponse;
import softeer.team_pineapple_be.domain.quiz.domain.QuizContent;
import softeer.team_pineapple_be.domain.quiz.domain.QuizHistory;
import softeer.team_pineapple_be.domain.quiz.domain.QuizInfo;
import softeer.team_pineapple_be.domain.quiz.domain.QuizReward;
import softeer.team_pineapple_be.domain.quiz.exception.QuizErrorCode;
import softeer.team_pineapple_be.domain.quiz.repository.QuizContentRepository;
import softeer.team_pineapple_be.domain.quiz.repository.QuizHistoryRepository;
import softeer.team_pineapple_be.domain.quiz.repository.QuizInfoRepository;
import softeer.team_pineapple_be.domain.quiz.repository.QuizRewardRepository;
import softeer.team_pineapple_be.domain.quiz.request.QuizInfoRequest;
import softeer.team_pineapple_be.domain.quiz.request.QuizModifyRequest;
import softeer.team_pineapple_be.domain.quiz.response.QuizContentResponse;
import softeer.team_pineapple_be.domain.quiz.response.QuizInfoResponse;
import softeer.team_pineapple_be.domain.quiz.response.QuizRewardCheckResponse;
import softeer.team_pineapple_be.domain.quiz.response.QuizSuccessInfoResponse;
import softeer.team_pineapple_be.global.auth.service.AuthMemberService;
import softeer.team_pineapple_be.global.exception.RestApiException;
import softeer.team_pineapple_be.global.message.MessageService;

//TODO: 예외처리(구조 맞추기 위해 남겨둠)

/**
 * QuizContent의 요청에 대한 처리를 담당하는 클래스
 */
@Service
@RequiredArgsConstructor
public class QuizService {

  private static final int FCFS_FAILED_ORDER = 501;
  private final QuizContentRepository quizContentRepository;
  private final QuizInfoRepository quizInfoRepository;
  private final QuizHistoryRepository quizHistoryRepository;
  private final MemberRepository memberRepository;  //멤버 서비스가 생성될 시에 리팩토링
  private final AuthMemberService authMemberService;
  private final FcfsService fcfsService;
  private final QuizRewardRepository quizRewardRepository;
  private final MessageService messageService;
  private final QuizRedisService quizRedisService;
  private final ReturnTypeParser genericReturnTypeParser;

  /**
   * 현재 날짜에 대한 이벤트 내용을 전송해주는 메서드
   *
   * @return 현재 날짜의 이벤트 내용
   */
  @Transactional(readOnly = true)
  public QuizContentResponse getQuizContent() {
    QuizContent quizContent = quizContentRepository.findByQuizDate(determineQuizDate())
                                                   .orElseThrow(
                                                       () -> new RestApiException(QuizErrorCode.NO_QUIZ_CONTENT));
    return QuizContentResponse.of(quizContent);
  }

  /**
   * 날짜 별 이벤트 내용 전송해주는 메서드
   *
   * @param date
   * @return 요청한 날짜에 해당하는 이벤트 내용
   */
  @Transactional(readOnly = true)
  public QuizContentResponse getQuizContentOfDate(LocalDate date) {
    QuizContent quizContent =
        quizContentRepository.findByQuizDate(date).orElseThrow(() -> new RestApiException(QuizErrorCode.NO_QUIZ_INFO));
    return QuizContentResponse.of(quizContent);
  }

  /**
   * 퀴즈 선착순 상품 이미지 전송
   *
   * @param participantId
   */
  @Transactional
  public void getQuizReward(String participantId) {
    String memberPhoneNumber = authMemberService.getMemberPhoneNumber();
    if (quizRedisService.wasMemberWinRewardToday(memberPhoneNumber)) {
      throw new RestApiException(QuizErrorCode.ALREADY_WIN_REWARD_TODAY);
    }
    Integer participantOrder = fcfsService.getParticipantOrder(participantId);
    QuizReward quizReward = quizRewardRepository.findBySuccessOrder(participantOrder)
                                                .orElseThrow(() -> new RestApiException(QuizErrorCode.NO_QUIZ_REWARD));
    quizReward.invalidate();
    messageService.sendPrizeImage(quizReward.getRewardImage());
    quizRedisService.saveRewardWin(authMemberService.getMemberPhoneNumber());
  }

  /**
   * 유저가 선착순 경품을 받았는지 여부를 리턴하는 메서드
   *
   * @return 경품 수령 여부 응답 객체
   */
  @Transactional(readOnly = true)
  public QuizRewardCheckResponse isMemberRewardedToday() {
    String memberPhoneNumber = authMemberService.getMemberPhoneNumber();
    Boolean isRewarded = quizRedisService.wasMemberWinRewardToday(memberPhoneNumber);
    return new QuizRewardCheckResponse(isRewarded);
  }

  /**
   * 퀴즈 문제 수정 메서드
   *
   * @param quizModifyRequest
   */
  @Transactional
  public void modifyQuizContent(QuizModifyRequest quizModifyRequest) {
    QuizContent quizContent = quizContentRepository.findById(quizModifyRequest.getQuizId())
                                                   .orElseThrow(
                                                       () -> new RestApiException(QuizErrorCode.NO_QUIZ_CONTENT));
    quizContent.update(quizModifyRequest);
  }

  /**
   * 퀴즈 참여 여부를 저장하는 메서드
   *
   * @return 참여 여부가 등록된 사용자의 툴박스 개수
   */
  @Transactional
  public MemberInfoResponse quizHistory() {
    String phoneNumber = authMemberService.getMemberPhoneNumber(); // 세션 없을 시 여기서 검증됨
    Member member = memberRepository.findByPhoneNumber(phoneNumber)
                                    .orElseThrow(() -> new RestApiException(MemberErrorCode.NO_MEMBER));
    QuizContent quizContent = quizContentRepository.findByQuizDate(determineQuizDate())
                                                   .orElseThrow(
                                                       () -> new RestApiException(QuizErrorCode.NO_QUIZ_CONTENT));
    quizHistoryRepository.findByMemberPhoneNumberAndQuizContentId(phoneNumber, quizContent.getId())
                         .ifPresent(quizHistory -> {
                           throw new RestApiException(QuizErrorCode.PARTICIPATION_EXISTS);
                         });

    member.incrementToolBoxCnt();
    memberRepository.save(member);
    QuizHistory quizHistory = new QuizHistory(member, quizContent);
    quizHistoryRepository.save(quizHistory);
    quizRedisService.participate(authMemberService.getMemberPhoneNumber());
    return MemberInfoResponse.of(member, true);
  }

  /**
   * 퀴즈 정답에 대한 여부를 판단하고 선착순 정보와 내용을 전송해주는 메서드
   *
   * @param quizInfoRequest 퀴즈 번호와 사용자가 제출한 정답을 받아오기 위한 객체
   * @return 정답 안내 정보에 대한 내용
   */
  @Transactional
  public QuizInfoResponse quizIsCorrect(QuizInfoRequest quizInfoRequest) {
    QuizInfo quizInfo = quizInfoRepository.findById(quizInfoRequest.getQuizId())
                                          .orElseThrow(() -> new RestApiException(QuizErrorCode.NO_QUIZ_INFO));
    if (!quizInfoRequest.getAnswerNum().equals(quizInfo.getAnswerNum())) {
      return QuizInfoResponse.of(quizInfo, false);
    }
    FcfsInfo fcfsInfo = fcfsService.getFirstComeFirstServe();
    if (fcfsInfo.order() < 1) {
      return new QuizSuccessInfoResponse(true, quizInfo.getQuizImage(), "NULL", FCFS_FAILED_ORDER);
    }
    return new QuizSuccessInfoResponse(true, quizInfo.getQuizImage(), fcfsInfo.uuid(), fcfsInfo.order().intValue());
  }

  private LocalDate determineQuizDate() {
    LocalTime onePm = LocalTime.of(13, 0);
    LocalTime atNoon = LocalTime.of(12, 0);
    LocalTime now = LocalTime.now();
    // 현재 시간이 12시 이전인지 확인
    if (now.isBefore(atNoon)) {
      return LocalDate.now().minusDays(1);
    }

    if (now.isBefore(onePm) && now.isAfter(atNoon)) {
      throw new RestApiException(QuizErrorCode.NO_QUIZ_CONTENT);
    }

    return LocalDate.now();
  }
}
