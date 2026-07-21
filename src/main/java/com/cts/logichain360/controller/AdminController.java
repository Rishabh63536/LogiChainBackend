package com.cts.logichain360.controller;

import com.cts.logichain360.dto.request.*;
import com.cts.logichain360.dto.response.*;
import com.cts.logichain360.service.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Admin", description = "Governance-only endpoints — all require ADMIN role.")
public class AdminController {

    private final UserService userService;
    private final CustomerService customerService;
    private final VendorService vendorService;
    private final DriverService driverService;
    private final WarehouseManagerService wmService;
    private final ProductService productService;
    private final OrderService orderService;


    @Operation(summary = "Activate / suspend any user account")
    @PatchMapping("/users/{id}/status")
    public ResponseEntity<UserResponse> updateUserStatus(
            @PathVariable Long id,
            @RequestBody UserStatusRequest req) {
        log.info("ADMIN PATCH /users/{}/status → {}", id, req.getStatus());
        return userService.updateUserStatus(id, req);
    }

    @Operation(summary = "Soft-delete any user and their role profile")
    @DeleteMapping("/users/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        log.info("ADMIN DELETE /users/{}", id);
        return userService.deleteUser(id);
    }


    @Operation(summary = "Delete any customer profile")
    @DeleteMapping("/customers/{id}")
    public ResponseEntity<Void> deleteCustomer(@PathVariable Long id) {
        log.info("ADMIN DELETE /customers/{}", id);
        return customerService.deleteCustomer(id);
    }


    @Operation(summary = "Delete any vendor profile")
    @DeleteMapping("/vendors/{id}")
    public ResponseEntity<Void> deleteVendor(@PathVariable Long id) {
        log.info("ADMIN DELETE /vendors/{}", id);
        return vendorService.deleteVendor(id);
    }


    @Operation(summary = "Override driver availability",
               description = "Force a driver to available or unavailable regardless of active orders.")
    @PatchMapping("/drivers/{id}/availability")
    public ResponseEntity<DriverResponse> updateDriverAvailability(
            @PathVariable Long id,
            @RequestBody DriverAvailabilityRequest req) {
        log.info("ADMIN PATCH /drivers/{}/availability → {}", id, req.getAvailable());
        return driverService.updateAvailability(id, req);
    }

    @Operation(summary = "Delete any driver profile")
    @DeleteMapping("/drivers/{id}")
    public ResponseEntity<Void> deleteDriver(@PathVariable Long id) {
        log.info("ADMIN DELETE /drivers/{}", id);
        return driverService.deleteDriver(id);
    }


    @Operation(summary = "Assign or reassign a warehouse to a manager")
    @PatchMapping("/warehouse-managers/{id}/assign-warehouse")
    public ResponseEntity<WarehouseManagerResponse> assignWarehouse(
            @PathVariable Long id,
            @RequestBody WarehouseAssignmentRequest req) {
        log.info("ADMIN PATCH /warehouse-managers/{}/assign-warehouse → wh={}", id, req.getWarehouseId());
        return wmService.assignWarehouse(id, req);
    }

    @Operation(summary = "Delete any warehouse manager profile")
    @DeleteMapping("/warehouse-managers/{id}")
    public ResponseEntity<Void> deleteWarehouseManager(@PathVariable Long id) {
        log.info("ADMIN DELETE /warehouse-managers/{}", id);
        return wmService.delete(id);
    }


    @Operation(summary = "Delete any product",
               description = "Admin can remove any vendor's product from the catalog.")
    @DeleteMapping("/products/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long id) {
        log.info("ADMIN DELETE /products/{}", id);
        return productService.deleteProduct(id);
    }


    @Operation(summary = "Force-cancel any order",
               description = "Admin can cancel any order that has not yet been DELIVERED.")
    @PatchMapping("/orders/{id}/cancel")
    public ResponseEntity<OrderResponse> cancelOrder(@PathVariable Long id) {
        log.info("ADMIN PATCH /orders/{}/cancel", id);
        return orderService.cancelOrder(id);
    }
}