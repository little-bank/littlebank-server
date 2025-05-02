package com.littlebank.finance.domain.feed.domain.repository;

import com.littlebank.finance.domain.feed.domain.*;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.List;

import static java.rmi.server.LogStream.log;
@Slf4j
public class FeedRepositoryImpl implements FeedRepositoryCustom {

    private final JPAQueryFactory queryFactory;
    public FeedRepositoryImpl(EntityManager em) {
        this.queryFactory = new JPAQueryFactory(em);
    }

    @Override
    public Page<Feed> findAllByFilters(GradeCategory grade, SubjectCategory subject, TagCategory tag, Pageable pageable) {
        QFeed feed = QFeed.feed;

        BooleanBuilder builder = new BooleanBuilder();
        if (grade != null) {
            if (grade == GradeCategory.ALL) {
                builder.and(feed.gradeCategory.in(GradeCategory.ELEMENTARY, GradeCategory.MIDDLE, GradeCategory.HIGH));
            } else {
                builder.and(feed.gradeCategory.eq(grade));
            }
        }
        if (subject != null) {
            if (subject == SubjectCategory.ALL) {
                builder.and(feed.subjectCategory.in(SubjectCategory.KOREAN, SubjectCategory.MATH, SubjectCategory.ENGLISH, SubjectCategory.SCIENCE, SubjectCategory.SOCIETY));
            } else {
                builder.and(feed.subjectCategory.eq(subject));
            }
        }
        if (tag != null) {
            if (tag == TagCategory.ALL) {
                builder.and(feed.tagCategory.in(TagCategory.STUDY_CERTIFICATION, TagCategory.HABIT_BUILDING, TagCategory.INFORMATION));
            } else {
                builder.and(feed.tagCategory.eq(tag));
            }
        }

        List<Feed> result = queryFactory.selectFrom(feed)
                .where(builder)
                .orderBy(feed.createdDate.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        long total = queryFactory.select(feed.count())
                .from(feed)
                .where(builder)
                .fetchOne();

        return new PageImpl<>(result, pageable, total);    }
}
