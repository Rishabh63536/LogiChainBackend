package com.cts.logichain360.config;

import com.cts.logichain360.annotation.Auditable;
import com.cts.logichain360.entity.AuditLog;
import com.cts.logichain360.service.AuditLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.lang.reflect.Method;
import java.time.LocalDateTime;

/**
 * Intercepts every service method annotated with @Auditable and writes one
 * AuditLog row per invocation (SUCCESS or FAILURE).
 * 
 * Separate from LoggingAspect: logging to file != persisting to DB.
 * 
 * Entity ID extraction: tries the return value's getId() first, then scans method args for the first Long, 
 * then gives up gracefully.
 * 
 * Uses AuditLogService.save() which runs in REQUIRES_NEW, so an audit failure never rolls back the 
 * calling business transaction.
 * Any exception thrown by this aspect is swallowed — audit must be invisible to the main request flow.
 */
@Aspect
@Component
@RequiredArgsConstructor
@Slf4j
public class AuditAspect {

    private static final String ANONYMOUS = "ANONYMOUS";
    private static final String SUCCESS   = "SUCCESS";
    private static final String FAILURE   = "FAILURE";

    private final AuditLogService auditLogService;

    @Around("@annotation(com.cts.logichain360.annotation.Auditable)")
    public Object audit(ProceedingJoinPoint joinPoint) throws Throwable {

        // Read annotation metadata
        MethodSignature sig    = (MethodSignature) joinPoint.getSignature();
        Method method = sig.getMethod();
        Auditable ann = method.getAnnotation(Auditable.class);

        String entityType = ann.entityType();

        //Resolve actor from SecurityContext
        String actorPhone = ANONYMOUS;
        String actorRole  = null;

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated()
                && !"anonymousUser".equals(auth.getPrincipal())) {
            actorPhone = auth.getName();  // phone, as set by JWTFilter / UserInfoConfigManager
            actorRole  = auth.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .findFirst()
                    .orElse(null);
        }

        //Resolve HTTP context (available because we're in a request thread)
        String httpMethod  = null;
        String requestUri  = null;

        ServletRequestAttributes attrs =
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attrs != null) {
            httpMethod = attrs.getRequest().getMethod();
            requestUri = attrs.getRequest().getRequestURI();
        }

        // Execute the real method 
        long start = System.currentTimeMillis();
        Object result;
        String outcome= SUCCESS;
        String errorMessage = null;
        Integer httpStatus= null;

        try {
            result = joinPoint.proceed();
        } catch (Throwable ex) {
            // Exception thrown (Pattern A — orElseThrow style)
            // outcome = FAILURE, status = 500 (we don't know the exact status here;
            // GlobalExceptionHandler maps it, but that runs after us)
            long elapsed = System.currentTimeMillis() - start;
            persistAudit(ann, entityType, actorPhone, actorRole,
                    httpMethod, requestUri,
                    FAILURE, 500, ex.getMessage(),
                    elapsed, null, joinPoint.getArgs());
            throw ex;   // re-throw — caller's flow is completely unaffected
        }

        long elapsed = System.currentTimeMillis() - start;

        // ── Determine outcome from ResponseEntity status (Pattern B fix) ──
        // Methods that use .orElseGet(() -> ResponseEntity.notFound().build())
        // never throw — they return a 4xx ResponseEntity. Without this check,
        // those would be recorded as SUCCESS even though nothing was done.
        if (result instanceof ResponseEntity<?> re) {
            httpStatus = re.getStatusCode().value();
            if (re.getStatusCode().isError()) {
                // 4xx or 5xx response → this is a logical failure
                outcome = FAILURE;
                errorMessage = "HTTP " + httpStatus + " returned by service";
            }
            // 2xx stays SUCCESS, httpStatus is still captured for the audit record
        }

        //Extract entity ID — only makes sense on SUCCESS
        Long entityId = SUCCESS.equals(outcome)
                ? extractEntityId(result, joinPoint.getArgs())
                : null;   // no entity was created/modified on failure

        persistAudit(ann, entityType, actorPhone, actorRole,
                httpMethod, requestUri,
                outcome, httpStatus, errorMessage,
                elapsed, entityId, joinPoint.getArgs());

        return result;
    }

    //Private helpers

    private void persistAudit(Auditable ann,
                               String entityType,
                               String actorPhone,
                               String actorRole,
                               String httpMethod,
                               String requestUri,
                               String outcome,
                               Integer httpStatus,
                               String errorMessage,
                               long   executionTimeMs,
                               Long   entityId,
                               Object[] args) {
        try {
            AuditLog entry = AuditLog.builder()
                    .actorPhone(actorPhone)
                    .actorRole(actorRole)
                    .action(ann.action())
                    .entityType(entityType)
                    .entityId(entityId)
                    .httpMethod(httpMethod)
                    .requestUri(requestUri)
                    .outcome(outcome)
                    .httpStatus(httpStatus)
                    .errorMessage(errorMessage != null && errorMessage.length() > 500
                            ? errorMessage.substring(0, 500) : errorMessage)
                    .executionTimeMs(executionTimeMs)
                    .timestamp(LocalDateTime.now())
                    .build();

            auditLogService.save(entry);

        } catch (Exception e) {
            // Absolute safety net — audit must never break anything
            log.error("AuditAspect.persistAudit failed unexpectedly: {}", e.getMessage(), e);
        }
    }

    /**
     * Best-effort entity ID extraction.
     *
     * Strategy:
     *  1. If the return value is a ResponseEntity whose body has a getId() method,
     *     call it. This covers the vast majority of service methods.
     *  2. Otherwise scan the method args for the first Long — typically the
     *     path-variable id passed to update/delete methods.
     *  3. Give up and return null.
     */
    private Long extractEntityId(Object result, Object[] args) {
        // Strategy 1: try the ResponseEntity body
        if (result instanceof ResponseEntity<?> re) {
            Object body = re.getBody();
            if (body != null) {
                try {
                    Method getId = body.getClass().getMethod("getId");
                    Object idVal = getId.invoke(body);
                    if (idVal instanceof Long l) return l;
                    if (idVal instanceof Number n) return n.longValue();
                } catch (NoSuchMethodException ignored) {
                    // Body DTO doesn't have getId() — try getProductId() as a fallback
                    // (Product uses productId as its PK field name)
                    try {
                        Method getProductId = body.getClass().getMethod("getProductId");
                        Object idVal = getProductId.invoke(body);
                        if (idVal instanceof Long l) return l;
                        if (idVal instanceof Number n) return n.longValue();
                    } catch (Exception ignored2) { /* continue */ }
                } catch (Exception ignored) { /* continue */ }
            }
        }

        // Strategy 2: first Long arg (path-variable id for update/delete methods)
        if (args != null) {
            for (Object arg : args) {
                if (arg instanceof Long l) return l;
            }
        }

        return null;
    }
}