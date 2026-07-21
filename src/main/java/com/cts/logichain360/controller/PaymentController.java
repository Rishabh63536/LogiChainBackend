package com.cts.logichain360.controller;

import com.cts.logichain360.dto.response.PaymentResponse;
import com.cts.logichain360.service.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/orders/{orderId}/payments")
@RequiredArgsConstructor
@Tag(name = "Payments", description = "Advance/final payment ledger for orders")
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping("/advance")
    @Operation(summary = "Pay the 50% advance on a PENDING order. Moves order to CONFIRMED.")
     @PreAuthorize("hasAnyRole('CUSTOMER','ADMIN')")
    public ResponseEntity<PaymentResponse> payAdvance(@PathVariable Long orderId) {
        return paymentService.payAdvance(orderId);
    }

    @PostMapping("/final")
    @Operation(summary = "Pay the remaining 50% on an IN_TRANSIT order. Required before delivery can complete.")
     @PreAuthorize("hasAnyRole('CUSTOMER','ADMIN')")
    public ResponseEntity<PaymentResponse> payFinal(@PathVariable Long orderId) {
        return paymentService.payFinal(orderId);
    }

    @GetMapping
    @Operation(summary = "Get all payments (ledger entries) for one order.")
    @PreAuthorize("hasAnyRole('ADMIN','CUSTOMER')")
    public ResponseEntity<List<PaymentResponse>> getByOrder(@PathVariable Long orderId) {
        return paymentService.getPaymentsByOrderId(orderId);
    }
}