package com.cts.logichain360.annotation;

import com.cts.logichain360.enums.AuditAction;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Mark a service-layer method for audit persistence.
 * The AuditAspect intercepts every method carrying this annotation and writes
 * one AuditLog row per invocation — regardless of success or failure.
 *
 * Usage:
 *   @Auditable(action = AuditAction.ORDER_PLACED, entityType = "Order")
 *   public ResponseEntity<OrderResponse> placeOrder(){}
 *
 * entityType is a free-form label (e.g. "Order", "Product", "User") used only
 */

//Target defines where the annotation can be used
//Retention defines how long annotation is retained - available at runtime

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Auditable {

    //The business event being performed
    AuditAction action();

    //The domain object type being affected(eg "Order", "Product")
    String entityType();
}