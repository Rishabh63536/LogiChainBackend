package com.cts.logichain360.controller;

import com.cts.logichain360.dto.response.InvoiceResponse;
import com.cts.logichain360.service.InvoiceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/invoices")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Invoice", description = "Invoice retrieval for customers and vendors.")
public class InvoiceController {

    private final InvoiceService invoiceService;

    @PreAuthorize("hasAnyRole('CUSTOMER', 'ADMIN')")
    @Operation(summary = "Get invoice for an order",
               description = "Customer retrieves the invoice generated when their order was confirmed.")
    @GetMapping("/order/{orderId}")
    public ResponseEntity<InvoiceResponse> getByOrder(@PathVariable Long orderId) {
        log.info("GET /invoices/order/{}", orderId);
        return invoiceService.getInvoiceByOrderId(orderId);
    }

    @PreAuthorize("hasAnyRole('CUSTOMER', 'ADMIN')")
    @Operation(summary = "Get all invoices for a customer")
    @GetMapping("/customer/{customerId}")
    public ResponseEntity<List<InvoiceResponse>> getByCustomer(@PathVariable Long customerId) {
        log.info("GET /invoices/customer/{}", customerId);
        return invoiceService.getInvoicesByCustomerId(customerId);
    }

    @PreAuthorize("hasAnyRole('VENDOR', 'ADMIN')")
    @Operation(summary = "Get all invoices (vendor / admin view)")
    @GetMapping
    public ResponseEntity<List<InvoiceResponse>> getAll() {
        log.info("GET /invoices — vendor/admin listing");
        return invoiceService.getAllInvoices();
    }
}
