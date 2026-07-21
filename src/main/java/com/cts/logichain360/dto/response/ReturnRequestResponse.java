package com.cts.logichain360.dto.response;

import com.cts.logichain360.enums.ReturnReason;
import com.cts.logichain360.enums.ReturnStatus;
import lombok.*;

import java.time.LocalDateTime;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class ReturnRequestResponse {
    private Long id;
    private Long orderId;
    private Long customerId;
    private Integer returnQuantity;
    private ReturnReason reason;
    private String notes;
    private String photoUrl; //null if no photo
    private ReturnStatus status;
    private LocalDateTime requestedAt;
    private LocalDateTime resolvedAt;
    private Long resolvedByManagerId;
    private Long pickupDriverId;
    private LocalDateTime restockedAt;
    private Double refundAmount;
    private Double handlingFeeAmount;
}