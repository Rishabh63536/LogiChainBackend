package com.cts.logichain360.dto.request;

import lombok.*;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class QuantityAdjustmentRequest {
    // Positive to add stock, negative to remove
    private Integer delta;
}