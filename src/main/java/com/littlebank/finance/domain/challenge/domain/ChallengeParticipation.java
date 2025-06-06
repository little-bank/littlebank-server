package com.littlebank.finance.domain.challenge.domain;

import com.littlebank.finance.domain.family.domain.FamilyMember;
import com.littlebank.finance.domain.user.domain.User;
import com.littlebank.finance.global.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@SQLDelete(sql = "UPDATE challenge_participation SET is_deleted = true WHERE participation_id = ?")
@Where(clause = "is_deleted = false")
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChallengeParticipation extends BaseEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "participation_id")
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY)
    private Challenge challenge;
    @ManyToOne(fetch = FetchType.LAZY)
    private User user;
    @Enumerated(EnumType.STRING)
    private ChallengeStatus challengeStatus;
    @Column(name = "start_date", nullable = false)
    private LocalDateTime startDate;
    @Column(name = "end_date", nullable = false)
    private LocalDateTime endDate;
    @Column(nullable = false, length = 100)
    private String title;
    @Column(nullable = false, length = 20)
    private String subject;
    @Column(name = "start_time", nullable = false)
    private LocalDateTime startTime;
    @Column(name = "total_study_time", nullable = false)
    private Integer totalStudyTime;
    @Column(nullable = false)
    private Integer reward;
    @Column(name = "is_accepted", nullable = false)
    private Boolean isAccepted = false;
    @Column(nullable = false)
    private Boolean isDeleted = false;
    @ManyToOne(fetch = FetchType.LAZY)
    private FamilyMember parent;
    @Builder
    public ChallengeParticipation(Challenge challenge, User user, ChallengeStatus challengeStatus, LocalDateTime startDate, LocalDateTime endDate, String title, String subject, LocalDateTime startTime, Integer totalStudyTime, Integer reward, FamilyMember parent) {
        this.challenge = challenge;
        this.user = user;
        this.challengeStatus = challengeStatus;
        this.startDate = startDate;
        this.endDate = endDate;
        this.title = title;
        this.subject = subject;
        this.startTime = startTime;
        this.totalStudyTime = totalStudyTime;
        this.reward = reward;
        this.parent = parent;
    }
}
