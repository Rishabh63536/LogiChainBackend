package com.cts.logichain360.dto.request;

import com.cts.logichain360.enums.ReturnReason;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.*;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
@Schema(description = "Customer requests a return on a DELIVERED order.")
public class CreateReturnRequestRequest {

    @NotNull(message = "customerId is required")
    @Positive(message = "customerId must be positive")
    @Schema(example = "1", description = "Must match the order's customer.")
    private Long customerId;

    @NotNull(message = "orderId is required")
    @Positive(message = "orderId must be positive")
    private Long orderId;

    @NotNull(message = "returnQuantity is required")
    @Positive(message = "returnQuantity must be positive")
    private Integer returnQuantity;

    @NotNull(message = "reason is required")
    private ReturnReason reason;

    @Size(max = 500, message = "notes must be at most 500 characters")
    private String notes;
}