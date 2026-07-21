package com.cts.logichain360.dto.request;
import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class UpdateCustomerRequest {
    private String companyName;
    private String gstNumber;
    private String email;
    private String shippingAddress;
    private String billingAddress;
    private Double creditLimit;
    private String paymentTerms;
}