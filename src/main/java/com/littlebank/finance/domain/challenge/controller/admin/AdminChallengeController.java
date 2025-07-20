package com.littlebank.finance.domain.challenge.controller.admin;

import com.littlebank.finance.domain.challenge.domain.ChallengeCategory;
import com.littlebank.finance.domain.challenge.dto.request.admin.ChallengeAdminRequestDto;
import com.littlebank.finance.domain.challenge.dto.response.admin.ChallengeAdminResponseDto;
import com.littlebank.finance.domain.challenge.service.admin.AdminChallengeService;
import com.littlebank.finance.global.security.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.List;

@RestController
@RequestMapping("/api-admin/challenge")
@RequiredArgsConstructor
@Tag(name = "challenge")
public class AdminChallengeController {

    private final AdminChallengeService adminChallengeService;

    @Operation(summary = "챌린지 생성")
    @PostMapping("/create")
    public ResponseEntity<ChallengeAdminResponseDto> createChallenge(
            @RequestBody ChallengeAdminRequestDto request,
            @AuthenticationPrincipal CustomUserDetails admin
            ) {
        ChallengeAdminResponseDto response = adminChallengeService.createChallenge(admin.getId(), request);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "챌린지 수정")
    @PutMapping("/update/{challengeId}")
    public ResponseEntity<ChallengeAdminResponseDto> updateChallenge (
            @PathVariable Long challengeId,
            @RequestBody ChallengeAdminRequestDto request,
            @AuthenticationPrincipal CustomUserDetails admin
    ) {
        ChallengeAdminResponseDto response = adminChallengeService.updateChallenge(admin.getId(), challengeId, request);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "챌린지 삭제")
    @DeleteMapping("/delete/{challengeId}")
    public ResponseEntity<Void> deleteChallenge (
            @PathVariable Long challengeId,
            @AuthenticationPrincipal CustomUserDetails admin
    ) {
        adminChallengeService.deleteChallenge(admin.getId(), challengeId);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "챌린지 조회")
    @GetMapping("/get/All")
    public ResponseEntity<List<ChallengeAdminResponseDto>> getAllChallenge(
            @RequestParam(required = false)ChallengeCategory challengeCategory
            ) {
        List<ChallengeAdminResponseDto> response = adminChallengeService.getAllChallenges(challengeCategory);
        return ResponseEntity.ok(response);
    }
}
