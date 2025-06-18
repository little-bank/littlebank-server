package com.littlebank.finance.domain.subscription.service;

import com.littlebank.finance.domain.subscription.domain.InviteCode;
import com.littlebank.finance.domain.subscription.domain.Subscription;
import com.littlebank.finance.domain.subscription.domain.repository.InviteCodeRepository;
import com.littlebank.finance.domain.subscription.domain.repository.SubscriptionRepository;
import com.littlebank.finance.domain.subscription.dto.request.SubscriptionCreateRequestDto;
import com.littlebank.finance.domain.subscription.dto.request.SubscriptionPurchaseRequestDto;
import com.littlebank.finance.domain.subscription.dto.response.GoogleIAPResponseDto;
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
    public SubscriptionResponseDto createSubscription(Long userId, SubscriptionCreateRequestDto request) {
        User owner = userRepository.findById(userId)
                .orElseThrow(() -> new UserException(ErrorCode.USER_NOT_FOUND));
        LocalDateTime startDate = LocalDateTime.now();
        LocalDateTime endDate = startDate.plusMonths(1);

        Subscription subscription = Subscription.builder()
                .owner(owner)
                .seat(request.getSeat())
                .startDate(startDate)
                .endDate(endDate)
                .build();
        subscription = subscriptionRepository.save(subscription);

        owner.setSubscription(subscription);
        userRepository.save(owner);

        for (int i=0; i< request.getSeat() -1; i++) {
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
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserException(ErrorCode.USER_NOT_FOUND));
        List<Subscription> subscriptions = new ArrayList<>();
        List<Subscription> owned = subscriptionRepository.findByOwner(user);
        subscriptions.addAll(owned);

        if (user.getSubscription() != null && !owned.contains(user.getSubscription())) {
            subscriptions.add(user.getSubscription());
        }
        subscriptions.sort((a,b) -> b.getStartDate().compareTo(a.getStartDate()));
        return subscriptions.stream()
                .map(s -> SubscriptionResponseDto.of(s))
                .collect(Collectors.toList());
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
}
