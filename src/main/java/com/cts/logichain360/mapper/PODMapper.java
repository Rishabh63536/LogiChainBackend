package com.cts.logichain360.mapper;

import org.springframework.stereotype.Component;

import com.cts.logichain360.dto.response.PODResponse;
import com.cts.logichain360.entity.POD;

@Component
public class PODMapper {

    //resource handler prefix registered in WebConfig, must be matched
    private static final String URL_PREFIX = "/pod-images/";

    public PODResponse toResponse(POD pod) {
        return PODResponse.builder()
                .id(pod.getId())
                .orderId(pod.getOrder().getId())
                .photoUrl(URL_PREFIX + pod.getPhotoFilename())
                .driverId(pod.getDriverId())
                .driverName(pod.getDriverName())
                .uploadedAt(pod.getUploadedAt())
                .build();
    }
}