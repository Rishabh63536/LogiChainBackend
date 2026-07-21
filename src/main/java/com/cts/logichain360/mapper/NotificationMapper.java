package com.cts.logichain360.mapper;

import org.springframework.stereotype.Component;

import com.cts.logichain360.dto.response.NotificationResponse;
import com.cts.logichain360.entity.Notification;

@Component
public class NotificationMapper {

    public NotificationResponse toResponse(Notification n) {
        return NotificationResponse.builder()
                .id(n.getId())
                .recipientUserId(n.getRecipient().getId())
                .type(n.getType())
                .message(n.getMessage())
                .createdAt(n.getCreatedAt())
                .read(n.isRead())
                .relatedEntityId(n.getRelatedEntityId())
                .relatedEntityType(n.getRelatedEntityType())
                .build();
    }
}