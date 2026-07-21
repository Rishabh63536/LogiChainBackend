package com.cts.logichain360.controller;

import com.cts.logichain360.dto.request.*;
import com.cts.logichain360.dto.response.WarehouseManagerResponse;
import com.cts.logichain360.service.WarehouseManagerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/v1/warehouse-managers")
@RequiredArgsConstructor
@Tag(name = "Warehouse Management", description = "APIs for managing warehouse managers")
public class WarehouseManagerController {
    private final WarehouseManagerService wmService;

    @PreAuthorize("hasAnyRole('ADMIN','WAREHOUSE_MANAGER')")
    @Operation(summary = "Get warehouse manager by ID", description = "Retrieves a specific warehouse manager by their unique ID")
    @GetMapping("/{id}")
    public ResponseEntity<WarehouseManagerResponse> getById(@PathVariable Long id) {
        return wmService.getById(id);
    }

//    @PreAuthorize("hasAnyRole('ADMIN','WAREHOUSE_MANAGER')")
//    @Operation(summary = "Get warehouse manager by User ID", description = "Retrieves a specific warehouse manager by their associated user ID")
//    @GetMapping("/user/{userId}")
//    public ResponseEntity<WarehouseManagerResponse> getByUserId(@PathVariable Long userId) {
//        return wmService.getByUserId(userId);
//    }

    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get all warehouse managers", description = "Retrieves a list of all warehouse managers")
    @GetMapping
    public ResponseEntity<List<WarehouseManagerResponse>> getAll() {
        return wmService.getAll();
    }

    @PreAuthorize("hasAnyRole('ADMIN','WAREHOUSE_MANAGER')")
    @Operation(summary = "Update a warehouse manager", description = "Updates an existing warehouse manager's details by their ID")
    @PutMapping("/{id}")
    public ResponseEntity<WarehouseManagerResponse> update(@PathVariable Long id,
                                                           @RequestBody UpdateWarehouseManagerRequest request) {
        return wmService.update(id, request);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Assign a warehouse", description = "Assigns a specific warehouse to a warehouse manager")
    @PatchMapping("/{id}/assign-warehouse")
    public ResponseEntity<WarehouseManagerResponse> assignWarehouse(@PathVariable Long id,
                                                                    @RequestBody WarehouseAssignmentRequest request) {
        return wmService.assignWarehouse(id, request);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete a warehouse manager", description = "Deletes a warehouse manager by their ID")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        return wmService.delete(id);
    }
}