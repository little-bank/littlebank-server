package com.littlebank.finance.domain.notification.domain.repository.impl;

import com.littlebank.finance.domain.notification.domain.QNotification;
import com.littlebank.finance.domain.notification.domain.repository.CustomNotificationRepository;
import com.littlebank.finance.domain.notification.dto.response.NotificationResponseDto;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.List;

import static com.littlebank.finance.domain.notification.domain.QNotification.notification;

@RequiredArgsConstructor
public class CustomNotificationRepositoryImpl implements CustomNotificationRepository {
    private final JPAQueryFactory queryFactory;
    private QNotification n =notification;

    @Override
    public Page<NotificationResponseDto> findByReceiverOrderByCreatedDateDesc(Long userId, Pageable pageable) {
        List<NotificationResponseDto> results = queryFactory
                .select(
                        Projections.constructor(
                                NotificationResponseDto.class,
                                n.id,
                                n.message,
                                n.type,
                                n.createdDate,
                                n.isRead
                        )
                )
                .from(n)
                .where(n.receiver.id.eq(userId))
                .orderBy(n.createdDate.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();
        return new PageImpl<>(results, pageable, results.size());

    }
}
