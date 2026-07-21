package com.cts.logichain360.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.*;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
@Schema(description = "WarehouseManager rejects a return request.")
public class RejectReturnRequest {

    @NotNull(message = "managerId is required")
    @Positive(message = "managerId must be positive")
    private Long managerId;

    @Size(max = 500, message = "notes must be at most 500 characters")
    @Schema(description = "Optional reason for rejection.")
    private String notes;
}