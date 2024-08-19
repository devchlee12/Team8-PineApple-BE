package softeer.team_pineapple_be.global.shortenurl.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import softeer.team_pineapple_be.global.auth.annotation.Auth;
import softeer.team_pineapple_be.global.shortenurl.response.ShortenUrlResponse;
import softeer.team_pineapple_be.global.shortenurl.service.ShortenUrlService;

@Tag(name = "단축 URL API", description = "단축 URL 처리")
@Controller
@RequiredArgsConstructor
public class ShortenUrlController {

    private final ShortenUrlService shortenUrlService;

    @Auth
    @GetMapping("/shorten-url")
    public ResponseEntity<ShortenUrlResponse> getShortenUrl() {
        return ResponseEntity.ok(shortenUrlService.getShortenUrl());
    }

    @GetMapping("/redirect/{shortenUrl}")
    public String redirectShortenUrl(@PathVariable String shortenUrl) {
        String redirectUrl = shortenUrlService.redirectUrl(shortenUrl);
        return "redirect:" + redirectUrl;
    }
}
