package com.cts.logichain360.dto.response;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VendorResponse {
    private Long id;
    private Long userId;
    private String userName;
    private String userPhone;
    private String companyName;
    private String gstNumber;
    private String email;
    private String businessAddress;
    private String contactPerson;
    private String paymentTerms;
}