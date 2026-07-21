package com.cts.logichain360.service;

import com.cts.logichain360.entity.AuditLog;
import com.cts.logichain360.enums.AuditAction;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.List;

public interface AuditLogService {

    /** Called by AuditAspect to persist one record. Never throws — failures are swallowed. */
    void save(AuditLog log);

    ResponseEntity<List<AuditLog>> getAll();

    ResponseEntity<List<AuditLog>> getByActor(String actorPhone);

    ResponseEntity<List<AuditLog>> getByAction(AuditAction action);

    ResponseEntity<List<AuditLog>> getByEntity(String entityType, Long entityId);

    ResponseEntity<List<AuditLog>> getByDateRange(LocalDateTime from, LocalDateTime to);

    ResponseEntity<List<AuditLog>> getFailures();
}