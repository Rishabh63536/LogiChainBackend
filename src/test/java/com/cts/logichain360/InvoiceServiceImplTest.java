package com.cts.logichain360;

import com.cts.logichain360.dto.response.InvoiceResponse;
import com.cts.logichain360.entity.*;
import com.cts.logichain360.enums.InvoiceStatus;
import com.cts.logichain360.enums.OrderStatus;
import com.cts.logichain360.exception.ResourceNotFoundException;
import com.cts.logichain360.repository.InvoiceRepository;
import com.cts.logichain360.service.impl.InvoiceServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InvoiceServiceImplTest {

    @Mock
    private InvoiceRepository invoiceRepo;

    @InjectMocks
    private InvoiceServiceImpl invoiceService;

    private Orders confirmedOrder;
    private Invoice savedInvoice;

    @BeforeEach
    void setUp() {
        // Build a realistic Orders object via nested entities
        User customerUser = User.builder().id(1L).name("Alice").phone("9000000001")
                .password("hash").role(com.cts.logichain360.enums.UserRole.CUSTOMER).build();
        Customer customer = Customer.builder().id(10L).user(customerUser)
                .companyName("Alice Corp").build();

        User vendorUser = User.builder().id(2L).name("Vendor Admin").phone("9000000002")
                .password("hash").role(com.cts.logichain360.enums.UserRole.VENDOR).build();
        Vendor vendor = Vendor.builder().id(20L).user(vendorUser).companyName("Sony Vendor").build();

        Product product = Product.builder().productId(50L)
                .productName("Sony Headphones").productPrice(29990.0).vendor(vendor).build();

        Warehouse warehouse = Warehouse.builder().id(100L).warehouseCode("WH-BLR-01").build();

        ProductWarehouse pw = ProductWarehouse.builder().id(55L).product(product)
                .warehouse(warehouse).stock(100).maxStock(200).rolPercent(20.0).build();

        confirmedOrder = Orders.builder()
                .id(999L).customer(customer).product(product).productWarehouse(pw)
                .quantity(2).productNameSnapshot("Sony Headphones").unitPriceSnapshot(29990.0)
                .totalAmount(59980.0).status(OrderStatus.CONFIRMED)
                .placedAt(LocalDateTime.now()).shippingAddress("12 MG Road, Bangalore")
                .build();

        savedInvoice = Invoice.builder()
                .id(1L).invoiceNumber("INV-2026-00001").order(confirmedOrder)
                .customerId(10L).customerName("Alice").customerCompany("Alice Corp")
                .vendorId(20L).vendorCompanyName("Sony Vendor")
                .productName("Sony Headphones").quantity(2).unitPrice(29990.0)
                .subtotal(59980.0).taxPercent(18.0).taxAmount(10796.4).totalAmount(70776.4)
                .shippingAddress("12 MG Road, Bangalore").issuedAt(LocalDateTime.now())
                .status(InvoiceStatus.ACTIVE).build();
    }

    // ── generateInvoice ───────────────────────────────────────────────

    @Test
    void generateInvoice_newOrder_createsInvoiceWithCorrectFields() {
        when(invoiceRepo.existsByOrder_Id(999L)).thenReturn(false);
        // First save returns invoice with id set but placeholder number
        Invoice withId = Invoice.builder().id(1L).invoiceNumber("PENDING")
                .order(confirmedOrder).issuedAt(LocalDateTime.now())
                .customerId(10L).customerName("Alice").customerCompany("Alice Corp")
                .vendorId(20L).vendorCompanyName("Sony Vendor")
                .productName("Sony Headphones").quantity(2).unitPrice(29990.0)
                .subtotal(59980.0).taxPercent(18.0).taxAmount(10796.4).totalAmount(70776.4)
                .shippingAddress("12 MG Road, Bangalore").status(InvoiceStatus.ACTIVE).build();
        when(invoiceRepo.save(any(Invoice.class))).thenReturn(withId).thenReturn(savedInvoice);

        InvoiceResponse resp = invoiceService.generateInvoice(confirmedOrder);

        assertNotNull(resp);
        assertEquals(10L, resp.getCustomerId());
        assertEquals("Alice Corp", resp.getCustomerCompany());
        assertEquals(20L, resp.getVendorId());
        assertEquals("Sony Vendor", resp.getVendorCompanyName());
        assertEquals(2, resp.getQuantity());
        assertEquals(29990.0, resp.getUnitPrice());
        assertEquals(59980.0, resp.getSubtotal(), 0.01);
        assertEquals(18.0, resp.getTaxPercent());
        assertEquals(10796.4, resp.getTaxAmount(), 0.01);
        assertEquals(70776.4, resp.getTotalAmount(), 0.01);
        assertEquals(InvoiceStatus.ACTIVE, resp.getStatus());
        verify(invoiceRepo, times(2)).save(any(Invoice.class));
    }

    @Test
    void generateInvoice_idempotent_returnsExistingIfAlreadyPresent() {
        when(invoiceRepo.existsByOrder_Id(999L)).thenReturn(true);
        when(invoiceRepo.findByOrder_Id(999L)).thenReturn(Optional.of(savedInvoice));

        InvoiceResponse resp = invoiceService.generateInvoice(confirmedOrder);

        assertNotNull(resp);
        assertEquals("INV-2026-00001", resp.getInvoiceNumber());
        verify(invoiceRepo, never()).save(any());
    }

    @Test
    void generateInvoice_taxCalculatedCorrectly() {
        when(invoiceRepo.existsByOrder_Id(999L)).thenReturn(false);
        Invoice withId = Invoice.builder().id(1L).invoiceNumber("PENDING")
                .order(confirmedOrder).issuedAt(LocalDateTime.now())
                .customerId(10L).customerName("Alice").customerCompany("Alice Corp")
                .vendorId(20L).vendorCompanyName("Sony Vendor")
                .productName("Sony Headphones").quantity(2).unitPrice(29990.0)
                .subtotal(59980.0).taxPercent(18.0).taxAmount(10796.4).totalAmount(70776.4)
                .shippingAddress("12 MG Road, Bangalore").status(InvoiceStatus.ACTIVE).build();
        when(invoiceRepo.save(any())).thenReturn(withId).thenReturn(savedInvoice);

        InvoiceResponse resp = invoiceService.generateInvoice(confirmedOrder);

        double expectedSubtotal = 29990.0 * 2;
        double expectedTax = expectedSubtotal * 18.0 / 100;
        double expectedTotal = expectedSubtotal + expectedTax;
        assertEquals(expectedSubtotal, resp.getSubtotal(), 0.01);
        assertEquals(expectedTax,      resp.getTaxAmount(), 0.01);
        assertEquals(expectedTotal,    resp.getTotalAmount(), 0.01);
    }

    // ── voidInvoice ───────────────────────────────────────────────────

    @Test
    void voidInvoice_setsStatusToVoid() {
        Invoice activeInvoice = Invoice.builder().id(1L).invoiceNumber("INV-2026-00001")
                .order(confirmedOrder).status(InvoiceStatus.ACTIVE).build();
        when(invoiceRepo.findByOrder_Id(999L)).thenReturn(Optional.of(activeInvoice));
        when(invoiceRepo.save(any())).thenReturn(activeInvoice);

        invoiceService.voidInvoice(999L);

        ArgumentCaptor<Invoice> captor = ArgumentCaptor.forClass(Invoice.class);
        verify(invoiceRepo).save(captor.capture());
        assertEquals(InvoiceStatus.VOID, captor.getValue().getStatus());
        assertNotNull(captor.getValue().getVoidedAt());
    }

    @Test
    void voidInvoice_noInvoiceExists_doesNotThrow() {
        when(invoiceRepo.findByOrder_Id(888L)).thenReturn(Optional.empty());

        assertDoesNotThrow(() -> invoiceService.voidInvoice(888L));
        verify(invoiceRepo, never()).save(any());
    }

    // ── Reads ─────────────────────────────────────────────────────────

    @Test
    void getInvoiceByOrderId_found() {
        when(invoiceRepo.findByOrder_Id(999L)).thenReturn(Optional.of(savedInvoice));

        ResponseEntity<InvoiceResponse> resp = invoiceService.getInvoiceByOrderId(999L);

        assertEquals(HttpStatus.OK, resp.getStatusCode());
        assertEquals("INV-2026-00001", resp.getBody().getInvoiceNumber());
    }

    @Test
    void getInvoiceByOrderId_notFound_throwsResourceNotFoundException() {
        when(invoiceRepo.findByOrder_Id(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> invoiceService.getInvoiceByOrderId(999L));
    }

    @Test
    void getInvoicesByCustomerId_returnsList() {
        when(invoiceRepo.findAllByCustomerIdOrderByIssuedAtDesc(10L))
                .thenReturn(List.of(savedInvoice));

        ResponseEntity<List<InvoiceResponse>> resp = invoiceService.getInvoicesByCustomerId(10L);

        assertEquals(HttpStatus.OK, resp.getStatusCode());
        assertEquals(1, resp.getBody().size());
        assertEquals(10L, resp.getBody().get(0).getCustomerId());
    }

    @Test
    void getInvoicesByCustomerId_empty() {
        when(invoiceRepo.findAllByCustomerIdOrderByIssuedAtDesc(99L)).thenReturn(List.of());

        ResponseEntity<List<InvoiceResponse>> resp = invoiceService.getInvoicesByCustomerId(99L);

        assertEquals(HttpStatus.OK, resp.getStatusCode());
        assertTrue(resp.getBody().isEmpty());
    }

    @Test
    void getAllInvoices_returnsList() {
        when(invoiceRepo.findAll()).thenReturn(List.of(savedInvoice));

        ResponseEntity<List<InvoiceResponse>> resp = invoiceService.getAllInvoices();

        assertEquals(HttpStatus.OK, resp.getStatusCode());
        assertEquals(1, resp.getBody().size());
    }

    @Test
    void getAllInvoices_empty() {
        when(invoiceRepo.findAll()).thenReturn(List.of());

        ResponseEntity<List<InvoiceResponse>> resp = invoiceService.getAllInvoices();

        assertEquals(HttpStatus.OK, resp.getStatusCode());
        assertTrue(resp.getBody().isEmpty());
    }
}
