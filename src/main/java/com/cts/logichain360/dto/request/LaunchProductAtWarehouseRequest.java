package com.cts.logichain360.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Launch a product at a warehouse with initial stock and a reorder threshold.")
public class LaunchProductAtWarehouseRequest {

    @NotNull(message = "productId is required")
    @Positive(message = "productId must be positive")
    @Schema(example = "1", description = "ID of the product (vendor's catalog entry).")
    private Long productId;

    @NotNull(message = "warehouseId is required")
    @Positive(message = "warehouseId must be positive")
    @Schema(example = "1", description = "ID of the warehouse where the product will be launched.")
    private Long warehouseId;

    @NotNull(message = "initial stock is required")
    @Min(value = 0, message = "stock cannot be negative")
    @Schema(example = "200", description = "Initial stock units to place at this warehouse.")
    private Integer stock;

    @NotNull(message = "maxStock is required")
    @Positive(message = "maxStock must be greater than zero")
    @Schema(example = "500", description = "Maximum stock capacity at this warehouse.")
    private Integer maxStock;

    @NotNull(message = "rolPercent is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "rolPercent must be > 0")
    @DecimalMax(value = "100.0", inclusive = false, message = "rolPercent must be < 100")
    @Schema(example = "40.0",
            description = "Reorder level as percentage of maxStock. e.g. 40 = restock when stock drops below 40%.")
    private Double rolPercent;
}