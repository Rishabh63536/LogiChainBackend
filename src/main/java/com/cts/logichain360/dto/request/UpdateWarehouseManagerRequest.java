package com.cts.logichain360.dto.request;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateWarehouseManagerRequest {
    private String employeeCode;
    private String designation;
}