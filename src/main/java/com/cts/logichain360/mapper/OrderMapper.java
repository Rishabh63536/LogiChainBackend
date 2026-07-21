package com.cts.logichain360.mapper;

import com.cts.logichain360.entity.*;
import org.springframework.stereotype.Component;

import com.cts.logichain360.dto.response.OrderResponse;

@Component
public class OrderMapper {

    public OrderResponse toResponse(Orders o) {
        ProductWarehouse pw = o.getProductWarehouse();
        Driver d = o.getDriver();
        Vendor v = o.getProduct().getVendor();
        Customer c = o.getCustomer();

        return OrderResponse.builder()
                .id(o.getId())
                .amountPaid(o.getAmountPaid())
                .status(o.getStatus())
                .placedAt(o.getPlacedAt())
                .quantity(o.getQuantity())
                .totalAmount(o.getTotalAmount())
                .shippingAddress(o.getShippingAddress())
                .customerId(o.getCustomer().getId())
                .customerName(o.getCustomer().getUser().getName())
                .customerPhone(c.getUser().getPhone())
                .productId(o.getProduct().getProductId())
                .productNameSnapshot(o.getProductNameSnapshot())
                .unitPriceSnapshot(o.getUnitPriceSnapshot())
                .vendorId(v.getId())
                .vendorCompanyName(v.getCompanyName())
                .warehouseId(pw.getWarehouse().getId())
                .warehouseCode(pw.getWarehouse().getWarehouseCode())
                .productWarehouseId(pw.getId())
                .driverId(d == null ? null : d.getId())
                .driverName(d == null ? null : d.getUser().getName())
                .build();
    }
}