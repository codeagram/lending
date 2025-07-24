package com.lending.backend.crud.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.CLASS)
public @interface CrudEntity {
    String value() default "";

    String path() default "";

    boolean enableSoftDelete() default true;

    boolean enableAudit() default true;

    boolean enableCache() default true;

    int cacheTimeoutMinutes() default 30;

    String[] searchableFields() default {};

    String[] sortableFields() default {};
}