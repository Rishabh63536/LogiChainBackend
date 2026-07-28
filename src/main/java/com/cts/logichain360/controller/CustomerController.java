package com.cts.logichain360.controller;

import com.cts.logichain360.dto.request.UpdateCustomerRequest;
import com.cts.logichain360.dto.response.CustomerResponse;
import com.cts.logichain360.service.CustomerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/v1/customers")
@RequiredArgsConstructor
@Tag(name = "Customer Management", description = "APIs for managing customers")
public class CustomerController {
    private final CustomerService customerService;

    @PreAuthorize("hasAnyRole('ADMIN','CUSTOMER','WAREHOUSE_MANAGER ')")
    @Operation(summary = "Get customer by ID", description = "Retrieves a specific customer by their unique ID")
    @GetMapping("/{id}")
    public ResponseEntity<CustomerResponse> getById(@PathVariable Long id) {
        return customerService.getCustomerById(id);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get all customers", description = "Retrieves a list of all customers")
    @GetMapping
    public ResponseEntity<List<CustomerResponse>> getAll() {
        return customerService.getAllCustomers();
    }

    @PreAuthorize("hasAnyRole('ADMIN','CUSTOMER')")
    @Operation(summary = "Update a customer", description = "Updates an existing customer's details by their ID")
    @PutMapping("/{id}")
    public ResponseEntity<CustomerResponse> update(@PathVariable Long id, @RequestBody UpdateCustomerRequest request) {
        return customerService.updateCustomer(id, request);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete a customer", description = "Deletes a customer by their ID")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        return customerService.deleteCustomer(id);
    }
}