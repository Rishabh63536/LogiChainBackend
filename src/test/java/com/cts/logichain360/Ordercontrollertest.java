package com.cts.logichain360;

import com.cts.logichain360.controller.OrderController;
import com.cts.logichain360.dto.request.AssignDriverRequest;
import com.cts.logichain360.dto.request.PlaceOrderRequest;
import com.cts.logichain360.dto.response.OrderResponse;
import com.cts.logichain360.service.OrderService;
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
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for OrderController using Mockito.
 * Covers: place, getById, getByCustomer, getActive, getPast,
 *         cancel, getByDriver, startDelivery, completeDelivery, assignDriver
 */
@ExtendWith(MockitoExtension.class)
class OrderControllerTest {

    @Mock
    private OrderService orderService;

    @InjectMocks
    private OrderController orderController;

    private OrderResponse confirmedOrder;
    private OrderResponse inTransitOrder;
    private OrderResponse deliveredOrder;
    private OrderResponse cancelledOrder;
    private PlaceOrderRequest placeOrderRequest;
    private AssignDriverRequest assignDriverRequest;

    @BeforeEach
    void setUp() {
        confirmedOrder = OrderResponse.builder()
                .id(1L).customerId(10L).customerName("Alice")
                .productId(5L).productNameSnapshot("Sony Headphones").unitPriceSnapshot(29990.0)
                .quantity(2).totalAmount(59980.0)
                .shippingAddress("12 MG Road, Bangalore")
                .placedAt(LocalDateTime.of(2026, 6, 1, 10, 0))
                .warehouseId(100L).warehouseCode("WH-CHN-01").productWarehouseId(50L)
                .build();

        inTransitOrder = OrderResponse.builder()
                .id(2L).customerId(10L).customerName("Alice")
                .productId(5L).productNameSnapshot("LG Monitor").unitPriceSnapshot(15000.0)
                .quantity(1).totalAmount(15000.0)
                .driverId(20L).driverName("Ravi Kumar")
                .shippingAddress("12 MG Road, Bangalore")
                .build();

        deliveredOrder = OrderResponse.builder()
                .id(3L).customerId(10L).customerName("Alice")
                .productId(6L).productNameSnapshot("Samsung TV").unitPriceSnapshot(45000.0)
                .quantity(1).totalAmount(45000.0)
                .build();

        cancelledOrder = OrderResponse.builder()
                .id(4L).customerId(10L).customerName("Alice")
                .productId(7L).productNameSnapshot("Keyboard").unitPriceSnapshot(2000.0)
                .quantity(3).totalAmount(6000.0)
                .build();

        placeOrderRequest = PlaceOrderRequest.builder()
                .customerId(10L).productId(5L).quantity(2)
                .shippingAddress("12 MG Road, Bangalore 560001")
                .build();

        assignDriverRequest = AssignDriverRequest.builder()
                .driverId(20L)
                .build();
    }

    // ─── place ────────────────────────────────────────────────────────────────

    @Test
    void place_ShouldReturnCreatedOrder_WhenValidRequest() {
        when(orderService.placeOrder(any(PlaceOrderRequest.class)))
                .thenReturn(ResponseEntity.status(HttpStatus.CREATED).body(confirmedOrder));

        ResponseEntity<OrderResponse> response = orderController.place(placeOrderRequest);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1L, response.getBody().getId());
        assertEquals(10L, response.getBody().getCustomerId());
        assertEquals(59980.0, response.getBody().getTotalAmount());
        verify(orderService, times(1)).placeOrder(any(PlaceOrderRequest.class));
    }

    @Test
    void place_ShouldReturnConflict_WhenInsufficientStock() {
        when(orderService.placeOrder(any(PlaceOrderRequest.class)))
                .thenReturn(ResponseEntity.status(HttpStatus.CONFLICT).build());

        ResponseEntity<OrderResponse> response = orderController.place(placeOrderRequest);

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
    }

    @Test
    void place_ShouldReturnNotFound_WhenCustomerOrProductNotFound() {
        when(orderService.placeOrder(any(PlaceOrderRequest.class)))
                .thenReturn(ResponseEntity.notFound().build());

        ResponseEntity<OrderResponse> response = orderController.place(placeOrderRequest);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void place_ShouldDelegateToServiceWithCorrectRequest() {
        when(orderService.placeOrder(placeOrderRequest))
                .thenReturn(ResponseEntity.status(HttpStatus.CREATED).body(confirmedOrder));

        orderController.place(placeOrderRequest);

        verify(orderService).placeOrder(placeOrderRequest);
    }

    // ─── getById ──────────────────────────────────────────────────────────────

    @Test
    void getById_ShouldReturnOrder_WhenOrderExists() {
        when(orderService.getOrderById(1L)).thenReturn(ResponseEntity.ok(confirmedOrder));

        ResponseEntity<OrderResponse> response = orderController.getById(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1L, response.getBody().getId());
        verify(orderService, times(1)).getOrderById(1L);
    }

    @Test
    void getById_ShouldReturnNotFound_WhenOrderDoesNotExist() {
        when(orderService.getOrderById(999L)).thenReturn(ResponseEntity.notFound().build());

        ResponseEntity<OrderResponse> response = orderController.getById(999L);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    // ─── getByCustomer ────────────────────────────────────────────────────────

    @Test
    void getByCustomer_ShouldReturnAllOrders_ForGivenCustomer() {
        List<OrderResponse> orders = Arrays.asList(confirmedOrder, inTransitOrder, deliveredOrder);
        when(orderService.getOrdersByCustomer(10L)).thenReturn(ResponseEntity.ok(orders));

        ResponseEntity<List<OrderResponse>> response = orderController.getByCustomer(10L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(3, response.getBody().size());
        verify(orderService, times(1)).getOrdersByCustomer(10L);
    }

    @Test
    void getByCustomer_ShouldReturnEmptyList_WhenCustomerHasNoOrders() {
        when(orderService.getOrdersByCustomer(10L))
                .thenReturn(ResponseEntity.ok(Collections.emptyList()));

        ResponseEntity<List<OrderResponse>> response = orderController.getByCustomer(10L);

        assertTrue(response.getBody().isEmpty());
    }

    // ─── getActive ────────────────────────────────────────────────────────────

    @Test
    void getActive_ShouldReturnActiveOrders_ForGivenCustomer() {
        List<OrderResponse> activeOrders = Arrays.asList(confirmedOrder, inTransitOrder);
        when(orderService.getActiveOrdersByCustomer(10L)).thenReturn(ResponseEntity.ok(activeOrders));

        ResponseEntity<List<OrderResponse>> response = orderController.getActive(10L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(2, response.getBody().size());
        verify(orderService, times(1)).getActiveOrdersByCustomer(10L);
    }

    @Test
    void getActive_ShouldNotCallGetPastOrders() {
        when(orderService.getActiveOrdersByCustomer(10L))
                .thenReturn(ResponseEntity.ok(Collections.emptyList()));

        orderController.getActive(10L);

        verify(orderService).getActiveOrdersByCustomer(10L);
        verify(orderService, never()).getPastOrdersByCustomer(anyLong());
    }

    // ─── getPast ──────────────────────────────────────────────────────────────

    @Test
    void getPast_ShouldReturnPastOrders_ForGivenCustomer() {
        List<OrderResponse> pastOrders = Arrays.asList(deliveredOrder, cancelledOrder);
        when(orderService.getPastOrdersByCustomer(10L)).thenReturn(ResponseEntity.ok(pastOrders));

        ResponseEntity<List<OrderResponse>> response = orderController.getPast(10L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(2, response.getBody().size());
        verify(orderService, times(1)).getPastOrdersByCustomer(10L);
    }

    @Test
    void getPast_ShouldNotCallGetActiveOrders() {
        when(orderService.getPastOrdersByCustomer(10L))
                .thenReturn(ResponseEntity.ok(Collections.emptyList()));

        orderController.getPast(10L);

        verify(orderService).getPastOrdersByCustomer(10L);
        verify(orderService, never()).getActiveOrdersByCustomer(anyLong());
    }

    // ─── cancel ───────────────────────────────────────────────────────────────

    @Test
    void cancel_ShouldReturnCancelledOrder_WhenOrderCanBeCancelled() {
        when(orderService.cancelOrder(1L)).thenReturn(ResponseEntity.ok(cancelledOrder));

        ResponseEntity<OrderResponse> response = orderController.cancel(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        verify(orderService, times(1)).cancelOrder(1L);
    }

    @Test
    void cancel_ShouldReturnBadRequest_WhenOrderAlreadyInTransit() {
        when(orderService.cancelOrder(2L))
                .thenReturn(ResponseEntity.badRequest().build());

        ResponseEntity<OrderResponse> response = orderController.cancel(2L);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void cancel_ShouldReturnNotFound_WhenOrderDoesNotExist() {
        when(orderService.cancelOrder(999L)).thenReturn(ResponseEntity.notFound().build());

        ResponseEntity<OrderResponse> response = orderController.cancel(999L);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    // ─── getByDriver ──────────────────────────────────────────────────────────

    @Test
    void getByDriver_ShouldReturnAssignedOrders_ForGivenDriver() {
        List<OrderResponse> driverOrders = Collections.singletonList(inTransitOrder);
        when(orderService.getOrdersByDriver(20L)).thenReturn(ResponseEntity.ok(driverOrders));

        ResponseEntity<List<OrderResponse>> response = orderController.getByDriver(20L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().size());
        assertEquals(20L, response.getBody().get(0).getDriverId());
        verify(orderService, times(1)).getOrdersByDriver(20L);
    }

    @Test
    void getByDriver_ShouldReturnEmptyList_WhenDriverHasNoOrders() {
        when(orderService.getOrdersByDriver(20L))
                .thenReturn(ResponseEntity.ok(Collections.emptyList()));

        ResponseEntity<List<OrderResponse>> response = orderController.getByDriver(20L);

        assertTrue(response.getBody().isEmpty());
    }

    // ─── startDelivery ────────────────────────────────────────────────────────

    @Test
    void startDelivery_ShouldReturnUpdatedOrder_WhenOrderIsAssigned() {
        when(orderService.startDelivery(2L)).thenReturn(ResponseEntity.ok(inTransitOrder));

        ResponseEntity<OrderResponse> response = orderController.startDelivery(2L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        verify(orderService, times(1)).startDelivery(2L);
    }

    @Test
    void startDelivery_ShouldReturnBadRequest_WhenOrderIsNotInAssignedState() {
        when(orderService.startDelivery(1L)).thenReturn(ResponseEntity.badRequest().build());

        ResponseEntity<OrderResponse> response = orderController.startDelivery(1L);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    // ─── completeDelivery ─────────────────────────────────────────────────────

    @Test
    void completeDelivery_ShouldReturnDeliveredOrder_WhenOrderIsInTransit() {
        when(orderService.completeDelivery(2L)).thenReturn(ResponseEntity.ok(deliveredOrder));

        ResponseEntity<OrderResponse> response = orderController.completeDelivery(2L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        verify(orderService, times(1)).completeDelivery(2L);
    }

    @Test
    void completeDelivery_ShouldReturnBadRequest_WhenOrderIsNotInTransit() {
        when(orderService.completeDelivery(1L)).thenReturn(ResponseEntity.badRequest().build());

        ResponseEntity<OrderResponse> response = orderController.completeDelivery(1L);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    // ─── assignDriver ─────────────────────────────────────────────────────────

    @Test
    void assignDriver_ShouldReturnOrderWithDriver_WhenDriverIsAvailable() {
        OrderResponse assignedOrder = OrderResponse.builder()
                .id(1L).customerId(10L).driverId(20L).driverName("Ravi Kumar").build();

        when(orderService.assignDriver(eq(1L), any(AssignDriverRequest.class)))
                .thenReturn(ResponseEntity.ok(assignedOrder));

        ResponseEntity<OrderResponse> response = orderController.assignDriver(1L, assignDriverRequest);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(20L, response.getBody().getDriverId());
        assertEquals("Ravi Kumar", response.getBody().getDriverName());
        verify(orderService, times(1)).assignDriver(eq(1L), any(AssignDriverRequest.class));
    }

    @Test
    void assignDriver_ShouldReturnConflict_WhenDriverIsNotAvailable() {
        when(orderService.assignDriver(eq(1L), any(AssignDriverRequest.class)))
                .thenReturn(ResponseEntity.status(HttpStatus.CONFLICT).build());

        ResponseEntity<OrderResponse> response = orderController.assignDriver(1L, assignDriverRequest);

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
    }

    @Test
    void assignDriver_ShouldReturnNotFound_WhenOrderDoesNotExist() {
        when(orderService.assignDriver(eq(999L), any(AssignDriverRequest.class)))
                .thenReturn(ResponseEntity.notFound().build());

        ResponseEntity<OrderResponse> response = orderController.assignDriver(999L, assignDriverRequest);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void assignDriver_ShouldPassCorrectIdAndRequestToService() {
        when(orderService.assignDriver(1L, assignDriverRequest))
                .thenReturn(ResponseEntity.ok(confirmedOrder));

        orderController.assignDriver(1L, assignDriverRequest);

        verify(orderService).assignDriver(1L, assignDriverRequest);
    }
}