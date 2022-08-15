package com.bakuard.flashcards.validation;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class NotBlankOrNullConstraintValidator  implements ConstraintValidator<NotBlankOrNull, String> {

    @Override
    public boolean isValid(String s, ConstraintValidatorContext constraintValidatorContext) {
        return s == null || !s.isBlank();
    }

}
