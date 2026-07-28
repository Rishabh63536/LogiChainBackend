package com.cts.logichain360.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.*;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
@Schema(description = "Place a new order for one product, one quantity.")
public class PlaceOrderRequest {

    @NotNull(message = "customerId is required")
    @Positive(message = "customerId must be positive")
    @Schema(example = "1", requiredMode = Schema.RequiredMode.REQUIRED,
            description = "ID of the customer placing the order.")
    private Long customerId;

    @NotNull(message = "productId is required")
    @Positive(message = "productId must be positive")
    @Schema(example = "1", requiredMode = Schema.RequiredMode.REQUIRED,
            description = "ID of the product to order. Product must be launched at a warehouse.")
    private Long productId;

    @NotNull(message = "quantity is required")
    @Positive(message = "quantity must be positive")
    @Schema(example = "20", requiredMode = Schema.RequiredMode.REQUIRED,
            description = "Number of units to order.")
    private Integer quantity;

    @NotBlank(message = "shippingAddress is required")
    @Size(max = 500, message = "shippingAddress must be at most 500 characters")
    @Schema(example = "12 MG Road, Bangalore 560001",
            requiredMode = Schema.RequiredMode.REQUIRED)
    private String shippingAddress;
}