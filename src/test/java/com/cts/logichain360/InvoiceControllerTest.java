package com.cts.logichain360;

import com.cts.logichain360.controller.InvoiceController;
import com.cts.logichain360.dto.response.InvoiceResponse;
import com.cts.logichain360.enums.InvoiceStatus;
import com.cts.logichain360.service.InvoiceService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InvoiceControllerTest {

    @Mock
    private InvoiceService invoiceService;

    @InjectMocks
    private InvoiceController invoiceController;

    private InvoiceResponse activeInvoice;
    private InvoiceResponse voidInvoice;

    @BeforeEach
    void setUp() {
        activeInvoice = InvoiceResponse.builder()
                .id(1L).invoiceNumber("INV-2026-00001").orderId(100L)
                .customerId(10L).customerName("Alice").customerCompany("Alice Corp")
                .vendorId(20L).vendorCompanyName("Sony Vendor")
                .productName("Sony Headphones").quantity(2).unitPrice(29990.0)
                .subtotal(59980.0).taxPercent(18.0).taxAmount(10796.4).totalAmount(70776.4)
                .shippingAddress("12 MG Road, Bangalore").issuedAt(LocalDateTime.now())
                .status(InvoiceStatus.ACTIVE).build();

        voidInvoice = InvoiceResponse.builder()
                .id(2L).invoiceNumber("INV-2026-00002").orderId(101L)
                .customerId(10L).customerName("Alice").customerCompany("Alice Corp")
                .productName("LG Monitor").quantity(1).unitPrice(15000.0)
                .subtotal(15000.0).taxPercent(18.0).taxAmount(2700.0).totalAmount(17700.0)
                .status(InvoiceStatus.VOID).voidedAt(LocalDateTime.now()).build();
    }

    @Test
    void getByOrder_activeInvoice_returnsOk() {
        when(invoiceService.getInvoiceByOrderId(100L)).thenReturn(ResponseEntity.ok(activeInvoice));

        ResponseEntity<InvoiceResponse> resp = invoiceController.getByOrder(100L);

        assertEquals(HttpStatus.OK, resp.getStatusCode());
        assertEquals("INV-2026-00001", resp.getBody().getInvoiceNumber());
        assertEquals(InvoiceStatus.ACTIVE, resp.getBody().getStatus());
        assertEquals(70776.4, resp.getBody().getTotalAmount());
    }

    @Test
    void getByOrder_voidInvoice_returnsOk() {
        when(invoiceService.getInvoiceByOrderId(101L)).thenReturn(ResponseEntity.ok(voidInvoice));

        ResponseEntity<InvoiceResponse> resp = invoiceController.getByOrder(101L);

        assertEquals(HttpStatus.OK, resp.getStatusCode());
        assertEquals(InvoiceStatus.VOID, resp.getBody().getStatus());
        assertNotNull(resp.getBody().getVoidedAt());
    }

    @Test
    void getByOrder_notFound_returns404() {
        when(invoiceService.getInvoiceByOrderId(999L)).thenReturn(ResponseEntity.notFound().build());

        ResponseEntity<InvoiceResponse> resp = invoiceController.getByOrder(999L);

        assertEquals(HttpStatus.NOT_FOUND, resp.getStatusCode());
    }

    @Test
    void getByCustomer_returnsList() {
        when(invoiceService.getInvoicesByCustomerId(10L))
                .thenReturn(ResponseEntity.ok(List.of(activeInvoice, voidInvoice)));

        ResponseEntity<List<InvoiceResponse>> resp = invoiceController.getByCustomer(10L);

        assertEquals(HttpStatus.OK, resp.getStatusCode());
        assertEquals(2, resp.getBody().size());
    }

    @Test
    void getByCustomer_empty() {
        when(invoiceService.getInvoicesByCustomerId(99L)).thenReturn(ResponseEntity.ok(List.of()));

        ResponseEntity<List<InvoiceResponse>> resp = invoiceController.getByCustomer(99L);

        assertEquals(HttpStatus.OK, resp.getStatusCode());
        assertTrue(resp.getBody().isEmpty());
    }

    @Test
    void getAll_returnsList() {
        when(invoiceService.getAllInvoices()).thenReturn(ResponseEntity.ok(List.of(activeInvoice, voidInvoice)));

        ResponseEntity<List<InvoiceResponse>> resp = invoiceController.getAll();

        assertEquals(HttpStatus.OK, resp.getStatusCode());
        assertEquals(2, resp.getBody().size());
    }

    @Test
    void getAll_empty() {
        when(invoiceService.getAllInvoices()).thenReturn(ResponseEntity.ok(List.of()));

        ResponseEntity<List<InvoiceResponse>> resp = invoiceController.getAll();

        assertEquals(HttpStatus.OK, resp.getStatusCode());
        assertTrue(resp.getBody().isEmpty());
    }

    @Test
    void invoiceContainsCorrectTaxCalculation() {
        when(invoiceService.getInvoiceByOrderId(100L)).thenReturn(ResponseEntity.ok(activeInvoice));

        InvoiceResponse inv = invoiceController.getByOrder(100L).getBody();

        assertNotNull(inv);
        assertEquals(18.0, inv.getTaxPercent());
        // subtotal * 18% = 59980 * 0.18 = 10796.4
        assertEquals(10796.4, inv.getTaxAmount(), 0.01);
        // total = subtotal + tax
        assertEquals(inv.getSubtotal() + inv.getTaxAmount(), inv.getTotalAmount(), 0.01);
    }

    @Test
    void getByCustomer_invoicesOrdered() {
        when(invoiceService.getInvoicesByCustomerId(10L))
                .thenReturn(ResponseEntity.ok(List.of(activeInvoice, voidInvoice)));

        List<InvoiceResponse> invoices = invoiceController.getByCustomer(10L).getBody();

        assertEquals(2, invoices.size());
        // First should be the newer (ACTIVE) invoice
        assertEquals("INV-2026-00001", invoices.get(0).getInvoiceNumber());
    }
}
