package com.cts.logichain360.service;

import com.cts.logichain360.dto.response.InvoiceResponse;
import com.cts.logichain360.entity.Orders;
import org.springframework.http.ResponseEntity;

import java.util.List;

public interface InvoiceService {

    //called from orderimpl when order gets confirmed
    InvoiceResponse generateInvoice(Orders order);

     //Called internally by OrderServiceImpl when an order is CANCELLED.
     // Sets the invoice status to VOID.
    
    void voidInvoice(Long orderId);

    ResponseEntity<InvoiceResponse> getInvoiceByOrderId(Long orderId);
    ResponseEntity<List<InvoiceResponse>> getInvoicesByCustomerId(Long customerId);
    ResponseEntity<List<InvoiceResponse>> getAllInvoices();
}
