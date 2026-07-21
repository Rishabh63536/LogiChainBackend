package com.cts.logichain360.repository;

import com.cts.logichain360.entity.AuditLog;
import com.cts.logichain360.enums.AuditAction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {

    /** All actions performed by a specific user (phone), newest first. */
    List<AuditLog> findAllByActorPhoneOrderByTimestampDesc(String actorPhone);

    /** All audit records for a specific action type, newest first. */
    List<AuditLog> findAllByActionOrderByTimestampDesc(AuditAction action);

    /** All audit records touching a specific entity (e.g. all events on Order 42). */
    List<AuditLog> findAllByEntityTypeAndEntityIdOrderByTimestampDesc(
            String entityType, Long entityId);

    /** All records within a time window, newest first. Useful for admin dashboards. */
    List<AuditLog> findAllByTimestampBetweenOrderByTimestampDesc(
            LocalDateTime from, LocalDateTime to);

    /** All failed operations, newest first. */
    List<AuditLog> findAllByOutcomeOrderByTimestampDesc(String outcome);
}