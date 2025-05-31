package com.littlebank.finance.domain.goal.controller;

import com.littlebank.finance.domain.goal.dto.request.GoalApplyRequest;
import com.littlebank.finance.domain.goal.dto.response.ChildGoalResponse;
import com.littlebank.finance.domain.goal.dto.response.CommonGoalResponse;
import com.littlebank.finance.domain.goal.dto.response.StampCheckResponse;
import com.littlebank.finance.domain.goal.dto.response.WeeklyGoalResponse;
import com.littlebank.finance.domain.goal.service.GoalService;
import com.littlebank.finance.global.security.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api-user/goal")
@RequiredArgsConstructor
@Tag(name = "Goal")
public class GoalController {
    private final GoalService goalService;

    @Operation(summary = "(아이)목표 신청 API", description = "아이가 부모에게 목표 신청")
    @PostMapping("/child/apply")
    public ResponseEntity<CommonGoalResponse> applyGoal(
            @RequestBody @Valid GoalApplyRequest request,
            @AuthenticationPrincipal CustomUserDetails customUserDetails
    ) {
        CommonGoalResponse response = goalService.applyGoal(customUserDetails.getId(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(summary = "(아이)이번 주 목표 조회 API")
    @GetMapping("/child/weekly")
    public ResponseEntity<List<WeeklyGoalResponse>> getWeeklyGoal(
            @AuthenticationPrincipal CustomUserDetails customUserDetails
    ) {
        List<WeeklyGoalResponse> response = goalService.getWeeklyGoal(customUserDetails.getId());
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "(부모)이번 주 아이들의 목표 조회 API")
    @GetMapping("/parent/weekly/{familyId}")
    public ResponseEntity<List<ChildGoalResponse>> getChildWeeklyGoal(
            @Parameter(description = "목표를 조회할 가족 식별 id")
            @PathVariable("familyId") Long familyId
    ) {
        List<ChildGoalResponse> response = goalService.getChildWeeklyGoal(familyId);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "(부모)아이들의 모든 목표 조회 API")
    @GetMapping("/parent/all/{familyId}")
    public ResponseEntity<List<ChildGoalResponse>> getChildGoals(
            @Parameter(description = "목표를 조회할 가족 식별 id")
            @PathVariable("familyId") Long familyId
    ) {
        List<ChildGoalResponse> response = goalService.getChildGoals(familyId);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "(부모)목표 신청 수락 API", description = "아이가 신청한 목표를 부모가 수락")
    @PatchMapping("/parent/apply/accept/{goalId}")
    public ResponseEntity<CommonGoalResponse> acceptApplyGoal(
            @Parameter(description = "신청을 수락할 목표 식별 id")
            @PathVariable("goalId") Long goalId
    ) {
        CommonGoalResponse response = goalService.acceptApplyGoal(goalId);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "(부모)목표 확인 도장 찍기 API")
    @PatchMapping("/parent/check/{goalId}")
    public ResponseEntity<CommonGoalResponse> acceptApplyGoal(
            @Parameter(description = "확인 도장 찍을 목표 식별 id")
            @PathVariable("goalId") Long goalId,
            @Parameter(description = "월요일 - 1, 화요일 - 2, 수요일 - 3, ...")
            @RequestParam("day") Integer day
    ) {
        CommonGoalResponse response = goalService.checkGoal(goalId, day);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "(공통)도장 확인 API")
    @GetMapping("/check/{goalId}")
    public ResponseEntity<StampCheckResponse> checkStamp(
            @Parameter(description = "도장을 확인할 목표 식별 id")
            @PathVariable("goalId") Long goalId
    ) {
        StampCheckResponse response = goalService.checkStamp(goalId);
        return ResponseEntity.ok(response);
    }
}
