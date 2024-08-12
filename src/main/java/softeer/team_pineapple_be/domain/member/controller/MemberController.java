package softeer.team_pineapple_be.domain.member.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import softeer.team_pineapple_be.domain.member.response.MemberInfoResponse;
import softeer.team_pineapple_be.domain.member.service.MemberService;

/**
 * 멤버 정보 컨트롤러
 */
@Tag(name = "멤버 정보 API", description = "멤버 정보 처리")
@RestController
@RequiredArgsConstructor
@RequestMapping("/members")
public class MemberController {
  private final MemberService memberService;

  @GetMapping
  public ResponseEntity<MemberInfoResponse> getMemberInfo() {
    MemberInfoResponse memberInfo = memberService.getMemberInfo();
    return ResponseEntity.ok().body(memberInfo);
  }
}
