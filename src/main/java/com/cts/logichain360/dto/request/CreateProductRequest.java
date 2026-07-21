package com.cts.logichain360.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Create a vendor's catalog product. Stock/warehouse is added separately via /product-warehouses.")
public class CreateProductRequest {

    @NotBlank(message = "productName is required")
    @Size(max = 255, message = "productName must be at most 255 characters")
    @Schema(example = "Sony WH-1000XM5 Headphones", requiredMode = Schema.RequiredMode.REQUIRED)
    private String productName;

    @NotNull(message = "productPrice is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "productPrice must be greater than 0")
    @Schema(example = "29990.00", requiredMode = Schema.RequiredMode.REQUIRED)
    private Double productPrice;

    @Size(max = 1000, message = "productDescription must be at most 1000 characters")
    @Schema(example = "Wireless noise-cancelling over-ear headphones")
    private String productDescription;

    @NotNull(message = "vendorId is required")
    @Positive(message = "vendorId must be positive")
    @Schema(example = "1", requiredMode = Schema.RequiredMode.REQUIRED, description = "Vendor's id (monopoly: vendor cannot have duplicate product names).")
    private Long vendorId;
}