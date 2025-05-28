package com.littlebank.finance.domain.challenge.domain.repository;

import com.littlebank.finance.domain.challenge.domain.ChallengeParticipation;
import com.littlebank.finance.domain.challenge.domain.ChallengeStatus;
import com.littlebank.finance.domain.challenge.domain.QChallenge;
import com.littlebank.finance.domain.challenge.domain.QChallengeParticipation;
import com.littlebank.finance.domain.user.domain.QUser;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;

import java.util.List;

@RequiredArgsConstructor
public class CustomChallengeParticipationRepositoryImpl implements CustomChallengeParticipationRepository {

    private final JPAQueryFactory queryFactory;

    QChallengeParticipation cp = QChallengeParticipation.challengeParticipation;
    QChallenge c = QChallenge.challenge;
    QUser u = QUser.user;

    @Override
    public Page<ChallengeParticipation> findOngoingParticipations(Long userId, Pageable pageable) {
        List<ChallengeParticipation> content = queryFactory
                .selectFrom(cp)
                .join(cp.challenge, c).fetchJoin()
                .join(cp.user, u).fetchJoin()
                .where(
                        cp.user.id.eq(userId),
                        cp.isDeleted.isFalse(),
                        cp.challenge.isDeleted.isFalse(),
                        cp.challenge.endDate.goe(java.time.LocalDate.now())
                )
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        return PageableExecutionUtils.getPage(content, pageable,
                () -> queryFactory
                        .select(cp.count())
                        .from(cp)
                        .where(
                                cp.user.id.eq(userId),
                                cp.isDeleted.isFalse(),
                                cp.challenge.isDeleted.isFalse(),
                                cp.challenge.endDate.goe(java.time.LocalDate.now())
                        )
                        .fetchOne());
    }

    @Override
    public Page<ChallengeParticipation> findCompletedParticipations(Long userId, Pageable pageable) {
        List<ChallengeParticipation> content = queryFactory
                .selectFrom(cp)
                .join(cp.challenge, c).fetchJoin()
                .join(cp.user, u).fetchJoin()
                .where(
                        cp.user.id.eq(userId),
                        cp.isDeleted.isFalse(),
                        cp.challenge.isDeleted.isFalse(),
                        cp.challenge.endDate.lt(java.time.LocalDate.now())
                )
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        return PageableExecutionUtils.getPage(content, pageable,
                () -> queryFactory
                        .select(cp.count())
                        .from(cp)
                        .where(
                                cp.user.id.eq(userId),
                                cp.isDeleted.isFalse(),
                                cp.challenge.isDeleted.isFalse(),
                                cp.challenge.endDate.lt(java.time.LocalDate.now())
                        )
                        .fetchOne());
    }

    @Override
    public Page<ChallengeParticipation> findByUserIdAndChallengeStatusIn(Long userId, List<ChallengeStatus> statuses, Pageable pageable) {
        QChallengeParticipation participation = QChallengeParticipation.challengeParticipation;

        BooleanExpression condition = participation.user.id.eq(userId)
                .and(participation.challengeStatus.in(statuses))
                .and(participation.isDeleted.eq(false));

        long total = queryFactory
                .select(participation.count())
                .from(participation)
                .where(condition)
                .fetchOne();

        List<ChallengeParticipation> content = queryFactory
                .selectFrom(participation)
                .where(condition)
                .orderBy(participation.createdDate.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        return new PageImpl<>(content, pageable, total);
    }
}