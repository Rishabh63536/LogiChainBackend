package com.cts.logichain360.mapper;

import org.springframework.stereotype.Component;

import com.cts.logichain360.dto.response.DriverResponse;
import com.cts.logichain360.entity.Driver;

@Component
public class DriverMapper {

    public DriverResponse toResponse(Driver d) {
        return DriverResponse.builder()
                .id(d.getId()).userId(d.getUser().getId())
                .userName(d.getUser().getName()).userPhone(d.getUser().getPhone())
                .available(d.getAvailable())
                .licenseNumber(d.getLicenseNumber()).licenseExpiry(d.getLicenseExpiry())
                .location(d.getLocation()).build();
    }
}