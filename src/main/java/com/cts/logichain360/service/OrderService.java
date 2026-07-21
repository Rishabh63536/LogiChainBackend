package com.cts.logichain360.service;

import com.cts.logichain360.dto.request.AssignDriverRequest;
import com.cts.logichain360.dto.request.PlaceOrderRequest;
import com.cts.logichain360.dto.response.OrderResponse;
import org.springframework.http.ResponseEntity;

import java.util.List;

public interface OrderService {
    //Customer places an order, Atomically decrements stock and triggers ROL notification if breach
    ResponseEntity<OrderResponse> placeOrder(PlaceOrderRequest request);

    ResponseEntity<OrderResponse> getOrderById(Long orderId);

    ResponseEntity<List<OrderResponse>> getOrdersByCustomer(Long customerId);

    //Active = PENDING, CONFIRMED, ASSIGNED, IN_TRANSIT
    ResponseEntity<List<OrderResponse>> getActiveOrdersByCustomer(Long customerId);

    //Past = DELIVERED, CANCELLED (persisted, if want to add analytics in future)
    ResponseEntity<List<OrderResponse>> getPastOrdersByCustomer(Long customerId);

    ResponseEntity<List<OrderResponse>> getOrdersByDriver(Long driverId);

    //Warehouse manager assigns a driver, only allowed when order status = CONFIRMED
    ResponseEntity<OrderResponse> assignDriver(Long orderId, AssignDriverRequest request);

    //Driver starts delivery only allowed when status = ASSIGNED
    ResponseEntity<OrderResponse> startDelivery(Long orderId);

    //Driver marks delivery complete, uploads pod, frees the driver, only allowed when status = IN_TRANSIT
    ResponseEntity<OrderResponse> completeDelivery(Long orderId, String photoFilename);
    
    //Customer cancels , Restores stock,  frees driver if one was assigned ,refused after IN_TRANSIT
    ResponseEntity<OrderResponse> cancelOrder(Long orderId);
    
    ResponseEntity<List<OrderResponse>> getOrdersByWarehouse(Long warehouseId);
    ResponseEntity<List<OrderResponse>> getOrdersAwaitingAssignment(Long warehouseId);

    ResponseEntity<List<OrderResponse>> getAllOrders();
}