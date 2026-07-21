package com.cts.logichain360.mapper;

import org.springframework.stereotype.Component;

import com.cts.logichain360.dto.response.InvoiceResponse;
import com.cts.logichain360.entity.Invoice;

@Component
public class InvoiceMapper {

    public InvoiceResponse toResponse(Invoice inv) {
        return InvoiceResponse.builder()
                .id(inv.getId())
                .invoiceNumber(inv.getInvoiceNumber())
                .orderId(inv.getOrder().getId())
                .customerId(inv.getCustomerId())
                .customerName(inv.getCustomerName())
                .customerCompany(inv.getCustomerCompany())
                .vendorId(inv.getVendorId())
                .vendorCompanyName(inv.getVendorCompanyName())
                .productName(inv.getProductName())
                .quantity(inv.getQuantity())
                .unitPrice(inv.getUnitPrice())
                .subtotal(inv.getSubtotal())
                .taxPercent(inv.getTaxPercent())
                .taxAmount(inv.getTaxAmount())
                .deliveryFee(inv.getDeliveryFee())
                .totalAmount(inv.getTotalAmount())
                .shippingAddress(inv.getShippingAddress())
                .issuedAt(inv.getIssuedAt())
                .status(inv.getStatus())
                .voidedAt(inv.getVoidedAt())
                .build();
    }
}