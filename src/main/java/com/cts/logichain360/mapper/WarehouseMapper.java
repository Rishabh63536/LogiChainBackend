package com.cts.logichain360.mapper;

import org.springframework.stereotype.Component;

import com.cts.logichain360.dto.response.WarehouseResponse;
import com.cts.logichain360.entity.Warehouse;
import com.cts.logichain360.entity.WarehouseManager;

@Component
public class WarehouseMapper {

    // Manager is on the other side of the relationship, the service looks it up
    // (via wmRepo.findByAssignedWarehouse_Id) and passes it in, keeping this pure.
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