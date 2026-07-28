package com.cts.logichain360.controller;

import com.cts.logichain360.entity.AuditLog;
import com.cts.logichain360.enums.AuditAction;
import com.cts.logichain360.service.AuditLogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/v1/audit-logs")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Audit Logs", description = "Read-only audit trail — ADMIN access only. No records are ever modified or deleted.")
public class AuditLogController {

    private final AuditLogService auditLogService;

    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "All audit records (newest first)")
    @GetMapping
    public ResponseEntity<List<AuditLog>> getAll() {
        log.info("ADMIN GET /audit-logs");
        return auditLogService.getAll();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Audit records for a specific actor",
               description = "Pass the actor's phone number (their login username).")
    @GetMapping("/actor/{phone}")
    public ResponseEntity<List<AuditLog>> getByActor(@PathVariable String phone) {
        log.info("ADMIN GET /audit-logs/actor/{}", phone);
        return auditLogService.getByActor(phone);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Audit records for a specific action type",
               description = "E.g. ORDER_PLACED, USER_DELETED. Uses the AuditAction enum.")
    @GetMapping("/action/{action}")
    public ResponseEntity<List<AuditLog>> getByAction(@PathVariable AuditAction action) {
        log.info("ADMIN GET /audit-logs/action/{}", action);
        return auditLogService.getByAction(action);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Full history of a specific entity",
               description = "E.g. GET /audit-logs/entity/Order/42 returns every event that touched Order #42.")
    @GetMapping("/entity/{entityType}/{entityId}")
    public ResponseEntity<List<AuditLog>> getByEntity(
            @PathVariable String entityType,
            @PathVariable Long entityId) {
        log.info("ADMIN GET /audit-logs/entity/{}/{}", entityType, entityId);
        return auditLogService.getByEntity(entityType, entityId);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Audit records within a time window",
               description = "ISO-8601 format: 2026-06-01T00:00:00")
    @GetMapping("/range")
    public ResponseEntity<List<AuditLog>> getByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to) {
        log.info("ADMIN GET /audit-logs/range from={} to={}", from, to);
        return auditLogService.getByDateRange(from, to);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "All failed operations",
               description = "Returns every audit record where outcome = FAILURE.")
    @GetMapping("/failures")
    public ResponseEntity<List<AuditLog>> getFailures() {
        log.info("ADMIN GET /audit-logs/failures");
        return auditLogService.getFailures();
    }
}