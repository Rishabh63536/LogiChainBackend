package com.cts.logichain360.dto.response;

import lombok.*;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DriverResponse {
    private Long id;
    private Long userId;
    private String userName;
    private String userPhone;
    private String licenseNumber;
    private LocalDate licenseExpiry;
    private Boolean available;
    private String location;
}