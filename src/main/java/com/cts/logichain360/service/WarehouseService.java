package com.cts.logichain360.service;

import com.cts.logichain360.dto.request.*;
import com.cts.logichain360.dto.response.WarehouseResponse;
import org.springframework.http.ResponseEntity;
import java.util.List;

public interface WarehouseService {
    ResponseEntity<WarehouseResponse>createWarehouse(CreateWarehouseRequest request);
    ResponseEntity<WarehouseResponse> getById(Long id);
    ResponseEntity<List<WarehouseResponse>> getAll();
    ResponseEntity<WarehouseResponse> update(Long id, UpdateWarehouseRequest request);
    ResponseEntity<Void> delete(Long id);
}