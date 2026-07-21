package com.cts.logichain360.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.*;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
@Schema(description = "Add stock to a product-warehouse entry. Use a positive amount to restock.")
public class RestockRequest {

    @NotNull(message = "amount is required")
    @Positive(message = "amount must be positive")
    @Schema(example = "100", description = "Units to add to current stock. Must be positive.")
    private Integer amount;
}