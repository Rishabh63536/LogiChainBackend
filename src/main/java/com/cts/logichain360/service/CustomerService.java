package com.cts.logichain360.service;

import com.cts.logichain360.dto.request.UpdateCustomerRequest;
import com.cts.logichain360.dto.response.CustomerResponse;
import org.springframework.http.ResponseEntity;
import java.util.List;

public interface CustomerService {
    ResponseEntity<CustomerResponse>       getCustomerById(Long id);
    ResponseEntity<CustomerResponse>       getCustomerByUserId(Long userId);
    ResponseEntity<List<CustomerResponse>> getAllCustomers();
    ResponseEntity<CustomerResponse>       updateCustomer(Long id, UpdateCustomerRequest request);
    ResponseEntity<Void>                   deleteCustomer(Long id);
}