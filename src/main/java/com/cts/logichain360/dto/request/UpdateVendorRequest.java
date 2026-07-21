package com.cts.logichain360.dto.request;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateVendorRequest {
    private String companyName;
    private String gstNumber;
    private String email;
    private String businessAddress;
    private String contactPerson;
    private String paymentTerms;
}