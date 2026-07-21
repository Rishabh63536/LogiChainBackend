package com.cts.logichain360.service.impl;

import com.cts.logichain360.entity.AuditLog;
import com.cts.logichain360.enums.AuditAction;
import com.cts.logichain360.repository.AuditLogRepository;
import com.cts.logichain360.service.AuditLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuditLogServiceImpl implements AuditLogService {

    private final AuditLogRepository auditLogRepo;

    /**
     * Persist one audit record.
     *
     * REQUIRES_NEW: runs in its own transaction so that an audit save never
     * interferes with — or gets rolled back by — the calling business transaction.
     * If the save itself fails, we log the error and swallow it; audit must never
     * break the main request flow.
     */
    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void save(AuditLog auditLog) {
        try {
            auditLogRepo.save(auditLog);
        } catch (Exception e) {
            // Audit failure must never propagate to the caller
            log.error("Failed to persist audit log [action={}, actor={}, entity={}]: {}",
                    auditLog.getAction(), auditLog.getActorPhone(),
                    auditLog.getEntityType(), e.getMessage(), e);
        }
    }

    @Override
    public ResponseEntity<List<AuditLog>> getAll() {
        return ResponseEntity.ok(auditLogRepo.findAll());
    }

    @Override
    public ResponseEntity<List<AuditLog>> getByActor(String actorPhone) {
        return ResponseEntity.ok(
                auditLogRepo.findAllByActorPhoneOrderByTimestampDesc(actorPhone));
    }

    @Override
    public ResponseEntity<List<AuditLog>> getByAction(AuditAction action) {
        return ResponseEntity.ok(
                auditLogRepo.findAllByActionOrderByTimestampDesc(action));
    }

    @Override
    public ResponseEntity<List<AuditLog>> getByEntity(String entityType, Long entityId) {
        return ResponseEntity.ok(
                auditLogRepo.findAllByEntityTypeAndEntityIdOrderByTimestampDesc(
                        entityType, entityId));
    }

    @Override
    public ResponseEntity<List<AuditLog>> getByDateRange(LocalDateTime from, LocalDateTime to) {
        return ResponseEntity.ok(
                auditLogRepo.findAllByTimestampBetweenOrderByTimestampDesc(from, to));
    }

    @Override
    public ResponseEntity<List<AuditLog>> getFailures() {
        return ResponseEntity.ok(
                auditLogRepo.findAllByOutcomeOrderByTimestampDesc("FAILURE"));
    }
}