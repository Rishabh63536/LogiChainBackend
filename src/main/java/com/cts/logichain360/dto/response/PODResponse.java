package com.cts.logichain360.dto.response;

import lombok.*;

import java.time.LocalDateTime;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class PODResponse {
    private Long id;
    private Long orderId;
    private String photoUrl;
    private Long driverId;
    private String driverName;
    private LocalDateTime uploadedAt;
}