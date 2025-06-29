package com.littlebank.finance.domain.subscription.controller;

import com.littlebank.finance.domain.subscription.domain.Subscription;
import com.littlebank.finance.domain.subscription.dto.request.FreeSubscriptionRequestDto;
import com.littlebank.finance.domain.subscription.dto.request.SubscriptionCreateRequestDto;
import com.littlebank.finance.domain.subscription.dto.request.SubscriptionPurchaseRequestDto;
import com.littlebank.finance.domain.subscription.dto.response.FreeSubscriptionResponseDto;
import com.littlebank.finance.domain.subscription.dto.response.SubscriptionResponseDto;
import com.littlebank.finance.domain.subscription.service.PurchaseService;
import com.littlebank.finance.domain.subscription.service.SubscriptionService;
import com.littlebank.finance.global.common.CustomPageResponse;
import com.littlebank.finance.global.common.PaginationPolicy;
import com.littlebank.finance.global.security.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;

@RestController
@RequestMapping("/api-user/subscription")
@RequiredArgsConstructor
@Tag(name = "Subscription")
public class SubscriptionController {

    private final SubscriptionService subscriptionService;
    private final PurchaseService purchaseService;

    @Operation(summary = "구독 생성")
    @PostMapping("/create")
    public ResponseEntity<SubscriptionResponseDto> createSubscription(
            @RequestBody SubscriptionCreateRequestDto request,
            @AuthenticationPrincipal CustomUserDetails user
            ) {
        SubscriptionResponseDto response = subscriptionService.createSubscription(user.getId(), request);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "나의 구독 정보 조회")
    @GetMapping("/my")
    public ResponseEntity<CustomPageResponse<SubscriptionResponseDto>> getMySubscription(
            @AuthenticationPrincipal CustomUserDetails user,
            @RequestParam(name = "pageNumber") Integer pageNumber
            ) {
        Pageable pageable = PageRequest.of(pageNumber, PaginationPolicy.GENERAL_PAGE_SIZE);
        CustomPageResponse<SubscriptionResponseDto> response = subscriptionService.getMySubscription(user.getId(), pageable);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "구독 코드 등록")
    @PostMapping("/redeem")
    public ResponseEntity<SubscriptionResponseDto> redeemSubscription(
            @RequestParam String code,
            @AuthenticationPrincipal CustomUserDetails user
    ) {
        SubscriptionResponseDto response = subscriptionService.redeemSubscription(user.getId(), code);
        return ResponseEntity.ok(response);
    }
    @Operation(summary = "구독권 결제")
    @PostMapping("/purchase/inapp")
    public ResponseEntity<?> verifyGooglePurchase (
            @RequestBody SubscriptionPurchaseRequestDto request,
            @AuthenticationPrincipal CustomUserDetails user
    ) {
        boolean response = purchaseService.verifyReceiptForGoogle(user.getId(), request);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "무료 구독권 시작")
    @PostMapping("/start/free")
    public ResponseEntity<FreeSubscriptionResponseDto> startFreeSubscription (
            @RequestBody FreeSubscriptionRequestDto request,
            @AuthenticationPrincipal CustomUserDetails user
            ) {
        FreeSubscriptionResponseDto response = subscriptionService.startFreeSubscription(user.getId(), request);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "무료 구독권 조회")
    @GetMapping("/free")
    public ResponseEntity<FreeSubscriptionResponseDto> getFreeSubscription (
            @AuthenticationPrincipal CustomUserDetails user
    ) {
        FreeSubscriptionResponseDto response = subscriptionService.getFreeSubscription(user.getId());
        return ResponseEntity.ok(response);
    }
}

