package com.littlebank.finance.domain.subscription.controller;

import com.littlebank.finance.domain.subscription.dto.request.FreeSubscriptionRequestDto;
import com.littlebank.finance.domain.subscription.dto.request.SubscriptionCreateRequestDto;
import com.littlebank.finance.domain.subscription.dto.request.SubscriptionDeleteRequest;
import com.littlebank.finance.domain.subscription.dto.request.SubscriptionPurchaseRequestDto;
import com.littlebank.finance.domain.subscription.dto.response.FreeSubscriptionResponseDto;
import com.littlebank.finance.domain.subscription.dto.response.InviteCodeResponseDto;
import com.littlebank.finance.domain.subscription.dto.response.SubscriptionResponseDto;
import com.littlebank.finance.domain.subscription.dto.response.SubscriptionStatusResponseDto;
import com.littlebank.finance.domain.subscription.service.PurchaseService;
import com.littlebank.finance.domain.subscription.service.SubscriptionService;
import com.littlebank.finance.global.security.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
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

    @Operation(summary = "구독 삭제")
    @DeleteMapping("/delete")
    public ResponseEntity<Void> deleteSubscription(
            @RequestBody @Valid SubscriptionDeleteRequest request,
            @AuthenticationPrincipal CustomUserDetails user
    ) {
        subscriptionService.deleteSubscription(user.getId(), request);
        return ResponseEntity.noContent().build();
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
    public ResponseEntity<SubscriptionResponseDto> verifyGooglePurchase (
            @RequestBody SubscriptionPurchaseRequestDto request,
            @AuthenticationPrincipal CustomUserDetails user
    ) {
        SubscriptionResponseDto response = purchaseService.verifySubscription(user.getId(), request);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "구독권에 결제자 포함 유무")
    @PostMapping("include-owner")
    public ResponseEntity<Void> includeOwner(@AuthenticationPrincipal CustomUserDetails user) {
        subscriptionService.includeOwnerToSubscription(user.getId());
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "마이 구독권 조회")
    @GetMapping("/my-status")
    public ResponseEntity<SubscriptionStatusResponseDto> getSubscriptionStatus(@AuthenticationPrincipal CustomUserDetails userDetails) {
        SubscriptionStatusResponseDto response = purchaseService.getSubscriptionStatus(userDetails.getId());
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "구독 초대코드 조회")
    @GetMapping("/get/invidecode")
    public ResponseEntity<List<InviteCodeResponseDto>> getSubscriptionInviteCode(@AuthenticationPrincipal CustomUserDetails user) {
        List<InviteCodeResponseDto> response = subscriptionService.getInviteCodes(user.getId());
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

