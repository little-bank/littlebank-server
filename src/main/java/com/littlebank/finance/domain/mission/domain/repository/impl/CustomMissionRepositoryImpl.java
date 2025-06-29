package com.littlebank.finance.domain.mission.domain.repository.impl;

import com.littlebank.finance.domain.family.domain.QFamilyMember;
import com.littlebank.finance.domain.friend.domain.QFriend;
import com.littlebank.finance.domain.mission.domain.QMission;
import com.littlebank.finance.domain.mission.domain.repository.CustomMissionRepository;
import com.littlebank.finance.domain.mission.dto.response.MissionStatDto;
import com.littlebank.finance.domain.notification.dto.response.MissionAchievementNotificationDto;
import com.littlebank.finance.domain.user.domain.QUser;
import com.littlebank.finance.domain.mission.domain.*;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;

import java.time.LocalDate;
import java.util.List;
import java.time.LocalDateTime;

@RequiredArgsConstructor
public class CustomMissionRepositoryImpl implements CustomMissionRepository {
    private QFriend f = QFriend.friend;
    private QMission m = QMission.mission;
    private QUser u = QUser.user;
    private QFamilyMember fm = QFamilyMember.familyMember;
    private final JPAQueryFactory queryFactory;


    @Override
    public Integer findRecentReward(Long childId, MissionType type, MissionCategory category, MissionSubject subject) {
        Mission result = queryFactory
                .selectFrom(m)
                .where(m.child.id.eq(childId),
                        m.type.eq(type),
                        m.status.eq(MissionStatus.ACHIEVEMENT),
                        m.category.eq(category),
                        category == MissionCategory.LEARNING ? m.subject.eq(subject) : null)
                .orderBy(m.endDate.desc())
                .fetchFirst();
        return result != null ? result.getReward() : 0;
    }

    @Override
    public List<MissionStatDto> getMissionStatsByPeriod(List<Long> friendIds, LocalDateTime start, LocalDateTime end) {
        return queryFactory
                .select(Projections.constructor(MissionStatDto.class,
                        m.child.id,
                        m.category,
                        m.subject,
                        m.status,
                        m.count()
                ))
                .from(m)
                .where(
                        m.child.id.in(friendIds),
                        m.endDate.between(start, end)
                )
                .groupBy(m.child.id, m.category, m.subject, m.status)
                .fetch();
    }

    @Override
    public List<Mission> updateExpiredMissionsToAchievement() {
        List<Mission> expired = queryFactory.selectFrom(m)
                .where(
                        m.endDate.before(LocalDateTime.now()),
                        m.status.eq(MissionStatus.ACCEPT),
                        m.isDeleted.isFalse()
                ).fetch();
        expired.forEach(m -> m.setStatus(MissionStatus.ACHIEVEMENT));
        return expired;
    }

    @Override
     public List<MissionAchievementNotificationDto> findMissionAchievementNotificationDto() {
        LocalDateTime yesterdayStart = LocalDate.now().minusDays(1).atStartOfDay();
        LocalDateTime yesterdayEnd = LocalDate.now().minusDays(1).atTime(23, 59, 59);
        List<MissionAchievementNotificationDto> results = queryFactory
                .select(Projections.constructor(MissionAchievementNotificationDto.class,
                        m.createdBy.user.id,
                        fm.nickname,
                        m.title,
                        m.id))
                .from(m)
                .join(fm).on(fm.user.id.eq(m.child.user.id))
                .where(
                        m.status.eq(MissionStatus.ACHIEVEMENT),
                        m.endDate.between(yesterdayStart, yesterdayEnd),
                        m.isDeleted.isFalse()
                )
                .fetch();
        return results;
    }
}
