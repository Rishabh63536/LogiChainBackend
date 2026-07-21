package com.cts.logichain360.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.*;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
@Schema(description = "Update a product's catalog details. Send only fields to change. Stock is managed via /product-warehouses.")
public class UpdateProductRequest {

    @Size(max = 255, message = "productName must be at most 255 characters")
    @Schema(example = "Sony WH-1000XM5 v2")
    private String productName;

    @DecimalMin(value = "0.0", inclusive = false, message = "productPrice must be greater than 0")
    @Schema(example = "27990.00")
    private Double productPrice;

    @Size(max = 1000, message = "productDescription must be at most 1000 characters")
    @Schema(example = "Updated description")
    private String productDescription;
}