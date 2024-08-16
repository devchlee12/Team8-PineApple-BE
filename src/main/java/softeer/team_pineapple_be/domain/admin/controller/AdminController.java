package softeer.team_pineapple_be.domain.admin.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import softeer.team_pineapple_be.domain.draw.request.DrawDailyMessageModifyRequest;
import softeer.team_pineapple_be.domain.draw.request.DrawPrizeRequest;
import softeer.team_pineapple_be.domain.draw.response.DrawDailyMessageResponse;
import softeer.team_pineapple_be.domain.draw.request.DrawProbabilityRequest;
import softeer.team_pineapple_be.domain.draw.request.DrawRewardInfoListRequest;
import softeer.team_pineapple_be.domain.draw.response.DrawProbabilityResponse;
import softeer.team_pineapple_be.domain.draw.response.DrawRewardInfoListResponse;
import softeer.team_pineapple_be.domain.draw.service.DrawPrizeService;
import softeer.team_pineapple_be.domain.draw.service.DrawService;
import softeer.team_pineapple_be.domain.draw.service.DrawProbabilityService;
import softeer.team_pineapple_be.domain.draw.service.DrawRewardInfoService;
import softeer.team_pineapple_be.domain.draw.service.DrawService;
import softeer.team_pineapple_be.domain.quiz.dao.QuizDao;
import softeer.team_pineapple_be.domain.quiz.domain.QuizInfo;
import softeer.team_pineapple_be.domain.quiz.exception.QuizErrorCode;
import softeer.team_pineapple_be.domain.quiz.request.QuizInfoModifyRequest;
import softeer.team_pineapple_be.domain.quiz.request.QuizModifyRequest;
import softeer.team_pineapple_be.domain.quiz.request.QuizRewardUploadRequest;
import softeer.team_pineapple_be.domain.quiz.response.QuizAnswerResponse;
import softeer.team_pineapple_be.domain.quiz.response.QuizContentResponse;
import softeer.team_pineapple_be.domain.quiz.service.QuizService;
import softeer.team_pineapple_be.global.auth.annotation.Admin;
import softeer.team_pineapple_be.global.common.response.SuccessResponse;
import softeer.team_pineapple_be.global.exception.RestApiException;

/**
 * 이벤트 어드민 컨트롤러
 */
@Admin
@RestController
@Tag(name = "어드민 API", description = "어드민 기능을 제공하는 API 입니다")
@RequiredArgsConstructor
@RequestMapping("/admin")
public class AdminController {
  private final QuizService quizService;
  private final QuizDao quizDao;
  private final DrawPrizeService drawPrizeService;
  private final DrawService drawService;
  private final DrawProbabilityService drawProbabilityService;
  private final DrawRewardInfoService drawRewardInfoService;

  @Operation(summary = "날짜에 해당하는 퀴즈 정보 가져오기")
  @GetMapping("/quiz/{day}")
  public ResponseEntity<QuizContentResponse> getDailyQuiz(@PathVariable("day") LocalDate day) {
    QuizContentResponse quizContentOfDate = quizService.getQuizContentOfDate(day);
    return ResponseEntity.ok(quizContentOfDate);
  }

  @Operation(summary = "날짜에 해당하는 퀴즈 정답 정보 가져오기")
  @GetMapping("/quiz/answers/{day}")
  public ResponseEntity<QuizAnswerResponse> getDailyQuizAnswer(@PathVariable("day") LocalDate day) {
    QuizInfo quizInfo =
        quizDao.getQuizInfoByDate(day).orElseThrow(() -> new RestApiException(QuizErrorCode.NO_QUIZ_INFO));
    return ResponseEntity.ok(QuizAnswerResponse.of(quizInfo));
  }

  @Operation(summary = "일자별 응모 정보 가져오기")
  @GetMapping("/draw/daily-info/{day}")
  public ResponseEntity<DrawDailyMessageResponse> getDrawDailyMessageInfo(@PathVariable("day") LocalDate day) {
    DrawDailyMessageResponse dailyMessageInfo = drawService.getDailyMessageInfo(day);
    return ResponseEntity.ok(dailyMessageInfo);
  }

  @Operation(summary = "퀴즈 등록/수정하기")
  @PutMapping("/quiz/{day}")
  public ResponseEntity<SuccessResponse> updateDailyQuiz(@PathVariable("day") LocalDate day,
      @RequestBody QuizModifyRequest quizModifyRequest) {
    quizService.modifyOrSaveQuizContent(day, quizModifyRequest);
    return ResponseEntity.ok(new SuccessResponse());
  }

  @Operation(summary = "퀴즈 정답 정보 등록/수정")
  @PutMapping("/quiz/answers/{day}")
  public ResponseEntity<SuccessResponse> updateDailyQuizAnswer(@PathVariable("day") LocalDate day,
      @ModelAttribute QuizInfoModifyRequest quizInfoModifyRequest) {
    quizService.modifyOrSaveQuizInfo(day, quizInfoModifyRequest);
    return ResponseEntity.ok(new SuccessResponse());
  }

  @Operation(summary = "일자별 응모 정보 등록/수정")
  @PutMapping("/draw/daily-info")
  public ResponseEntity<SuccessResponse> updateDrawDailyMessageInfo(
      @ModelAttribute DrawDailyMessageModifyRequest drawDailyMessageModifyRequest) {
    drawService.updateOrSaveDailyMessageInfo(drawDailyMessageModifyRequest);
    return ResponseEntity.ok(new SuccessResponse());
  }

  @Operation(summary = "응모 경품(바코드)이미지 등록 및 삭제")
  @PostMapping("/drawPrize")
  public ResponseEntity<SuccessResponse> uploadDrawPrize(@Valid @ModelAttribute DrawPrizeRequest drawPrizeRequest) {
    drawPrizeService.uploadDrawPrizeZipFile(drawPrizeRequest.getFile(), drawPrizeRequest.getRanking());
    return ResponseEntity.ok(new SuccessResponse());
  }

  @Operation(summary = "선착순 경품(바코드)이미지 등록 및 삭제")
  @PostMapping("/quizReward")
  public ResponseEntity<SuccessResponse> uploadQuizReward(
      @ModelAttribute QuizRewardUploadRequest quizRewardUploadRequest) {
    quizService.uploadQuizRewardZipFile(quizRewardUploadRequest.getFile(), quizRewardUploadRequest.getQuizDate());
    return ResponseEntity.ok(new SuccessResponse());
  }

  @Operation(summary = "응모 당첨 확률 조회")
  @GetMapping("/drawProbability")
  public ResponseEntity<DrawProbabilityResponse> getDrawProbability() {
    return ResponseEntity.ok(drawProbabilityService.getDrawProbability());
  }

  @Operation(summary = "응모 당첨 확률 수정")
  @PutMapping("/drawProbability")
  public ResponseEntity<SuccessResponse> setDrawProbability(@RequestBody DrawProbabilityRequest request) {
    drawProbabilityService.setDrawProbability(request);
    return ResponseEntity.ok(new SuccessResponse());
  }

  @Operation(summary = "응모 상품 조회")
  @GetMapping("/drawRewardInfo")
  public ResponseEntity<DrawRewardInfoListResponse> getAllDrawRewardInfo() {
    return ResponseEntity.ok(drawRewardInfoService.getAllDrawRewardInfo());
  }

  @Operation(summary = "응모 상품 수정")
  @PutMapping("/drawRewardInfo")
  public ResponseEntity<SuccessResponse> addDrawRewardInfoList(@RequestBody DrawRewardInfoListRequest drawRewardInfoListRequest) {
    drawRewardInfoService.setDrawRewardInfoList(drawRewardInfoListRequest);
    return ResponseEntity.ok(new SuccessResponse());
  }
}
