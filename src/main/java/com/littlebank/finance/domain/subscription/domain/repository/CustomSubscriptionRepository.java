package com.littlebank.finance.domain.subscription.domain.repository;

import com.littlebank.finance.domain.subscription.domain.Subscription;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface CustomSubscriptionRepository {
   Page<Subscription> findSubscriptionsByUserId(Long userId, Pageable pageable);
}
