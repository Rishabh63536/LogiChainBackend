package com.cts.logichain360.controller;

import com.cts.logichain360.dto.response.WarehouseCollectionRecordResponse;
import com.cts.logichain360.service.ReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/reports")
@RequiredArgsConstructor
@Tag(name = "Reports", description = "Admin-facing KPI/reporting endpoints.")
public class ReportController {

    private final ReportService reportService;

    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Flat, unaggregated feed of every payment tagged by warehouse",
               description = "One row per payment (ADVANCE/FINAL/REFUND). No grouping or date " +
                             "filtering — the frontend groups/sums/filters this itself, so this one " +
                             "endpoint serves every reporting view without needing a new endpoint per view.")
    @GetMapping("/warehouse-collections")
    public ResponseEntity<List<WarehouseCollectionRecordResponse>> getWarehouseCollections() {
        return reportService.getWarehouseCollections();
    }
}