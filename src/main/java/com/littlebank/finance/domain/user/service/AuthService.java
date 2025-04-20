package com.littlebank.finance.domain.user.service;

import com.littlebank.finance.domain.user.domain.Authority;
import com.littlebank.finance.domain.user.domain.User;
import com.littlebank.finance.domain.user.domain.repository.UserRepository;
import com.littlebank.finance.domain.user.dto.request.LoginRequest;
import com.littlebank.finance.domain.user.dto.request.SocialLoginRequest;
import com.littlebank.finance.global.jwt.TokenProvider;
import com.littlebank.finance.global.jwt.dto.TokenDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Service
@Transactional
@RequiredArgsConstructor
public class AuthService {
    private final UserRepository userRepository;
    private final RestTemplate restTemplate;
    private final AuthenticationManager authenticationManager;
    private final TokenProvider tokenProvider;

    @Transactional(readOnly = true)
    public TokenDto login(LoginRequest request) {
        return authenticateAndGenerateTokens(request.getEmail(), request.getPassword());
    }

    public TokenDto kakaoLogin(SocialLoginRequest request) {
        // 1. 카카오 사용자 정보 조회
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(request.getAccessToken());
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        ResponseEntity<Map> response = restTemplate.exchange(
                "https://kapi.kakao.com/v2/user/me",
                HttpMethod.GET,
                entity,
                Map.class
        );

        Map kakaoAccount = (Map) response.getBody().get("kakao_account");
        Map kakaoProfile = (Map) kakaoAccount.get("profile");

        String email = kakaoAccount.get("email").toString();
        String nickname = kakaoProfile.get("nickname").toString();
        String profileImageUrl;
        if (kakaoProfile.containsKey("profile_image_url")) {
            profileImageUrl = kakaoProfile.get("profile_image_url").toString();
        } else {
            profileImageUrl = null;
        }


        User user = userRepository.findByEmail(email)
                .orElseGet(() -> userRepository.save(User.builder()
                        .email(email)
                        .name(nickname)
                        .profileImagePath(profileImageUrl)
                        .authority(Authority.USER)
                        .build())
                );

        return authenticateAndGenerateTokens(user.getEmail(), null);
    }

    private TokenDto authenticateAndGenerateTokens(String email, String password) {
        UsernamePasswordAuthenticationToken authenticationToken =
                new UsernamePasswordAuthenticationToken(email, password);
        Authentication authentication = authenticationManager.authenticate(authenticationToken);
        SecurityContextHolder.getContext().setAuthentication(authentication);

        String accessToken = tokenProvider.provideAccessToken(authentication);
        String refreshToken = tokenProvider.provideRefreshToken(authentication);

        return TokenDto.of(accessToken, refreshToken);
    }
}
