package softeer.team_pineapple_be.domain.admin.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import java.time.LocalDate;

import lombok.RequiredArgsConstructor;
import softeer.team_pineapple_be.domain.quiz.dao.QuizDao;
import softeer.team_pineapple_be.domain.quiz.domain.QuizInfo;
import softeer.team_pineapple_be.domain.quiz.request.QuizModifyRequest;
import softeer.team_pineapple_be.domain.quiz.response.QuizAnswerResponse;
import softeer.team_pineapple_be.domain.quiz.response.QuizContentResponse;
import softeer.team_pineapple_be.domain.quiz.service.QuizService;
import softeer.team_pineapple_be.global.auth.annotation.Auth;
import softeer.team_pineapple_be.global.common.response.SuccessResponse;

/**
 * 이벤트 어드민 컨트롤러
 */
@Auth
@Controller
@RequiredArgsConstructor
@RequestMapping("/admin")
public class AdminController {
  private final QuizService quizService;
  private final QuizDao quizDao;

  @GetMapping("/quiz/{day}")
  public ResponseEntity<QuizContentResponse> getDailyQuiz(@PathVariable("day") LocalDate day) {
    QuizContentResponse quizContentOfDate = quizService.getQuizContentOfDate(day);
    return ResponseEntity.ok(quizContentOfDate);
  }

  @GetMapping("/quiz/answer/{day}")
  public ResponseEntity<QuizAnswerResponse> getDailyQuizAnswer(@PathVariable("day") LocalDate day) {
    QuizInfo quizInfo = quizDao.getQuizInfoByDate(day);
    return ResponseEntity.ok(QuizAnswerResponse.of(quizInfo));
  }

  @PutMapping("/quiz")
  public ResponseEntity<SuccessResponse> updateDailyQuiz(@RequestBody QuizModifyRequest quizModifyRequest) {
    quizService.modifyQuizContent(quizModifyRequest);
    return ResponseEntity.ok(new SuccessResponse());
  }
}
