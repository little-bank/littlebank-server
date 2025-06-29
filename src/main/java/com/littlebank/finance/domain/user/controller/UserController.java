package com.littlebank.finance.domain.user.controller;

import com.littlebank.finance.domain.user.domain.Authority;
import com.littlebank.finance.domain.user.dto.request.*;
import com.littlebank.finance.domain.user.dto.response.*;
import com.littlebank.finance.domain.user.service.UserService;
import com.littlebank.finance.global.security.CustomUserDetails;
import com.littlebank.finance.global.util.CookieUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api-user/user")
@RequiredArgsConstructor
@Tag(name = "User")
public class UserController {
    private final UserService userService;
    private final CookieUtil cookieUtil;

    @Operation(summary = "회원가입 API")
    @SecurityRequirements()
    @PostMapping("/public/signup")
    public ResponseEntity<SignupResponse> saveUser(
            @RequestBody @Valid SignupRequest request
    ) {
        SignupResponse response = userService.saveUser(request, Authority.USER);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(summary = "프로필 이미지 path 업데이트 API")
    @PatchMapping("/profile-image")
    public ResponseEntity<ProfileImagePathUpdateResponse> updateProfileImagePath(
            @RequestBody @Valid ProfileImagePathUpdateRequest request,
            @AuthenticationPrincipal CustomUserDetails customUserDetails
            ) {
        ProfileImagePathUpdateResponse response = userService.updateProfileImagePath(customUserDetails.getId(), request.getProfileImagePath());
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "내 정보 조회 API")
    @GetMapping("/info")
    public ResponseEntity<MyInfoResponse> getMyInfo(
            @AuthenticationPrincipal CustomUserDetails customUserDetails
    ) {
        MyInfoResponse response = userService.getMyInfo(customUserDetails.getId());
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "유저 상세 정보 조회 API")
    @GetMapping("/info/details/{userId}")
    public ResponseEntity<UserDetailsInfoResponse> getUserDetailsInfo(
            @Parameter(description = "조회할 유저 ID")
            @PathVariable("userId") Long targetUserId,
            @AuthenticationPrincipal CustomUserDetails customUserDetails
    ) {
        UserDetailsInfoResponse response = userService.getUserDetailsInfo(targetUserId, customUserDetails.getId());
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "내 정보 수정 API")
    @PutMapping("/info")
    public ResponseEntity<MyInfoResponse> updateMyInfo(
            @RequestBody @Valid UserInfoUpdateRequest request,
            @AuthenticationPrincipal CustomUserDetails customUserDetails
    ) {
        MyInfoResponse response = userService.updateMyInfo(customUserDetails.getId(), request);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "상태 메시지 수정 API")
    @PatchMapping("/status-message")
    public ResponseEntity<UpdateStatusMessageResponse> updateStatusMessage(
            @RequestBody @Valid UpdateStatusMessageRequest request,
            @AuthenticationPrincipal CustomUserDetails customUserDetails
    ) {
        UpdateStatusMessageResponse response = userService.updateStatusMessage(customUserDetails.getId(), request);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "계정 탈퇴 API")
    @DeleteMapping
    public ResponseEntity<Void> deleteUser(
            @AuthenticationPrincipal CustomUserDetails customUserDetails
    ) {
        userService.deleteUser(customUserDetails.getId());


        return ResponseEntity.noContent()
                .header(HttpHeaders.SET_COOKIE, cookieUtil.deleteCookie().toString())
                .build();
    }

    @Operation(summary = "최초 소셜 로그인 추가 정보 저장 API")
    @PatchMapping("/social/additional-info")
    public ResponseEntity<SocialLoginAdditionalInfoResponse> updateAdditionalInfoAfterSocialLogin(
            @RequestBody @Valid SocialLoginAdditionalInfoRequest request,
            @AuthenticationPrincipal CustomUserDetails customUserDetails
    ) {
        SocialLoginAdditionalInfoResponse response = userService.updateAdditionalInfoAfterSocialLogin(request, customUserDetails.getId());
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "유저 검색 API")
    @GetMapping("/search")
    public ResponseEntity<UserSearchResponse> searchUser(
            @Parameter(
                    name = "phone",
                    description = "'-'를 제외한 11자리 전화번호",
                    required = true,
                    in = ParameterIn.QUERY,
                    example = "01012345678"
            )
            @RequestParam(value = "phone") String phone,
            @AuthenticationPrincipal CustomUserDetails customUserDetails
    ) {
        UserSearchResponse response = userService.searchUser(phone, customUserDetails.getId());
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "계좌정보 유저 조회 API")
    @GetMapping("/search/by-account")
    public ResponseEntity<CommonUserInfoResponse> searchUserByAccount(
            @Parameter(description = "은행 고유 코드")
            @RequestParam(value = "bankCode") String bankCode,
            @Parameter(description = "계좌 번호")
            @RequestParam(value = "account") String account
    ) {
        CommonUserInfoResponse response = userService.searchUserByAccount(bankCode, account);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "계좌 pin 번호 재설정 API")
    @PatchMapping("/account/pin/reset")
    public ResponseEntity<AccountPinResetResponse> resetAccountPin(
            @RequestBody @Valid AccountPinResetRequest request,
            @AuthenticationPrincipal CustomUserDetails customUserDetails
    ) {
        AccountPinResetResponse response = userService.resetAccountPin(customUserDetails.getId(), request);
        return ResponseEntity.ok(response);
    }
}
