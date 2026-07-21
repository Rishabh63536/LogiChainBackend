package com.cts.logichain360.service;

import com.cts.logichain360.dto.response.WarehouseCollectionRecordResponse;
import org.springframework.http.ResponseEntity;

import java.util.List;

public interface ReportService {

    ResponseEntity<List<WarehouseCollectionRecordResponse>> getWarehouseCollections();
}