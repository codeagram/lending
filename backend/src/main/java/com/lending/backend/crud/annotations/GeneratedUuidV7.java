package com.lending.backend.crud.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.ElementType;

import org.hibernate.annotations.IdGeneratorType;

import com.lending.backend.crud.util.UuidV7Generator;

@IdGeneratorType(UuidV7Generator.class)
@Target({ ElementType.FIELD, ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
public @interface GeneratedUuidV7 {
}