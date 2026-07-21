package com.cts.logichain360.controller;

import com.cts.logichain360.dto.request.*;
import com.cts.logichain360.dto.response.ProductWarehouseResponse;
import com.cts.logichain360.service.ProductWarehouseService;
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
import java.util.List;

@RestController
@RequestMapping("/api/v1/product-warehouses")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Product-Warehouse",
     description = "Stock entries: links a product to a warehouse with stock, capacity, and reorder threshold.")
public class ProductWarehouseController {

    private final ProductWarehouseService productWarehouseService;

    @Operation(summary = "Launch a product at a warehouse",
               description = "Creates a stock entry. Today this is 1:1 — refuses if the product is already launched.")
    
    @PreAuthorize("hasAnyRole('ADMIN','VENDOR')")
    @PostMapping
    public ResponseEntity<ProductWarehouseResponse> launch(
            @Valid @RequestBody LaunchProductAtWarehouseRequest request) {
        log.info("POST /product-warehouses — launching product {} at warehouse {}",
                request.getProductId(), request.getWarehouseId());
        return productWarehouseService.launch(request);
    }

    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get a stock entry by its id")
    @GetMapping("/{id}")
    public ResponseEntity<ProductWarehouseResponse> getById(@PathVariable Long id) {
        return productWarehouseService.getById(id);
    }

    
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get the stock entry for a product",
               description = "Since each product currently lives at exactly one warehouse, this returns one entry.")
    @GetMapping("/product/{productId}")
    public ResponseEntity<ProductWarehouseResponse> getByProductId(@PathVariable Long productId) {
        return productWarehouseService.getByProductId(productId);
    }


    @PreAuthorize("hasAnyRole('ADMIN','WAREHOUSE_MANAGER')")
    @Operation(summary = "List all stock entries at a warehouse")
    @GetMapping("/warehouse/{warehouseId}")
    public ResponseEntity<List<ProductWarehouseResponse>> getByWarehouseId(@PathVariable Long warehouseId) {
        return productWarehouseService.getByWarehouseId(warehouseId);
    }

    
    @PreAuthorize("hasAnyRole('ADMIN','WAREHOUSE_MANAGER','VENDOR')")
    @Operation(summary = "List all stock entries (admin/global view)")
    @GetMapping
    public ResponseEntity<List<ProductWarehouseResponse>> getAll() {
        return productWarehouseService.getAll();
    }
    

    @PreAuthorize("hasAnyRole('ADMIN','WAREHOUSE_MANAGER')")
    @Operation(summary = "Find ALL products in danger (below ROL across all warehouses)",
               description = "Returns every stock entry where current stock %% is below the row's rolPercent.")
    @GetMapping("/low-stock")
    public ResponseEntity<List<ProductWarehouseResponse>> getLowStock() {
        log.info("GET /product-warehouses/low-stock — fetching all low-stock entries");
        return productWarehouseService.getLowStock();
    }
    

    @Operation(summary = "Manager dashboard: products in danger AT THIS warehouse",
               description = "Same as /low-stock but filtered to a single warehouse.")
    @GetMapping("/low-stock/warehouse/{warehouseId}")
    public ResponseEntity<List<ProductWarehouseResponse>> getLowStockByWarehouse(@PathVariable Long warehouseId) {
        log.info("GET /low-stock/warehouse/{} — manager dashboard query", warehouseId);
        return productWarehouseService.getLowStockByWarehouse(warehouseId);
    }

    
    @PreAuthorize("hasAnyRole('ADMIN','WAREHOUSE_MANAGER')")
    @Operation(summary = "Update maxStock or rolPercent",
               description = "Send only the fields you want to change. Stock cannot exceed maxStock after update.")
    @PutMapping("/{id}")
    public ResponseEntity<ProductWarehouseResponse> updateThresholds(
            @PathVariable Long id,
            @Valid @RequestBody UpdateProductWarehouseRequest request) {
        return productWarehouseService.updateThresholds(id, request);
    }
    

    @PreAuthorize("hasAnyRole('ADMIN','WAREHOUSE_MANAGER','VENDOR')")
    @Operation(summary = "Restock — add units to current stock",
               description = "Refuses if new total would exceed maxStock.")
    @PatchMapping("/{id}/restock")
    public ResponseEntity<ProductWarehouseResponse> restock(
            @PathVariable Long id,
            @Valid @RequestBody RestockRequest request) {
        log.info("PATCH /product-warehouses/{}/restock — amount={}", id, request.getAmount());
        return productWarehouseService.restock(id, request);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete a stock entry")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        return productWarehouseService.delete(id);
    }
}