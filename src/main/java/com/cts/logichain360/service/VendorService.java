package com.cts.logichain360.service;

import com.cts.logichain360.dto.request.UpdateVendorRequest;
import com.cts.logichain360.dto.response.VendorResponse;
import org.springframework.http.ResponseEntity;
import java.util.List;

public interface VendorService {
    ResponseEntity<VendorResponse>       getVendorByUserId(Long userId);
    ResponseEntity<List<VendorResponse>> getAllVendors();
    ResponseEntity<VendorResponse>       updateVendor(Long id, UpdateVendorRequest request);
    ResponseEntity<Void>                 deleteVendor(Long id);
	ResponseEntity<VendorResponse> getVendorById(Long id);
}