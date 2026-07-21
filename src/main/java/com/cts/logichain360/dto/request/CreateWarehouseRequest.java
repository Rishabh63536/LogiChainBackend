package com.cts.logichain360.dto.request;

import lombok.*;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class CreateWarehouseRequest {
    private String  warehouseCode;
    private String  location;
    private Integer capacity;
}