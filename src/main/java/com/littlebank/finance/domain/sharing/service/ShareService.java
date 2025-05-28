package com.littlebank.finance.domain.sharing.service;

import com.littlebank.finance.domain.sharing.dto.response.KakaoProfileResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.util.Map;


@Service
@Transactional
@RequiredArgsConstructor
public class ShareService {
    private static final String PROFILE_API_URL = "https://kapi.kakao.com/v1/api/talk/profile";
    private final RestTemplate restTemplate;

    public KakaoProfileResponseDto getKakaoProfile(String accessToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        HttpEntity<Void> entity = new HttpEntity<>(headers);
        ResponseEntity<Map>  response = restTemplate.exchange(
                "https://kapi.kakao.com/v1/api/talk/profile",
                HttpMethod.GET,
                entity,
                Map.class
        );

        Map<String, Object> body = response.getBody();
        if (body == null || !body.containsKey("nickname")) {
            throw new IllegalStateException("카카오 프로필 정보를 가져올 수 없습니다.");
        }

        return KakaoProfileResponseDto.builder()
                .nickname(body.get("nickname").toString())
                .profileImageUrl(body.getOrDefault("profileImageURL", "").toString())
                .thumbnailImageUrl(body.getOrDefault("thumbnailURL", "").toString())
                .build();
    }
}
