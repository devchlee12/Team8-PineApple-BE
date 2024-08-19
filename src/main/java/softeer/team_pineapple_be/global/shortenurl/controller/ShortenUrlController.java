package softeer.team_pineapple_be.global.shortenurl.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import softeer.team_pineapple_be.global.auth.annotation.Auth;
import softeer.team_pineapple_be.global.shortenurl.response.ShortenUrlResponse;
import softeer.team_pineapple_be.global.shortenurl.service.ShortenUrlService;

@Tag(name = "단축 URL API", description = "단축 URL 처리")
@Controller
@RequiredArgsConstructor
public class ShortenUrlController {

  private final ShortenUrlService shortenUrlService;

  @Operation(summary = "단축 URL 받기")
  @Auth
  @GetMapping("/shorten-url")
  public ResponseEntity<ShortenUrlResponse> getShortenUrl() {
    return ResponseEntity.ok(shortenUrlService.getShortenUrl());
  }

  @Operation(summary = "단축 URL을 통해 원본 URL로 리다이렉트")
  @GetMapping("/redirect/{shortenUrl}")
  public String redirectShortenUrl(@PathVariable String shortenUrl) {
    String redirectUrl = shortenUrlService.redirectUrl(shortenUrl);
    return "redirect:" + redirectUrl;
  }
}
