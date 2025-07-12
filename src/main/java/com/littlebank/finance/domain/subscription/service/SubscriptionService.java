package com.littlebank.finance.domain.subscription.service;


import com.littlebank.finance.domain.subscription.domain.InviteCode;
import com.littlebank.finance.domain.subscription.domain.Subscription;
import com.littlebank.finance.domain.subscription.domain.TrialSubscription;
import com.littlebank.finance.domain.subscription.domain.repository.InviteCodeRepository;
import com.littlebank.finance.domain.subscription.domain.repository.SubscriptionRepository;
import com.littlebank.finance.domain.subscription.domain.repository.TrialSubscriptionRepository;
import com.littlebank.finance.domain.subscription.dto.request.FreeSubscriptionRequestDto;
import com.littlebank.finance.domain.subscription.dto.request.SubscriptionCreateRequestDto;
import com.littlebank.finance.domain.subscription.dto.request.SubscriptionDeleteRequest;
import com.littlebank.finance.domain.subscription.dto.response.FreeSubscriptionResponseDto;
import com.littlebank.finance.domain.subscription.dto.response.InviteCodeResponseDto;
import com.littlebank.finance.domain.subscription.dto.response.SubscriptionResponseDto;
import com.littlebank.finance.domain.subscription.exception.SubscriptionException;
import com.littlebank.finance.domain.user.domain.User;
import com.littlebank.finance.domain.user.domain.repository.UserRepository;
import com.littlebank.finance.domain.user.exception.UserException;
import com.littlebank.finance.global.error.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class SubscriptionService {
    private final UserRepository userRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final InviteCodeRepository inviteCodeRepository;
    private final TrialSubscriptionRepository trialSubscriptionRepository;
    public SubscriptionResponseDto createSubscription(Long userId, SubscriptionCreateRequestDto request) {
        User owner = userRepository.findById(userId)
                .orElseThrow(() -> new UserException(ErrorCode.USER_NOT_FOUND));
        LocalDateTime startDate = LocalDateTime.now();
        LocalDateTime endDate = startDate.plusMonths(1);
        boolean includeOwner = request.isIncludeOwner() || request.getSeat() == 1;

        Subscription subscription = Subscription.builder()
                .owner(owner)
                .seat(request.getSeat())
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .purchaseToken(request.getPurchaseToken())
                .build();
        subscription = subscriptionRepository.save(subscription);

        if (includeOwner) {
            owner.setSubscription(subscription);
            userRepository.save(owner);
        }
        int inviteCodeCount = includeOwner? request.getSeat()-1: request.getSeat();

        for (int i=0; i< inviteCodeCount; i++) {
            String code = UUID.randomUUID().toString().replace("-","").substring(0,12);
            InviteCode inviteCode = InviteCode.builder()
                    .code(code)
                    .used(false)
                    .subscription(subscription)
                    .build();
            inviteCodeRepository.save(inviteCode);
            subscription.addInviteCode(inviteCode);
        }

        return SubscriptionResponseDto.of(subscription);
    }

    public List<SubscriptionResponseDto> getMySubscription(Long userId) {
        List<Subscription> subscriptionss = subscriptionRepository.findAllByUserId(userId);
        return subscriptionss.stream()
                .map(s -> SubscriptionResponseDto.of(s))
                .collect(Collectors.toList());
    }

    public void deleteSubscription(Long userId, SubscriptionDeleteRequest request) {
        Subscription subscription = subscriptionRepository.findTopByOwnerIdOrderByStartDateDesc(userId)
                .orElseThrow(() -> new SubscriptionException(ErrorCode.SUBSCRIPTION_NOT_FOUND));

        if (!subscription.getOwner().getId().equals(userId)) {
            throw new SubscriptionException(ErrorCode.SUBSCRIPTION_NOT_FOUND);
        }
        subscription.setIsDeleted(true);
        subscription.getInviteCodes().forEach(inviteCode -> inviteCode.setIsDeleted(true));
    }

    public SubscriptionResponseDto redeemSubscription(Long userId, String code) {
        InviteCode inviteCode = inviteCodeRepository.findByCodeAndUsedFalse(code)
                .orElseThrow(() -> new SubscriptionException(ErrorCode.INVITECODE_NOT_FOUND));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserException(ErrorCode.USER_NOT_FOUND));
        Subscription subscription = inviteCode.getSubscription();

        if (subscription.getMembers().contains(user)) {
            throw new SubscriptionException(ErrorCode.ALREADY_SUBSCRIBED);
        }

        subscription.getMembers().add(user);
        user.setSubscription(subscription);
        inviteCode.setIsUsed();
        inviteCode.setRedeemedBy(user);
        inviteCodeRepository.save(inviteCode);
        subscriptionRepository.save(subscription);

        return SubscriptionResponseDto.of(subscription);
    }

    public FreeSubscriptionResponseDto startFreeSubscription(Long userId, FreeSubscriptionRequestDto request) {
        if (!"littlebank".equals(request.getCode())) {
            throw new SubscriptionException(ErrorCode.INVALID_CODE);
        }
        if (trialSubscriptionRepository.existsByUserId(userId)) {
            throw new SubscriptionException(ErrorCode.ALREADY_USED_TRIAL);
        }
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserException(ErrorCode.USER_NOT_FOUND));
        TrialSubscription trial = TrialSubscription.builder()
                .user(user)
                .startDate(LocalDateTime.now())
                .endDate(LocalDateTime.now().plusWeeks(2))
                .used(true)
                .build();

        trialSubscriptionRepository.save(trial);
        return FreeSubscriptionResponseDto.of(trial);
    }

    public FreeSubscriptionResponseDto getFreeSubscription(Long userId) {
        return trialSubscriptionRepository.findByUserId(userId)
                .map(FreeSubscriptionResponseDto::of)
                .orElseGet(() -> FreeSubscriptionResponseDto.builder()
                        .trialId(null)
                        .startDate(null)
                        .endDate(null)
                        .used(false)
                        .build()
                );
    }


    public void includeOwnerToSubscription(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserException(ErrorCode.USER_NOT_FOUND));

        Subscription subscription = subscriptionRepository.findTopByOwnerIdOrderByStartDateDesc(userId)
                .orElseThrow(() -> new SubscriptionException(ErrorCode.SUBSCRIPTION_NOT_FOUND));
        if (subscription.getMembers().contains(user)) return;

        if (subscription.getMembers().size() >= subscription.getSeat()) {
            throw new SubscriptionException(ErrorCode.EXCEEDED_SUBSCRIPTUIN_SEATS);
        }

        InviteCode inviteCode = inviteCodeRepository
                .findFirstBySubscriptionIdAndUsedFalseOrderByIdAsc(subscription.getId())
                .orElseThrow(() -> new SubscriptionException(ErrorCode.INVITECODE_NOT_FOUND));

        inviteCode.setIsUsed();
        inviteCode.setRedeemedBy(user);
        inviteCodeRepository.save(inviteCode);

        user.setSubscription(subscription);
        userRepository.save(user);
    }

    public List<InviteCodeResponseDto> getInviteCodes(Long userId) {
        List<InviteCodeResponseDto> response = inviteCodeRepository.findAllByOwnerId(userId);
        return response;
    }

}
