package com.cts.logichain360.serviceimpl;

import com.cts.logichain360.dto.request.AssignDriverRequest;
import com.cts.logichain360.dto.request.PlaceOrderRequest;
import com.cts.logichain360.dto.response.OrderResponse;
import com.cts.logichain360.entity.*;
import com.cts.logichain360.enums.OrderStatus;
import com.cts.logichain360.enums.UserRole;
import com.cts.logichain360.enums.UserStatus;
import com.cts.logichain360.exception.InsufficientStockException;
import com.cts.logichain360.exception.ResourceNotFoundException;
import com.cts.logichain360.repository.*;
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
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceImplTest {

    @Mock private OrderRepository orderRepo;
    @Mock private CustomerRepository customerRepo;
    @Mock private DriverRepository driverRepo;
    @Mock private ProductWarehouseRepository pwRepo;
    @Mock private NotificationService notificationService;

    @InjectMocks private OrderServiceImpl orderService;

    private User mockUser;
    private Customer mockCustomer;
    private Vendor mockVendor;
    private Product mockProduct;
    private Warehouse mockWarehouse;
    private ProductWarehouse mockPW;
    private Driver mockDriver;
    private Orders mockOrder;

    @BeforeEach
    void setUp() {
        mockUser = User.builder().id(10L).name("Alice").phone("9876543210")
                .role(UserRole.CUSTOMER).status(UserStatus.ACTIVE).build();

        mockCustomer = Customer.builder().id(1L).user(mockUser)
                .companyName("Alice Corp").build();

        mockVendor = Vendor.builder().id(30L)
                .companyName("Sony India Pvt Ltd")
                .user(User.builder().id(30L).name("Sony").phone("9999999999").build())
                .build();

        mockProduct = Product.builder().productId(5L)
                .productName("Sony Headphones").productPrice(29990.0)
                .vendor(mockVendor).build();

        mockWarehouse = Warehouse.builder().id(100L)
                .warehouseCode("WH-CHN-01").location("Chennai").build();

        // Normal stock - above ROL (stock=300, max=500, rol=40% -> 60% current, not below)
        mockPW = ProductWarehouse.builder().id(10L)
                .product(mockProduct).warehouse(mockWarehouse)
                .stock(300).maxStock(500).rolPercent(40.0).build();

        User driverUser = User.builder().id(20L).name("Ravi Kumar").phone("9876543211").build();
        mockDriver = Driver.builder().id(1L).user(driverUser).available(true).build();

        mockOrder = Orders.builder().id(1L)
                .customer(mockCustomer).product(mockProduct)
                .productWarehouse(mockPW).driver(null)
                .quantity(2).productNameSnapshot("Sony Headphones")
                .unitPriceSnapshot(29990.0).totalAmount(59980.0)
                .status(OrderStatus.CONFIRMED)
                .placedAt(LocalDateTime.of(2026, 6, 1, 10, 0))
                .shippingAddress("12 MG Road, Bangalore").build();
    }

    // ─── placeOrder ───────────────────────────────────────────────────────────

    @Test
    void placeOrder_ShouldReturnCreated_WhenValidRequest() {
        PlaceOrderRequest req = PlaceOrderRequest.builder()
                .customerId(1L).productId(5L).quantity(2)
                .shippingAddress("12 MG Road, Bangalore").build();

        when(customerRepo.findById(1L)).thenReturn(Optional.of(mockCustomer));
        when(pwRepo.findByProduct_ProductId(5L)).thenReturn(Optional.of(mockPW));
        when(orderRepo.save(any(Orders.class))).thenReturn(mockOrder);
        when(pwRepo.save(any(ProductWarehouse.class))).thenReturn(mockPW);

        ResponseEntity<OrderResponse> response = orderService.placeOrder(req);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1L, response.getBody().getId());
        assertEquals(OrderStatus.CONFIRMED, response.getBody().getStatus());
        assertEquals(59980.0, response.getBody().getTotalAmount());
        // stock must be decremented
        assertEquals(298, mockPW.getStock());
        verify(orderRepo).save(any(Orders.class));
        verify(pwRepo).save(mockPW);
    }

    @Test
    void placeOrder_ShouldThrowInsufficientStockException_WhenStockIsLow() {
        PlaceOrderRequest req = PlaceOrderRequest.builder()
                .customerId(1L).productId(5L).quantity(999)
                .shippingAddress("12 MG Road").build();

        when(customerRepo.findById(1L)).thenReturn(Optional.of(mockCustomer));
        when(pwRepo.findByProduct_ProductId(5L)).thenReturn(Optional.of(mockPW));

        assertThrows(InsufficientStockException.class, () -> orderService.placeOrder(req));
        verify(orderRepo, never()).save(any());
    }

    @Test
    void placeOrder_ShouldThrowResourceNotFoundException_WhenCustomerNotFound() {
        PlaceOrderRequest req = PlaceOrderRequest.builder()
                .customerId(999L).productId(5L).quantity(2)
                .shippingAddress("12 MG Road").build();

        when(customerRepo.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> orderService.placeOrder(req));
    }

    @Test
    void placeOrder_ShouldThrowResourceNotFoundException_WhenProductNotLaunched() {
        PlaceOrderRequest req = PlaceOrderRequest.builder()
                .customerId(1L).productId(999L).quantity(2)
                .shippingAddress("12 MG Road").build();

        when(customerRepo.findById(1L)).thenReturn(Optional.of(mockCustomer));
        when(pwRepo.findByProduct_ProductId(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> orderService.placeOrder(req));
    }

    @Test
    void placeOrder_ShouldSendRolNotification_WhenStockFallsBelowRol() {
        // Use low stock: stock=90, max=500, rol=40% -> 18% < 40% = below ROL
        ProductWarehouse lowStockPW = ProductWarehouse.builder().id(10L)
                .product(mockProduct).warehouse(mockWarehouse)
                .stock(90).maxStock(500).rolPercent(40.0).build();

        PlaceOrderRequest req = PlaceOrderRequest.builder()
                .customerId(1L).productId(5L).quantity(2)
                .shippingAddress("12 MG Road").build();

        when(customerRepo.findById(1L)).thenReturn(Optional.of(mockCustomer));
        when(pwRepo.findByProduct_ProductId(5L)).thenReturn(Optional.of(lowStockPW));
        when(orderRepo.save(any())).thenReturn(mockOrder);
        when(pwRepo.save(any())).thenReturn(lowStockPW);

        orderService.placeOrder(req);

        verify(notificationService).notifyRolBreach(lowStockPW);
    }

    @Test
    void placeOrder_ShouldNotSendRolNotification_WhenStockAboveRol() {
        PlaceOrderRequest req = PlaceOrderRequest.builder()
                .customerId(1L).productId(5L).quantity(2)
                .shippingAddress("12 MG Road").build();

        when(customerRepo.findById(1L)).thenReturn(Optional.of(mockCustomer));
        when(pwRepo.findByProduct_ProductId(5L)).thenReturn(Optional.of(mockPW));
        when(orderRepo.save(any())).thenReturn(mockOrder);
        when(pwRepo.save(any())).thenReturn(mockPW);

        orderService.placeOrder(req);

        verify(notificationService, never()).notifyRolBreach(any());
    }

    @Test
    void placeOrder_ShouldNotFailOrder_WhenRolNotificationThrowsException() {
        ProductWarehouse lowStockPW = ProductWarehouse.builder().id(10L)
                .product(mockProduct).warehouse(mockWarehouse)
                .stock(90).maxStock(500).rolPercent(40.0).build();

        PlaceOrderRequest req = PlaceOrderRequest.builder()
                .customerId(1L).productId(5L).quantity(2)
                .shippingAddress("12 MG Road").build();

        when(customerRepo.findById(1L)).thenReturn(Optional.of(mockCustomer));
        when(pwRepo.findByProduct_ProductId(5L)).thenReturn(Optional.of(lowStockPW));
        when(orderRepo.save(any())).thenReturn(mockOrder);
        when(pwRepo.save(any())).thenReturn(lowStockPW);
        doThrow(new RuntimeException("Notification failed"))
                .when(notificationService).notifyRolBreach(any());

        // Order should still succeed even if notification fails
        ResponseEntity<OrderResponse> response = orderService.placeOrder(req);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
    }

    // ─── getOrderById ─────────────────────────────────────────────────────────

    @Test
    void getOrderById_ShouldReturnOrder_WhenExists() {
        when(orderRepo.findById(1L)).thenReturn(Optional.of(mockOrder));

        ResponseEntity<OrderResponse> response = orderService.getOrderById(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1L, response.getBody().getId());
        assertEquals("Alice", response.getBody().getCustomerName());
    }

    @Test
    void getOrderById_ShouldThrowResourceNotFoundException_WhenNotExists() {
        when(orderRepo.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> orderService.getOrderById(999L));
    }

    // ─── getOrdersByCustomer ──────────────────────────────────────────────────

    @Test
    void getOrdersByCustomer_ShouldReturnOrders_WhenCustomerExists() {
        when(customerRepo.existsById(1L)).thenReturn(true);
        when(orderRepo.findAllByCustomer_IdOrderByPlacedAtDesc(1L))
                .thenReturn(List.of(mockOrder));

        ResponseEntity<List<OrderResponse>> response = orderService.getOrdersByCustomer(1L);

        assertEquals(1, response.getBody().size());
    }

    @Test
    void getOrdersByCustomer_ShouldThrowException_WhenCustomerNotFound() {
        when(customerRepo.existsById(999L)).thenReturn(false);

        assertThrows(ResourceNotFoundException.class,
                () -> orderService.getOrdersByCustomer(999L));
    }

    // ─── getActiveOrdersByCustomer ────────────────────────────────────────────

    @Test
    void getActiveOrdersByCustomer_ShouldReturnActiveOrders_WhenCustomerExists() {
        when(customerRepo.existsById(1L)).thenReturn(true);
        when(orderRepo.findAllByCustomer_IdAndStatusInOrderByPlacedAtDesc(eq(1L), any()))
                .thenReturn(List.of(mockOrder));

        ResponseEntity<List<OrderResponse>> response = orderService.getActiveOrdersByCustomer(1L);

        assertEquals(1, response.getBody().size());
    }

    @Test
    void getActiveOrdersByCustomer_ShouldThrowException_WhenCustomerNotFound() {
        when(customerRepo.existsById(999L)).thenReturn(false);

        assertThrows(ResourceNotFoundException.class,
                () -> orderService.getActiveOrdersByCustomer(999L));
    }

    // ─── getPastOrdersByCustomer ──────────────────────────────────────────────

    @Test
    void getPastOrdersByCustomer_ShouldReturnPastOrders_WhenCustomerExists() {
        Orders deliveredOrder = Orders.builder().id(2L).customer(mockCustomer)
                .product(mockProduct).productWarehouse(mockPW)
                .status(OrderStatus.DELIVERED).quantity(1)
                .productNameSnapshot("Sony").unitPriceSnapshot(29990.0).totalAmount(29990.0)
                .placedAt(LocalDateTime.now()).shippingAddress("12 MG Road").build();

        when(customerRepo.existsById(1L)).thenReturn(true);
        when(orderRepo.findAllByCustomer_IdAndStatusInOrderByPlacedAtDesc(eq(1L), any()))
                .thenReturn(List.of(deliveredOrder));

        ResponseEntity<List<OrderResponse>> response = orderService.getPastOrdersByCustomer(1L);

        assertEquals(1, response.getBody().size());
    }

    @Test
    void getPastOrdersByCustomer_ShouldThrowException_WhenCustomerNotFound() {
        when(customerRepo.existsById(999L)).thenReturn(false);

        assertThrows(ResourceNotFoundException.class,
                () -> orderService.getPastOrdersByCustomer(999L));
    }

    // ─── getOrdersByDriver ────────────────────────────────────────────────────

    @Test
    void getOrdersByDriver_ShouldReturnOrders_WhenDriverExists() {
        mockOrder.setDriver(mockDriver);
        when(driverRepo.existsById(1L)).thenReturn(true);
        when(orderRepo.findAllByDriver_IdOrderByPlacedAtDesc(1L)).thenReturn(List.of(mockOrder));

        ResponseEntity<List<OrderResponse>> response = orderService.getOrdersByDriver(1L);

        assertEquals(1, response.getBody().size());
    }

    @Test
    void getOrdersByDriver_ShouldThrowException_WhenDriverNotFound() {
        when(driverRepo.existsById(999L)).thenReturn(false);

        assertThrows(ResourceNotFoundException.class,
                () -> orderService.getOrdersByDriver(999L));
    }

    // ─── assignDriver ─────────────────────────────────────────────────────────

    @Test
    void assignDriver_ShouldAssignDriver_WhenOrderIsConfirmedAndDriverAvailable() {
        AssignDriverRequest req = AssignDriverRequest.builder().driverId(1L).build();
        Orders savedOrder = Orders.builder().id(1L).customer(mockCustomer)
                .product(mockProduct).productWarehouse(mockPW).driver(mockDriver)
                .status(OrderStatus.ASSIGNED).quantity(2)
                .productNameSnapshot("Sony").unitPriceSnapshot(29990.0).totalAmount(59980.0)
                .placedAt(LocalDateTime.now()).shippingAddress("12 MG Road").build();

        when(orderRepo.findById(1L)).thenReturn(Optional.of(mockOrder));
        when(driverRepo.findById(1L)).thenReturn(Optional.of(mockDriver));
        when(driverRepo.save(any())).thenReturn(mockDriver);
        when(orderRepo.save(any())).thenReturn(savedOrder);

        ResponseEntity<OrderResponse> response = orderService.assignDriver(1L, req);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(OrderStatus.ASSIGNED, mockOrder.getStatus());
        assertFalse(mockDriver.getAvailable()); // driver marked unavailable
        verify(driverRepo).save(mockDriver);
        verify(orderRepo).save(mockOrder);
    }

    @Test
    void assignDriver_ShouldThrowException_WhenOrderIsNotConfirmed() {
        mockOrder.setStatus(OrderStatus.ASSIGNED);
        AssignDriverRequest req = AssignDriverRequest.builder().driverId(1L).build();
        when(orderRepo.findById(1L)).thenReturn(Optional.of(mockOrder));

        assertThrows(IllegalArgumentException.class,
                () -> orderService.assignDriver(1L, req));
    }

    @Test
    void assignDriver_ShouldThrowException_WhenDriverNotAvailable() {
        mockDriver.setAvailable(false);
        AssignDriverRequest req = AssignDriverRequest.builder().driverId(1L).build();

        when(orderRepo.findById(1L)).thenReturn(Optional.of(mockOrder));
        when(driverRepo.findById(1L)).thenReturn(Optional.of(mockDriver));

        assertThrows(IllegalArgumentException.class,
                () -> orderService.assignDriver(1L, req));
    }

    @Test
    void assignDriver_ShouldThrowException_WhenDriverAvailableIsNull() {
        mockDriver.setAvailable(null);
        AssignDriverRequest req = AssignDriverRequest.builder().driverId(1L).build();

        when(orderRepo.findById(1L)).thenReturn(Optional.of(mockOrder));
        when(driverRepo.findById(1L)).thenReturn(Optional.of(mockDriver));

        assertThrows(IllegalArgumentException.class,
                () -> orderService.assignDriver(1L, req));
    }

    @Test
    void assignDriver_ShouldThrowResourceNotFoundException_WhenDriverNotFound() {
        AssignDriverRequest req = AssignDriverRequest.builder().driverId(999L).build();
        when(orderRepo.findById(1L)).thenReturn(Optional.of(mockOrder));
        when(driverRepo.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> orderService.assignDriver(1L, req));
    }

    // ─── startDelivery ────────────────────────────────────────────────────────

    @Test
    void startDelivery_ShouldSetInTransit_WhenOrderIsAssigned() {
        mockOrder.setStatus(OrderStatus.ASSIGNED);
        Orders saved = Orders.builder().id(1L).customer(mockCustomer).product(mockProduct)
                .productWarehouse(mockPW).status(OrderStatus.IN_TRANSIT).quantity(2)
                .productNameSnapshot("Sony").unitPriceSnapshot(29990.0).totalAmount(59980.0)
                .placedAt(LocalDateTime.now()).shippingAddress("12 MG Road").build();

        when(orderRepo.findById(1L)).thenReturn(Optional.of(mockOrder));
        when(orderRepo.save(any())).thenReturn(saved);

        ResponseEntity<OrderResponse> response = orderService.startDelivery(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(OrderStatus.IN_TRANSIT, mockOrder.getStatus());
    }

    @Test
    void startDelivery_ShouldThrowException_WhenOrderIsNotAssigned() {
        mockOrder.setStatus(OrderStatus.CONFIRMED);
        when(orderRepo.findById(1L)).thenReturn(Optional.of(mockOrder));

        assertThrows(IllegalArgumentException.class,
                () -> orderService.startDelivery(1L));
    }

    // ─── completeDelivery ─────────────────────────────────────────────────────

    @Test
    void completeDelivery_ShouldSetDeliveredAndFreeDriver_WhenInTransit() {
        mockOrder.setStatus(OrderStatus.IN_TRANSIT);
        mockOrder.setDriver(mockDriver);
        mockDriver.setAvailable(false);

        Orders saved = Orders.builder().id(1L).customer(mockCustomer).product(mockProduct)
                .productWarehouse(mockPW).driver(mockDriver).status(OrderStatus.DELIVERED)
                .quantity(2).productNameSnapshot("Sony").unitPriceSnapshot(29990.0)
                .totalAmount(59980.0).placedAt(LocalDateTime.now())
                .shippingAddress("12 MG Road").build();

        when(orderRepo.findById(1L)).thenReturn(Optional.of(mockOrder));
        when(driverRepo.save(any())).thenReturn(mockDriver);
        when(orderRepo.save(any())).thenReturn(saved);

        ResponseEntity<OrderResponse> response = orderService.completeDelivery(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(OrderStatus.DELIVERED, mockOrder.getStatus());
        assertTrue(mockDriver.getAvailable()); // driver freed
        verify(driverRepo).save(mockDriver);
    }

    @Test
    void completeDelivery_ShouldSetDelivered_WhenNoDriverAssigned() {
        mockOrder.setStatus(OrderStatus.IN_TRANSIT);
        mockOrder.setDriver(null);

        Orders saved = Orders.builder().id(1L).customer(mockCustomer).product(mockProduct)
                .productWarehouse(mockPW).status(OrderStatus.DELIVERED)
                .quantity(2).productNameSnapshot("Sony").unitPriceSnapshot(29990.0)
                .totalAmount(59980.0).placedAt(LocalDateTime.now())
                .shippingAddress("12 MG Road").build();

        when(orderRepo.findById(1L)).thenReturn(Optional.of(mockOrder));
        when(orderRepo.save(any())).thenReturn(saved);

        orderService.completeDelivery(1L);

        verify(driverRepo, never()).save(any());
    }

    @Test
    void completeDelivery_ShouldThrowException_WhenOrderIsNotInTransit() {
        mockOrder.setStatus(OrderStatus.ASSIGNED);
        when(orderRepo.findById(1L)).thenReturn(Optional.of(mockOrder));

        assertThrows(IllegalArgumentException.class,
                () -> orderService.completeDelivery(1L));
    }

    // ─── cancelOrder ─────────────────────────────────────────────────────────

    @Test
    void cancelOrder_ShouldCancelOrder_AndRestoreStock_WhenConfirmed() {
        mockOrder.setStatus(OrderStatus.CONFIRMED);
        int stockBefore = mockPW.getStock();

        Orders saved = Orders.builder().id(1L).customer(mockCustomer).product(mockProduct)
                .productWarehouse(mockPW).status(OrderStatus.CANCELLED)
                .quantity(2).productNameSnapshot("Sony").unitPriceSnapshot(29990.0)
                .totalAmount(59980.0).placedAt(LocalDateTime.now())
                .shippingAddress("12 MG Road").build();

        when(orderRepo.findById(1L)).thenReturn(Optional.of(mockOrder));
        when(pwRepo.save(any())).thenReturn(mockPW);
        when(orderRepo.save(any())).thenReturn(saved);

        ResponseEntity<OrderResponse> response = orderService.cancelOrder(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(OrderStatus.CANCELLED, mockOrder.getStatus());
        assertEquals(stockBefore + 2, mockPW.getStock()); // stock restored
        verify(pwRepo).save(mockPW);
    }

    @Test
    void cancelOrder_ShouldFreeDriver_WhenDriverWasAssigned() {
        mockOrder.setStatus(OrderStatus.ASSIGNED);
        mockOrder.setDriver(mockDriver);
        mockDriver.setAvailable(false);

        when(orderRepo.findById(1L)).thenReturn(Optional.of(mockOrder));
        when(pwRepo.save(any())).thenReturn(mockPW);
        when(driverRepo.save(any())).thenReturn(mockDriver);
        when(orderRepo.save(any())).thenReturn(mockOrder);

        orderService.cancelOrder(1L);

        assertTrue(mockDriver.getAvailable());
        verify(driverRepo).save(mockDriver);
    }

    @Test
    void cancelOrder_ShouldClampRestoredStock_WhenRestorationExceedsMaxStock() {
        mockPW.setStock(499);  // stock is nearly full
        mockOrder.setQuantity(10); // restoring 10 would exceed maxStock=500
        mockOrder.setStatus(OrderStatus.CONFIRMED);

        when(orderRepo.findById(1L)).thenReturn(Optional.of(mockOrder));
        when(pwRepo.save(any())).thenReturn(mockPW);
        when(orderRepo.save(any())).thenReturn(mockOrder);

        orderService.cancelOrder(1L);

        assertEquals(500, mockPW.getStock()); // clamped to maxStock
    }

    @Test
    void cancelOrder_ShouldThrowException_WhenOrderIsInTransit() {
        mockOrder.setStatus(OrderStatus.IN_TRANSIT);
        when(orderRepo.findById(1L)).thenReturn(Optional.of(mockOrder));

        assertThrows(IllegalArgumentException.class,
                () -> orderService.cancelOrder(1L));
    }

    @Test
    void cancelOrder_ShouldThrowException_WhenOrderIsAlreadyDelivered() {
        mockOrder.setStatus(OrderStatus.DELIVERED);
        when(orderRepo.findById(1L)).thenReturn(Optional.of(mockOrder));

        assertThrows(IllegalArgumentException.class,
                () -> orderService.cancelOrder(1L));
    }
}