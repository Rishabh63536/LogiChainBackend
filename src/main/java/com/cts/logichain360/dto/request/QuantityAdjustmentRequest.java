package com.cts.logichain360.dto.request;

import lombok.*;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class QuantityAdjustmentRequest {
    private Integer delta;
}