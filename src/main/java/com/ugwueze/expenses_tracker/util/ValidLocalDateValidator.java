package com.ugwueze.expenses_tracker.util;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.time.LocalDate;

public class ValidLocalDateValidator implements ConstraintValidator<ValidLocalDate, LocalDate> {

    @Override
    public void initialize(ValidLocalDate constraintAnnotation) {
    }

    @Override
    public boolean isValid(LocalDate value, ConstraintValidatorContext context) {
        return true;
    }
}