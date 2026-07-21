package com.cts.logichain360.service.impl;

import com.cts.logichain360.annotation.Auditable;
import com.cts.logichain360.dto.request.UpdateVendorRequest;
import com.cts.logichain360.enums.AuditAction;
import com.cts.logichain360.dto.response.VendorResponse;
import com.cts.logichain360.mapper.VendorMapper;
import com.cts.logichain360.repository.VendorRepository;
import com.cts.logichain360.service.VendorService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service @RequiredArgsConstructor
public class VendorServiceImpl implements VendorService {
    private final VendorRepository vendorRepo;
    private final VendorMapper vendorMapper;

    @Override
    public ResponseEntity<VendorResponse> getVendorById(Long id) {
        return vendorRepo.findById(id)
                .map(vendor -> ResponseEntity.ok(vendorMapper.toResponse(vendor)))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @Override
    public ResponseEntity<VendorResponse> getVendorByUserId(Long userId) {
        return vendorRepo.findByUser_Id(userId)
                .map(vendor -> ResponseEntity.ok(vendorMapper.toResponse(vendor)))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @Override
    public ResponseEntity<List<VendorResponse>> getAllVendors() {
        return ResponseEntity.ok(vendorRepo.findAll().stream().map(vendorMapper::toResponse).toList());
    }

    @Override
    @Transactional
    @Auditable(action = AuditAction.VENDOR_UPDATED, entityType = "Vendor")
    public ResponseEntity<VendorResponse> updateVendor(Long id, UpdateVendorRequest req) {
        return vendorRepo.findById(id)
                .map(v -> {
                    if (req.getCompanyName()     != null) v.setCompanyName(req.getCompanyName());
                    if (req.getGstNumber()       != null) v.setGstNumber(req.getGstNumber());
                    if (req.getEmail()           != null) v.setEmail(req.getEmail());
                    if (req.getBusinessAddress() != null) v.setBusinessAddress(req.getBusinessAddress());
                    if (req.getContactPerson()   != null) v.setContactPerson(req.getContactPerson());
                    if (req.getPaymentTerms()    != null) v.setPaymentTerms(req.getPaymentTerms());
                    return ResponseEntity.ok(vendorMapper.toResponse(vendorRepo.save(v)));
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @Override
    @Transactional
    @Auditable(action = AuditAction.VENDOR_DELETED, entityType = "Vendor")
    public ResponseEntity<Void> deleteVendor(Long id) {
        return vendorRepo.findById(id)
                .map(v -> {
                    vendorRepo.delete(v);
                    return new ResponseEntity<Void>(HttpStatus.NO_CONTENT);
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}