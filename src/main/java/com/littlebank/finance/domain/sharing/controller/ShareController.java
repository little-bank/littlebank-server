package com.littlebank.finance.domain.sharing.controller;

import com.littlebank.finance.domain.sharing.dto.response.KakaoProfileResponseDto;
import com.littlebank.finance.domain.sharing.service.ShareService;
import com.littlebank.finance.global.security.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api-user/share")
@RequiredArgsConstructor
@Tag(name = "share")
public class ShareController {
    private final ShareService shareService;

    @Operation(summary = "카카오톡으로 구독 링크 공유 API")
    @GetMapping("/profile")
    public ResponseEntity<KakaoProfileResponseDto> getProfile (
            @RequestHeader("Authorization") String authorizationHeader
    ) {
        String accessToken = authorizationHeader.replace("Bearer ", "");
        KakaoProfileResponseDto response = shareService.getKakaoProfile(accessToken);
        return ResponseEntity.ok(response);
    }
}
