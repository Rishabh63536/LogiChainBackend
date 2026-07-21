package com.cts.logichain360.service;

import com.cts.logichain360.dto.response.PODResponse;
import org.springframework.http.ResponseEntity;

public interface PODService {
    ResponseEntity<PODResponse> getByOrderId(Long orderId);
}