package com.cts.logichain360.controller;

import com.cts.logichain360.dto.request.*;
import com.cts.logichain360.dto.response.WarehouseResponse;
import com.cts.logichain360.service.WarehouseService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/v1/warehouses")
@RequiredArgsConstructor
public class WarehouseController {

    private final WarehouseService warehouseService;

    
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<WarehouseResponse> create(@RequestBody CreateWarehouseRequest request) {
        return warehouseService.createWarehouse(request);
    }

    @PreAuthorize("hasAnyRole('ADMIN','WAREHOUSE_MANAGER')")
    @GetMapping("/{id}")
    public ResponseEntity<WarehouseResponse> getById(@PathVariable Long id) {
        return warehouseService.getById(id);
    }

    @PreAuthorize("hasAnyRole('ADMIN','WAREHOUSE_MANAGER', 'VENDOR')")
    @GetMapping
    public ResponseEntity<List<WarehouseResponse>> getAll() {
        return warehouseService.getAll();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<WarehouseResponse> update(@PathVariable Long id,
                                                    @RequestBody UpdateWarehouseRequest request) {
        return warehouseService.update(id, request);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        return warehouseService.delete(id);
    }
}