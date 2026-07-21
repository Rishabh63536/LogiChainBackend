package com.cts.logichain360.mapper;

import org.springframework.stereotype.Component;

import com.cts.logichain360.dto.response.PaymentResponse;
import com.cts.logichain360.entity.Payment;

@Component
public class PaymentMapper {

    public PaymentResponse toResponse(Payment p) {
        return PaymentResponse.builder()
                .id(p.getId())
                .orderId(p.getOrder().getId())
                .amount(p.getAmount())
                .type(p.getType())
                .status(p.getStatus())
                .method(p.getMethod())
                .paidAt(p.getPaidAt())
                .orderAmountPaid(p.getOrder().getAmountPaid())
                .orderTotalAmount(p.getOrder().getTotalAmount())
                .build();
    }
}