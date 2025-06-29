package com.littlebank.finance.domain.point.domain.repository.impl;

import com.littlebank.finance.domain.point.domain.QRefund;
import com.littlebank.finance.domain.point.domain.RefundStatus;
import com.littlebank.finance.domain.point.domain.repository.CustomRefundRepository;
import com.littlebank.finance.domain.point.dto.response.LatestRefundDepositTargetResponse;
import com.littlebank.finance.domain.point.dto.response.SendPointHistoryResponse;
import com.littlebank.finance.domain.point.dto.response.RefundHistoryResponse;
import com.littlebank.finance.domain.user.domain.QUser;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.List;

import static com.littlebank.finance.domain.point.domain.QRefund.refund;
import static com.littlebank.finance.domain.user.domain.QUser.user;

@RequiredArgsConstructor
public class CustomRefundRepositoryImpl implements CustomRefundRepository {
    private final JPAQueryFactory queryFactory;
    private QRefund r = refund;
    private QUser u = user;
    private final static int LATEST_REFUND_DEPOSIT_TARGET_ROW = 3;

    @Override
    public Page<RefundHistoryResponse> findRefundHistoryByUserId(Long userId, Pageable pageable) {
        List<RefundHistoryResponse> results =
                queryFactory.select(Projections.constructor(
                                RefundHistoryResponse.class,
                                r.id,
                                r.requestedAmount,
                                r.processedAmount,
                                r.status,
                                r.createdDate,
                                u.id,
                                u.name,
                                u.bankName,
                                u.bankAccount
                        ))
                        .from(r)
                        .join(r.depositTargetUser, u)
                        .where(r.user.id.eq(userId))
                        .offset(pageable.getOffset())
                        .limit(pageable.getPageSize())
                        .orderBy(r.createdDate.desc())
                        .fetch();

        return new PageImpl<>(results, pageable, results.size());
    }

    @Override
    public List<SendPointHistoryResponse> findRefundHistoryByUserId(Long userId) {
        List<SendPointHistoryResponse> results = queryFactory
                .select(Projections.constructor(
                        SendPointHistoryResponse.class,
                        r.id,
                        Expressions.constant("REFUND"),
                        r.requestedAmount,
                        Expressions.stringTemplate("CONCAT('내 계좌로 ', {0}, '원 환전')", r.processedAmount),
                        r.remainingPoint,
                        Expressions.nullExpression(Long.class),
                        Expressions.constant("내 계좌"),
                        r.exchangedAt
                ))
                .from(r)
                .where(r.user.id.eq(userId)
                        .and(r.status.eq(RefundStatus.PROCESSED)))
                .fetch();

        return results;
    }

    @Override
    public Page<LatestRefundDepositTargetResponse> findRefundDepositTargetByUserId(Long userId, Pageable pageable) {
        List<LatestRefundDepositTargetResponse> results = queryFactory
                .select(Projections.constructor(
                        LatestRefundDepositTargetResponse.class,
                        r.id,
                        u.id,
                        u.name,
                        u.bankName,
                        u.bankAccount,
                        r.createdDate
                ))
                .from(r)
                .join(r.depositTargetUser, u)
                .where(r.user.id.eq(userId))
                .orderBy(r.createdDate.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        return new PageImpl<>(results, pageable, results.size());
    }
}
