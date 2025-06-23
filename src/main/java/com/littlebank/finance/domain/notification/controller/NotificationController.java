package com.littlebank.finance.domain.notification.controller;

import com.littlebank.finance.domain.notification.dto.response.NotificationResponseDto;
import com.littlebank.finance.domain.notification.service.NotificationService;
import com.littlebank.finance.global.common.CustomPageResponse;
import com.littlebank.finance.global.common.PaginationPolicy;
import com.littlebank.finance.global.security.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api-user/feed/notification")
@RequiredArgsConstructor
@Tag(name = "Notification")
public class NotificationController {

    private final NotificationService notificationService;

    @Operation(summary = "피드 알림 API")
    @SecurityRequirements()
    @GetMapping
    public ResponseEntity<CustomPageResponse<NotificationResponseDto>> getNotifications(
            @AuthenticationPrincipal CustomUserDetails currentUser,
            @RequestParam(name = "pageNumber") Integer pageNumber
    ) {
        Pageable pageable = PageRequest.of(pageNumber, PaginationPolicy.GENERAL_PAGE_SIZE);
        CustomPageResponse<NotificationResponseDto> response = notificationService.getUserNotifications(currentUser.getId(), pageable);
        return ResponseEntity.ok(response);
    }
}
