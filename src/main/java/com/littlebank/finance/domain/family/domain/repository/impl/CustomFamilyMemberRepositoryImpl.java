package com.littlebank.finance.domain.family.domain.repository.impl;

import com.littlebank.finance.domain.family.domain.FamilyMember;
import com.littlebank.finance.domain.family.domain.QFamily;
import com.littlebank.finance.domain.family.domain.QFamilyMember;
import com.littlebank.finance.domain.family.domain.Status;
import com.littlebank.finance.domain.family.domain.repository.CustomFamilyMemberRepository;
import com.littlebank.finance.domain.family.dto.response.FamilyInfoResponse;
import com.littlebank.finance.domain.family.dto.response.FamilyMemberInfoResponse;
import com.littlebank.finance.domain.family.exception.FamilyException;
import com.littlebank.finance.domain.user.domain.QUser;
import com.littlebank.finance.domain.user.domain.UserRole;
import com.littlebank.finance.global.error.exception.ErrorCode;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Optional;

import static com.littlebank.finance.domain.family.domain.QFamily.family;
import static com.littlebank.finance.domain.family.domain.QFamilyMember.familyMember;
import static com.littlebank.finance.domain.user.domain.QUser.user;

@RequiredArgsConstructor
public class CustomFamilyMemberRepositoryImpl implements CustomFamilyMemberRepository {
    private final JPAQueryFactory queryFactory;
    private QUser u = user;
    private QFamilyMember m = familyMember;
    private QFamily f = family;

    @Override
    public Optional<FamilyMember> findByUserIdAndStatusWithFamily(Long userId, Status status) {
        return Optional.ofNullable(
                queryFactory
                        .selectFrom(m)
                        .join(m.family, f).fetchJoin()
                        .where(m.user.id.eq(userId)
                                .and(m.status.eq(status)))
                        .fetchOne());
    }

    @Override
    public Optional<FamilyMember> findByUserIdAndStatusWithUser(Long userId, Status status) {
        return Optional.ofNullable(
                queryFactory
                        .selectFrom(m)
                        .join(m.user, u).fetchJoin()
                        .where(m.user.id.eq(userId)
                                .and(m.status.eq(status)))
                        .fetchOne());
    }

    @Override
    public List<FamilyMember> findByMemberIdWithFamilyAndUser(Long memberId) {
        return queryFactory
                .selectFrom(m)
                .join(m.user, u).fetchJoin()
                .where(m.family.id.eq(
                                        queryFactory
                                                .select(m.family.id)
                                                .from(m)
                                                .where(m.id.eq(memberId))
                                                .fetchOne()
                                )
                                .and(m.status.eq(Status.JOINED))
                )
                .fetch();
    }

    @Override
    public List<FamilyMember> findAllByFamilyIdAndStatusWithUser(Long familyId, Status status) {
        QUser u1 = new QUser("u1");
        return queryFactory
                .selectFrom(m)
                .join(m.user, u).fetchJoin()
                .join(m.invitedBy, u1).fetchJoin()
                .where(m.family.id.eq(familyId)
                        .and(m.status.eq(status))
                )
                .fetch();
    }

    @Override
    public List<FamilyMember> findParentsByFamilyId(Long familyId) {
        return queryFactory
                .selectFrom(m)
                .join(u).on(u.id.eq(m.user.id)).fetchJoin()
                .join(f).on(f.id.eq(m.family.id)).fetchJoin()
                .where(f.id.eq(familyId)
                        .and(m.status.eq(Status.JOINED))
                        .and(u.role.eq(UserRole.PARENT))
                )
                .fetch();
    }

    @Override
    public List<FamilyMember> findChildsByFamilyId(Long familyId) {
        return queryFactory
                .selectFrom(m)
                .join(u).on(u.id.eq(m.user.id)).fetchJoin()
                .join(f).on(f.id.eq(m.family.id)).fetchJoin()
                .where(f.id.eq(familyId)
                        .and(m.status.eq(Status.JOINED))
                        .and(u.role.eq(UserRole.CHILD))
                )
                .fetch();
    }

    @Override
    public FamilyInfoResponse getFamilyInfoByUserId(Long userId) {
        Long familyId = queryFactory
                .select(m.family.id)
                .from(m)
                .where(m.user.id.eq(userId)
                        .and(m.status.eq(Status.JOINED)))
                .fetchOne();

        if (familyId == null) {
            throw new FamilyException(ErrorCode.FAMILY_NOT_FOUND);
        }

        List<FamilyMemberInfoResponse> members = queryFactory
                .select(Projections.constructor(
                        FamilyMemberInfoResponse.class,
                        m.id,
                        m.nickname,
                        m.user.id,
                        m.user.email,
                        m.user.name,
                        m.user.phone,
                        m.user.rrn,
                        m.user.bankName,
                        m.user.bankAccount,
                        m.user.profileImagePath,
                        m.user.role
                ))
                .from(m)
                .join(m.user, u)
                .where(m.family.id.eq(familyId)
                        .and(m.status.eq(Status.JOINED)))
                .fetch();

        return new FamilyInfoResponse(familyId, members);
    }
}
