package com.cts.logichain360.service.impl;

import com.cts.logichain360.dto.response.WarehouseCollectionRecordResponse;
import com.cts.logichain360.repository.PaymentRepository;
import com.cts.logichain360.service.ReportService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReportServiceImpl implements ReportService {

    private final PaymentRepository paymentRepo;

    @Override
    public ResponseEntity<List<WarehouseCollectionRecordResponse>> getWarehouseCollections() {
        log.info("Fetching flat warehouse-collection payment records for reporting.");
        return ResponseEntity.ok(paymentRepo.findAllWarehouseCollectionRecords());
    }
}