package com.cts.logichain360.service;

import com.cts.logichain360.dto.request.*;
import com.cts.logichain360.dto.response.ProductWarehouseResponse;
import org.springframework.http.ResponseEntity;
import java.util.List;

public interface ProductWarehouseService {

    ResponseEntity<ProductWarehouseResponse> launch(LaunchProductAtWarehouseRequest request);

    ResponseEntity<ProductWarehouseResponse> getById(Long id);

    //Lookup the warehouse entry for a given product (1:1 today returns one)
    ResponseEntity<ProductWarehouseResponse> getByProductId(Long productId);

    // All stock entries at a warehouse
    ResponseEntity<List<ProductWarehouseResponse>> getByWarehouseId(Long warehouseId);

    ResponseEntity<List<ProductWarehouseResponse>> getAll();

    // All entries where current stock % is below their rolPercent
    ResponseEntity<List<ProductWarehouseResponse>> getLowStock();

    // Low-stock filtered to one warehouse — manager's dashboard view
    ResponseEntity<List<ProductWarehouseResponse>> getLowStockByWarehouse(Long warehouseId);

    ResponseEntity<ProductWarehouseResponse> updateThresholds(Long id, UpdateProductWarehouseRequest request);

    ResponseEntity<ProductWarehouseResponse> restock(Long id, RestockRequest request);

    ResponseEntity<Void> delete(Long id);
}