package com.littlebank.finance.domain.user.domain.repository.impl;

import com.littlebank.finance.domain.challenge.domain.ChallengeStatus;
import com.littlebank.finance.domain.challenge.domain.QChallengeParticipation;
import com.littlebank.finance.domain.friend.domain.QFriend;
import com.littlebank.finance.domain.friend.dto.response.CommonFriendInfoResponse;
import com.littlebank.finance.domain.mission.domain.MissionStatus;
import com.littlebank.finance.domain.mission.domain.QMission;
import com.littlebank.finance.domain.ranking.dto.response.TargetAmountResetDto;
import com.littlebank.finance.domain.user.domain.QUser;
import com.littlebank.finance.domain.user.domain.repository.CustomUserRepository;
import com.littlebank.finance.domain.user.dto.response.UserDetailsInfoResponse;
import com.littlebank.finance.domain.user.dto.response.UserSearchResponse;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Optional;

import static com.littlebank.finance.domain.challenge.domain.QChallengeParticipation.challengeParticipation;
import static com.littlebank.finance.domain.friend.domain.QFriend.friend;
import static com.littlebank.finance.domain.mission.domain.QMission.mission;
import static com.littlebank.finance.domain.user.domain.QUser.user;
import static com.querydsl.core.types.dsl.Expressions.constant;

@RequiredArgsConstructor
public class CustomUserRepositoryImpl implements CustomUserRepository {
    private final JPAQueryFactory queryFactory;
    private final QUser u = user;
    private final QFriend f = friend;
    private QFriend f1 = new QFriend("f1");
    private QMission m = mission;
    private QChallengeParticipation c = challengeParticipation;

    @Override
    public Optional<UserDetailsInfoResponse> findUserDetailsInfo(Long targetUserId, Long userId) {
        long friendCount = queryFactory
                .select(f.count())
                .from(f)
                .where(f.fromUser.id.eq(targetUserId).and(f.isBlocked.isFalse()))
                .fetchOne();

        long missionCount = queryFactory
                .select(m.count())
                .from(m)
                .where(m.child.id.eq(targetUserId).and(m.status.eq(MissionStatus.ACHIEVEMENT)))
                .fetchOne();

        long challengeCount = queryFactory
                .select(c.count())
                .from(c)
                .where(c.user.id.eq(targetUserId).and(c.challengeStatus.eq(ChallengeStatus.ACHIEVEMENT)))
                .fetchOne();

        return Optional.ofNullable(
                queryFactory
                        .select(Projections.constructor(
                                UserDetailsInfoResponse.class,
                                u.id.as("userId"),
                                u.email,
                                u.name,
                                u.statusMessage,
                                u.phone,
                                u.rrn,
                                u.bankName,
                                u.bankCode,
                                u.bankAccount,
                                u.profileImagePath,
                                u.role,
                                Projections.constructor(
                                        CommonFriendInfoResponse.class,
                                        f.id.isNotNull(),
                                        f.id,
                                        f.customName,
                                        f.isBlocked,
                                        f.isBestFriend
                                ),
                                constant(friendCount),
                                constant(missionCount),
                                constant(challengeCount)
                        ))
                        .from(u)
                        .leftJoin(f).on(f.fromUser.id.eq(userId)
                                .and(f.toUser.id.eq(u.id))
                        )
                        .where(u.id.eq(targetUserId))
                        .fetchOne()
        );
    }

    @Override
    public Optional<UserSearchResponse> findUserSearchResponse(Long requesterId, String phone) {
        return Optional.ofNullable(
                queryFactory
                        .select(Projections.constructor(
                                UserSearchResponse.class,
                                u.id.as("searchUserId"),
                                u.email,
                                u.name,
                                u.rrn,
                                u.statusMessage,
                                u.profileImagePath,
                                u.role,
                                Projections.constructor(
                                        CommonFriendInfoResponse.class,
                                        f.id.isNotNull(),
                                        f.id,
                                        f.customName,
                                        f.isBlocked,
                                        f.isBestFriend
                                )
                        ))
                        .from(u)
                        .leftJoin(f).on(f.fromUser.id.eq(requesterId)
                                .and(f.toUser.id.eq(u.id))
                        )
                        .where(u.phone.eq(phone)
                                .and(
                                        JPAExpressions.selectOne()
                                                .from(f1)
                                                .where(
                                                        f1.fromUser.id.eq(f.toUser.id),
                                                        f1.toUser.id.eq(f.fromUser.id),
                                                        f1.isBlocked.isTrue()
                                                )
                                                .notExists()
                                )
                        )
                        .fetchOne()
        );
    }

    @Override
    public List<TargetAmountResetDto> findAllByTargetAmountIsNotNull() {
        return queryFactory
                .select(Projections.constructor(
                        TargetAmountResetDto.class,
                        u.id,
                        u.name))
                .from(u)
                .where(u.targetAmount.isNotNull())
                .fetch();
    }
}
