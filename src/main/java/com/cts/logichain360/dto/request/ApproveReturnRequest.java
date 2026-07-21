package com.cts.logichain360.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.*;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
@Schema(description = "WarehouseManager approves a return and assigns a pickup driver in one action.")
public class ApproveReturnRequest {

    @NotNull(message = "managerId is required")
    @Positive(message = "managerId must be positive")
    private Long managerId;

    @NotNull(message = "driverId is required")
    @Positive(message = "driverId must be positive")
    @Schema(description = "Must currently be available — same check as order driver-assignment.")
    private Long driverId;
}