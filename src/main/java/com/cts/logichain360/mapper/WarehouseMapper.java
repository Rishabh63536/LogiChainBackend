package com.cts.logichain360.mapper;

import org.springframework.stereotype.Component;

import com.cts.logichain360.dto.response.WarehouseResponse;
import com.cts.logichain360.entity.Warehouse;
import com.cts.logichain360.entity.WarehouseManager;

@Component
public class WarehouseMapper {

    public WarehouseResponse toResponse(Warehouse w, WarehouseManager manager) {
        return WarehouseResponse.builder()
                .id(w.getId())
                .warehouseCode(w.getWarehouseCode())
                .location(w.getLocation())
                .capacity(w.getCapacity())
                .managerId(manager == null ? null : manager.getId())
                .managerName(manager == null ? null : manager.getUser().getName())
                .managerEmployeeCode(manager == null ? null : manager.getEmployeeCode())
                .build();
    }
}