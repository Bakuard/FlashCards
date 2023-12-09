package com.bakuard.flashcards.validation.annotation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.List;
import java.util.Objects;

public class NotContainsNullValidator implements ConstraintValidator<NotContainsNull, List<?>> {

    @Override
    public boolean isValid(List<?> objects, ConstraintValidatorContext constraintValidatorContext) {
        return objects.stream().allMatch(Objects::nonNull);
    }

}