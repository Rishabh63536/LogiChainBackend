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
        MethodSignature sig = (MethodSignature) joinPoint.getSignature();
        Method method = sig.getMethod();
        Auditable ann = method.getAnnotation(Auditable.class);

        String entityType = ann.entityType();

        //resolving actor from SecurityContext
        String actorPhone = ANONYMOUS;
        String actorRole  = null;

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated()
                && !"anonymousUser".equals(auth.getPrincipal())) {
            actorPhone = auth.getName();  // phone, as set by JWTFilter
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
            long elapsed = System.currentTimeMillis() - start;
            persistAudit(ann, entityType, actorPhone, actorRole,
                    httpMethod, requestUri,
                    FAILURE, 500, ex.getMessage(),
                    elapsed, null, joinPoint.getArgs());
            throw ex;
        }

        long elapsed = System.currentTimeMillis() - start;

        if (result instanceof ResponseEntity<?> re) {
            httpStatus = re.getStatusCode().value();
            if (re.getStatusCode().isError()) {
                // 4xx or 5xx response → this is a logical failure
                outcome = FAILURE;
                errorMessage = "HTTP " + httpStatus + " returned by service";
            }
        }

        Long entityId = SUCCESS.equals(outcome)
                ? extractEntityId(result, joinPoint.getArgs())
                : null;

        persistAudit(ann, entityType, actorPhone, actorRole,
                httpMethod, requestUri,
                outcome, httpStatus, errorMessage,
                elapsed, entityId, joinPoint.getArgs());

        return result;
    }

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
            log.error("AuditAspect.persistAudit failed unexpectedly: {}", e.getMessage(), e);
        }
    }
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
                    try {
                        Method getProductId = body.getClass().getMethod("getProductId");
                        Object idVal = getProductId.invoke(body);
                        if (idVal instanceof Long l) return l;
                        if (idVal instanceof Number n) return n.longValue();
                    } catch (Exception ignored2) { /* continue */ }
                } catch (Exception ignored) { /* continue */ }
            }
        }

        if (args != null) {
            for (Object arg : args) {
                if (arg instanceof Long l) return l;
            }
        }

        return null;
    }
}