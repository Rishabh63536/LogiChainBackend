package com.cts.logichain360.service;

import com.cts.logichain360.dto.request.*;
import com.cts.logichain360.dto.response.WarehouseManagerResponse;
import org.springframework.http.ResponseEntity;
import java.util.List;

public interface WarehouseManagerService {
    ResponseEntity<WarehouseManagerResponse> getById(Long id);
    ResponseEntity<WarehouseManagerResponse> getByUserId(Long userId);
    ResponseEntity<List<WarehouseManagerResponse>>getAll();
    ResponseEntity<WarehouseManagerResponse> update(Long id, UpdateWarehouseManagerRequest request);
    ResponseEntity<WarehouseManagerResponse> assignWarehouse(Long id, WarehouseAssignmentRequest request);
    ResponseEntity<Void> delete(Long id);
}