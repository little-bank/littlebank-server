package com.littlebank.finance.domain.point.service;

import com.littlebank.finance.domain.challenge.domain.ChallengeParticipation;
import com.littlebank.finance.domain.challenge.domain.repository.ChallengeParticipationRepository;
import com.littlebank.finance.domain.challenge.exception.ChallengeException;
import com.littlebank.finance.domain.goal.domain.Goal;
import com.littlebank.finance.domain.goal.domain.repository.GoalRepository;
import com.littlebank.finance.domain.goal.exception.GoalException;
import com.littlebank.finance.domain.mission.domain.Mission;
import com.littlebank.finance.domain.mission.domain.repository.MissionRepository;
import com.littlebank.finance.domain.mission.exception.MissionException;
import com.littlebank.finance.domain.notification.domain.Notification;
import com.littlebank.finance.domain.notification.domain.NotificationType;
import com.littlebank.finance.domain.notification.domain.repository.NotificationRepository;
import com.littlebank.finance.domain.point.domain.Refund;
import com.littlebank.finance.domain.point.domain.TransactionHistory;
import com.littlebank.finance.domain.point.domain.constant.RefundStatus;
import com.littlebank.finance.domain.point.domain.constant.RewardType;
import com.littlebank.finance.domain.point.domain.repository.PaymentRepository;
import com.littlebank.finance.domain.point.domain.repository.RefundRepository;
import com.littlebank.finance.domain.point.domain.repository.TransactionHistoryRepository;
import com.littlebank.finance.domain.point.dto.request.*;
import com.littlebank.finance.domain.point.dto.response.*;
import com.littlebank.finance.domain.point.exception.PointException;
import com.littlebank.finance.domain.user.domain.User;
import com.littlebank.finance.domain.user.domain.UserRole;
import com.littlebank.finance.domain.user.domain.repository.UserRepository;
import com.littlebank.finance.domain.user.exception.UserException;
import com.littlebank.finance.global.business.PointPolicy;
import com.littlebank.finance.global.common.CustomPageResponse;
import com.littlebank.finance.global.error.exception.ErrorCode;
import com.littlebank.finance.global.firebase.FirebaseService;
import com.littlebank.finance.global.toss.TossService;
import com.littlebank.finance.global.toss.config.TossProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class PointService {
    private final PaymentRepository paymentRepository;
    private final UserRepository userRepository;
    private final TransactionHistoryRepository transactionHistoryRepository;
    private final RefundRepository refundRepository;
    private final GoalRepository goalRepository;
    private final MissionRepository missionRepository;
    private final ChallengeParticipationRepository challengeParticipationRepository;
    private final NotificationRepository notificationRepository;
    private final FirebaseService firebaseService;
    private final TossProperties tossProperties;

    @Transactional(readOnly = true)
    public AmountTempSaveResponse tempSave(Long userId, AmountTempSaveRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserException(ErrorCode.USER_NOT_FOUND));

        String orderName = "포인트 충전";

        return AmountTempSaveResponse.of(request.getOrderId(), orderName, tossProperties, user);
    }

    @Transactional(readOnly = true)
    public CustomPageResponse<PaymentHistoryResponse> getPaymentHistory(Long userId, Pageable pageable) {
        return CustomPageResponse.of(paymentRepository.findHistoryByUserId(userId, pageable));
    }

    public CommonPointTransferResponse transferPointGeneral(Long userId, PointTransferRequest request) {
        User sender = userRepository.findByIdWithLock(userId)
                .orElseThrow(() -> new UserException(ErrorCode.USER_NOT_FOUND));
        if (sender.getPoint() < request.getPointAmount()) {
            throw new PointException(ErrorCode.INSUFFICIENT_POINT_BALANCE);
        }

        User receiver = userRepository.findByIdWithLock(request.getReceiverId())
                .orElseThrow(() -> new UserException(ErrorCode.USER_NOT_FOUND));

        sender.sendPoint(request.getPointAmount());
        receiver.receivePoint(request.getPointAmount());

        TransactionHistory transactionHistory = transactionHistoryRepository.save(
                TransactionHistory.builder()
                        .pointAmount(request.getPointAmount())
                        .message(request.getMessage())
                        .senderRemainingPoint(sender.getPoint())
                        .receiverRemainingPoint(receiver.getPoint())
                        .sender(sender)
                        .receiver(receiver)
                        .rewardType(RewardType.GENERAL)
                        .build()
        );

        try {
            Notification receiverNotification = notificationRepository.save(
                    Notification.builder()
                            .receiver(receiver)
                            .message(sender.getName() + "님에게 " + request.getPointAmount() + "포인트를 받았습니다!")
                            .subMessage("앱에 들어가서 확인해보세요!")
                            .type(NotificationType.POINT_TRANSFER)
                            .targetId(transactionHistory.getId())
                            .isRead(false)
                            .build()
            );
            firebaseService.sendNotification(receiverNotification);
        } catch (DataIntegrityViolationException e) {
            log.warn("이미 동일한 알림이 존재합니다.");
        }

        return CommonPointTransferResponse.of(transactionHistory);
    }

    public CommonPointTransferResponse transferPointMission(Long userId, MissionPointTransferRequest request) {
        User sender = userRepository.findByIdWithLock(userId)
                .orElseThrow(() -> new UserException(ErrorCode.USER_NOT_FOUND));
        if (sender.getPoint() < request.getPointAmount()) {
            throw new PointException(ErrorCode.INSUFFICIENT_POINT_BALANCE);
        }
        Mission mission = missionRepository.findById(request.getMissionId())
                .orElseThrow(() -> new MissionException(ErrorCode.MISSION_NOT_FOUND));

        User receiver = userRepository.findByIdWithLock(request.getReceiverId())
                .orElseThrow(() -> new UserException(ErrorCode.USER_NOT_FOUND));

        sender.sendPoint(request.getPointAmount());
        receiver.receivePoint(request.getPointAmount());

        TransactionHistory transactionHistory = transactionHistoryRepository.save(
                TransactionHistory.builder()
                        .pointAmount(request.getPointAmount())
                        .message(request.getMessage())
                        .senderRemainingPoint(sender.getPoint())
                        .receiverRemainingPoint(receiver.getPoint())
                        .sender(sender)
                        .receiver(receiver)
                        .rewardType(RewardType.MISSION)
                        .rewardId(mission.getId())
                        .build()
        );

        try {
            Notification receiverNotification = notificationRepository.save(
                    Notification.builder()
                            .receiver(receiver)
                            .message(sender.getName() + "님에게 미션 수행 보상으로 " + request.getPointAmount() + "포인트를 받았습니다!")
                            .subMessage("앱에 들어가서 확인해보세요!")
                            .type(NotificationType.POINT_TRANSFER)
                            .targetId(transactionHistory.getId())
                            .isRead(false)
                            .build()
            );
            firebaseService.sendNotification(receiverNotification);
        } catch (DataIntegrityViolationException e) {
            log.warn("이미 동일한 알림이 존재합니다.");
        }
        return CommonPointTransferResponse.of(transactionHistory);
    }

    public CommonPointTransferResponse transferPointChallenge(Long userId, ChallengePointTransferRequest request) {
        User sender = userRepository.findByIdWithLock(userId)
                .orElseThrow(() -> new UserException(ErrorCode.USER_NOT_FOUND));
        if (sender.getPoint() < request.getPointAmount()) {
            throw new PointException(ErrorCode.INSUFFICIENT_POINT_BALANCE);
        }
        ChallengeParticipation participation = challengeParticipationRepository.findById(request.getParticipationId())
                .orElseThrow(() -> new ChallengeException(ErrorCode.CHALLENGE_NOT_FOUND));

        User receiver = userRepository.findByIdWithLock(request.getReceiverId())
                .orElseThrow(() -> new UserException(ErrorCode.USER_NOT_FOUND));

        sender.sendPoint(request.getPointAmount());
        receiver.receivePoint(request.getPointAmount());

        TransactionHistory transactionHistory = transactionHistoryRepository.save(
                TransactionHistory.builder()
                        .pointAmount(request.getPointAmount())
                        .message(request.getMessage())
                        .senderRemainingPoint(sender.getPoint())
                        .receiverRemainingPoint(receiver.getPoint())
                        .sender(sender)
                        .receiver(receiver)
                        .rewardType(RewardType.CHALLENGE)
                        .rewardId(participation.getId())
                        .build()
        );

        try {
            Notification receiverNotification = notificationRepository.save(
                    Notification.builder()
                            .receiver(receiver)
                            .message(sender.getName() + "님에게 챌린지 참여 보상으로 " + request.getPointAmount() + "포인트를 받았습니다!")
                            .subMessage("앱에 들어가서 확인해보세요!")
                            .type(NotificationType.POINT_TRANSFER)
                            .targetId(transactionHistory.getId())
                            .isRead(false)
                            .build()
            );
            firebaseService.sendNotification(receiverNotification);
        } catch (DataIntegrityViolationException e) {
            log.warn("이미 동일한 알림이 존재합니다.");
        }
        return CommonPointTransferResponse.of(transactionHistory);
    }

    public CommonPointTransferResponse transferPointGoal(Long userId, GoalPointTransferRequest request) {
        User sender = userRepository.findByIdWithLock(userId)
                .orElseThrow(() -> new UserException(ErrorCode.USER_NOT_FOUND));
        if (sender.getPoint() < request.getPointAmount()) {
            throw new PointException(ErrorCode.INSUFFICIENT_POINT_BALANCE);
        }
        Goal goal = goalRepository.findById(request.getGoalId())
                .orElseThrow(() -> new GoalException(ErrorCode.GOAL_NOT_FOUND));

        User receiver = userRepository.findByIdWithLock(request.getReceiverId())
                .orElseThrow(() -> new UserException(ErrorCode.USER_NOT_FOUND));

        sender.sendPoint(request.getPointAmount());
        receiver.receivePoint(request.getPointAmount());

        TransactionHistory transactionHistory = transactionHistoryRepository.save(
                TransactionHistory.builder()
                        .pointAmount(request.getPointAmount())
                        .message(request.getMessage())
                        .senderRemainingPoint(sender.getPoint())
                        .receiverRemainingPoint(receiver.getPoint())
                        .sender(sender)
                        .receiver(receiver)
                        .rewardType(RewardType.GOAL)
                        .rewardId(goal.getId())
                        .build()
        );

        try {
            Notification receiverNotification = notificationRepository.save(
                    Notification.builder()
                            .receiver(receiver)
                            .message(sender.getName() + "님에게 목표 실천 보상으로 " + request.getPointAmount() + "포인트를 받았습니다!")
                            .subMessage("앱에 들어가서 확인해보세요!")
                            .type(NotificationType.POINT_TRANSFER)
                            .targetId(transactionHistory.getId())
                            .isRead(false)
                            .build()
            );
            firebaseService.sendNotification(receiverNotification);
        } catch (DataIntegrityViolationException e) {
            log.warn("이미 동일한 알림이 존재합니다.");
        }
        return CommonPointTransferResponse.of(transactionHistory);
    }
    @Transactional(readOnly = true)
    public CustomPageResponse<ReceivePointHistoryResponse> getReceivedPointHistory(Long userId, Pageable pageable) {
        List<ReceivePointHistoryResponse> sendResults = transactionHistoryRepository.findReceivedPointHistoryByUserId(userId);
        List<ReceivePointHistoryResponse> paymentResults = paymentRepository.findPaymentHistoryByUserId(userId);

        List<ReceivePointHistoryResponse> merged = new ArrayList<>();
        merged.addAll(sendResults);
        merged.addAll(paymentResults);

        merged.sort((a, b) -> b.getReceivedAt().compareTo(a.getReceivedAt()));

        int start = (int) pageable.getOffset();
        int end = Math.min(start + pageable.getPageSize(), merged.size());
        List<ReceivePointHistoryResponse> pageContent = merged.subList(start, end);

        return CustomPageResponse.of(new PageImpl<>(pageContent, pageable, merged.size()));
    }

    @Transactional(readOnly = true)
    public CustomPageResponse<SendPointHistoryResponse> getSentPointHistory(Long userId, Pageable pageable) {
        List<SendPointHistoryResponse> sendResults = transactionHistoryRepository.findSentPointHistoryByUserId(userId);
        List<SendPointHistoryResponse> refundResults = refundRepository.findRefundHistoryByUserId(userId);

        List<SendPointHistoryResponse> merged = new ArrayList<>();
        merged.addAll(sendResults);
        merged.addAll(refundResults);

        merged.stream().forEach(e -> {
            System.out.println(e.getHistoryId() + "," + e.getSentAt());
        });

        merged.sort((a, b) -> b.getSentAt().compareTo(a.getSentAt()));

        int start = (int) pageable.getOffset();
        int end = Math.min(start + pageable.getPageSize(), merged.size());
        List<SendPointHistoryResponse> pageContent = merged.subList(start, end);

        return CustomPageResponse.of(new PageImpl<>(pageContent, pageable, merged.size()));
    }

    @Transactional(readOnly = true)
    public CustomPageResponse<LatestSentAccountResponse> getLatestSentAccount(Long userId, Pageable pageable) {
        return CustomPageResponse.of(transactionHistoryRepository.findLatestSentAccountByUserId(userId, pageable));
    }

    public PointRefundResponse refundPointByParent(Long userId, ParentPointRefundRequest request) {
        User user = userRepository.findByIdWithLock(userId)
                .orElseThrow(() -> new UserException(ErrorCode.USER_NOT_FOUND));
        if (user.getRole() != UserRole.PARENT) {
            throw new UserException(ErrorCode.FORBIDDEN_PARENT_ONLY);
        }
        if (user.getBankName() == null || user.getBankAccount() == null) {
            throw new UserException(ErrorCode.EXCHANGE_ACCOUNT_NOT_REGISTERED);
        }
        if (user.getPoint() < request.getExchangeAmount()) {
            throw new PointException(ErrorCode.INSUFFICIENT_POINT_BALANCE);
        }

        user.exchangePointToMoney(request.getExchangeAmount());

        int processedAmount = request.getExchangeAmount();

        Refund refund = refundRepository.save(
                Refund.builder()
                        .requestedAmount(request.getExchangeAmount())
                        .processedAmount(processedAmount)
                        .remainingPoint(user.getPoint())
                        .status(RefundStatus.WAIT)
                        .user(user)
                        .depositTargetUser(user)
                        .build()
        );
        return PointRefundResponse.of(refund);
    }

    public PointRefundResponse refundPointByChild(Long userId, ChildPointRefundRequest request) {
        User user = userRepository.findByIdWithLock(userId)
                .orElseThrow(() -> new UserException(ErrorCode.USER_NOT_FOUND));
        if (user.getRole() != UserRole.CHILD) {
            throw new UserException(ErrorCode.FORBIDDEN_CHILD_ONLY);
        }

        User depositTargetUser = userRepository.findById(request.getDepositTargetUserId())
                .orElseThrow(() -> new UserException(ErrorCode.USER_NOT_FOUND));
        if (depositTargetUser.getBankName() == null || depositTargetUser.getBankAccount() == null) {
            throw new UserException(ErrorCode.EXCHANGE_ACCOUNT_NOT_REGISTERED);
        }

        if (user.getPoint() < request.getExchangeAmount()) {
            throw new PointException(ErrorCode.INSUFFICIENT_POINT_BALANCE);
        }

        user.exchangePointToMoney(request.getExchangeAmount());

        int processedAmount = request.getExchangeAmount();
        if (request.getExchangeAmount() < PointPolicy.EXCHANGE_FEE_EXEMPTION_AMOUNT) {
            processedAmount -= PointPolicy.CHILD_COMMISSION;
        }

        Refund refund = refundRepository.save(
                Refund.builder()
                        .requestedAmount(request.getExchangeAmount())
                        .processedAmount(processedAmount)
                        .remainingPoint(user.getPoint())
                        .status(RefundStatus.WAIT)
                        .user(user)
                        .depositTargetUser(depositTargetUser)
                        .build()
        );

        try {
            Notification notification = Notification.builder()
                    .receiver(depositTargetUser)
                    .message(user.getName() + "님이 " + processedAmount + "포인트 꺼내기를 요청했습니다!")
                    .subMessage("앱에서 확인해보세요!")
                    .type(NotificationType.POINT_REFUND)
                    .targetId(refund.getId())
                    .isRead(false)
                    .build();
            notificationRepository.save(notification);
            firebaseService.sendNotification(notification);
        } catch (DataIntegrityViolationException e) {
            log.warn("이미 동일한 알림이 존재합니다.");
        }

        return PointRefundResponse.of(refund);
    }

    @Transactional(readOnly = true)
    public List<LatestRefundDepositTargetResponse> getRefundLatestDepositTarget(Long userId) {
        return refundRepository.findRefundDepositTargetByUserId(userId);
    }

    @Transactional(readOnly = true)
    public CustomPageResponse<RefundHistoryResponse> getRefundHistory(Long userId, Pageable pageable) {
        return CustomPageResponse.of(refundRepository.findRefundHistoryByUserId(userId, pageable));
    }

    public void cancelRefund(Long userId, Long refundId) {
        User user = userRepository.findByIdWithLock(userId)
                .orElseThrow(() -> new UserException(ErrorCode.USER_NOT_FOUND));
        Refund refund = refundRepository.findById(refundId)
                .orElseThrow(() -> new PointException(ErrorCode.REFUND_NOT_FOUND));

        user.cancelRefund(refund.getRequestedAmount());

        refundRepository.deleteById(refund.getId());
    }

}
