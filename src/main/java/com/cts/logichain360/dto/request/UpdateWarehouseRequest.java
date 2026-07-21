package com.cts.logichain360.dto.request;

import lombok.*;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class UpdateWarehouseRequest {
    private String  warehouseCode;
    private String  location;
    private Integer capacity;
}