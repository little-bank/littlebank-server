package com.littlebank.finance.domain.challenge.controller;

import com.littlebank.finance.domain.challenge.domain.ChallengeCategory;
import com.littlebank.finance.domain.challenge.domain.ChallengeStatus;
import com.littlebank.finance.domain.challenge.dto.request.ChallengeUserRequestDto;
import com.littlebank.finance.domain.challenge.dto.response.admin.ChallengeAdminResponseDto;
import com.littlebank.finance.domain.challenge.dto.response.ChallengeUserResponseDto;
import com.littlebank.finance.domain.challenge.service.ChallengeService;
import com.littlebank.finance.global.common.CustomPageResponse;
import com.littlebank.finance.global.security.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.List;

@RestController
@RequestMapping("/api-user/challenge")
@RequiredArgsConstructor
@Tag(name = "challenge")
public class ChallengeController {

    private final ChallengeService challengeService;

    @Operation(summary = "챌린지 참여하기 API")
    @PostMapping("/join/{challengeId}")
    public ResponseEntity<ChallengeUserResponseDto> joinChallenge(
            @PathVariable Long challengeId,
            @RequestBody ChallengeUserRequestDto request,
            @AuthenticationPrincipal CustomUserDetails user) {
        ChallengeUserResponseDto response = challengeService.joinChallenge(user.getId(), challengeId, request);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "챌린지 전체 조회 API")
    @GetMapping
    public ResponseEntity<CustomPageResponse<ChallengeAdminResponseDto>> getAllChallenges(
            @RequestParam(required = false) ChallengeCategory challengeCategory,
            @RequestParam(defaultValue = "0") int page,
            @AuthenticationPrincipal CustomUserDetails user
            ) {
        CustomPageResponse<ChallengeAdminResponseDto> response = challengeService.getAllChallenges(user.getId(), challengeCategory, page);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "챌린지 상세 조회 API")
    @GetMapping("/{challengeId}")
    public ResponseEntity<ChallengeAdminResponseDto> getChallengeDetial(
            @PathVariable Long challengeId,
            @AuthenticationPrincipal CustomUserDetails user
    ) {
        ChallengeAdminResponseDto response = challengeService.getChallengeDetail(user.getId(), challengeId);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "내가 참여한 챌린지 조회 API")
    @GetMapping("/my")
    public ResponseEntity<CustomPageResponse<ChallengeUserResponseDto>> getMyChallenges(
            @AuthenticationPrincipal CustomUserDetails user,
            @RequestParam(required = true) String type,
            @RequestParam(defaultValue = "0") int page
    ) {
        CustomPageResponse<ChallengeUserResponseDto> response = challengeService.getMyChallenges(user.getId(), type, page);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "(부모) 자녀가 참여 중인 챌린지 조회 API")
    @GetMapping("/parent/{familyId}")
    public ResponseEntity<List<ChallengeUserResponseDto>> getChildInProgressChallenge (
            @Parameter(description = "챌린지를 조회할 가족 식별 id")
            @PathVariable("familyId") Long familyId,
            @AuthenticationPrincipal CustomUserDetails user

    ) {
        List<ChallengeUserResponseDto> response = challengeService.getChildInProgressChallenge(familyId, user.getId());
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "(부모) 챌린지 신청 수락하기 API")
    @PatchMapping("/parent/apply/accept/{participationId}")
    public ResponseEntity<ChallengeUserResponseDto> acceptApplyChallenge (
            @Parameter(description = "신청을 수락할 챌린지참여 식별 id")
            @PathVariable("participationId") Long participationId,
            @AuthenticationPrincipal CustomUserDetails parent
    ) {
        ChallengeUserResponseDto response = challengeService.acceptApplyChallenge(participationId, parent.getId());
        return ResponseEntity.ok(response);
    }

}
