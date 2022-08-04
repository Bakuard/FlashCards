package com.bakuard.flashcards.validation;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.time.LocalDate;

public class PresentConstraintValidator implements ConstraintValidator<Present, LocalDate> {

    @Override
    public boolean isValid(LocalDate date, ConstraintValidatorContext constraintValidatorContext) {
        return date != null && LocalDate.now().isEqual(date);
    }

}
