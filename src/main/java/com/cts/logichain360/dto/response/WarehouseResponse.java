package com.cts.logichain360.dto.response;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WarehouseResponse {
    private Long id;
    private String warehouseCode;
    private String location;
    private Integer capacity;

    // Flattened, null if no manager assigned yet
    private Long managerId;
    private String managerName;
    private String managerEmployeeCode;
}