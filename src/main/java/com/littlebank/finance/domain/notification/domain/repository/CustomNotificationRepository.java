package com.littlebank.finance.domain.notification.domain.repository;

import com.littlebank.finance.domain.notification.dto.response.NotificationResponseDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface CustomNotificationRepository {
    Page<NotificationResponseDto> findByReceiverOrderByCreatedDateDesc(Long userId, Pageable pageable);
}
