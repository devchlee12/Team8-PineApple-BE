package softeer.team_pineapple_be.domain.admin.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import softeer.team_pineapple_be.domain.quiz.dao.QuizDao;
import softeer.team_pineapple_be.domain.quiz.domain.QuizInfo;
import softeer.team_pineapple_be.domain.quiz.exception.QuizErrorCode;
import softeer.team_pineapple_be.domain.quiz.request.QuizInfoModifyRequest;
import softeer.team_pineapple_be.domain.quiz.request.QuizModifyRequest;
import softeer.team_pineapple_be.domain.quiz.response.QuizAnswerResponse;
import softeer.team_pineapple_be.domain.quiz.response.QuizContentResponse;
import softeer.team_pineapple_be.domain.quiz.service.QuizService;
import softeer.team_pineapple_be.global.auth.annotation.Auth;
import softeer.team_pineapple_be.global.common.response.SuccessResponse;
import softeer.team_pineapple_be.global.exception.RestApiException;

/**
 * 이벤트 어드민 컨트롤러
 */
@Auth
@RestController
@Tag(name = "어드민 API", description = "어드민 기능을 제공하는 API 입니다")
@RequiredArgsConstructor
@RequestMapping("/admin")
public class AdminController {
  private final QuizService quizService;
  private final QuizDao quizDao;

  @Operation(summary = "날짜에 해당하는 퀴즈 정보 가져오기")
  @GetMapping("/quiz/{day}")
  public ResponseEntity<QuizContentResponse> getDailyQuiz(@PathVariable("day") LocalDate day) {
    QuizContentResponse quizContentOfDate = quizService.getQuizContentOfDate(day);
    return ResponseEntity.ok(quizContentOfDate);
  }

  @Operation(summary = "날짜에 해당하는 퀴즈 답 정보 가져오기")
  @GetMapping("/quiz/answers/{day}")
  public ResponseEntity<QuizAnswerResponse> getDailyQuizAnswer(@PathVariable("day") LocalDate day) {
    QuizInfo quizInfo =
        quizDao.getQuizInfoByDate(day).orElseThrow(() -> new RestApiException(QuizErrorCode.NO_QUIZ_INFO));
    return ResponseEntity.ok(QuizAnswerResponse.of(quizInfo));
  }

  @Operation(summary = "퀴즈 수정하기")
  @PutMapping("/quiz")
  public ResponseEntity<SuccessResponse> updateDailyQuiz(@RequestBody QuizModifyRequest quizModifyRequest) {
    quizService.modifyQuizContent(quizModifyRequest);
    return ResponseEntity.ok(new SuccessResponse());
  }

  @Operation(summary = "퀴즈 정답 정보 수정")
  @PutMapping("/quiz/answers/{day}")
  public ResponseEntity<SuccessResponse> updateDailyQuizAnswer(@PathVariable("day") LocalDate day,
      @RequestBody QuizInfoModifyRequest quizInfoModifyRequest) {
    quizService.modifyQuizInfo(day, quizInfoModifyRequest);
    return ResponseEntity.ok(new SuccessResponse());
  }
}
