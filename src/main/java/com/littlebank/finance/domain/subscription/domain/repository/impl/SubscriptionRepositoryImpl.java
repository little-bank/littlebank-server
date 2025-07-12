package com.littlebank.finance.domain.subscription.domain.repository.impl;

import com.littlebank.finance.domain.subscription.domain.QInviteCode;
import com.littlebank.finance.domain.subscription.domain.QSubscription;
import com.littlebank.finance.domain.subscription.domain.Subscription;
import com.littlebank.finance.domain.subscription.domain.repository.CustomSubscriptionRepository;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import java.util.ArrayList;
import java.util.List;
import static com.littlebank.finance.domain.subscription.domain.QInviteCode.inviteCode;
import static com.littlebank.finance.domain.subscription.domain.QSubscription.subscription;

@RequiredArgsConstructor
public class SubscriptionRepositoryImpl implements CustomSubscriptionRepository {
    private final JPAQueryFactory queryFactory;
    private QSubscription s = subscription;
    private QInviteCode i = inviteCode;

    @Override
    public List<Subscription> findAllByUserId(Long userId) {
        List<Subscription> owned = queryFactory
                .selectFrom(s)
                .where(s.owner.id.eq(userId))
                .fetch();

        List<Subscription> redeemed = queryFactory
                .select(i.subscription)
                .from(i)
                .where(i.redeemedBy.id.eq(userId))
                .fetch();

        List<Subscription> all = new ArrayList<>();
        all.addAll(owned);
        all.addAll(redeemed);
        return all;
    }
}
