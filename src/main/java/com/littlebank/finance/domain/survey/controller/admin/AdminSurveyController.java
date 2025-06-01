package com.littlebank.finance.domain.survey.controller.admin;

import com.littlebank.finance.domain.survey.dto.request.CreateSurveyRequestDto;
import com.littlebank.finance.domain.survey.dto.response.CreateSurveyResponseDto;
import com.littlebank.finance.domain.survey.service.admin.AdminSurveyService;
import com.littlebank.finance.global.security.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api-admin/survey")
@Tag(name = "survey")
@RequiredArgsConstructor
public class AdminSurveyController {
    private final AdminSurveyService adminSurveyService;

    @Operation(summary = "설문 생성")
    @PostMapping("/create")
    public ResponseEntity<CreateSurveyResponseDto> createSurvey (
            @RequestBody CreateSurveyRequestDto request,
            @AuthenticationPrincipal CustomUserDetails admin
            ) {
        CreateSurveyResponseDto response = adminSurveyService.createSurvey(admin.getId(), request);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "설문 수정")
    @PutMapping("/update/{surveyId}")
    public ResponseEntity<CreateSurveyResponseDto> updateSurvey (
            @PathVariable Long surveyId,
            @RequestBody CreateSurveyRequestDto request,
            @AuthenticationPrincipal CustomUserDetails admin
    ) {
        CreateSurveyResponseDto response = adminSurveyService.updateSurvey(admin.getId(), surveyId, request);
        return ResponseEntity.ok(response);
    }
}
