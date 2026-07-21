package com.cts.logichain360.dto.response;

import com.cts.logichain360.enums.NotificationType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "An in-app notification for a user.")
public class NotificationResponse {

    @Schema(example = "1")
    private Long id;

    @Schema(example = "5")
    private Long recipientUserId;

    @Schema(example = "ROL_BREACH")
    private NotificationType type;

    @Schema(example = "Product 'Sony WH-1000XM5' at WH-MUM-01 is at 26.0% of capacity (below 40% ROL).")
    private String message;

    @Schema(example = "2026-06-01T10:32:15")
    private LocalDateTime createdAt;

    @Schema(example = "false")
    private boolean read;

    @Schema(example = "1")
    private Long relatedEntityId;

    @Schema(example = "ProductWarehouse")
    private String relatedEntityType;
}