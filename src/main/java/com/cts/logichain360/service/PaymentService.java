package com.cts.logichain360.service;

import com.cts.logichain360.dto.response.PaymentResponse;
import org.springframework.http.ResponseEntity;

import java.util.List;

public interface PaymentService {
	//pay adv 50% and generate invoice
    ResponseEntity<PaymentResponse> payAdvance(Long orderId);

    //pays the remaining 50% on an IN_TRANSIT order, doesnt change order status
    ResponseEntity<PaymentResponse> payFinal(Long orderId);

    ResponseEntity<List<PaymentResponse>> getPaymentsByOrderId(Long orderId);

    ResponseEntity<List<PaymentResponse>> getAllPayments();
}