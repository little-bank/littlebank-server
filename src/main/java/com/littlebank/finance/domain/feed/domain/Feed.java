package com.littlebank.finance.domain.feed.domain;


import com.littlebank.finance.domain.user.domain.User;
import com.littlebank.finance.global.common.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "feed")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Feed extends BaseEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "feed_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(length = 100, nullable = false)
    private String title;

    @Enumerated(EnumType.STRING)
    private GradeCategory gradeCategory;

    @Enumerated(EnumType.STRING)
    private SubjectCategory subjectCategory;

    @Enumerated(EnumType.STRING)
    private TagCategory tagCategory;

    @Column(length = 2000)
    private String content;

    private int viewCount = 0;
    private int likeCount = 0;
    private int commentCount = 0;

    @OneToMany(mappedBy = "feed", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<FeedImage> images = new ArrayList<>();

    @Builder
    public Feed(User user, String title, GradeCategory gradeCategory, SubjectCategory subjectCategory, TagCategory tagCategory, String content,
                int viewCount, int likeCount, int commentCount) {
        this.user = user;
        this.title = title;
        this.gradeCategory = gradeCategory;
        this.subjectCategory = subjectCategory;
        this.tagCategory = tagCategory;
        this.content = content;
        this.viewCount = viewCount;
        this.likeCount = likeCount;
        this.commentCount = commentCount;
    }

    public void update(String title, GradeCategory gradeCategory, SubjectCategory subjectCategory, TagCategory tagCategory, String content) {
        this.title = title;
        this.gradeCategory = gradeCategory;
        this.subjectCategory = subjectCategory;
        this.tagCategory = tagCategory;
        this.content = content;
    }

    public void increaseViewCount() {
        this.viewCount++;
    }
}
