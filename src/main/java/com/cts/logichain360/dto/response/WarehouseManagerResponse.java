package com.cts.logichain360.dto.response;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WarehouseManagerResponse {
    private Long id;
    private Long userId;
    private String userName;
    private String userPhone;
    private String employeeCode;
    private String designation;

    // from warehouse entity
    private Long assignedWarehouseId;
    private String assignedWarehouseCode;
    private String assignedWarehouseLocation;
    private Long warehouseId;
}