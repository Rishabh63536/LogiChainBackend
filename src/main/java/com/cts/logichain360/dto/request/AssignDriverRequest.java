package com.cts.logichain360.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Assign a driver to a confirmed order.")
public class AssignDriverRequest {

    @NotNull(message = "driverId is required")
    @Positive(message = "driverId must be positive")
    @Schema(example = "1", requiredMode = Schema.RequiredMode.REQUIRED, description = "ID of the driver to assign. Driver must be currently available.")
    private Long driverId;
}