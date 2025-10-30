package com.ugwueze.expenses_tracker.util;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = ValidLocalDateValidator.class)
public @interface ValidLocalDate {
    String message() default "Invalid date format";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}