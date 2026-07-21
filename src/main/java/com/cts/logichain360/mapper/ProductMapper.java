package com.cts.logichain360.mapper;

import org.springframework.stereotype.Component;

import com.cts.logichain360.dto.response.ProductResponse;
import com.cts.logichain360.entity.Product;

@Component
public class ProductMapper {

    public ProductResponse toResponse(Product p) {
        return ProductResponse.builder()
                .productId(p.getProductId())
                .productName(p.getProductName())
                .productPrice(p.getProductPrice())
                .productDescription(p.getProductDescription())
                .vendorId(p.getVendor().getId())
                .vendorCompanyName(p.getVendor().getCompanyName())
                .build();
    }
}