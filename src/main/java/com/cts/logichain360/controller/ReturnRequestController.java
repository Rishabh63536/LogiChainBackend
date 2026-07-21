package com.cts.logichain360.controller;

import com.cts.logichain360.dto.request.ApproveReturnRequest;
import com.cts.logichain360.dto.request.CreateReturnRequestRequest;
import com.cts.logichain360.dto.request.RejectReturnRequest;
import com.cts.logichain360.dto.response.ReturnRequestResponse;
import com.cts.logichain360.service.FileStorageService;
import com.cts.logichain360.service.ReturnRequestService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/v1/return-requests")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Return Requests", description = "Post-delivery returns: request, approve/reject, pickup+restock.")
public class ReturnRequestController {

    private final ReturnRequestService returnRequestService;
    private final FileStorageService fileStorageService;

    @PreAuthorize("hasAnyRole('CUSTOMER','ADMIN')")
    @Operation(summary = "Customer requests a return", description = "Order must be DELIVERED.")
    @PostMapping
    public ResponseEntity<ReturnRequestResponse> create(@Valid @RequestBody CreateReturnRequestRequest request) {
        log.info("POST /return requests, customer={}, order={}", request.getCustomerId(), request.getOrderId());
        return returnRequestService.createReturnRequest(request);
    }

    @PreAuthorize("hasAnyRole('ADMIN','WAREHOUSE_MANAGER')")
    @Operation(summary = "Warehouse manager approves a return and assigns a pickup driver")
    @PatchMapping("/{id}/approve")
    public ResponseEntity<ReturnRequestResponse> approve(@PathVariable Long id, @Valid @RequestBody ApproveReturnRequest request) {
        log.info("PATCH /return-requests/{}/approve", id);
        return returnRequestService.approve(id, request);
    }

    @PreAuthorize("hasAnyRole('ADMIN','WAREHOUSE_MANAGER')")
    @Operation(summary = "Warehouse manager rejects a return")
    @PatchMapping("/{id}/reject")
    public ResponseEntity<ReturnRequestResponse> reject(@PathVariable Long id, @Valid @RequestBody RejectReturnRequest request) {
        log.info("PATCH /return-requests/{}/reject", id);
        return returnRequestService.reject(id, request);
    }

    @PreAuthorize("hasAnyRole('ADMIN','DRIVER')")
    @Operation(summary = "Driver confirms pickup and restock in one action",
               description = "Photo evidence is optional, omit the 'photo' part to skip it")
    @PatchMapping(value = "/{id}/restock", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ReturnRequestResponse> restock(@PathVariable Long id, @RequestParam(value = "photo", required = false) MultipartFile photo) {
        log.info("PATCH /return-requests/{}/restock, photo provided={}", id, photo != null && !photo.isEmpty());
        String storedFilename = (photo != null && !photo.isEmpty()) ? fileStorageService.store(photo) : null;
        return returnRequestService.completeRestock(id, storedFilename);
    }

    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get a return request by id")
    @GetMapping("/{id}")
    public ResponseEntity<ReturnRequestResponse> getById(@PathVariable Long id) {
        return returnRequestService.getById(id);
    }

    @PreAuthorize("hasAnyRole('ADMIN','CUSTOMER')")
    @Operation(summary = "All return requests for a customer")
    @GetMapping("/customer/{customerId}")
    public ResponseEntity<List<ReturnRequestResponse>> getByCustomer(@PathVariable Long customerId) {
        return returnRequestService.getByCustomerId(customerId);
    }

    @PreAuthorize("hasAnyRole('ADMIN','WAREHOUSE_MANAGER')")
    @Operation(summary = "All pending (REQUESTED) return requests, the WarehouseManager's queue")
    @GetMapping("/pending")
    public ResponseEntity<List<ReturnRequestResponse>> getPending() {
        return returnRequestService.getPending();
    }
    
    @PreAuthorize("hasAnyRole('ADMIN','DRIVER','WAREHOUSE_MANAGER')")
    @Operation(summary = "Driver's pickup queue",
               description = "Approved returns assigned to this driver, not yet restocked.")
    @GetMapping("/driver/{driverId}")
    public ResponseEntity<List<ReturnRequestResponse>> getByDriver(@PathVariable Long driverId) {
        return returnRequestService.getByDriverId(driverId);
    }
}