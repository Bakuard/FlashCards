package com.bakuard.flashcards.validation.annotation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class PasswordConstraintValidator implements ConstraintValidator<Password, String> {

    @Override
    public boolean isValid(String s, ConstraintValidatorContext constraintValidatorContext) {
        return s != null
                && !s.isBlank()
                && s.length() >= 8
                && s.length() <= 50;
    }

}
