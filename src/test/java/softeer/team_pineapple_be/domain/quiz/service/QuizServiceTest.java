package softeer.team_pineapple_be.domain.quiz.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.springframework.web.multipart.MultipartFile;
import softeer.team_pineapple_be.domain.draw.exception.DrawErrorCode;
import softeer.team_pineapple_be.domain.fcfs.dto.FcfsInfo;
import softeer.team_pineapple_be.domain.fcfs.service.FcfsService;
import softeer.team_pineapple_be.domain.member.domain.Member;
import softeer.team_pineapple_be.domain.member.exception.MemberErrorCode;
import softeer.team_pineapple_be.domain.member.repository.MemberRepository;
import softeer.team_pineapple_be.domain.member.response.MemberInfoResponse;
import softeer.team_pineapple_be.domain.quiz.dao.QuizDao;
import softeer.team_pineapple_be.domain.quiz.domain.QuizContent;
import softeer.team_pineapple_be.domain.quiz.domain.QuizHistory;
import softeer.team_pineapple_be.domain.quiz.domain.QuizInfo;
import softeer.team_pineapple_be.domain.quiz.domain.QuizReward;
import softeer.team_pineapple_be.domain.quiz.exception.QuizErrorCode;
import softeer.team_pineapple_be.domain.quiz.repository.QuizContentRepository;
import softeer.team_pineapple_be.domain.quiz.repository.QuizHistoryRepository;
import softeer.team_pineapple_be.domain.quiz.repository.QuizInfoRepository;
import softeer.team_pineapple_be.domain.quiz.repository.QuizRewardRepository;
import softeer.team_pineapple_be.domain.quiz.request.QuizInfoModifyRequest;
import softeer.team_pineapple_be.domain.quiz.request.QuizInfoRequest;
import softeer.team_pineapple_be.domain.quiz.request.QuizModifyRequest;
import softeer.team_pineapple_be.domain.quiz.response.QuizContentResponse;
import softeer.team_pineapple_be.domain.quiz.response.QuizInfoResponse;
import softeer.team_pineapple_be.domain.quiz.response.QuizRewardCheckResponse;
import softeer.team_pineapple_be.domain.quiz.response.QuizSuccessInfoResponse;
import softeer.team_pineapple_be.global.auth.service.AuthMemberService;
import softeer.team_pineapple_be.global.cloud.service.S3DeleteService;
import softeer.team_pineapple_be.global.cloud.service.S3UploadService;
import softeer.team_pineapple_be.global.exception.RestApiException;
import softeer.team_pineapple_be.global.message.MessageService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

public class QuizServiceTest {

  @InjectMocks
  private QuizService quizService;

  @Mock
  private QuizContentRepository quizContentRepository;

  @Mock
  private QuizInfoRepository quizInfoRepository;

  @Mock
  private QuizHistoryRepository quizHistoryRepository;

  @Mock
  private MemberRepository memberRepository;

  @Mock
  private AuthMemberService authMemberService;

  @Mock
  private FcfsService fcfsService;

  @Mock
  private QuizRedisService quizRedisService;

  @Mock
  private QuizRewardRepository quizRewardRepository; // 의존성 모의
  @Mock
  private MessageService messageService; // 의존성 모의

  @Mock
  private S3UploadService s3UploadService;

  @Mock
  private S3DeleteService s3DeleteService;

  @Mock
  private QuizDao quizDao;

  private QuizContent quizContent;
  private Member member;
  private Integer quizId;
  private Byte correctAnswerNum;
  private Byte incorrectAnswerNum;
  private String phoneNumber;
  private String quizImage;
  private String participantId;
  private Integer successOrder;
  private Long order;
  private QuizReward quizReward;

  @BeforeEach
  void setUp() {
    MockitoAnnotations.openMocks(this);
    quizId = 1;
    correctAnswerNum = (byte) 1;
    incorrectAnswerNum = (byte) 2;
    phoneNumber = "010-1234-5678";
    quizImage = "quiz_image.png";
    participantId = "testParticipantId";
    successOrder = 1;
    order = 1L;
    quizContent = new QuizContent(1, "퀴즈 설명",           // quizDescription
            "첫 번째 질문",       // quizQuestion1
            "두 번째 질문",       // quizQuestion2
            "세 번째 질문",       // quizQuestion3
            "네 번째 질문",       // quizQuestion4
            LocalDate.now()      // quizDate
    );
    quizReward = new QuizReward(1, "prizeImageUrl", LocalDate.now());
    member = new Member(phoneNumber);
  }

  @Test
  @DisplayName("12~13시 사이의 퀴즈 참여 시간이 아니어서 에러가 발생되는 결과 테스트 - FailureCase")
  void getQuizContent_NotQuizTime_ReturnsContent() {
    // Given
    LocalTime yesterDayQuizTime = LocalTime.of(12, 30);
    LocalTime onePm = LocalTime.of(13, 0);
    LocalTime atNoon = LocalTime.of(12, 0);

    try (MockedStatic<LocalTime> mockedStatic = mockStatic(LocalTime.class)) {
      mockedStatic.when(LocalTime::now).thenReturn(yesterDayQuizTime);
      mockedStatic.when(() -> LocalTime.of(12, 0)).thenReturn(atNoon);
      mockedStatic.when(() -> LocalTime.of(13, 0)).thenReturn(onePm);
      when(quizContentRepository.findByQuizDate(any())).thenReturn(Optional.of(quizContent));
      // When
      assertThatThrownBy(() -> {
        quizService.getQuizContent();
      }).isInstanceOf(RestApiException.class).satisfies(exception -> {
        RestApiException restApiException = (RestApiException) exception; // 캐스팅
        assertThat(restApiException.getErrorCode()).isEqualTo(QuizErrorCode.NO_QUIZ_CONTENT);
      });

    }
  }

  @Test
  @DisplayName("퀴즈 컨텐츠가 성공적으로 반한되지 못한 결과 테스트 - FailureCase")
  void getQuizContent_QuizContentNotFound_ThrowsRestApiException() {
    LocalTime quizTime = LocalTime.of(15, 30);
    LocalTime onePm = LocalTime.of(13, 0);
    LocalTime atNoon = LocalTime.of(12, 0);
    try (MockedStatic<LocalTime> mockedStatic = mockStatic(LocalTime.class)) {
      mockedStatic.when(LocalTime::now).thenReturn(quizTime);
      mockedStatic.when(() -> LocalTime.of(12, 0)).thenReturn(atNoon);
      mockedStatic.when(() -> LocalTime.of(13, 0)).thenReturn(onePm);
      // Given
      when(quizContentRepository.findByQuizDate(any())).thenReturn(Optional.empty());

      // When & Then: 예외가 발생하는지 확인
      assertThatThrownBy(() -> {
        quizService.getQuizContent();
      }).isInstanceOf(RestApiException.class).satisfies(exception -> {
        RestApiException restApiException = (RestApiException) exception; // 캐스팅
        assertThat(restApiException.getErrorCode()).isEqualTo(QuizErrorCode.NO_QUIZ_CONTENT);
      });
    }
  }

  @Test
  @DisplayName("13시 이후 퀴즈 컨텐츠가 성공적으로 반한된 결과 테스트 - SuccessCase")
  void getQuizContent_TodayQuizContentExists_ReturnsContent() {
    // Given
    LocalTime yesterDayQuizTime = LocalTime.of(15, 0, 0, 0);
    LocalTime onePm = LocalTime.of(13, 0);
    LocalTime atNoon = LocalTime.of(12, 0);

    try (MockedStatic<LocalTime> mockedStatic = mockStatic(LocalTime.class)) {
      mockedStatic.when(LocalTime::now).thenReturn(yesterDayQuizTime);
      mockedStatic.when(() -> LocalTime.of(12, 0)).thenReturn(atNoon);
      mockedStatic.when(() -> LocalTime.of(13, 0)).thenReturn(onePm);
      when(quizContentRepository.findByQuizDate(any())).thenReturn(Optional.of(quizContent));
      // When
      QuizContentResponse response = quizService.getQuizContent();

      // Then
      assertThat(response).isNotNull();
      assertThat(response.getQuizDescription()).isEqualTo(quizContent.getQuizDescription());
      assertThat(response.getQuizQuestions().get(1)).isEqualTo(quizContent.getQuizQuestion1());
      assertThat(response.getQuizQuestions().get(2)).isEqualTo(quizContent.getQuizQuestion2());
      assertThat(response.getQuizQuestions().get(3)).isEqualTo(quizContent.getQuizQuestion3());
      assertThat(response.getQuizQuestions().get(4)).isEqualTo(quizContent.getQuizQuestion4());

    }
  }

  @Test
  @DisplayName("12시 이전 퀴즈 컨텐츠가 성공적으로 반한된 결과 테스트 - SuccessCase")
  void getQuizContent_YesterdayQuizContentExists_ReturnsContent() {
    // Given
    LocalTime yesterdayQuizTime = LocalTime.of(8, 0, 0, 0);
    LocalTime onePm = LocalTime.of(13, 0);
    LocalTime atNoon = LocalTime.of(12, 0);

    try (MockedStatic<LocalTime> mockedStatic = mockStatic(LocalTime.class)) {
      mockedStatic.when(LocalTime::now).thenReturn(yesterdayQuizTime);
      mockedStatic.when(() -> LocalTime.of(12, 0)).thenReturn(atNoon);
      mockedStatic.when(() -> LocalTime.of(13, 0)).thenReturn(onePm);
      when(quizContentRepository.findByQuizDate(any())).thenReturn(Optional.of(quizContent));
      // When
      QuizContentResponse response = quizService.getQuizContent();

      // Then
      assertThat(response).isNotNull();
      assertThat(response.getQuizDescription()).isEqualTo(quizContent.getQuizDescription());
      assertThat(response.getQuizQuestions().get(1)).isEqualTo(quizContent.getQuizQuestion1());
      assertThat(response.getQuizQuestions().get(2)).isEqualTo(quizContent.getQuizQuestion2());
      assertThat(response.getQuizQuestions().get(3)).isEqualTo(quizContent.getQuizQuestion3());
      assertThat(response.getQuizQuestions().get(4)).isEqualTo(quizContent.getQuizQuestion4());

    }
  }

  @Test
  @DisplayName("퀴즈 참여기록을 조회하려고 했으나 유저가 존재하지 않는 경우- FailureCase")
  void quizHistory_MemberDoesNotExist_ThrowsRestApiException() {
    LocalTime quizTime = LocalTime.of(15, 30);
    LocalTime onePm = LocalTime.of(13, 0);
    LocalTime atNoon = LocalTime.of(12, 0);
    try (MockedStatic<LocalTime> mockedStatic = mockStatic(LocalTime.class)) {
      mockedStatic.when(LocalTime::now).thenReturn(quizTime);
      mockedStatic.when(() -> LocalTime.of(12, 0)).thenReturn(atNoon);
      mockedStatic.when(() -> LocalTime.of(13, 0)).thenReturn(onePm);
      // Given
      when(authMemberService.getMemberPhoneNumber()).thenReturn(phoneNumber);
      when(quizContentRepository.findByQuizDate(any())).thenReturn(Optional.empty());

      // When & Then
      assertThatThrownBy(() -> quizService.quizHistory()).isInstanceOf(RestApiException.class).satisfies(exception -> {
        RestApiException restApiException = (RestApiException) exception; // 캐스팅
        assertThat(restApiException.getErrorCode()).isEqualTo(MemberErrorCode.NO_MEMBER);
      });
    }
  }

  @Test
  @DisplayName("퀴즈 참여기록이 이미 존재할 때 결과 테스트- FailureCase")
  void quizHistory_ParticipationExists_ThrowsRestApiException() {
    LocalTime quizTime = LocalTime.of(15, 30);
    LocalTime onePm = LocalTime.of(13, 0);
    LocalTime atNoon = LocalTime.of(12, 0);
    try (MockedStatic<LocalTime> mockedStatic = mockStatic(LocalTime.class)) {
      mockedStatic.when(LocalTime::now).thenReturn(quizTime);
      mockedStatic.when(() -> LocalTime.of(12, 0)).thenReturn(atNoon);
      mockedStatic.when(() -> LocalTime.of(13, 0)).thenReturn(onePm);
      // Given
      when(authMemberService.getMemberPhoneNumber()).thenReturn(phoneNumber);
      when(memberRepository.findByPhoneNumber(phoneNumber)).thenReturn(Optional.of(member));
      when(quizContentRepository.findByQuizDate(any())).thenReturn(Optional.of(quizContent));
      when(quizHistoryRepository.findByMemberPhoneNumberAndQuizContentId(phoneNumber, quizContent.getId())).thenReturn(
              Optional.of(new QuizHistory())); // 참여 이력 존재

      // When & Then
      assertThatThrownBy(() -> quizService.quizHistory()).isInstanceOf(RestApiException.class).satisfies(exception -> {
        RestApiException restApiException = (RestApiException) exception; // 캐스팅
        assertThat(restApiException.getErrorCode()).isEqualTo(QuizErrorCode.PARTICIPATION_EXISTS);
      });
    }
  }

  @Test
  @DisplayName("퀴즈 참여기록을 조회하려고 했으나 퀴즈 컨텐츠가 존재하지 않는 경우- FailureCase")
  void quizHistory_QuizContentDoesNotExist_ThrowsRestApiException() {
    LocalTime quizTime = LocalTime.of(15, 30);
    LocalTime onePm = LocalTime.of(13, 0);
    LocalTime atNoon = LocalTime.of(12, 0);
    try (MockedStatic<LocalTime> mockedStatic = mockStatic(LocalTime.class)) {
      mockedStatic.when(LocalTime::now).thenReturn(quizTime);
      mockedStatic.when(() -> LocalTime.of(12, 0)).thenReturn(atNoon);
      mockedStatic.when(() -> LocalTime.of(13, 0)).thenReturn(onePm);
      // Given
      when(authMemberService.getMemberPhoneNumber()).thenReturn(phoneNumber);
      when(memberRepository.findByPhoneNumber(phoneNumber)).thenReturn(Optional.of(member));
      when(quizContentRepository.findByQuizDate(any())).thenReturn(Optional.empty());

      // When & Then
      assertThatThrownBy(() -> quizService.quizHistory()).isInstanceOf(RestApiException.class).satisfies(exception -> {
        RestApiException restApiException = (RestApiException) exception; // 캐스팅
        assertThat(restApiException.getErrorCode()).isEqualTo(QuizErrorCode.NO_QUIZ_CONTENT);
      });
    }
  }

  @Test
  @DisplayName("퀴즈 참여기록을 성공적으로 저장했을 때 결과 테스트- SuccessCase")
  void quizHistory_QuizContentExists_And_NoParticipation_ReturnsMemberInfoResponse() {
    LocalTime quizTime = LocalTime.of(15, 30);
    LocalTime onePm = LocalTime.of(13, 0);
    LocalTime atNoon = LocalTime.of(12, 0);
    try (MockedStatic<LocalTime> mockedStatic = mockStatic(LocalTime.class)) {
      mockedStatic.when(LocalTime::now).thenReturn(quizTime);
      mockedStatic.when(() -> LocalTime.of(12, 0)).thenReturn(atNoon);
      mockedStatic.when(() -> LocalTime.of(13, 0)).thenReturn(onePm);
      // Given
      when(authMemberService.getMemberPhoneNumber()).thenReturn(phoneNumber);
      when(quizContentRepository.findByQuizDate(any())).thenReturn(Optional.of(quizContent));
      when(quizHistoryRepository.findByMemberPhoneNumberAndQuizContentId(phoneNumber, quizContent.getId())).thenReturn(
              Optional.empty());
      when(memberRepository.findByPhoneNumber(phoneNumber)).thenReturn(Optional.of(member));
      when(quizRedisService.wasParticipatedInQuiz(member.getPhoneNumber())).thenReturn(false);


      // When
      MemberInfoResponse response = quizService.quizHistory();

      // Then
      assertThat(response).isNotNull();
      assertThat(response.getPhoneNumber()).isEqualTo(phoneNumber);
      assertThat(member.getToolBoxCnt()).isEqualTo(1); // 툴박스 카운트 증가 확인
      verify(quizHistoryRepository).save(any(QuizHistory.class)); // 퀴즈 이력 저장 확인
    }
  }

  @Test
  @DisplayName("퀴즈 정답이 맞았고, 선착순일 때 결과 테스트 - SuccessCase")
  void quizIsCorrect_CorrectAnswerAndFcfs_ReturnsQuizSuccessInfoResponse() {
    // Given
    QuizContent quizContent = new QuizContent(); // QuizContent 객체 생성
    QuizInfo quizInfo = new QuizInfo(quizId, quizContent, correctAnswerNum, quizImage);
    QuizInfoRequest request = new QuizInfoRequest(quizId, correctAnswerNum);
    when(quizInfoRepository.findById(quizId)).thenReturn(Optional.of(quizInfo));
    when(fcfsService.getFirstComeFirstServe()).thenReturn(new FcfsInfo(participantId, order));

    // When
    QuizSuccessInfoResponse response = (QuizSuccessInfoResponse) quizService.quizIsCorrect(request);

    // Then
    assertThat(response.getIsCorrect()).isTrue();
    assertThat(response.getQuizImage()).isEqualTo(quizInfo.getQuizImage());
    assertThat(response.getQuizParticipantId()).isEqualTo(participantId);
    assertThat(response.getSuccessOrder()).isEqualTo(order.intValue());
  }

  @Test
  @DisplayName("퀴즈 정답이 맞았고, 선착순이 아닐 때 결과 테스트 - SuccessCase")
  void quizIsCorrect_CorrectAnswerAndNotFcfs_ReturnsQuizSuccessInfoResponse() {
    // Given
    QuizContent quizContent = new QuizContent(); // QuizContent 객체 생성
    QuizInfo quizInfo = new QuizInfo(quizId, quizContent, correctAnswerNum, quizImage);
    QuizInfoRequest request = new QuizInfoRequest(quizId, correctAnswerNum);
    when(quizInfoRepository.findById(quizId)).thenReturn(Optional.of(quizInfo));
    when(fcfsService.getFirstComeFirstServe()).thenReturn(new FcfsInfo("testUUID", 0L));

    // When
    QuizSuccessInfoResponse response = (QuizSuccessInfoResponse) quizService.quizIsCorrect(request);

    // Then
    assertThat(response.getIsCorrect()).isTrue();
    assertThat(response.getQuizImage()).isEqualTo(quizInfo.getQuizImage());
    assertThat(response.getQuizParticipantId()).isEqualTo("NULL");
    assertThat(response.getSuccessOrder()).isEqualTo(501);
  }

  @Test
  @DisplayName("퀴즈 정답이 틀렸을 때 결과 테스트 - FailureCase")
  void quizIsCorrect_IncorrectAnswer_ReturnsFalse() {
    // Given
    QuizContent quizContent = new QuizContent();
    QuizInfo quizInfo = new QuizInfo(quizId, quizContent, correctAnswerNum, quizImage);
    QuizInfoRequest request = new QuizInfoRequest(quizId, incorrectAnswerNum);
    when(quizInfoRepository.findById(quizId)).thenReturn(Optional.of(quizInfo));

    // When
    QuizInfoResponse response = quizService.quizIsCorrect(request);

    // Then
    assertThat(response.getIsCorrect()).isFalse();
    assertThat(response.getQuizImage()).isEqualTo(quizInfo.getQuizImage());
  }

  @Test
  @DisplayName("퀴즈 정답 제출 시 해당하는 퀴즈가 없을 때 테스트 - FailureCase")
  void quizIsCorrect_QuizNotFound_ThrowsRestApiException() {
    // Given
    QuizInfoRequest request = new QuizInfoRequest(quizId, correctAnswerNum);
    when(quizInfoRepository.findById(quizId)).thenReturn(Optional.empty());

    // When & Then
    assertThatThrownBy(() -> {
      quizService.quizIsCorrect(request);
    }).isInstanceOf(RestApiException.class).satisfies(exception -> {
      RestApiException restApiException = (RestApiException) exception; // 캐스팅
      assertThat(restApiException.getErrorCode()).isEqualTo(QuizErrorCode.NO_QUIZ_INFO);
    });
  }

  @Test
  void getQuizContentOfDate_ExistingQuizContent_ShouldReturnResponse() {
    // Given
    LocalDate date = LocalDate.now();
    when(quizContentRepository.findByQuizDate(date)).thenReturn(Optional.of(quizContent));

    // When
    QuizContentResponse response = quizService.getQuizContentOfDate(date);

    // Then
    assertThat(response).isNotNull();
    assertThat(response.getQuizQuestions().get(1)).isEqualTo("첫 번째 질문");
  }

  @Test
  void getQuizContentOfDate_NoQuizContent_ShouldThrowException() {
    // Given
    LocalDate date = LocalDate.now();
    when(quizContentRepository.findByQuizDate(date)).thenReturn(Optional.empty());

    // When & Then
    assertThatThrownBy(() -> quizService.getQuizContentOfDate(date))
            .isInstanceOf(RestApiException.class);
  }

  @Test
  void getQuizReward_AlreadyRewardedToday_ShouldThrowException() {
    // Given
    when(authMemberService.getMemberPhoneNumber()).thenReturn(phoneNumber);
    when(quizRedisService.wasMemberWinRewardToday(phoneNumber)).thenReturn(true);

    // When & Then
    assertThatThrownBy(() -> quizService.getQuizReward(participantId))
            .isInstanceOf(RestApiException.class);
  }

  @Test
  void getQuizReward_ValidReward_ShouldProcessReward() {
    // Given
    when(authMemberService.getMemberPhoneNumber()).thenReturn(phoneNumber);
    when(quizRedisService.wasMemberWinRewardToday(phoneNumber)).thenReturn(false);
    when(fcfsService.getParticipantOrder(participantId)).thenReturn(successOrder);
    when(quizRewardRepository.findBySuccessOrderAndQuizDate(successOrder, LocalDate.now())).thenReturn(Optional.of(quizReward));

    // When
    quizService.getQuizReward(participantId);

    // Then
    verify(quizRedisService).saveRewardWin(phoneNumber);
  }

  @Test
  void isMemberRewardedToday_ShouldReturnRewardStatus() {
    // Given
    when(authMemberService.getMemberPhoneNumber()).thenReturn(phoneNumber);
    when(quizRedisService.wasMemberWinRewardToday(phoneNumber)).thenReturn(true);

    // When
    QuizRewardCheckResponse response = quizService.isMemberRewardedToday();

    // Then
    assertThat(response.getRewarded()).isTrue();
  }

  @Test
  void modifyOrSaveQuizContent_NewContent_ShouldSaveQuizContent() {
    // Given
    LocalDate date = LocalDate.now();
    QuizModifyRequest quizModifyRequest = new QuizModifyRequest();
    quizModifyRequest.setQuizDescription("New Description");
    quizModifyRequest.setQuizQuestions(Map.of(
            "1", "첫 번째 질문",
            "2", "두 번째 질문",
            "3", "세 번째 질문",
            "4", "네 번째 질문"
    ));

    when(quizContentRepository.findByQuizDate(date)).thenReturn(Optional.empty());

    // When
    quizService.modifyOrSaveQuizContent(date, quizModifyRequest);

    // Then
    verify(quizContentRepository).save(any(QuizContent.class));
  }

  @Test
  void modifyOrSaveQuizContent_ExistingContent_ShouldUpdateQuizContent() {
    // Given
    LocalDate date = LocalDate.now();
    QuizModifyRequest quizModifyRequest = new QuizModifyRequest();
    quizModifyRequest.setQuizDescription("Updated Description");
    Map<String, String> questions = new HashMap<>();
    questions.put("1", "첫 번째 질문");
    questions.put("2", "두 번째 질문");
    quizModifyRequest.setQuizQuestions(questions);
    when(quizContentRepository.findByQuizDate(date)).thenReturn(Optional.of(quizContent));

    // When
    quizService.modifyOrSaveQuizContent(date, quizModifyRequest);

    // Then
    assertThat(quizContent.getQuizDescription()).isEqualTo("Updated Description");
    assertThat(quizContent.getQuizQuestion1()).isEqualTo("첫 번째 질문");
  }

  @Test
  void modifyOrSaveQuizInfo_NewQuizInfo_ShouldSaveQuizInfo() {
    // Given
    LocalDate day = LocalDate.now();
    QuizInfoModifyRequest quizInfoModifyRequest = new QuizInfoModifyRequest();
    quizInfoModifyRequest.setAnswerNum(correctAnswerNum);
    quizInfoModifyRequest.setQuizImage(mock(MultipartFile.class));
    when(quizDao.getQuizInfoByDate(day)).thenReturn(Optional.empty());
    when(quizContentRepository.findByQuizDate(day)).thenReturn(Optional.of(quizContent));

    // When
    quizService.modifyOrSaveQuizInfo(day, quizInfoModifyRequest);

    // Then
    verify(quizInfoRepository).save(any(QuizInfo.class));
  }

  @Test
  void modifyOrSaveQuizInfo_ExistingQuizInfo_ShouldUpdateQuizInfo() throws IOException {
    // Given
    LocalDate day = LocalDate.now();
    String QUIZ_INFO_FOLDER = "quizInfo/";
    String fileName = QUIZ_INFO_FOLDER + day.toString() + "/";
    QuizInfoModifyRequest quizInfoModifyRequest = new QuizInfoModifyRequest();
    quizInfoModifyRequest.setAnswerNum(correctAnswerNum);
    quizInfoModifyRequest.setQuizImage(mock(MultipartFile.class));

    QuizInfo quizInfo = new QuizInfo(quizContent, correctAnswerNum, quizImage);
    when(quizDao.getQuizInfoByDate(day)).thenReturn(Optional.of(quizInfo));
    String expectedImageUrl = "http://example.com/test-image.jpg";
    when(s3UploadService.saveFile(quizInfoModifyRequest.getQuizImage(), fileName)).thenReturn(expectedImageUrl);
    when(quizContentRepository.findByQuizDate(day)).thenReturn(Optional.of(quizContent));

    // When
    quizService.modifyOrSaveQuizInfo(day, quizInfoModifyRequest);

    // Then
    assertThat(quizInfo.getAnswerNum()).isEqualTo(correctAnswerNum);
    assertThat(quizInfo.getQuizImage()).isEqualTo(expectedImageUrl);
  }

  @Test
  void modifyOrSaveQuizInfo_ExistingQuizInfo_ShouldThrowIoeException() throws IOException {
    // Given
    LocalDate day = LocalDate.now();
    String QUIZ_INFO_FOLDER = "quizInfo/";
    String fileName = QUIZ_INFO_FOLDER + day.toString() + "/";
    QuizInfoModifyRequest quizInfoModifyRequest = new QuizInfoModifyRequest();
    quizInfoModifyRequest.setAnswerNum(correctAnswerNum);
    quizInfoModifyRequest.setQuizImage(mock(MultipartFile.class));

    QuizInfo quizInfo = new QuizInfo(quizContent, correctAnswerNum, quizImage);
    when(quizDao.getQuizInfoByDate(day)).thenReturn(Optional.of(quizInfo));
    String expectedImageUrl = "http://example.com/test-image.jpg";
    when(s3UploadService.saveFile(quizInfoModifyRequest.getQuizImage(), fileName)).thenThrow(IOException.class);
    when(quizContentRepository.findByQuizDate(day)).thenReturn(Optional.of(quizContent));

    // Then
    assertThatThrownBy(() -> quizService.modifyOrSaveQuizInfo(day, quizInfoModifyRequest))
            .isInstanceOf(RestApiException.class)
            .satisfies(exception -> {
              RestApiException restApiException = (RestApiException) exception; // 캐스팅
              assertThat(restApiException.getErrorCode()).isEqualTo(QuizErrorCode.NO_QUIZ_IMAGE);
            });
  }

  @Test
  void modifyOrSaveQuizInfo_ExistingQuizInfo_ShouldThrowRuntimeException() throws IOException {
    // Given
    LocalDate day = LocalDate.now();
    String QUIZ_INFO_FOLDER = "quizInfo/";
    String fileName = QUIZ_INFO_FOLDER + day.toString() + "/";
    QuizInfoModifyRequest quizInfoModifyRequest = new QuizInfoModifyRequest();
    quizInfoModifyRequest.setAnswerNum(correctAnswerNum);
    quizInfoModifyRequest.setQuizImage(mock(MultipartFile.class));

    QuizInfo quizInfo = new QuizInfo(quizContent, correctAnswerNum, quizImage);
    when(quizDao.getQuizInfoByDate(day)).thenReturn(Optional.of(quizInfo));
    String expectedImageUrl = "http://example.com/test-image.jpg";
    when(s3UploadService.saveFile(quizInfoModifyRequest.getQuizImage(), fileName)).thenThrow(RuntimeException.class);
    when(quizContentRepository.findByQuizDate(day)).thenReturn(Optional.of(quizContent));

    // Then
    assertThatThrownBy(() -> quizService.modifyOrSaveQuizInfo(day, quizInfoModifyRequest))
            .isInstanceOf(RestApiException.class)
            .satisfies(exception -> {
              RestApiException restApiException = (RestApiException) exception; // 캐스팅
              assertThat(restApiException.getErrorCode()).isEqualTo(QuizErrorCode.NO_QUIZ_IMAGE);
            });
  }

  @Test
  void modifyOrSaveQuizInfo_ExistingQuizInfo_ShouldThrowRestApiException() throws IOException {
    // Given
    LocalDate day = LocalDate.now();
    QuizInfoModifyRequest quizInfoModifyRequest = new QuizInfoModifyRequest();
    quizInfoModifyRequest.setAnswerNum(correctAnswerNum);
    quizInfoModifyRequest.setQuizImage(mock(MultipartFile.class));

    QuizInfo quizInfo = new QuizInfo(quizContent, correctAnswerNum, quizImage);
    when(quizDao.getQuizInfoByDate(day)).thenReturn(Optional.of(quizInfo));
    when(quizContentRepository.findByQuizDate(day)).thenReturn(Optional.empty());

    // Then
    assertThatThrownBy(() -> quizService.modifyOrSaveQuizInfo(day, quizInfoModifyRequest))
            .isInstanceOf(RestApiException.class)
            .satisfies(exception -> {
              RestApiException restApiException = (RestApiException) exception; // 캐스팅
              assertThat(restApiException.getErrorCode()).isEqualTo(QuizErrorCode.NO_QUIZ_CONTENT);
            });
  }

  //    @Test
  //    void getQuizReward_RewardExists_SendPrizeImage() {
  //        // Given
  //        when(fcfsService.getParticipantOrder(participantId)).thenReturn(successOrder);
  //        when(quizRewardRepository.findBySuccessOrder(successOrder)).thenReturn(Optional.of(quizReward));
  //
  //        // When
  //        quizService.getQuizReward(participantId);
  //
  //        // Then
  //        verify(quizRewardRepository).findBySuccessOrder(successOrder);
  //        verify(messageService).sendPrizeImage("prizeImageUrl");
  //        assertThat(quizReward.getValid()).isFalse();
  //    }
  //
  //    @Test
  //    void getQuizReward_RewardDoesNotExist_ThrowRestApiException() {
  //        // Given
  //        when(fcfsService.getParticipantOrder(participantId)).thenReturn(successOrder);
  //        when(quizRewardRepository.findBySuccessOrder(successOrder)).thenReturn(Optional.empty());
  //
  //        // When & Then
  //        assertThatThrownBy(() -> quizService.getQuizReward(participantId))
  //                .isInstanceOf(RestApiException.class)
  //                .satisfies(exception -> {
  //                    RestApiException restApiException = (RestApiException) exception; // 캐스팅
  //                    assertThat(restApiException.getErrorCode()).isEqualTo(QuizErrorCode.NO_QUIZ_REWARD);
  //                });
  //    }
}
