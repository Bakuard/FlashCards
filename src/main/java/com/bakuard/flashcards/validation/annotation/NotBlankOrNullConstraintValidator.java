package com.bakuard.flashcards.validation.annotation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class NotBlankOrNullConstraintValidator  implements ConstraintValidator<NotBlankOrNull, String> {

    @Override
    public boolean isValid(String s, ConstraintValidatorContext constraintValidatorContext) {
        return s == null || !s.isBlank();
    }

}
