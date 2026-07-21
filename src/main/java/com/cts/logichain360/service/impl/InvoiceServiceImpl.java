package com.cts.logichain360.service.impl;

import com.cts.logichain360.dto.response.InvoiceResponse;
import com.cts.logichain360.entity.*;
import com.cts.logichain360.enums.InvoiceStatus;
import com.cts.logichain360.exception.ResourceNotFoundException;
import com.cts.logichain360.mapper.InvoiceMapper;
import com.cts.logichain360.repository.InvoiceRepository;
import com.cts.logichain360.service.InvoiceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class InvoiceServiceImpl implements InvoiceService {

    private final InvoiceRepository invoiceRepo;
    private final InvoiceMapper     invoiceMapper;

    private static final String INV_PREFIX = "INV";
    @Value("${tax.percent:18.0}")
    private  double taxPercent;

    @Value("${delivery.fee.percent}")
    private double deliveryFeePercent;

    @Override
    @Transactional
    public InvoiceResponse generateInvoice(Orders order) {
        // Idempotency: if somehow called twice for the same order, return existing
        if (invoiceRepo.existsByOrder_Id(order.getId())) {
            log.warn("Invoice already exists for orderId={}. Returning existing.", order.getId());
            return invoiceMapper.toResponse(invoiceRepo.findByOrder_Id(order.getId()).orElseThrow());
        }

        Customer customer = order.getCustomer();
        Product  product  = order.getProduct();
        Vendor   vendor   = product.getVendor();

        double subtotal   = order.getUnitPriceSnapshot() * order.getQuantity();
        double taxAmount = subtotal * taxPercent / 100.0;
        double deliveryFee = subtotal * deliveryFeePercent/100;
        double total  = subtotal + taxAmount +deliveryFee;

        // Persist with a placeholder invoice number first to get the DB id
        Invoice invoice = Invoice.builder()
                .invoiceNumber("PENDING")	// temporary
                .order(order)
                .customerId(customer.getId())
                .customerName(customer.getUser().getName())
                .customerCompany(customer.getCompanyName() != null ? customer.getCompanyName() : "N/A")
                .vendorId(vendor.getId())
                .vendorCompanyName(vendor.getCompanyName())
                .productName(order.getProductNameSnapshot())
                .quantity(order.getQuantity())
                .unitPrice(order.getUnitPriceSnapshot())
                .subtotal(subtotal)
                .taxPercent(taxPercent)
                .taxAmount(taxAmount)
                .deliveryFee(deliveryFee)
                .totalAmount(total)
                .shippingAddress(order.getShippingAddress())
                .issuedAt(LocalDateTime.now())
                .status(InvoiceStatus.ACTIVE)
                .build();

        Invoice saved = invoiceRepo.save(invoice);

        // Build a deterministic, readable invoice number using DB id
        String invoiceNumber = String.format("%s-%d-%05d",
                INV_PREFIX,
                saved.getIssuedAt().getYear(),
                saved.getId());
        saved.setInvoiceNumber(invoiceNumber);
        saved = invoiceRepo.save(saved);

        log.info("Invoice {} generated for orderId={}, total={}", invoiceNumber, order.getId(), total);
        return invoiceMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public void voidInvoice(Long orderId) {
        invoiceRepo.findByOrder_Id(orderId).ifPresentOrElse(inv -> {
            inv.setStatus(InvoiceStatus.VOID);
            inv.setVoidedAt(LocalDateTime.now());
            invoiceRepo.save(inv);
            log.info("Invoice {} voided (orderId={}).", inv.getInvoiceNumber(), orderId);
        }, () -> log.warn("voidInvoice called for orderId={} but no invoice found. Skipping.", orderId));
    }

    // ── Public reads ──────────────────────────────────────────────────

    @Override
    public ResponseEntity<InvoiceResponse> getInvoiceByOrderId(Long orderId) {
        return invoiceRepo.findByOrder_Id(orderId)
                .map(inv -> ResponseEntity.ok(invoiceMapper.toResponse(inv)))
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No invoice found for order " + orderId));
    }

    @Override
    public ResponseEntity<List<InvoiceResponse>> getInvoicesByCustomerId(Long customerId) {
        return ResponseEntity.ok(
                invoiceRepo.findAllByCustomerIdOrderByIssuedAtDesc(customerId)
                           .stream().map(invoiceMapper::toResponse).toList());
    }

    @Override
    public ResponseEntity<List<InvoiceResponse>> getAllInvoices() {
        return ResponseEntity.ok(
                invoiceRepo.findAll().stream().map(invoiceMapper::toResponse).toList());
    }
}