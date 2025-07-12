package com.littlebank.finance.domain.subscription.domain.repository;

import com.littlebank.finance.domain.subscription.domain.Subscription;

import java.util.List;

public interface CustomSubscriptionRepository {
    List<Subscription> findAllByUserId(Long userId);
}
