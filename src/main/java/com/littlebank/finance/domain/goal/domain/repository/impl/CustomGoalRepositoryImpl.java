package com.littlebank.finance.domain.goal.domain.repository.impl;

import com.littlebank.finance.domain.family.domain.QFamily;
import com.littlebank.finance.domain.family.domain.QFamilyMember;
import com.littlebank.finance.domain.family.domain.Status;
import com.littlebank.finance.domain.goal.domain.Goal;
import com.littlebank.finance.domain.goal.domain.GoalCategory;
import com.littlebank.finance.domain.goal.domain.GoalStatus;
import com.littlebank.finance.domain.goal.domain.QGoal;
import com.littlebank.finance.domain.goal.domain.repository.CustomGoalRepository;
import com.littlebank.finance.domain.goal.dto.response.ChildGoalResponse;
import com.littlebank.finance.domain.notification.dto.GoalAchievementNotificationDto;
import com.littlebank.finance.domain.user.domain.QUser;
import com.littlebank.finance.domain.user.domain.UserRole;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.NumberTemplate;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import static com.littlebank.finance.domain.family.domain.QFamily.family;
import static com.littlebank.finance.domain.family.domain.QFamilyMember.familyMember;
import static com.littlebank.finance.domain.goal.domain.QGoal.goal;
import static com.littlebank.finance.domain.user.domain.QUser.user;

@RequiredArgsConstructor
public class CustomGoalRepositoryImpl implements CustomGoalRepository {
    private final JPAQueryFactory queryFactory;
    private QGoal g = goal;
    private QFamily f = family;
    private QFamilyMember fm = familyMember;
    private QUser u = user;
    private final static int MONDAY = 1;

    /**
     * 동일 주간 목표 중 동일 카테고리 존재 여부
     */
    public Boolean existsCategorySameWeek(Long userId, GoalCategory category, LocalDateTime compareTargetDate) {
        Boolean exists = queryFactory
                .selectOne()
                .from(g)
                .where(g.createdBy.id.eq(userId)
                        .and(
                                g.category.eq(category)
                        )
                        .and(
                                Expressions.stringTemplate(
                                                "YEARWEEK({0}, {1})", g.startDate, MONDAY
                                        )
                                        .eq(
                                                Expressions.stringTemplate("YEARWEEK({0}, {1})", compareTargetDate, MONDAY)
                                        )
                        )
                )
                .fetchOne() != null;

        return exists;
    }

    public List<Goal> findByCreatedByAndWeekly(Long userId) {
        List<Goal> results = queryFactory
                .selectFrom(g)
                .where(g.createdBy.id.eq(userId)
                        .and(
                                Expressions.stringTemplate(
                                        "YEARWEEK({0}, {1})", g.startDate, MONDAY
                                        )
                                        .eq(
                                                Expressions.stringTemplate(
                                                        "YEARWEEK(CURDATE(), {0})", MONDAY
                                                )
                                        )
                        )
                )
                .fetch();

        return results;
    }

    public List<ChildGoalResponse> findChildWeeklyGoalResponses(Long familyId) {
        NumberTemplate<Integer> stampCount = Expressions.numberTemplate(Integer.class,
                "({0} + {1} + {2} + {3} + {4} + {5} + {6})",
                g.mon.castToNum(Integer.class),
                g.tue.castToNum(Integer.class),
                g.wed.castToNum(Integer.class),
                g.thu.castToNum(Integer.class),
                g.fri.castToNum(Integer.class),
                g.sat.castToNum(Integer.class),
                g.sun.castToNum(Integer.class)
        );

        return queryFactory
                .select(Projections.constructor(
                        ChildGoalResponse.class,
                        g.id,
                        g.title,
                        g.category,
                        g.reward,
                        g.startDate,
                        g.endDate,
                        g.status,
                        fm.id,
                        fm.nickname,
                        g.isRewarded,
                        f.minStampCount,
                        stampCount
                ))
                .from(g)
                .join(g.family, f)
                .join(f.members, fm)
                .where(
                        g.family.id.eq(familyId)
                                .and(
                                        Expressions.stringTemplate("YEARWEEK({0}, {1})", g.startDate, MONDAY)
                                                .eq(Expressions.stringTemplate("YEARWEEK(CURDATE(), {0})", MONDAY))
                                )
                                .and(g.createdBy.id.eq(fm.user.id))
                )
                .fetch();
    }

    public List<ChildGoalResponse> findAllChildGoalResponses(Long familyId) {
        NumberTemplate<Integer> stampCount = Expressions.numberTemplate(Integer.class,
                "({0} + {1} + {2} + {3} + {4} + {5} + {6})",
                g.mon.castToNum(Integer.class),
                g.tue.castToNum(Integer.class),
                g.wed.castToNum(Integer.class),
                g.thu.castToNum(Integer.class),
                g.fri.castToNum(Integer.class),
                g.sat.castToNum(Integer.class),
                g.sun.castToNum(Integer.class)
        );

        return queryFactory
                .select(Projections.constructor(
                        ChildGoalResponse.class,
                        g.id,
                        g.title,
                        g.category,
                        g.reward,
                        g.startDate,
                        g.endDate,
                        g.status,
                        fm.id,
                        fm.nickname,
                        g.isRewarded,
                        f.minStampCount,
                        stampCount
                ))
                .from(g)
                .join(g.family, f)
                .join(f.members, fm)
                .where(
                        g.family.id.eq(familyId)
                                .and(g.createdBy.id.eq(fm.user.id))
                )
                .fetch();
    }

    /**
     * [자녀 목표 달성 시 부모 푸시 알림 대상 조회]
     * - Goal의 startDate가 지난주(월~일)
     * - Goal의 도장 요일(true 개수) ≥ Family.minStampCount
     * - 자녀/부모 모두 FamilyMember.status = JOINED
     * - 부모의 User.role = PARENT
     */
    public List<GoalAchievementNotificationDto> findGoalAchievementNotificationDto() {
        QFamilyMember parentMember = new QFamilyMember("parentMember");

        LocalDate lastMonday = LocalDate.now().minusWeeks(1).with(DayOfWeek.MONDAY);
        LocalDate lastSunday = LocalDate.now().minusWeeks(1).with(DayOfWeek.SUNDAY);

        NumberTemplate<Integer> stampCount = Expressions.numberTemplate(Integer.class,
                "({0} + {1} + {2} + {3} + {4} + {5} + {6})",
                g.mon.castToNum(Integer.class),
                g.tue.castToNum(Integer.class),
                g.wed.castToNum(Integer.class),
                g.thu.castToNum(Integer.class),
                g.fri.castToNum(Integer.class),
                g.sat.castToNum(Integer.class),
                g.sun.castToNum(Integer.class)
        );

        List<GoalAchievementNotificationDto> results = queryFactory
                .select(Projections.constructor(
                        GoalAchievementNotificationDto.class,
                        u.id, // 부모
                        fm.nickname,
                        g.title,
                        g.id,
                        stampCount,
                        fm.user.id // 자녀
                ))
                .from(g)
                .join(f).on(f.id.eq(g.family.id))
                .join(fm).on(fm.user.id.eq(g.createdBy.id)) // 자녀
                .join(parentMember).on(parentMember.family.id.eq(f.id)) // 부모 후보
                .join(u).on(u.id.eq(parentMember.user.id)) // 부모 유저
                .where(
                        g.startDate.between(lastMonday.atStartOfDay(), lastSunday.atTime(LocalTime.MAX)),
                        fm.status.eq(Status.JOINED),
                        parentMember.status.eq(Status.JOINED),
                        u.role.eq(UserRole.PARENT),
                        g.status.eq(GoalStatus.ACHIEVEMENT)
                )
                .fetch();

        return results;
    }

}
