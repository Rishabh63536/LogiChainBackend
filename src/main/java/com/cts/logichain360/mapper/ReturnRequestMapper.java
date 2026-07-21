package com.cts.logichain360.mapper;

import org.springframework.stereotype.Component;

import com.cts.logichain360.dto.response.ReturnRequestResponse;
import com.cts.logichain360.entity.ReturnRequest;

@Component
public class ReturnRequestMapper {

    // resources handler prefix connfigured in webconfig to get photo url
    private static final String URL_PREFIX = "/pod-images/";

    public ReturnRequestResponse toResponse(ReturnRequest rr) {
        return ReturnRequestResponse.builder()
                .id(rr.getId())
                .orderId(rr.getOrder().getId())
                .customerId(rr.getOrder().getCustomer().getId())
                .returnQuantity(rr.getReturnQuantity())
                .reason(rr.getReason())
                .notes(rr.getNotes())
                .photoUrl(rr.getPhotoFilename() == null ? null : URL_PREFIX + rr.getPhotoFilename())
                .status(rr.getStatus())
                .requestedAt(rr.getRequestedAt())
                .resolvedAt(rr.getResolvedAt())
                .resolvedByManagerId(rr.getResolvedByManagerId())
                .pickupDriverId(rr.getPickupDriverId())
                .restockedAt(rr.getRestockedAt())
                .refundAmount(rr.getRefundAmount())
                .handlingFeeAmount(rr.getHandlingFeeAmount())
                .build();
    }
}