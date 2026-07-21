package com.cts.logichain360.service.impl;

import com.cts.logichain360.annotation.Auditable;
import com.cts.logichain360.dto.request.UpdateCustomerRequest;
import com.cts.logichain360.enums.AuditAction;
import com.cts.logichain360.dto.response.CustomerResponse;
import com.cts.logichain360.mapper.CustomerMapper;
import com.cts.logichain360.repository.CustomerRepository;
import com.cts.logichain360.service.CustomerService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service @RequiredArgsConstructor
public class CustomerServiceImpl implements CustomerService {
    private final CustomerRepository customerRepo;
    private final CustomerMapper customerMapper;

    @Override
    public ResponseEntity<CustomerResponse> getCustomerById(Long id) {
        return customerRepo.findById(id)
                .map(c -> ResponseEntity.ok(customerMapper.toResponse(c)))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @Override
    public ResponseEntity<CustomerResponse> getCustomerByUserId(Long userId) {
        return customerRepo.findByUser_Id(userId)
                .map(c -> ResponseEntity.ok(customerMapper.toResponse(c)))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @Override
    public ResponseEntity<List<CustomerResponse>> getAllCustomers() {
        return ResponseEntity.ok(customerRepo.findAll().stream().map(customerMapper::toResponse).toList());
    }

    @Override
    @Transactional
    @Auditable(action = AuditAction.CUSTOMER_UPDATED, entityType = "Customer")
    public ResponseEntity<CustomerResponse> updateCustomer(Long id, UpdateCustomerRequest req) {
        return customerRepo.findById(id)
                .map(c -> {
                    if (req.getCompanyName()     != null) c.setCompanyName(req.getCompanyName());
                    if (req.getGstNumber()       != null) c.setGstNumber(req.getGstNumber());
                    if (req.getEmail()           != null) c.setEmail(req.getEmail());
                    if (req.getShippingAddress() != null) c.setShippingAddress(req.getShippingAddress());
                    if (req.getBillingAddress()  != null) c.setBillingAddress(req.getBillingAddress());
                    if (req.getCreditLimit()     != null) c.setCreditLimit(req.getCreditLimit());
                    if (req.getPaymentTerms()    != null) c.setPaymentTerms(req.getPaymentTerms());
                    return ResponseEntity.ok(customerMapper.toResponse(customerRepo.save(c)));
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @Override
    @Transactional
    @Auditable(action = AuditAction.CUSTOMER_DELETED, entityType = "Customer")
    public ResponseEntity<Void> deleteCustomer(Long id) {
        return customerRepo.findById(id)
                .map(c -> {
                    customerRepo.delete(c);
                    return new ResponseEntity<Void>(HttpStatus.NO_CONTENT);
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}