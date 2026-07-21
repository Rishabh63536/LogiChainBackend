package com.cts.logichain360.dto.response;

import com.cts.logichain360.enums.PaymentType;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class WarehouseCollectionRecordResponse {
    private Long warehouseId;
    private String warehouseCode;
    private Long orderId;
    private PaymentType paymentType;
    private Double amount; // positive for ADVANCE/FINAL, negative for REFUND
    private LocalDateTime paidAt;
}