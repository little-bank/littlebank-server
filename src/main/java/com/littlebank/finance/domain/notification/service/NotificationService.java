package com.littlebank.finance.domain.notification.service;


import com.littlebank.finance.domain.notification.domain.repository.NotificationRepository;
import com.littlebank.finance.domain.notification.dto.response.NotificationResponseDto;
import com.littlebank.finance.global.common.CustomPageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
public class NotificationService {
    private final NotificationRepository notificationRepository;

    public CustomPageResponse<NotificationResponseDto> getUserNotifications(Long userId, Pageable pageable) {
        return CustomPageResponse.of(notificationRepository.findByReceiverOrderByCreatedDateDesc(userId, pageable));
    }
}
