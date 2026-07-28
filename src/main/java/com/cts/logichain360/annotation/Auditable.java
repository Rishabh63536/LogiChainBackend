package com.cts.logichain360.annotation;

import com.cts.logichain360.enums.AuditAction;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

//Target defines where the annotation can be used
//Retention defines how long annotation is retained ,available at runtime

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Auditable {

    //event thts being perfomred
    AuditAction action();

    //The object type being affected eg Order
    String entityType();
}