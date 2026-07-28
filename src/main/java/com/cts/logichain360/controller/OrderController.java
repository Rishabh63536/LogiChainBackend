package com.cts.logichain360.controller;

import com.cts.logichain360.dto.request.AssignDriverRequest;
import com.cts.logichain360.dto.request.PlaceOrderRequest;
import com.cts.logichain360.dto.response.OrderResponse;
import com.cts.logichain360.dto.response.PODResponse;
import com.cts.logichain360.service.FileStorageService;
import com.cts.logichain360.service.OrderService;
import com.cts.logichain360.service.PODService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.http.MediaType;

import java.util.List;

@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Order", description = "Customer orders: place, view, assign driver, track delivery, cancel.")
public class OrderController {
    private final OrderService orderService;
    private final FileStorageService fileStorageService;
    private final PODService podService;                 

    @Operation(summary = "Place a new order",
               description = "Customer orders one product with a quantity. Stock is decremented atomically"+
                             "If stock falls below the reorder threshold, the warehouse manager is notified.")
    
    @PreAuthorize("hasAnyRole('CUSTOMER','ADMIN')")
    @PostMapping
    public ResponseEntity<OrderResponse> place(@Valid @RequestBody PlaceOrderRequest request) {
        log.info("POST /orders — customer={}, product={}, qty={}",
                request.getCustomerId(), request.getProductId(), request.getQuantity());
        return orderService.placeOrder(request);
    }

    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get an order by id")
    @GetMapping("/{id}")
    public ResponseEntity<OrderResponse> getById(@PathVariable Long id) {
        return orderService.getOrderById(id);
    }

    @PreAuthorize("hasAnyRole('ADMIN','CUSTOMER')")
    @Operation(summary = "All orders for a customer (newest first)")
    @GetMapping("/customer/{customerId}")
    public ResponseEntity<List<OrderResponse>> getByCustomer(@PathVariable Long customerId) {
        return orderService.getOrdersByCustomer(customerId);
    }

    @PreAuthorize("hasAnyRole('ADMIN','CUSTOMER')")
    @Operation(summary = "Active orders for a customer",
               description = "PENDING, CONFIRMED, ASSIGNED, or IN_TRANSIT.")
    @GetMapping("/customer/{customerId}/active")
    public ResponseEntity<List<OrderResponse>> getActive(@PathVariable Long customerId) {
        return orderService.getActiveOrdersByCustomer(customerId);
    }

    @PreAuthorize("hasAnyRole('ADMIN','CUSTOMER')")
    @Operation(summary = "Past orders for a customer",
               description = "DELIVERED or CANCELLED.")
    @GetMapping("/customer/{customerId}/past")
    public ResponseEntity<List<OrderResponse>> getPast(@PathVariable Long customerId) {
        return orderService.getPastOrdersByCustomer(customerId);
    }

    @PreAuthorize("hasAnyRole('ADMIN','CUSTOMER')")
    @Operation(summary = "Customer cancels an order",
               description = "Refunds stock and frees an assigned driver. Refused after IN_TRANSIT.")
    @PatchMapping("/{id}/cancel")
    public ResponseEntity<OrderResponse> cancel(@PathVariable Long id) {
        log.info("PATCH /orders/{}/cancel", id);
        return orderService.cancelOrder(id);
    }

    //driver-facing 
    @PreAuthorize("hasAnyRole('ADMIN','DRIVER','WAREHOUSE_MANAGER')")
    @Operation(summary = "Orders assigned to a driver")
    @GetMapping("/driver/{driverId}")
    public ResponseEntity<List<OrderResponse>> getByDriver(@PathVariable Long driverId) {
        return orderService.getOrdersByDriver(driverId);
    }

    @PreAuthorize("hasAnyRole('ADMIN','DRIVER')")
    @Operation(summary = "Driver starts delivery", description = "Order must be ASSIGNED.")
    @PatchMapping("/{id}/start-delivery")
    public ResponseEntity<OrderResponse> startDelivery(@PathVariable Long id) {
        log.info("PATCH /orders/{}/start-delivery", id);
        return orderService.startDelivery(id);
    }

    @PreAuthorize("hasAnyRole('ADMIN','DRIVER')")
    @Operation(summary = "Driver completes delivery with Proof of Delivery photo",
               description = "Driver uploads a JPEG/PNG photo (max 5MB) as proof of successful delivery. "+ "Driver is freed back to available.")
    @PatchMapping(value = "/{id}/complete-delivery", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<OrderResponse> completeDelivery(@PathVariable Long id, @RequestParam("photo") MultipartFile photo) {
    	log.info("PATCH /orders/{}/complete-delivery — photo={} ({} bytes)",id, photo.getOriginalFilename(), photo.getSize());
    	String storedFilename = fileStorageService.store(photo);
    	return orderService.completeDelivery(id, storedFilename);
    }

    //WHM facing 
    @PreAuthorize("hasAnyRole('ADMIN','WAREHOUSE_MANAGER')")
    @Operation(summary = "Warehouse manager assigns a driver",
               description = "Order must be CONFIRMED, and the driver must currently be available.")
    @PatchMapping("/{id}/assign-driver")
    public ResponseEntity<OrderResponse> assignDriver(@PathVariable Long id, @Valid @RequestBody AssignDriverRequest request) {
        log.info("PATCH /orders/{}/assign-driver — driver={}", id, request.getDriverId());
        return orderService.assignDriver(id, request);
    }
    
    @PreAuthorize("hasAnyRole('ADMIN','WAREHOUSE_MANAGER')")
    @Operation(summary = "All orders whose stock came from this warehouse")
    @GetMapping("/warehouse/{warehouseId}")
    public ResponseEntity<List<OrderResponse>> getByWarehouse(@PathVariable Long warehouseId) {
        return orderService.getOrdersByWarehouse(warehouseId);
    }
   
    @PreAuthorize("hasAnyRole('ADMIN','WAREHOUSE_MANAGER')")
    @Operation(summary = "Orders awaiting driver assignment at this warehouse",
               description = "CONFIRMED orders only — the WarehouseManager's assignment queue.")
    @GetMapping("/warehouse/{warehouseId}/awaiting-assignment")
    public ResponseEntity<List<OrderResponse>> getAwaitingAssignment(@PathVariable Long warehouseId) {
        return orderService.getOrdersAwaitingAssignment(warehouseId);
    }

    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get the Proof of Delivery photo/details for an order",
            description = "Returns 404 if the order hasn't been delivered yet (no POD exists until completeDelivery() runs).")
    @GetMapping("/{id}/pod")
    public ResponseEntity<PODResponse> getPod(@PathVariable Long id) {
        return podService.getByOrderId(id);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Retrive all orders across different warehouses",
            description = "Orders from all warehouses can be fetched")
    @GetMapping("/all")
    public ResponseEntity<List<OrderResponse>> all() {
        return orderService.getAllOrders();
    }
}