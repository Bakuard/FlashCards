package com.bakuard.flashcards.validation;

import org.springframework.beans.factory.annotation.Autowired;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.time.Clock;
import java.time.LocalDate;

public class PresentConstraintValidator implements ConstraintValidator<Present, LocalDate> {

    private Clock clock;

    @Autowired
    public PresentConstraintValidator(Clock clock) {
        this.clock = clock;
    }

    @Override
    public boolean isValid(LocalDate date, ConstraintValidatorContext constraintValidatorContext) {
        return date != null && LocalDate.now(clock).isEqual(date);
    }

}
