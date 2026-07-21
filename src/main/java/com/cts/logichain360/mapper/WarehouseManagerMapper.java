package com.cts.logichain360.mapper;

import org.springframework.stereotype.Component;

import com.cts.logichain360.dto.response.WarehouseManagerResponse;
import com.cts.logichain360.entity.Warehouse;
import com.cts.logichain360.entity.WarehouseManager;

@Component
public class WarehouseManagerMapper {

    public WarehouseManagerResponse toResponse(WarehouseManager wm) {
        Warehouse aw = wm.getAssignedWarehouse();
        return WarehouseManagerResponse.builder()
                .id(wm.getId())
                .userId(wm.getUser().getId())
                .userName(wm.getUser().getName())
                .userPhone(wm.getUser().getPhone())
                .employeeCode(wm.getEmployeeCode())
                .designation(wm.getDesignation())
                .assignedWarehouseId(aw == null ? null : aw.getId())
                .assignedWarehouseCode(aw == null ? null : aw.getWarehouseCode())
                .assignedWarehouseLocation(aw == null ? null : aw.getLocation())
                .build();
    }
}