package com.littlebank.finance.domain.subscription.controller;

import com.littlebank.finance.domain.subscription.dto.request.SubscriptionCreateRequestDto;
import com.littlebank.finance.domain.subscription.dto.request.SubscriptionPurchaseRequestDto;
import com.littlebank.finance.domain.subscription.dto.response.SubscriptionResponseDto;
import com.littlebank.finance.domain.subscription.service.PurchaseService;
import com.littlebank.finance.domain.subscription.service.SubscriptionService;
import com.littlebank.finance.global.security.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
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
    public ResponseEntity<List<SubscriptionResponseDto>> getMySubscription(
            @AuthenticationPrincipal CustomUserDetails user
    ) {
        List<SubscriptionResponseDto> response = subscriptionService.getMySubscription(user.getId());
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
}
