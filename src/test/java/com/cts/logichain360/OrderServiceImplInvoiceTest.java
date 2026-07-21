package com.cts.logichain360;

// ══════════════════════════════════════════════════════════════════════
//  SUPPLEMENT to existing Ordercontrollertest.java
//  Add these test methods to cover invoice-related order behavior.
//  Also includes OrderServiceImpl-level unit tests for the invoice path.
// ══════════════════════════════════════════════════════════════════════

import com.cts.logichain360.dto.request.PlaceOrderRequest;
import com.cts.logichain360.dto.response.InvoiceResponse;
import com.cts.logichain360.dto.response.OrderResponse;
import com.cts.logichain360.entity.*;
import com.cts.logichain360.enums.*;
import com.cts.logichain360.exception.InsufficientStockException;
import com.cts.logichain360.exception.ResourceNotFoundException;
import com.cts.logichain360.repository.*;
import com.cts.logichain360.service.InvoiceService;
import com.cts.logichain360.service.NotificationService;
import com.cts.logichain360.service.impl.OrderServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceImplInvoiceTest {

    @Mock private OrderRepository          orderRepo;
    @Mock private CustomerRepository       customerRepo;
    @Mock private DriverRepository         driverRepo;
    @Mock private ProductWarehouseRepository pwRepo;
    @Mock private NotificationService      notificationService;
    @Mock private InvoiceService           invoiceService;

    @InjectMocks private OrderServiceImpl orderService;

    private Customer        customer;
    private Vendor          vendor;
    private Product         product;
    private Warehouse       warehouse;
    private ProductWarehouse pw;
    private PlaceOrderRequest placeReq;

    @BeforeEach
    void setUp() {
        User custUser = User.builder().id(1L).name("Alice").phone("9000000001")
                .password("hash").role(UserRole.CUSTOMER).build();
        customer = Customer.builder().id(10L).user(custUser).companyName("Alice Corp").build();

        User vendUser = User.builder().id(2L).name("Vendor Admin").phone("9000000002")
                .password("hash").role(UserRole.VENDOR).build();
        vendor = Vendor.builder().id(20L).user(vendUser).companyName("Sony Vendor").build();

        product = Product.builder().productId(50L)
                .productName("Sony Headphones").productPrice(29990.0).vendor(vendor).build();

        warehouse = Warehouse.builder().id(100L).warehouseCode("WH-BLR-01").build();

        pw = ProductWarehouse.builder().id(55L).product(product)
                .warehouse(warehouse).stock(50).maxStock(200).rolPercent(20.0).build();

        placeReq = PlaceOrderRequest.builder()
                .customerId(10L).productId(50L).quantity(2)
                .shippingAddress("12 MG Road, Bangalore 560001").build();
    }

    private Orders buildSavedOrder(Long id) {
        return Orders.builder()
                .id(id).customer(customer).product(product).productWarehouse(pw)
                .quantity(2).productNameSnapshot("Sony Headphones").unitPriceSnapshot(29990.0)
                .totalAmount(59980.0).status(OrderStatus.CONFIRMED)
                .placedAt(LocalDateTime.now()).shippingAddress("12 MG Road, Bangalore 560001")
                .build();
    }

    // ── Invoice integration with placeOrder ───────────────────────────

    @Test
    void placeOrder_generatesInvoice_onSuccess() {
        when(customerRepo.findById(10L)).thenReturn(Optional.of(customer));
        when(pwRepo.findByProduct_ProductId(50L)).thenReturn(Optional.of(pw));
        Orders saved = buildSavedOrder(1L);
        when(orderRepo.save(any())).thenReturn(saved);
        when(pwRepo.save(any())).thenReturn(pw);
        InvoiceResponse inv = InvoiceResponse.builder().id(1L).invoiceNumber("INV-2026-00001").build();
        when(invoiceService.generateInvoice(any(Orders.class))).thenReturn(inv);

        ResponseEntity<OrderResponse> resp = orderService.placeOrder(placeReq);

        assertEquals(HttpStatus.CREATED, resp.getStatusCode());
        verify(invoiceService).generateInvoice(any(Orders.class));
    }

    @Test
    void placeOrder_invoiceFailure_doesNotRollBackOrder() {
        when(customerRepo.findById(10L)).thenReturn(Optional.of(customer));
        when(pwRepo.findByProduct_ProductId(50L)).thenReturn(Optional.of(pw));
        Orders saved = buildSavedOrder(1L);
        when(orderRepo.save(any())).thenReturn(saved);
        when(pwRepo.save(any())).thenReturn(pw);
        when(invoiceService.generateInvoice(any())).thenThrow(new RuntimeException("DB error during invoice"));

        // Order should still succeed even though invoice failed
        ResponseEntity<OrderResponse> resp = orderService.placeOrder(placeReq);

        assertEquals(HttpStatus.CREATED, resp.getStatusCode());
        assertNotNull(resp.getBody());
    }

    @Test
    void placeOrder_insufficientStock_doesNotGenerateInvoice() {
        pw.setStock(1); // Only 1 in stock, but requesting 2
        when(customerRepo.findById(10L)).thenReturn(Optional.of(customer));
        when(pwRepo.findByProduct_ProductId(50L)).thenReturn(Optional.of(pw));

        assertThrows(InsufficientStockException.class, () -> orderService.placeOrder(placeReq));
        verify(invoiceService, never()).generateInvoice(any());
    }

    @Test
    void placeOrder_customerNotFound_doesNotGenerateInvoice() {
        when(customerRepo.findById(10L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> orderService.placeOrder(placeReq));
        verify(invoiceService, never()).generateInvoice(any());
    }

    // ── Invoice void on cancelOrder ───────────────────────────────────

    @Test
    void cancelOrder_voidsInvoice() {
        Orders order = buildSavedOrder(1L);
        when(orderRepo.findById(1L)).thenReturn(Optional.of(order));
        when(pwRepo.save(any())).thenReturn(pw);
        when(orderRepo.save(any())).thenReturn(order);
        doNothing().when(invoiceService).voidInvoice(1L);

        ResponseEntity<OrderResponse> resp = orderService.cancelOrder(1L);

        assertEquals(HttpStatus.OK, resp.getStatusCode());
        verify(invoiceService).voidInvoice(1L);
    }

    @Test
    void cancelOrder_invoiceVoidFailure_doesNotRollBackCancellation() {
        Orders order = buildSavedOrder(1L);
        when(orderRepo.findById(1L)).thenReturn(Optional.of(order));
        when(pwRepo.save(any())).thenReturn(pw);
        when(orderRepo.save(any())).thenReturn(order);
        doThrow(new RuntimeException("invoice void error")).when(invoiceService).voidInvoice(1L);

        // Cancellation still succeeds despite invoice void failure
        ResponseEntity<OrderResponse> resp = orderService.cancelOrder(1L);

        assertEquals(HttpStatus.OK, resp.getStatusCode());
        assertEquals(OrderStatus.CANCELLED, resp.getBody().getStatus());
    }

    @Test
    void cancelOrder_alreadyInTransit_throwsAndNoVoid() {
        Orders order = buildSavedOrder(1L);
        order.setStatus(OrderStatus.IN_TRANSIT);
        when(orderRepo.findById(1L)).thenReturn(Optional.of(order));

        assertThrows(IllegalArgumentException.class, () -> orderService.cancelOrder(1L));
        verify(invoiceService, never()).voidInvoice(any());
    }

    @Test
    void cancelOrder_alreadyDelivered_throwsAndNoVoid() {
        Orders order = buildSavedOrder(1L);
        order.setStatus(OrderStatus.DELIVERED);
        when(orderRepo.findById(1L)).thenReturn(Optional.of(order));

        assertThrows(IllegalArgumentException.class, () -> orderService.cancelOrder(1L));
        verify(invoiceService, never()).voidInvoice(any());
    }

    @Test
    void placeOrder_stockDecrementedBeforeInvoice() {
        when(customerRepo.findById(10L)).thenReturn(Optional.of(customer));
        when(pwRepo.findByProduct_ProductId(50L)).thenReturn(Optional.of(pw));
        Orders saved = buildSavedOrder(1L);
        when(orderRepo.save(any())).thenReturn(saved);
        when(pwRepo.save(any())).thenReturn(pw);
        when(invoiceService.generateInvoice(any())).thenReturn(
                InvoiceResponse.builder().id(1L).build());

        int stockBefore = pw.getStock(); // 50
        orderService.placeOrder(placeReq);

        assertEquals(48, pw.getStock()); // 50 - 2 = 48
        verify(pwRepo).save(pw);
    }
}
