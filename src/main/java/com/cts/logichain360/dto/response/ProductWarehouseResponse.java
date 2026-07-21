package com.cts.logichain360.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "A product launched at a warehouse, with stock and reorder info.")
public class ProductWarehouseResponse {

    @Schema(example = "1")
    private Long id;
    @Schema(example = "1")
    private Long productId;
    @Schema(example = "Sony WH-1000XM5 Headphones")
    private String productName;
    @Schema(example = "29990.00")
    private Double productPrice;

    @Schema(example = "1")
    private Long vendorId;
    @Schema(example = "Sony India Pvt Ltd")
    private String vendorCompanyName;

    @Schema(example = "1")
    private Long warehouseId;
    @Schema(example = "WH-MUM-01")
    private String warehouseCode;
    @Schema(example = "Bhiwandi, Mumbai")
    private String warehouseLocation;

    @Schema(example = "180", description = "Current stock units.")
    private Integer stock;

    @Schema(example = "500", description = "Max capacity at this warehouse for this product.")
    private Integer maxStock;

    @Schema(example = "40.0", description = "Reorder threshold as % of maxStock.")
    private Double rolPercent;

    @Schema(example = "36.0", description = "Current stock as % of maxStock (computed).")
    private Double currentStockPercent;

    @Schema(example = "true", description = "True when current stock % is below rolPercent.")
    private Boolean belowRol;
}