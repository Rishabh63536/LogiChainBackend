package com.cts.logichain360.service.impl;

import com.cts.logichain360.annotation.Auditable;
import com.cts.logichain360.dto.response.PaymentResponse;
import com.cts.logichain360.entity.Orders;
import com.cts.logichain360.entity.Payment;
import com.cts.logichain360.enums.AuditAction; // ASSUMPTION: add PAYMENT_ADVANCE_PAID and PAYMENT_FINAL_PAID constants to your AuditAction enum — not yet reviewed, will not compile until added
import com.cts.logichain360.enums.OrderStatus;
import com.cts.logichain360.enums.PaymentStatus;
import com.cts.logichain360.enums.PaymentType;
import com.cts.logichain360.exception.ResourceNotFoundException;
import com.cts.logichain360.mapper.PaymentMapper;
import com.cts.logichain360.repository.OrderRepository;
import com.cts.logichain360.repository.PaymentRepository;
import com.cts.logichain360.service.InvoiceService;
import com.cts.logichain360.service.PaymentService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepo;
    private final OrderRepository orderRepo;
    private final InvoiceService invoiceService;
    private final PaymentMapper paymentMapper;
    
    @Value("${delivery.fee.percent}")
    private double deliveryPercent;

    @Value("${tax.percent: 18.0}")
    private Double taxPercent;

    
    @Override
    @Transactional
    @Auditable(action = AuditAction.PAYMENT_ADVANCE_PAID, entityType = "Payment")
    public ResponseEntity<PaymentResponse> payAdvance(Long orderId) {
        log.info("Processing advance payment for order={}", orderId);
        Orders order = load(orderId);

        if (order.getStatus() != OrderStatus.PENDING) {
            throw new IllegalArgumentException("Cannot pay advance as order " + orderId + " is " + order.getStatus() + ", it must be PENDING.");
        }
        if (paymentRepo.existsByOrder_IdAndType(orderId, PaymentType.ADVANCE)) {
            throw new IllegalArgumentException("Advance payment already made for order " + orderId + ".");
        }
        
        //using custom round fn to round off to 2 decimals
        double amountDueBeforeDelivery = order.getTotalAmount() * (1+ taxPercent/100.0);
        double advanceAmount = round2(amountDueBeforeDelivery * 0.5);

        Payment payment = paymentRepo.save(Payment.builder()
                .order(order)
                .amount(advanceAmount)
                .type(PaymentType.ADVANCE)
                .status(PaymentStatus.SUCCESS)
                .paidAt(LocalDateTime.now())
                .build());

        order.setAmountPaid(round2(order.getAmountPaid() + advanceAmount));
        order.setStatus(OrderStatus.CONFIRMED);
        orderRepo.save(order);

        log.info("Advance payment of {} recorded for order={}. Status is now CONFIRMED.", advanceAmount, orderId);

        //invoice gen
        try {
            invoiceService.generateInvoice(order);
        } catch (Exception e) {
            log.error("Invoice generation failed for orderId={} (order still confirmed): {}",
                    orderId, e.getMessage(), e);
        }

        return new ResponseEntity<>(paymentMapper.toResponse(payment), HttpStatus.CREATED);
    }

    @Override
    @Transactional
    @Auditable(action = AuditAction.PAYMENT_FINAL_PAID, entityType = "Payment")
    public ResponseEntity<PaymentResponse> payFinal(Long orderId) {
        log.info("Processing final payment for order={}", orderId);
        Orders order = load(orderId);

        if (order.getStatus() != OrderStatus.IN_TRANSIT) {
            throw new IllegalArgumentException("Cannot pay final amount as order " + orderId + " is " + order.getStatus() + ", must be IN_TRANSIT.");
        }
        if (!paymentRepo.existsByOrder_IdAndType(orderId, PaymentType.ADVANCE)) {
            throw new IllegalArgumentException("Cannot pay final amount, advance payment was never made for order " + orderId + ".");
        }
        if (paymentRepo.existsByOrder_IdAndType(orderId, PaymentType.FINAL)) {
            throw new IllegalArgumentException("Final payment already made for order " + orderId + ".");
        }

        double amountDueBeforeDelivery = order.getTotalAmount() * (1 + taxPercent/ 100);
        double deliveryFee = order.getTotalAmount() * (deliveryPercent/100);
        double finalAmount = round2(amountDueBeforeDelivery - order.getAmountPaid())+ deliveryFee;

        Payment payment = paymentRepo.save(Payment.builder()
                .order(order)
                .amount(finalAmount)
                .type(PaymentType.FINAL)
                .status(PaymentStatus.SUCCESS)
                .paidAt(LocalDateTime.now())
                .build());

        order.setAmountPaid(round2(order.getAmountPaid() + finalAmount));
        orderRepo.save(order);

        log.info("Final payment of {} recorded for order={}. Order fully paid={}",
                finalAmount, orderId, order.isFullyPaid());

        return new ResponseEntity<>(paymentMapper.toResponse(payment), HttpStatus.CREATED);
    }

    @Override
    public ResponseEntity<List<PaymentResponse>> getPaymentsByOrderId(Long orderId) {
        if (!orderRepo.existsById(orderId))
            throw new ResourceNotFoundException("Order " + orderId + " not found.");
        return ResponseEntity.ok(paymentRepo.findAllByOrder_IdOrderByPaidAtAsc(orderId)
                .stream().map(paymentMapper::toResponse).toList());
    }

    @Override
    public ResponseEntity<List<PaymentResponse>> getAllPayments() {
        return ResponseEntity.ok(paymentRepo.findAll().stream().map(paymentMapper::toResponse).toList());
    }

    private Orders load(Long orderId) {
        return orderRepo.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order " + orderId + " not found."));
    }

    //to avoid floating point recurrence like 4999.99999, so round off
    private double round2(double value) {
        return Math.round(value * 100.0) / 100.0;
    }
}