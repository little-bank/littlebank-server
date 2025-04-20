package com.littlebank.finance.domain.user.controller;

import com.littlebank.finance.domain.user.dto.request.LoginRequest;
import com.littlebank.finance.domain.user.dto.request.SocialLoginRequest;
import com.littlebank.finance.domain.user.dto.response.LoginResponse;
import com.littlebank.finance.domain.user.service.AuthService;
import com.littlebank.finance.global.jwt.dto.TokenDto;
import com.littlebank.finance.global.util.CookieUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api-user/auth")
@RequiredArgsConstructor
@Tag(name = "Auth")
public class AuthController {
    private final AuthService authService;
    private final CookieUtil cookieUtil;

    @Operation(summary = "로그인 API", description = "refresh 토큰은 응답 헤더에서 파싱하여 저장")
    @SecurityRequirements()
    @PostMapping("/public/login")
    public ResponseEntity<LoginResponse> login(
            @RequestBody @Valid LoginRequest request
    ) {
        TokenDto tokenDto = authService.login(request);
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookieUtil.getCookie(tokenDto.getRefreshToken()).toString())
                .body(LoginResponse.of(tokenDto.getAccessToken()));
    }

    @Operation(summary = "카카오 API 로그인", description = "카카오 인증 토큰(accessToken)을 담아서 요청")
    @SecurityRequirements()
    @PostMapping("/public/kakao/login")
    public ResponseEntity<LoginResponse> kakaoLogin(
            @RequestBody @Valid SocialLoginRequest request
    ) {
        TokenDto tokenDto = authService.kakaoLogin(request);
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookieUtil.getCookie(tokenDto.getRefreshToken()).toString())
                .body(LoginResponse.of(tokenDto.getAccessToken()));
    }
}
