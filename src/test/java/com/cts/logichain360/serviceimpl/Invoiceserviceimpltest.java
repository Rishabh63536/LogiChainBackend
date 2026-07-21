package com.cts.logichain360.serviceimpl;

import com.cts.logichain360.dto.response.InvoiceResponse;
import com.cts.logichain360.entity.*;
import com.cts.logichain360.enums.InvoiceStatus;
import com.cts.logichain360.enums.UserRole;
import com.cts.logichain360.enums.UserStatus;
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
import java.util.Arrays;
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

    private Customer mockCustomer;
    private Vendor mockVendor;
    private Product mockProduct;
    private Warehouse mockWarehouse;
    private ProductWarehouse mockPW;
    private Orders mockOrder;
    private Invoice mockInvoice;

    // Fixed issuedAt used in all invoice builds — year 2026 used in invoice number
    private static final LocalDateTime ISSUED_AT = LocalDateTime.of(2026, 6, 1, 10, 0);

    @BeforeEach
    void setUp() {
        User customerUser = User.builder()
                .id(10L).name("Alice Smith").phone("9876543210")
                .role(UserRole.CUSTOMER).status(UserStatus.ACTIVE).build();

        mockCustomer = Customer.builder()
                .id(1L).user(customerUser).companyName("Alice Corp").build();

        User vendorUser = User.builder()
                .id(30L).name("Sony India").phone("9999999999").build();

        mockVendor = Vendor.builder()
                .id(30L).user(vendorUser).companyName("Sony India Pvt Ltd").build();

        mockProduct = Product.builder()
                .productId(5L).productName("Sony WH-1000XM5")
                .productPrice(29990.0).vendor(mockVendor).build();

        mockWarehouse = Warehouse.builder()
                .id(100L).warehouseCode("WH-CHN-01").location("Chennai").build();

        mockPW = ProductWarehouse.builder()
                .id(10L).product(mockProduct).warehouse(mockWarehouse)
                .stock(300).maxStock(500).rolPercent(40.0).build();

        mockOrder = Orders.builder()
                .id(1L).customer(mockCustomer).product(mockProduct)
                .productWarehouse(mockPW).quantity(2)
                .productNameSnapshot("Sony WH-1000XM5")
                .unitPriceSnapshot(29990.0).totalAmount(59980.0)
                .shippingAddress("12 MG Road, Bangalore")
                .placedAt(ISSUED_AT).build();

        // Pre-built final invoice used for read-method tests
        mockInvoice = Invoice.builder()
                .id(1L).invoiceNumber("INV-2026-00001").order(mockOrder)
                .customerId(1L).customerName("Alice Smith").customerCompany("Alice Corp")
                .vendorId(30L).vendorCompanyName("Sony India Pvt Ltd")
                .productName("Sony WH-1000XM5").quantity(2)
                .unitPrice(29990.0).subtotal(59980.0)
                .taxPercent(18.0).taxAmount(10796.4).totalAmount(70776.4)
                .shippingAddress("12 MG Road, Bangalore")
                .issuedAt(ISSUED_AT).status(InvoiceStatus.ACTIVE).build();
    }

    // Helper: builds an invoice with all required fields and non-null issuedAt.
    // CRITICAL: issuedAt must NOT be null because generateInvoice calls
    //           saved.getIssuedAt().getYear() on the FIRST save return.
    private Invoice buildPendingInvoice(Long id) {
        return Invoice.builder()
                .id(id).invoiceNumber("PENDING").order(mockOrder)
                .customerId(1L).customerName("Alice Smith").customerCompany("Alice Corp")
                .vendorId(30L).vendorCompanyName("Sony India Pvt Ltd")
                .productName("Sony WH-1000XM5").quantity(2)
                .unitPrice(29990.0).subtotal(59980.0)
                .taxPercent(18.0).taxAmount(10796.4).totalAmount(70776.4)
                .shippingAddress("12 MG Road, Bangalore")
                .issuedAt(ISSUED_AT)   // ← must be set — used for invoice number year
                .status(InvoiceStatus.ACTIVE).build();
    }

    // ─── generateInvoice ──────────────────────────────────────────────────────

    @Test
    void generateInvoice_ShouldSaveTwiceAndReturnResponse_WhenNoExistingInvoice() {
        // Flow: 1st save → get id+issuedAt → build invoiceNumber → 2nd save → return
        Invoice pendingInvoice = buildPendingInvoice(1L);

        when(invoiceRepo.existsByOrder_Id(1L)).thenReturn(false);
        when(invoiceRepo.save(any(Invoice.class)))
                .thenReturn(pendingInvoice)   // 1st save: returns with id + issuedAt
                .thenReturn(mockInvoice);     // 2nd save: returns with invoice number

        InvoiceResponse response = invoiceService.generateInvoice(mockOrder);

        assertNotNull(response);
        assertEquals("INV-2026-00001", response.getInvoiceNumber());
        assertEquals(1L, response.getOrderId());
        assertEquals(1L, response.getCustomerId());
        assertEquals("Alice Smith", response.getCustomerName());
        assertEquals("Alice Corp", response.getCustomerCompany());
        assertEquals(30L, response.getVendorId());
        assertEquals("Sony India Pvt Ltd", response.getVendorCompanyName());
        assertEquals(2, response.getQuantity());
        assertEquals(29990.0, response.getUnitPrice());
        assertEquals(59980.0, response.getSubtotal());
        assertEquals(18.0, response.getTaxPercent());
        assertEquals(10796.4, response.getTaxAmount());
        assertEquals(70776.4, response.getTotalAmount());
        assertEquals(InvoiceStatus.ACTIVE, response.getStatus());
        // Exactly 2 saves — PENDING save + invoice-number save
        verify(invoiceRepo, times(2)).save(any(Invoice.class));
    }

    @Test
    void generateInvoice_ShouldCalculateTaxCorrectly() {
        // subtotal = 29990 * 2 = 59980.0
        // taxAmount = 59980 * 18 / 100 = 10796.4
        // total = 59980 + 10796.4 = 70776.4

        Invoice pendingInvoice = buildPendingInvoice(1L);

        when(invoiceRepo.existsByOrder_Id(1L)).thenReturn(false);
        when(invoiceRepo.save(any(Invoice.class)))
                .thenReturn(pendingInvoice)
                .thenReturn(mockInvoice);

        invoiceService.generateInvoice(mockOrder);

        // Capture all saves and verify the first one has correct tax fields
        ArgumentCaptor<Invoice> captor = ArgumentCaptor.forClass(Invoice.class);
        verify(invoiceRepo, times(2)).save(captor.capture());

        Invoice firstSavedInvoice = captor.getAllValues().get(0);
        assertEquals(59980.0,  firstSavedInvoice.getSubtotal(),    0.001);
        assertEquals(18.0,     firstSavedInvoice.getTaxPercent(),  0.001);
        assertEquals(10796.4,  firstSavedInvoice.getTaxAmount(),   0.001);
        assertEquals(70776.4,  firstSavedInvoice.getTotalAmount(), 0.001);
        assertEquals("PENDING", firstSavedInvoice.getInvoiceNumber());
    }

    @Test
    void generateInvoice_ShouldReturnExistingInvoice_WhenAlreadyExists() {
        // Idempotency check — no new save if invoice already exists
        when(invoiceRepo.existsByOrder_Id(1L)).thenReturn(true);
        when(invoiceRepo.findByOrder_Id(1L)).thenReturn(Optional.of(mockInvoice));

        InvoiceResponse response = invoiceService.generateInvoice(mockOrder);

        assertNotNull(response);
        assertEquals("INV-2026-00001", response.getInvoiceNumber());
        // No save should happen
        verify(invoiceRepo, never()).save(any());
    }

    @Test
    void generateInvoice_ShouldUseNA_WhenCustomerCompanyIsNull() {
        // customer with null companyName → invoice should have "N/A"
        User cUser = User.builder().id(11L).name("Bob").phone("9876543220").build();
        Customer noCompanyCustomer = Customer.builder()
                .id(2L).user(cUser).companyName(null).build();

        Orders orderNoCompany = Orders.builder()
                .id(2L).customer(noCompanyCustomer).product(mockProduct)
                .productWarehouse(mockPW).quantity(1)
                .productNameSnapshot("Sony WH-1000XM5")
                .unitPriceSnapshot(29990.0).totalAmount(29990.0)
                .shippingAddress("12 MG Road").placedAt(ISSUED_AT).build();

        Invoice pendingNoCompany = Invoice.builder()
                .id(2L).invoiceNumber("PENDING").order(orderNoCompany)
                .customerId(2L).customerName("Bob").customerCompany("N/A")
                .vendorId(30L).vendorCompanyName("Sony India Pvt Ltd")
                .productName("Sony WH-1000XM5").quantity(1)
                .unitPrice(29990.0).subtotal(29990.0)
                .taxPercent(18.0).taxAmount(5398.2).totalAmount(35388.2)
                .shippingAddress("12 MG Road").issuedAt(ISSUED_AT)
                .status(InvoiceStatus.ACTIVE).build();

        when(invoiceRepo.existsByOrder_Id(2L)).thenReturn(false);
        when(invoiceRepo.save(any(Invoice.class)))
                .thenReturn(pendingNoCompany)
                .thenReturn(pendingNoCompany);

        invoiceService.generateInvoice(orderNoCompany);

        // Capture the first save and verify customerCompany is "N/A"
        ArgumentCaptor<Invoice> captor = ArgumentCaptor.forClass(Invoice.class);
        verify(invoiceRepo, times(2)).save(captor.capture());
        assertEquals("N/A", captor.getAllValues().get(0).getCustomerCompany());
    }

    // ─── voidInvoice ──────────────────────────────────────────────────────────

    @Test
    void voidInvoice_ShouldSetStatusVoidAndVoidedAt_WhenInvoiceExists() {
        when(invoiceRepo.findByOrder_Id(1L)).thenReturn(Optional.of(mockInvoice));
        when(invoiceRepo.save(any())).thenReturn(mockInvoice);

        invoiceService.voidInvoice(1L);

        assertEquals(InvoiceStatus.VOID, mockInvoice.getStatus());
        assertNotNull(mockInvoice.getVoidedAt());
        verify(invoiceRepo).save(mockInvoice);
    }

    @Test
    void voidInvoice_ShouldDoNothing_WhenNoInvoiceFoundForOrder() {
        when(invoiceRepo.findByOrder_Id(999L)).thenReturn(Optional.empty());

        // Must NOT throw — ifPresentOrElse just logs warning
        assertDoesNotThrow(() -> invoiceService.voidInvoice(999L));
        verify(invoiceRepo, never()).save(any());
    }

    // ─── getInvoiceByOrderId ──────────────────────────────────────────────────

    @Test
    void getInvoiceByOrderId_ShouldReturnInvoice_WhenExists() {
        when(invoiceRepo.findByOrder_Id(1L)).thenReturn(Optional.of(mockInvoice));

        ResponseEntity<InvoiceResponse> response = invoiceService.getInvoiceByOrderId(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("INV-2026-00001", response.getBody().getInvoiceNumber());
        assertEquals(InvoiceStatus.ACTIVE, response.getBody().getStatus());
        assertNull(response.getBody().getVoidedAt());
    }

    @Test
    void getInvoiceByOrderId_ShouldMapVoidedAt_WhenInvoiceIsVoided() {
        LocalDateTime voidTime = LocalDateTime.of(2026, 6, 5, 12, 0);
        Invoice voidedInvoice = Invoice.builder()
                .id(2L).invoiceNumber("INV-2026-00002").order(mockOrder)
                .customerId(1L).customerName("Alice Smith").customerCompany("Alice Corp")
                .vendorId(30L).vendorCompanyName("Sony India Pvt Ltd")
                .productName("Sony WH-1000XM5").quantity(2).unitPrice(29990.0)
                .subtotal(59980.0).taxPercent(18.0).taxAmount(10796.4).totalAmount(70776.4)
                .shippingAddress("12 MG Road").issuedAt(ISSUED_AT)
                .status(InvoiceStatus.VOID).voidedAt(voidTime).build();

        when(invoiceRepo.findByOrder_Id(2L)).thenReturn(Optional.of(voidedInvoice));

        ResponseEntity<InvoiceResponse> response = invoiceService.getInvoiceByOrderId(2L);

        assertEquals(InvoiceStatus.VOID, response.getBody().getStatus());
        assertEquals(voidTime, response.getBody().getVoidedAt());
    }

    @Test
    void getInvoiceByOrderId_ShouldThrowException_WhenNotFound() {
        when(invoiceRepo.findByOrder_Id(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> invoiceService.getInvoiceByOrderId(999L));
    }

    // ─── getInvoicesByCustomerId ──────────────────────────────────────────────

    @Test
    void getInvoicesByCustomerId_ShouldReturnAllInvoices_ForCustomer() {
        Invoice invoice2 = Invoice.builder()
                .id(2L).invoiceNumber("INV-2026-00002").order(mockOrder)
                .customerId(1L).customerName("Alice Smith").customerCompany("Alice Corp")
                .vendorId(30L).vendorCompanyName("Sony India Pvt Ltd")
                .productName("LG Monitor").quantity(1).unitPrice(15000.0)
                .subtotal(15000.0).taxPercent(18.0).taxAmount(2700.0).totalAmount(17700.0)
                .shippingAddress("12 MG Road").issuedAt(ISSUED_AT)
                .status(InvoiceStatus.ACTIVE).build();

        when(invoiceRepo.findAllByCustomerIdOrderByIssuedAtDesc(1L))
                .thenReturn(Arrays.asList(mockInvoice, invoice2));

        ResponseEntity<List<InvoiceResponse>> response =
                invoiceService.getInvoicesByCustomerId(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(2, response.getBody().size());
    }

    @Test
    void getInvoicesByCustomerId_ShouldReturnEmptyList_WhenNone() {
        when(invoiceRepo.findAllByCustomerIdOrderByIssuedAtDesc(999L)).thenReturn(List.of());

        assertTrue(invoiceService.getInvoicesByCustomerId(999L).getBody().isEmpty());
    }

    // ─── getAllInvoices ───────────────────────────────────────────────────────

    @Test
    void getAllInvoices_ShouldReturnAll() {
        when(invoiceRepo.findAll()).thenReturn(List.of(mockInvoice));

        ResponseEntity<List<InvoiceResponse>> response = invoiceService.getAllInvoices();

        assertEquals(1, response.getBody().size());
        assertEquals("INV-2026-00001", response.getBody().get(0).getInvoiceNumber());
    }

    @Test
    void getAllInvoices_ShouldReturnEmptyList_WhenNone() {
        when(invoiceRepo.findAll()).thenReturn(List.of());

        assertTrue(invoiceService.getAllInvoices().getBody().isEmpty());
    }
}