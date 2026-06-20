package com.facturemanagement.application.service.skeleton.audit.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface DocumentAuditable {
    DocumentOperationType operationType();

    String moduleName() default "FACTURAS";
}