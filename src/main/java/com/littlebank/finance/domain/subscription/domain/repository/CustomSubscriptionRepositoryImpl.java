package com.littlebank.finance.domain.subscription.domain.repository;

import com.littlebank.finance.domain.subscription.domain.QSubscription;
import com.littlebank.finance.domain.subscription.domain.Subscription;
import com.littlebank.finance.domain.user.domain.QUser;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.List;

import static com.littlebank.finance.domain.subscription.domain.QSubscription.subscription;
import static com.littlebank.finance.domain.user.domain.QUser.user;

@RequiredArgsConstructor
public class CustomSubscriptionRepositoryImpl implements CustomSubscriptionRepository {
    private final JPAQueryFactory queryFactory;
    private QSubscription s = subscription;
    private QUser u = user;
    QUser owner = new QUser("owner");
    QUser member = new QUser("member");
    @Override
    public Page<Subscription> findSubscriptionsByUserId(Long userId, Pageable pageable) {
        QSubscription s = QSubscription.subscription;
        QUser owner = new QUser("owner");
        QUser member = new QUser("member");

        List<Subscription> results = queryFactory
                .selectFrom(s)
                .leftJoin(s.owner, owner).fetchJoin()
                .leftJoin(s.members, member).fetchJoin()
                .leftJoin(s.inviteCodes).fetchJoin()
                .where(
                        s.owner.id.eq(userId)
                                .or(member.id.eq(userId))
                )
                .distinct()
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .orderBy(s.startDate.desc())
                .fetch();

        Long total = queryFactory
                .select(s.countDistinct())
                .from(s)
                .leftJoin(s.owner, owner)
                .leftJoin(s.members, member)
                .where(
                        s.owner.id.eq(userId)
                                .or(member.id.eq(userId))
                )
                .fetchOne();

        return new PageImpl<>(results, pageable, total != null ? total : 0);
    }

}
