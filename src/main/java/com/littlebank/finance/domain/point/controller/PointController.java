package com.littlebank.finance.domain.point.controller;

import com.littlebank.finance.domain.point.dto.request.*;
import com.littlebank.finance.domain.point.dto.response.*;
import com.littlebank.finance.domain.point.service.PointService;

import com.littlebank.finance.global.common.CustomPageResponse;
import com.littlebank.finance.global.common.PaginationPolicy;
import com.littlebank.finance.global.security.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api-user/point")
@RequiredArgsConstructor
@Tag(name = "Point")
public class PointController {
    private final PointService pointService;

    @Operation(summary = "포인트 충전 금액 임시 저장 API")
    @PostMapping("/temp/save-amount")
    public ResponseEntity<AmountTempSaveResponse> tempSave(
            HttpSession session,
            @RequestBody @Valid AmountTempSaveRequest saveAmountRequest,
            @AuthenticationPrincipal CustomUserDetails customUserDetails
    ) {
        session.setAttribute(saveAmountRequest.getOrderId(), saveAmountRequest.getAmount());
        return ResponseEntity.ok(pointService.tempSave(customUserDetails.getId(), saveAmountRequest));
    }

    @Operation(summary = "임시 저장한 포인트 충전 금액 검증 API")
    @PostMapping("/temp/verify-amount")
    public ResponseEntity<VerifyTempSaveAmountResponse> verifyTempSaveAmount(
            HttpSession session,
            @RequestBody @Valid AmountTempSaveRequest saveAmountRequest,
            @AuthenticationPrincipal CustomUserDetails customUserDetails
    ) {
        Integer amount = (Integer) session.getAttribute(saveAmountRequest.getOrderId());

        if(amount == null || !amount.equals(saveAmountRequest.getAmount()))
            return ResponseEntity.badRequest().body(VerifyTempSaveAmountResponse.builder().code(400).message("결제 금액 정보가 유효하지 않습니다.").build());

        session.removeAttribute(saveAmountRequest.getOrderId());
        return ResponseEntity.ok(VerifyTempSaveAmountResponse.builder().code(200).message("결제 금액 정보가 일치합니다.").build());
    }

    @Operation(summary = "포인트 충전 내역 조회 API")
    @GetMapping("/charge/payment/history")
    public ResponseEntity<CustomPageResponse<PaymentHistoryResponse>> getPaymentHistory(
            @Parameter(description = "페이지 번호, 0부터 시작")
            @RequestParam(name = "pageNumber") Integer pageNumber,
            @AuthenticationPrincipal CustomUserDetails customUserDetails
    ) {
        Pageable pageable = PageRequest.of(pageNumber, PaginationPolicy.GENERAL_PAGE_SIZE);
        CustomPageResponse<PaymentHistoryResponse> response = pointService.getPaymentHistory(customUserDetails.getId(), pageable);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "일반 포인트 보내기 API")
    @PostMapping("/transfer/general")
    public ResponseEntity<CommonPointTransferResponse> transferPointGeneral(
            @RequestBody @Valid PointTransferRequest request,
            @AuthenticationPrincipal CustomUserDetails customUserDetails
    ) {
        CommonPointTransferResponse response = pointService.transferPointGeneral(customUserDetails.getId(), request);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "미션 보상 포인트 보내기 API")
    @PostMapping("/transfer/mission")
    public ResponseEntity<CommonPointTransferResponse> transferPointMission(
            @RequestBody @Valid MissionPointTransferRequest request,
            @AuthenticationPrincipal CustomUserDetails customUserDetails
    ) {
        CommonPointTransferResponse response = pointService.transferPointMission(customUserDetails.getId(), request);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "챌린지 보상 포인트 보내기 API")
    @PostMapping("/transfer/challenge")
    public ResponseEntity<CommonPointTransferResponse> transferPointChallenge(
            @RequestBody @Valid ChallengePointTransferRequest request,
            @AuthenticationPrincipal CustomUserDetails customUserDetails
    ) {
        CommonPointTransferResponse response = pointService.transferPointChallenge(customUserDetails.getId(), request);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "목표 보상 포인트 보내기 API")
    @PostMapping("/transfer/goal")
    public ResponseEntity<CommonPointTransferResponse> transferPointGoal(
            @RequestBody @Valid GoalPointTransferRequest request,
            @AuthenticationPrincipal CustomUserDetails customUserDetails
    ) {
        CommonPointTransferResponse response = pointService.transferPointGoal(customUserDetails.getId(), request);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "들어온 포인트 내역 조회 API")
    @GetMapping("/transfer/receive/history")
    public ResponseEntity<CustomPageResponse<ReceivePointHistoryResponse>> getReceivedPointHistory(
            @Parameter(description = "페이지 번호, 0부터 시작")
            @RequestParam(name = "pageNumber") Integer pageNumber,
            @AuthenticationPrincipal CustomUserDetails customUserDetails
    ) {
        Pageable pageable = PageRequest.of(pageNumber, PaginationPolicy.GENERAL_PAGE_SIZE);
        CustomPageResponse<ReceivePointHistoryResponse> response = pointService.getReceivedPointHistory(customUserDetails.getId(), pageable);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "나간 포인트 내역 조회 API")
    @GetMapping("/transfer/sent/history")
    public ResponseEntity<CustomPageResponse<SendPointHistoryResponse>> getSentPointHistory(
            @Parameter(description = "페이지 번호, 0부터 시작")
            @RequestParam(name = "pageNumber") Integer pageNumber,
            @AuthenticationPrincipal CustomUserDetails customUserDetails
    ) {
        Pageable pageable = PageRequest.of(pageNumber, PaginationPolicy.GENERAL_PAGE_SIZE);
        CustomPageResponse<SendPointHistoryResponse> response = pointService.getSentPointHistory(customUserDetails.getId(), pageable);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "최근 포인트를 보낸 대상 조회 API")
    @GetMapping("/transfer/latest/account")
    public ResponseEntity<CustomPageResponse<LatestSentAccountResponse>> getLatestSentAccount(
            @Parameter(description = "페이지 번호, 0부터 시작")
            @RequestParam(name = "pageNumber") Integer pageNumber,
            @AuthenticationPrincipal CustomUserDetails customUserDetails
    ) {
        Pageable pageable = PageRequest.of(pageNumber, PaginationPolicy.GENERAL_PAGE_SIZE);
        CustomPageResponse<LatestSentAccountResponse> response = pointService.getLatestSentAccount(customUserDetails.getId(), pageable);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "(부모)포인트 꺼내기 API")
    @PostMapping("/parent/refund")
    public ResponseEntity<PointRefundResponse> refundPointByParent(
            @RequestBody @Valid ParentPointRefundRequest request,
            @AuthenticationPrincipal CustomUserDetails customUserDetails
    ) {
        PointRefundResponse response = pointService.refundPointByParent(customUserDetails.getId(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(summary = "(아이)포인트 꺼내기 API")
    @PostMapping("/child/refund")
    public ResponseEntity<PointRefundResponse> refundPointByChild(
            @RequestBody @Valid ChildPointRefundRequest request,
            @AuthenticationPrincipal CustomUserDetails customUserDetails
    ) {
        PointRefundResponse response = pointService.refundPointByChild(customUserDetails.getId(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(summary = "최근 포인트를 꺼낸 대상 조회 API")
    @GetMapping("/refund/latest/deposit-target")
    public ResponseEntity<List<LatestRefundDepositTargetResponse>> getRefundLatestDepositTarget(
            @AuthenticationPrincipal CustomUserDetails customUserDetails
    ) {
        List<LatestRefundDepositTargetResponse> response = pointService.getRefundLatestDepositTarget(customUserDetails.getId());
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "포인트 꺼내기 내역 조회 API")
    @GetMapping("/refund/history")
    public ResponseEntity<CustomPageResponse<RefundHistoryResponse>> getRefundHistory(
            @Parameter(description = "페이지 번호, 0부터 시작")
            @RequestParam(name = "pageNumber") Integer pageNumber,
            @AuthenticationPrincipal CustomUserDetails customUserDetails
    ) {
        Pageable pageable = PageRequest.of(pageNumber, PaginationPolicy.GENERAL_PAGE_SIZE);
        CustomPageResponse<RefundHistoryResponse> response = pointService.getRefundHistory(customUserDetails.getId(), pageable);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "포인트 꺼내기 취소 API")
    @DeleteMapping("/refund/cancel/{refundId}")
    public ResponseEntity<Void> cancelRefund(
            @Parameter(description = "포인트 환전 내역 식별 id")
            @PathVariable(name = "refundId") Long refundId,
            @AuthenticationPrincipal CustomUserDetails customUserDetails
    ) {
        pointService.cancelRefund(customUserDetails.getId(), refundId);
        return ResponseEntity.noContent().build();
    }
}
