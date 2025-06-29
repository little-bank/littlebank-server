package com.littlebank.finance.domain.notification.domain.repository;

import com.littlebank.finance.domain.notification.domain.Notification;
import org.springframework.data.jpa.repository.JpaRepository;


public interface NotificationRepository extends JpaRepository<Notification, Long>, CustomNotificationRepository {
}
