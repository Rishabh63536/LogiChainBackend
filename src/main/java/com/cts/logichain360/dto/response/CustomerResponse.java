package com.cts.logichain360.dto.response;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CustomerResponse {
    private Long id;
    private Long userId;
    private String userName;
    private String userPhone;
    private String companyName;
    private String gstNumber;
    private String email;
    private String shippingAddress;
    private String billingAddress;
    private Double creditLimit;
    private String paymentTerms;
}