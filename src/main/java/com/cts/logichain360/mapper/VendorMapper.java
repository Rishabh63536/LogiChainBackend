package com.cts.logichain360.mapper;

import org.springframework.stereotype.Component;

import com.cts.logichain360.dto.response.VendorResponse;
import com.cts.logichain360.entity.Vendor;

@Component
public class VendorMapper {

    public VendorResponse toResponse(Vendor v) {
        return VendorResponse.builder()
                .id(v.getId()).userId(v.getUser().getId())
                .userName(v.getUser().getName()).userPhone(v.getUser().getPhone())
                .companyName(v.getCompanyName()).gstNumber(v.getGstNumber())
                .email(v.getEmail()).businessAddress(v.getBusinessAddress())
                .contactPerson(v.getContactPerson()).paymentTerms(v.getPaymentTerms()).build();
    }
}