package com.cts.logichain360.service.impl;

import com.cts.logichain360.dto.response.PODResponse;
import com.cts.logichain360.exception.ResourceNotFoundException;
import com.cts.logichain360.mapper.PODMapper;
import com.cts.logichain360.repository.PODRepository;
import com.cts.logichain360.service.PODService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PODServiceImpl implements PODService {

    private final PODRepository podRepo;
    private final PODMapper podMapper;

    @Override
    public ResponseEntity<PODResponse> getByOrderId(Long orderId) {
        return podRepo.findByOrder_Id(orderId)
                .map(pod -> ResponseEntity.ok(podMapper.toResponse(pod)))
                .orElseThrow(() -> new ResourceNotFoundException("No POD found for order " + orderId + " ,delivery not yet completed."));
    }
}