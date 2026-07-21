package com.cts.logichain360.controller;

import com.cts.logichain360.dto.request.*;
import com.cts.logichain360.dto.response.ProductResponse;
import com.cts.logichain360.service.ProductService;
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
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Product", description = "Vendor catalog: products defined by vendors. Stock lives in /product-warehouses.")
public class ProductController {

    private final ProductService productService;

    
    @Operation(summary = "Create a catalog product for a vendor")
    
    @PreAuthorize("hasAnyRole('ADMIN','VENDOR')")
    @PostMapping
    public ResponseEntity<ProductResponse> create(@Valid @RequestBody CreateProductRequest request) {
        log.info("POST /products — name='{}', vendorId={}", request.getProductName(), request.getVendorId());
        return productService.createProduct(request);
    }

    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get a product by id")
    @GetMapping("/{id}")
    public ResponseEntity<ProductResponse> getById(@PathVariable Long id) {
        return productService.getProductById(id);
    }

    @PreAuthorize("hasAnyRole('ADMIN','CUSTOMER')")
    @Operation(summary = "List all products")
    @GetMapping
    public ResponseEntity<List<ProductResponse>> getAll() {
        return productService.getAllProducts();
    }

    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "List all products created by a vendor")
    @GetMapping("/vendor/{vendorId}")
    public ResponseEntity<List<ProductResponse>> getByVendor(@PathVariable Long vendorId) {
        return productService.getProductsByVendor(vendorId);
    }

    @PreAuthorize("hasAnyRole('ADMIN','VENDOR')")
    @Operation(summary = "Update a product's catalog details")
    @PutMapping("/{id}")
    public ResponseEntity<ProductResponse> update(@PathVariable Long id,
                                                  @Valid @RequestBody UpdateProductRequest request) {
        log.info("PUT /products/{}", id);
        return productService.updateProduct(id, request);
    }

    @PreAuthorize("hasAnyRole('ADMIN','VENDOR')")
    @Operation(summary = "Delete a product")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        log.info("DELETE /products/{}", id);
        return productService.deleteProduct(id);
    }
}