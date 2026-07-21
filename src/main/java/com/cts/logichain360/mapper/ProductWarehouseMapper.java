package com.cts.logichain360.mapper;

import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import com.cts.logichain360.dto.response.ProductWarehouseResponse;
import com.cts.logichain360.entity.ProductWarehouse;
@Component
public class ProductWarehouseMapper {
	
	public ProductWarehouseResponse toResponse(ProductWarehouse pw) {
        double currentPercent = (pw.getMaxStock() == null || pw.getMaxStock() == 0) ? 0.0
                : (pw.getStock().doubleValue() / pw.getMaxStock().doubleValue()) * 100.0;

        return ProductWarehouseResponse.builder()
                .id(pw.getId())
                .productId(pw.getProduct().getProductId())
                .productName(pw.getProduct().getProductName())
                .productPrice(pw.getProduct().getProductPrice())
                .vendorId(pw.getProduct().getVendor().getId())
                .vendorCompanyName(pw.getProduct().getVendor().getCompanyName())
                .warehouseId(pw.getWarehouse().getId())
                .warehouseCode(pw.getWarehouse().getWarehouseCode())
                .warehouseLocation(pw.getWarehouse().getLocation())
                .stock(pw.getStock())
                .maxStock(pw.getMaxStock())
                .rolPercent(pw.getRolPercent())
                .currentStockPercent(Math.round(currentPercent * 100.0) / 100.0)
                .belowRol(pw.isBelowRol())
                .build();
    }

}
