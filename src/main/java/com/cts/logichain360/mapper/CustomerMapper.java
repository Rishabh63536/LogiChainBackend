package com.cts.logichain360.mapper;

import org.springframework.stereotype.Component;

import com.cts.logichain360.dto.response.CustomerResponse;
import com.cts.logichain360.entity.Customer;

@Component
public class CustomerMapper {

    public CustomerResponse toResponse(Customer c) {
        return CustomerResponse.builder()
                .id(c.getId()).userId(c.getUser().getId())
                .userName(c.getUser().getName()).userPhone(c.getUser().getPhone())
                .companyName(c.getCompanyName()).gstNumber(c.getGstNumber())
                .email(c.getEmail()).shippingAddress(c.getShippingAddress())
                .billingAddress(c.getBillingAddress())
                .creditLimit(c.getCreditLimit()).paymentTerms(c.getPaymentTerms()).build();
    }
}