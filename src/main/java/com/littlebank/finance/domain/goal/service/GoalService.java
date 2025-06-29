package com.littlebank.finance.domain.goal.service;

import com.littlebank.finance.domain.family.domain.Family;
import com.littlebank.finance.domain.family.domain.FamilyMember;
import com.littlebank.finance.domain.family.domain.repository.FamilyMemberRepository;
import com.littlebank.finance.domain.family.domain.repository.FamilyRepository;
import com.littlebank.finance.domain.family.exception.FamilyException;
import com.littlebank.finance.domain.goal.domain.Goal;
import com.littlebank.finance.domain.goal.domain.GoalCategory;
import com.littlebank.finance.domain.goal.domain.GoalStatus;
import com.littlebank.finance.domain.goal.domain.repository.GoalRepository;
import com.littlebank.finance.domain.goal.dto.request.GoalApplyRequest;
import com.littlebank.finance.domain.goal.dto.request.GoalUpdateRequest;
import com.littlebank.finance.domain.goal.dto.response.*;
import com.littlebank.finance.domain.goal.exception.GoalException;
import com.littlebank.finance.domain.notification.domain.Notification;
import com.littlebank.finance.domain.notification.domain.NotificationType;
import com.littlebank.finance.domain.notification.domain.repository.NotificationRepository;
import com.littlebank.finance.domain.user.domain.User;
import com.littlebank.finance.domain.user.domain.repository.UserRepository;
import com.littlebank.finance.domain.user.exception.UserException;
import com.littlebank.finance.global.error.exception.ErrorCode;
import com.littlebank.finance.global.firebase.FirebaseService;
import com.littlebank.finance.global.firebase.record.NotificationToSend;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import static java.util.Calendar.*;

@Slf4j
@Service
@Transactional
@AllArgsConstructor
public class GoalService {
    private final UserRepository userRepository;
    private final FamilyRepository familyRepository;
    private final FamilyMemberRepository familyMemberRepository;
    private final GoalRepository goalRepository;
    private final NotificationRepository notificationRepository;
    private final FirebaseService firebaseService;

    public CommonGoalResponse applyGoal(Long userId, GoalApplyRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserException(ErrorCode.USER_NOT_FOUND));
        verifyDuplicateGoalCategory(user.getId(), request.getCategory(), request.getStartDate());

        Family family = familyRepository.findByUserIdWithMember(userId)
                .orElseThrow(() -> new FamilyException(ErrorCode.FAMILY_NOT_FOUND));

        Goal goal = goalRepository.save(request.toEntity(user, family));

        List<FamilyMember> parents = familyMemberRepository.findParentsByFamilyId(family.getId());
        try {
            parents.stream()
                    .forEach(p -> {
                        Notification notification = notificationRepository.save(Notification.builder()
                                .receiver(p.getUser())
                                .message(family.getMembers().get(0).getNickname() + "(이)가 목표 승인을 요청했어요!")
                                .type(NotificationType.GOAL_PROPOSAL)
                                .targetId(goal.getId())
                                .isRead(false)
                                .build());
                        firebaseService.sendNotification(
                                new NotificationToSend(
                                        notification.getMessage(),
                                        notification.getSubMessage(),
                                        notification.getReceiver().getFcmToken()
                                )
                        );
                    });
        } catch (DataIntegrityViolationException e) {
            log.warn("이미 동일한 알림이 존재합니다.");
        }

        return CommonGoalResponse.of(goal);
    }

    @Transactional(readOnly = true)
    public List<WeeklyGoalResponse> getWeeklyGoal(Long userId) {
        List<Goal> goals = goalRepository.findByCreatedByAndWeekly(userId);

        return goals.stream()
                .map(g -> WeeklyGoalResponse.of(g))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<MyGoalResponse> getMyGoals(Long userId) {
        List<Goal> goals = goalRepository.findByCreatedById(userId);

        return goals.stream()
                .map(g -> MyGoalResponse.of(g))
                .collect(Collectors.toList());
    }

    public CommonGoalResponse updateGoal(Long userId, GoalUpdateRequest request) {
        Goal goal = goalRepository.findById(request.getGoalId())
                .orElseThrow(() -> new GoalException(ErrorCode.GOAL_NOT_FOUND));
        verifyDuplicateGoalCategory(userId, request.getCategory(), request.getStartDate());

        if (goal.getStatus() != GoalStatus.REQUESTED) {
            throw new GoalException(ErrorCode.INVALID_MODIFICATION_STATUS);
        }

        Goal target = request.toEntity();
        goal.update(target);

        return CommonGoalResponse.of(goal);
    }

    public void deleteGoal(Long goalId) {
        Goal goal = goalRepository.findById(goalId)
                .orElseThrow(() -> new GoalException(ErrorCode.GOAL_NOT_FOUND));

        if (goal.getStatus() != GoalStatus.REQUESTED) {
            throw new GoalException(ErrorCode.INVALID_DELETE_STATUS);
        }

        goalRepository.deleteById(goal.getId());
    }

    @Transactional(readOnly = true)
    public List<ChildGoalResponse> getChildWeeklyGoal(Long familyId) {
        return goalRepository.findChildWeeklyGoalResponses(familyId);
    }

    public CommonGoalResponse acceptApplyGoal(Long targetGoalId) {
        Goal goal = goalRepository.findById(targetGoalId)
                .orElseThrow(() -> new GoalException(ErrorCode.GOAL_NOT_FOUND));

        if (goal.getEndDate().isBefore(LocalDateTime.now())) {
            throw new GoalException(ErrorCode.GOAL_END_DATE_EXPIRED);
        }

        goal.acceptProposal();
        try {
            Notification notification = notificationRepository.save(Notification.builder()
                            .receiver(goal.getCreatedBy())
                            .message("우리 부모님(" + ")이 목표를 승낙했습니다!") //부모 중 누구인지 추가하기
                            .type(NotificationType.GOAL_ACCEPT)
                            .targetId(targetGoalId)
                            .isRead(false)
                            .build());
            firebaseService.sendNotification(notification);
        } catch (DataIntegrityViolationException e) {
            log.warn("이미 동일한 알림이 존재합니다.");
        }
        return CommonGoalResponse.of(goal);
    }

    @Transactional(readOnly = true)
    public List<ChildGoalResponse> getChildGoals(Long familyId) {
        return goalRepository.findAllChildGoalResponses(familyId);
    }

    public CommonGoalResponse checkGoal(Long goalId, Integer day) {
        Goal goal = goalRepository.findById(goalId)
                .orElseThrow(() -> new GoalException(ErrorCode.GOAL_NOT_FOUND));

        switch (day % 7 + 1) {
            case MONDAY -> goal.checkMon(Boolean.TRUE);
            case TUESDAY -> goal.checkTue(Boolean.TRUE);
            case WEDNESDAY -> goal.checkWed(Boolean.TRUE);
            case THURSDAY -> goal.checkThu(Boolean.TRUE);
            case FRIDAY -> goal.checkFri(Boolean.TRUE);
            case SATURDAY -> goal.checkSat(Boolean.TRUE);
            case SUNDAY -> goal.checkSun(Boolean.TRUE);
        }

        // 최소 도장 갯수에 도달하면 상태 업데이트
        if ((goal.getStampCount() >= goal.getFamily().getMinStampCount()) &&
                goal.getStatus() != GoalStatus.ACHIEVEMENT) {
            goal.achieve();
        }

        // 알림
        try {
            Notification notification = notificationRepository.save(Notification.builder()
                    .receiver(goal.getCreatedBy())
                    .message("우리 부모님(" + ")이 목표 도장을 찍어줬어요!") //부모 중 누구인지 추가하기
                    .subMessage("오늘 목표치도 화이팅!")
                    .type(NotificationType.PERMIT_GOAL)
                    .targetId(goal.getId())
                    .isRead(false)
                    .build());
            firebaseService.sendNotification(notification);
        } catch (DataIntegrityViolationException e) {
            log.warn("이미 동일한 알림이 존재합니다.");
        }
        return CommonGoalResponse.of(goal);
    }

    @Transactional(readOnly = true)
    public StampCheckResponse checkStamp(Long goalId) {
        Goal goal = goalRepository.findById(goalId)
                .orElseThrow(() -> new GoalException(ErrorCode.GOAL_NOT_FOUND));

        return StampCheckResponse.of(goal);
    }

    private void verifyDuplicateGoalCategory(Long userId, GoalCategory category, LocalDateTime dateTime) {
        if (goalRepository.existsCategorySameWeek(userId, category, dateTime)) {
            throw new GoalException(ErrorCode.GOAL_CATEGORY_DUPLICATED);
        }
    }
}
