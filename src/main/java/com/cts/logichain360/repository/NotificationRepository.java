package com.cts.logichain360.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.cts.logichain360.entity.Notification;
import com.cts.logichain360.enums.NotificationType;

public interface NotificationRepository extends JpaRepository<Notification, Long>{
	List<Notification> findAllByRecipient_IdOrderByCreatedAtDesc(Long userId);
	 
    List<Notification> findAllByRecipient_IdAndReadFalseOrderByCreatedAtDesc(Long userId);
 
    List<Notification> findAllByRecipient_IdAndTypeOrderByCreatedAtDesc(
            Long userId, NotificationType type);
}
