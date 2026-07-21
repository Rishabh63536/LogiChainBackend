package com.cts.logichain360.entity;

import com.cts.logichain360.enums.AuditAction;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Immutable audit record — never updated, never deleted.
 * Does NOT extend SoftDeletableEntity: an audit trail that can be erased defeats its purpose.
 *
 * One row is written per @Auditable service method invocation, whether it succeeds or fails.
 *
 * Table is audit_logs
 */
@Entity
@Table(name = "audit_logs", indexes = {
        @Index(name = "idx_audit_actor",      columnList = "actor_phone"),
        @Index(name = "idx_audit_action",     columnList = "action"),
        @Index(name = "idx_audit_entity",     columnList = "entity_type, entity_id"),
        @Index(name = "idx_audit_timestamp",  columnList = "timestamp")
})
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    //who
    // Phone number (username) extracted from the JWT principal. ANONYMOUS if unauthenticated
    @Column(name = "actor_phone", nullable = false, length = 20)
    private String actorPhone;

    /** Role of the acting user at the time of the call (e.g. ADMIN, VENDOR). */
    @Column(name = "actor_role", length = 30)
    private String actorRole;

    //what

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 60)
    private AuditAction action;

    /** Domain type label (e.g. "Order", "Product"). Sourced from @Auditable.entityType(). */
    @Column(name = "entity_type", nullable = false, length = 50)
    private String entityType;

    /**
     * ID of the affected entity.
     * Extracted from the return value's getId() or the first Long argument.
     * Null when the ID cannot be determined (e.g. bulk operations, login).
     */
    @Column(name = "entity_id")
    private Long entityId;

    // ── Where (HTTP context) ─────────────────────────────────────────

    /** HTTP verb (GET, POST, PUT, PATCH, DELETE). Null when called outside a request thread. */
    @Column(name = "http_method", length = 10)
    private String httpMethod;

    /** Request URI (e.g. /api/v1/orders/5/cancel). */
    @Column(name = "request_uri", length = 300)
    private String requestUri;

    // ── Result ────────────────────────────────────────────────────────

    /** SUCCESS or FAILURE. */
    @Column(nullable = false, length = 10)
    private String outcome;

    /**
     * HTTP status code of the response.
     * 200/201/204 → SUCCESS. 4xx/5xx → FAILURE.
     * Captured from ResponseEntity.getStatusCode() on normal return.
     * Set to 500 when an uncaught exception propagates.
     * Null only if the method returns something other than ResponseEntity (shouldn't happen).
     */
    @Column(name = "http_status")
    private Integer httpStatus;

    /** Exception message captured on FAILURE. Null on SUCCESS. */
    @Column(name = "error_message", length = 500)
    private String errorMessage;

    /** Wall-clock time the service method took to execute (ms). */
    @Column(name = "execution_time_ms")
    private Long executionTimeMs;

    //when

    @Column(nullable = false)
    private LocalDateTime timestamp;
}