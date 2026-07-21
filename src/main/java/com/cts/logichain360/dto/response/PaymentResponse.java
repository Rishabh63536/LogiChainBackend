package com.cts.logichain360.dto.response;

import com.cts.logichain360.enums.PaymentStatus;
import com.cts.logichain360.enums.PaymentType;
import lombok.*;

import java.time.LocalDateTime;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class PaymentResponse {
    private Long id;
    private Long orderId;
    private Double amount;
    private PaymentType type;
    private PaymentStatus status;
    private String method;
    private LocalDateTime paidAt;
    private Double orderAmountPaid;
    private Double orderTotalAmount;
}