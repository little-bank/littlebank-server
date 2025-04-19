package com.littlebank.finance.domain.user.controller;

import com.littlebank.finance.domain.user.domain.Authority;
import com.littlebank.finance.domain.user.dto.request.ProfileImagePathUpdateRequest;
import com.littlebank.finance.domain.user.dto.request.SignupRequest;
import com.littlebank.finance.domain.user.dto.request.UserInfoUpdateRequest;
import com.littlebank.finance.domain.user.dto.response.ProfileImagePathUpdateResponse;
import com.littlebank.finance.domain.user.dto.response.SignupResponse;
import com.littlebank.finance.domain.user.dto.response.UserInfoResponse;
import com.littlebank.finance.domain.user.service.UserService;
import com.littlebank.finance.global.security.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
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

    @Operation(summary = "회원가입 API")
    @SecurityRequirements()
    @PostMapping("/public/signup")
    public ResponseEntity<SignupResponse> saveUser(
            @RequestBody @Valid SignupRequest request
    ) {
        SignupResponse response = userService.saveUser(request.toEntity(Authority.USER));
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
    public ResponseEntity<UserInfoResponse> getMyInfo(
            @AuthenticationPrincipal CustomUserDetails customUserDetails
    ) {
        UserInfoResponse response = userService.getMyInfo(customUserDetails.getId());
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "내 정보 수정 API")
    @PutMapping("/info")
    public ResponseEntity<UserInfoResponse> updateMyInfo(
            @RequestBody @Valid UserInfoUpdateRequest request,
            @AuthenticationPrincipal CustomUserDetails customUserDetails
    ) {
        UserInfoResponse response = userService.updateMyInfo(customUserDetails.getId(), request.toEntity());
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "계정 탈퇴 API")
    @DeleteMapping
    public ResponseEntity<Void> deleteUser(
            @AuthenticationPrincipal CustomUserDetails customUserDetails
    ) {
        userService.deleteUser(customUserDetails.getId());
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

}
