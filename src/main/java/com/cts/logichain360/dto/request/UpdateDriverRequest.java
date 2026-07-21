package com.cts.logichain360.dto.request;

import lombok.*;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateDriverRequest {
    private String licenseNumber;
    private LocalDate licenseExpiry;
    private String location;
}