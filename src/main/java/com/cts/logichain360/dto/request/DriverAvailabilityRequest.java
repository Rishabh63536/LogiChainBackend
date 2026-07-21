package com.cts.logichain360.dto.request;
import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class DriverAvailabilityRequest {
    private Boolean available;
}