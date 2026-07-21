package com.cts.logichain360.controller;

import com.cts.logichain360.dto.response.NotificationResponse;
import com.cts.logichain360.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Notification",
     description = "In-app notifications. ROL breaches trigger one for the assigned warehouse manager.")
public class NotificationController {

    private final NotificationService notificationService;

    @Operation(summary = "All notifications for a user (newest first)")
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<NotificationResponse>> getForUser(@PathVariable Long userId) {
        return notificationService.getNotificationsForUser(userId);
    }

    @Operation(summary = "Unread notifications for a user")
    @GetMapping("/user/{userId}/unread")
    public ResponseEntity<List<NotificationResponse>> getUnread(@PathVariable Long userId) {
        return notificationService.getUnreadNotificationsForUser(userId);
    }

    @Operation(summary = "Mark a notification as read")
    @PatchMapping("/{id}/read")
    public ResponseEntity<NotificationResponse> markAsRead(@PathVariable Long id) {
        log.info("PATCH /notifications/{}/read", id);
        return notificationService.markAsRead(id);
    }
}