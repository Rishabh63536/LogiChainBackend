package com.cts.logichain360.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Adjust the reorder threshold or maximum capacity for a stock entry. Send only fields to change.")
public class UpdateProductWarehouseRequest {

    @Positive(message = "maxStock must be greater than zero")
    @Schema(example = "600", description = "New maximum stock capacity (optional).")
    private Integer maxStock;

    @DecimalMin(value = "0.0", inclusive = false, message = "rolPercent must be > 0")
    @DecimalMax(value = "100.0", inclusive = false, message = "rolPercent must be < 100")
    @Schema(example = "30.0", description = "New reorder level percentage (optional).")
    private Double rolPercent;
}