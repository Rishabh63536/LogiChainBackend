package com.cts.logichain360;

import com.cts.logichain360.controller.AdminController;
import com.cts.logichain360.dto.request.*;
import com.cts.logichain360.dto.response.*;
import com.cts.logichain360.enums.*;
import com.cts.logichain360.service.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class AdminControllerTest {

    @Mock private UserService         userService;
    @Mock private CustomerService     customerService;
    @Mock private VendorService       vendorService;
    @Mock private DriverService       driverService;
    @Mock private WarehouseManagerService wmService;
    @Mock private ProductService      productService;
    @Mock private OrderService        orderService;
    @Mock private InvoiceService      invoiceService;

    @InjectMocks private AdminController adminController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    // ── Test data ─────────────────────────────────────────────────────

    private UserResponse           userResponse;
    private CustomerResponse       customerResponse;
    private VendorResponse         vendorResponse;
    private DriverResponse         driverResponse;
    private WarehouseManagerResponse wmResponse;
    private ProductResponse        productResponse;
    private OrderResponse          orderResponse;
    private InvoiceResponse        invoiceResponse;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(adminController).build();
        objectMapper = new ObjectMapper();

        userResponse = UserResponse.builder()
                .id(1L).name("Test User").phone("9000000001")
                .role(UserRole.CUSTOMER).status(UserStatus.ACTIVE).build();

        customerResponse = CustomerResponse.builder()
                .id(10L).userId(1L).companyName("Test Co").build();

        vendorResponse = VendorResponse.builder()
                .id(20L).userId(2L).companyName("Vendor Co").build();

        driverResponse = DriverResponse.builder()
                .id(30L).userId(3L).licenseNumber("MH12345").available(true).build();

        wmResponse = WarehouseManagerResponse.builder()
                .id(40L).userId(4L).employeeCode("EMP001").build();

        productResponse = ProductResponse.builder()
                .productId(50L).productName("Headphones").productPrice(2999.0).vendorId(20L).build();

        orderResponse = OrderResponse.builder()
                .id(100L).customerId(10L).customerName("Test User")
                .productId(50L).productNameSnapshot("Headphones").unitPriceSnapshot(2999.0)
                .quantity(2).totalAmount(5998.0).status(OrderStatus.CONFIRMED)
                .placedAt(LocalDateTime.now()).build();

        invoiceResponse = InvoiceResponse.builder()
                .id(1L).invoiceNumber("INV-2026-00001").orderId(100L)
                .customerId(10L).customerName("Test User").customerCompany("Test Co")
                .vendorId(20L).vendorCompanyName("Vendor Co")
                .productName("Headphones").quantity(2).unitPrice(2999.0)
                .subtotal(5998.0).taxPercent(18.0).taxAmount(1079.64).totalAmount(7077.64)
                .shippingAddress("123 Main St").issuedAt(LocalDateTime.now())
                .status(InvoiceStatus.ACTIVE).build();
    }

    // ═══════════════════════════════════════════════════════════════════
    //  USER MANAGEMENT
    // ═══════════════════════════════════════════════════════════════════

//    @Test
//    void getAllUsers_returnsUserList() {
//        when(userService.getAllUsers()).thenReturn(ResponseEntity.ok(List.of(userResponse)));
//
//        ResponseEntity<List<UserResponse>> resp = adminController.getAllUsers();
//
//        assertEquals(HttpStatus.OK, resp.getStatusCode());
//        assertNotNull(resp.getBody());
//        assertEquals(1, resp.getBody().size());
//        assertEquals("Test User", resp.getBody().get(0).getName());
//        verify(userService).getAllUsers();
//    }

//    @Test
//    void getAllUsers_returnsEmptyList() {
//        when(userService.getAllUsers()).thenReturn(ResponseEntity.ok(List.of()));
//
//        ResponseEntity<List<UserResponse>> resp = adminController.getAllUsers();
//
//        assertEquals(HttpStatus.OK, resp.getStatusCode());
//        assertTrue(resp.getBody().isEmpty());
//    }

//    @Test
//    void getUserById_returnsUser() {
//        when(userService.getUserById(1L)).thenReturn(ResponseEntity.ok(userResponse));
//
//        ResponseEntity<UserResponse> resp = adminController.getUserById(1L);
//
//        assertEquals(HttpStatus.OK, resp.getStatusCode());
//        assertEquals(1L, resp.getBody().getId());
//    }

//    @Test
//    void getUserById_notFound() {
//        when(userService.getUserById(99L)).thenReturn(ResponseEntity.notFound().build());
//
//        ResponseEntity<UserResponse> resp = adminController.getUserById(99L);
//
//        assertEquals(HttpStatus.NOT_FOUND, resp.getStatusCode());
//    }

    @Test
    void updateUserStatus_success() {
        UserStatusRequest req = new UserStatusRequest(UserStatus.INACTIVE);
        UserResponse inactive = UserResponse.builder().id(1L).status(UserStatus.INACTIVE).build();
        when(userService.updateUserStatus(1L, req)).thenReturn(ResponseEntity.ok(inactive));

        ResponseEntity<UserResponse> resp = adminController.updateUserStatus(1L, req);

        assertEquals(HttpStatus.OK, resp.getStatusCode());
        assertEquals(UserStatus.INACTIVE, resp.getBody().getStatus());
    }

    @Test
    void deleteUser_success() {
        when(userService.deleteUser(1L)).thenReturn(ResponseEntity.noContent().build());

        ResponseEntity<Void> resp = adminController.deleteUser(1L);

        assertEquals(HttpStatus.NO_CONTENT, resp.getStatusCode());
        verify(userService).deleteUser(1L);
    }

    @Test
    void deleteUser_notFound() {
        when(userService.deleteUser(99L)).thenReturn(ResponseEntity.notFound().build());

        ResponseEntity<Void> resp = adminController.deleteUser(99L);

        assertEquals(HttpStatus.NOT_FOUND, resp.getStatusCode());
    }

    // ═══════════════════════════════════════════════════════════════════
    //  CUSTOMER MANAGEMENT
    // ═══════════════════════════════════════════════════════════════════

//    @Test
//    void getAllCustomers_returnsCustomerList() {
//        when(customerService.getAllCustomers()).thenReturn(ResponseEntity.ok(List.of(customerResponse)));
//
//        ResponseEntity<List<CustomerResponse>> resp = adminController.getAllCustomers();
//
//        assertEquals(HttpStatus.OK, resp.getStatusCode());
//        assertEquals(1, resp.getBody().size());
//        assertEquals("Test Co", resp.getBody().get(0).getCompanyName());
//    }

//    @Test
//    void getCustomerById_returnsCustomer() {
//        when(customerService.getCustomerById(10L)).thenReturn(ResponseEntity.ok(customerResponse));
//
//        ResponseEntity<CustomerResponse> resp = adminController.getCustomerById(10L);
//
//        assertEquals(HttpStatus.OK, resp.getStatusCode());
//        assertEquals(10L, resp.getBody().getId());
//    }

//    @Test
//    void updateCustomer_success() {
//        UpdateCustomerRequest req = new UpdateCustomerRequest();
//        when(customerService.updateCustomer(10L, req)).thenReturn(ResponseEntity.ok(customerResponse));
//
//        ResponseEntity<CustomerResponse> resp = adminController.updateCustomer(10L, req);
//
//        assertEquals(HttpStatus.OK, resp.getStatusCode());
//        verify(customerService).updateCustomer(10L, req);
//    }

    @Test
    void deleteCustomer_success() {
        when(customerService.deleteCustomer(10L)).thenReturn(ResponseEntity.noContent().build());

        ResponseEntity<Void> resp = adminController.deleteCustomer(10L);

        assertEquals(HttpStatus.NO_CONTENT, resp.getStatusCode());
    }

    // ═══════════════════════════════════════════════════════════════════
    //  VENDOR MANAGEMENT
    // ═══════════════════════════════════════════════════════════════════

//    @Test
//    void getAllVendors_returnsVendorList() {
//        when(vendorService.getAllVendors()).thenReturn(ResponseEntity.ok(List.of(vendorResponse)));
//
//        ResponseEntity<List<VendorResponse>> resp = adminController.getAllVendors();
//
//        assertEquals(HttpStatus.OK, resp.getStatusCode());
//        assertEquals(1, resp.getBody().size());
//    }

//    @Test
//    void getVendorById_success() {
//        when(vendorService.getVendorById(20L)).thenReturn(ResponseEntity.ok(vendorResponse));
//
//        ResponseEntity<VendorResponse> resp = adminController.getVendorById(20L);
//
//        assertEquals(HttpStatus.OK, resp.getStatusCode());
//        assertEquals("Vendor Co", resp.getBody().getCompanyName());
//    }

//    @Test
//    void updateVendor_success() {
//        UpdateVendorRequest req = new UpdateVendorRequest();
//        when(vendorService.updateVendor(20L, req)).thenReturn(ResponseEntity.ok(vendorResponse));
//
//        ResponseEntity<VendorResponse> resp = adminController.updateVendor(20L, req);
//
//        assertEquals(HttpStatus.OK, resp.getStatusCode());
//        verify(vendorService).updateVendor(20L, req);
//    }

    @Test
    void deleteVendor_success() {
        when(vendorService.deleteVendor(20L)).thenReturn(ResponseEntity.noContent().build());

        ResponseEntity<Void> resp = adminController.deleteVendor(20L);

        assertEquals(HttpStatus.NO_CONTENT, resp.getStatusCode());
    }

    // ═══════════════════════════════════════════════════════════════════
    //  DRIVER MANAGEMENT
    // ═══════════════════════════════════════════════════════════════════

//    @Test
//    void getAllDrivers_returnsDriverList() {
//        when(driverService.getAllDrivers()).thenReturn(ResponseEntity.ok(List.of(driverResponse)));
//
//        ResponseEntity<List<DriverResponse>> resp = adminController.getAllDrivers();
//
//        assertEquals(HttpStatus.OK, resp.getStatusCode());
//        assertEquals(1, resp.getBody().size());
//    }
//
//    @Test
//    void getDriverById_success() {
//        when(driverService.getDriverById(30L)).thenReturn(ResponseEntity.ok(driverResponse));
//
//        ResponseEntity<DriverResponse> resp = adminController.getDriverById(30L);
//
//        assertEquals(HttpStatus.OK, resp.getStatusCode());
//        assertEquals("MH12345", resp.getBody().getLicenseNumber());
//    }

//    @Test
//    void getAvailableDrivers_returnsList() {
//        when(driverService.getAvailableDrivers()).thenReturn(ResponseEntity.ok(List.of(driverResponse)));
//
//        ResponseEntity<List<DriverResponse>> resp = adminController.getAvailableDrivers();
//
//        assertEquals(HttpStatus.OK, resp.getStatusCode());
//        assertTrue(resp.getBody().get(0).getAvailable());
//    }

    @Test
    void updateDriverAvailability_success() {
        DriverAvailabilityRequest req = new DriverAvailabilityRequest(false);
        DriverResponse unavailable = DriverResponse.builder().id(30L).available(false).build();
        when(driverService.updateAvailability(30L, req)).thenReturn(ResponseEntity.ok(unavailable));

        ResponseEntity<DriverResponse> resp = adminController.updateDriverAvailability(30L, req);

        assertEquals(HttpStatus.OK, resp.getStatusCode());
        assertFalse(resp.getBody().getAvailable());
    }

    @Test
    void deleteDriver_success() {
        when(driverService.deleteDriver(30L)).thenReturn(ResponseEntity.noContent().build());

        ResponseEntity<Void> resp = adminController.deleteDriver(30L);

        assertEquals(HttpStatus.NO_CONTENT, resp.getStatusCode());
    }

    // ═══════════════════════════════════════════════════════════════════
    //  WAREHOUSE MANAGER MANAGEMENT
    // ═══════════════════════════════════════════════════════════════════

//    @Test
//    void getAllWarehouseManagers_returnsList() {
//        when(wmService.getAll()).thenReturn(ResponseEntity.ok(List.of(wmResponse)));
//
//        ResponseEntity<List<WarehouseManagerResponse>> resp = adminController.getAllWarehouseManagers();
//
//        assertEquals(HttpStatus.OK, resp.getStatusCode());
//        assertEquals(1, resp.getBody().size());
//        assertEquals("EMP001", resp.getBody().get(0).getEmployeeCode());
//    }

//    @Test
//    void getWarehouseManagerById_success() {
//        when(wmService.getById(40L)).thenReturn(ResponseEntity.ok(wmResponse));
//
//        ResponseEntity<WarehouseManagerResponse> resp = adminController.getWarehouseManagerById(40L);
//
//        assertEquals(HttpStatus.OK, resp.getStatusCode());
//        assertEquals(40L, resp.getBody().getId());
//    }

//    @Test
//    void updateWarehouseManager_success() {
//        UpdateWarehouseManagerRequest req = new UpdateWarehouseManagerRequest();
//        when(wmService.update(40L, req)).thenReturn(ResponseEntity.ok(wmResponse));
//
//        ResponseEntity<WarehouseManagerResponse> resp = adminController.updateWarehouseManager(40L, req);
//
//        assertEquals(HttpStatus.OK, resp.getStatusCode());
//    }

    @Test
    void assignWarehouse_success() {
        WarehouseAssignmentRequest req = new WarehouseAssignmentRequest(5L);
        WarehouseManagerResponse assigned = WarehouseManagerResponse.builder()
                .id(40L).warehouseId(5L).build();
        when(wmService.assignWarehouse(40L, req)).thenReturn(ResponseEntity.ok(assigned));

        ResponseEntity<WarehouseManagerResponse> resp = adminController.assignWarehouse(40L, req);

        assertEquals(HttpStatus.OK, resp.getStatusCode());
        assertEquals(5L, resp.getBody().getWarehouseId());
    }

    @Test
    void deleteWarehouseManager_success() {
        when(wmService.delete(40L)).thenReturn(ResponseEntity.noContent().build());

        ResponseEntity<Void> resp = adminController.deleteWarehouseManager(40L);

        assertEquals(HttpStatus.NO_CONTENT, resp.getStatusCode());
    }

    // ═══════════════════════════════════════════════════════════════════
    //  PRODUCT MANAGEMENT
    // ═══════════════════════════════════════════════════════════════════

//    @Test
//    void getAllProducts_returnsProductList() {
//        when(productService.getAllProducts()).thenReturn(ResponseEntity.ok(List.of(productResponse)));
//
//        ResponseEntity<List<ProductResponse>> resp = adminController.getAllProducts();
//
//        assertEquals(HttpStatus.OK, resp.getStatusCode());
//        assertEquals(1, resp.getBody().size());
//        assertEquals("Headphones", resp.getBody().get(0).getProductName());
//    }
//
//    @Test
//    void getProductById_success() {
//        when(productService.getProductById(50L)).thenReturn(ResponseEntity.ok(productResponse));
//
//        ResponseEntity<ProductResponse> resp = adminController.getProductById(50L);
//
//        assertEquals(HttpStatus.OK, resp.getStatusCode());
//        assertEquals(50L, resp.getBody().getProductId());
//    }

//    @Test
//    void updateProduct_success() {
//        UpdateProductRequest req = new UpdateProductRequest();
//        when(productService.updateProduct(50L, req)).thenReturn(ResponseEntity.ok(productResponse));
//
//        ResponseEntity<ProductResponse> resp = adminController.updateProduct(50L, req);
//
//        assertEquals(HttpStatus.OK, resp.getStatusCode());
//        verify(productService).updateProduct(50L, req);
//    }

    @Test
    void deleteProduct_success() {
        when(productService.deleteProduct(50L)).thenReturn(ResponseEntity.noContent().build());

        ResponseEntity<Void> resp = adminController.deleteProduct(50L);

        assertEquals(HttpStatus.NO_CONTENT, resp.getStatusCode());
    }

    // ═══════════════════════════════════════════════════════════════════
    //  ORDER OVERSIGHT
    // ═══════════════════════════════════════════════════════════════════

//    @Test
//    void getOrderById_success() {
//        when(orderService.getOrderById(100L)).thenReturn(ResponseEntity.ok(orderResponse));
//
//        ResponseEntity<OrderResponse> resp = adminController.getOrderById(100L);
//
//        assertEquals(HttpStatus.OK, resp.getStatusCode());
//        assertEquals(100L, resp.getBody().getId());
//    }

//    @Test
//    void getOrdersByCustomer_success() {
//        when(orderService.getOrdersByCustomer(10L)).thenReturn(ResponseEntity.ok(List.of(orderResponse)));
//
//        ResponseEntity<List<OrderResponse>> resp = adminController.getOrdersByCustomer(10L);
//
//        assertEquals(HttpStatus.OK, resp.getStatusCode());
//        assertEquals(1, resp.getBody().size());
//    }

    @Test
    void cancelOrder_success() {
        OrderResponse cancelled = OrderResponse.builder().id(100L).status(OrderStatus.CANCELLED).build();
        when(orderService.cancelOrder(100L)).thenReturn(ResponseEntity.ok(cancelled));

        ResponseEntity<OrderResponse> resp = adminController.cancelOrder(100L);

        assertEquals(HttpStatus.OK, resp.getStatusCode());
        assertEquals(OrderStatus.CANCELLED, resp.getBody().getStatus());
    }

    @Test
    void cancelOrder_notFound() {
        when(orderService.cancelOrder(999L)).thenReturn(ResponseEntity.notFound().build());

        ResponseEntity<OrderResponse> resp = adminController.cancelOrder(999L);

        assertEquals(HttpStatus.NOT_FOUND, resp.getStatusCode());
    }

    // ═══════════════════════════════════════════════════════════════════
    //  INVOICE MANAGEMENT
    // ═══════════════════════════════════════════════════════════════════

//    @Test
//    void getInvoiceByOrder_success() {
//        when(invoiceService.getInvoiceByOrderId(100L)).thenReturn(ResponseEntity.ok(invoiceResponse));
//
//        ResponseEntity<InvoiceResponse> resp = adminController.getInvoiceByOrder(100L);
//
//        assertEquals(HttpStatus.OK, resp.getStatusCode());
//        assertEquals("INV-2026-00001", resp.getBody().getInvoiceNumber());
//        assertEquals(InvoiceStatus.ACTIVE, resp.getBody().getStatus());
//    }

//    @Test
//    void getInvoicesByCustomer_returnsList() {
//        when(invoiceService.getInvoicesByCustomerId(10L)).thenReturn(ResponseEntity.ok(List.of(invoiceResponse)));
//
//        ResponseEntity<List<InvoiceResponse>> resp = adminController.getInvoicesByCustomer(10L);
//
//        assertEquals(HttpStatus.OK, resp.getStatusCode());
//        assertEquals(1, resp.getBody().size());
//        assertEquals(100L, resp.getBody().get(0).getOrderId());
//    }

//    @Test
//    void getAllInvoices_returnsList() {
//        when(invoiceService.getAllInvoices()).thenReturn(ResponseEntity.ok(List.of(invoiceResponse)));
//
//        ResponseEntity<List<InvoiceResponse>> resp = adminController.getAllInvoices();
//
//        assertEquals(HttpStatus.OK, resp.getStatusCode());
//        assertEquals(1, resp.getBody().size());
//    }

//    @Test
//    void getAllInvoices_empty() {
//        when(invoiceService.getAllInvoices()).thenReturn(ResponseEntity.ok(List.of()));
//
//        ResponseEntity<List<InvoiceResponse>> resp = adminController.getAllInvoices();
//
//        assertEquals(HttpStatus.OK, resp.getStatusCode());
//        assertTrue(resp.getBody().isEmpty());
//    }
}
