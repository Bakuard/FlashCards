package com.bakuard.flashcards.validation;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.Validator;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class ValidatorUtil {

    private Validator validator;

    public ValidatorUtil(Validator validator) {
        this.validator = validator;
    }

    public <T> T check(T entity) {
        Set<ConstraintViolation<T>> violations = validator.validate(entity);

        if(!violations.isEmpty()) {
            String reasons = violations.stream().
                    map(Object::toString).
                    reduce((a, b) -> String.join(", ", a, b)).
                    orElseThrow();
            throw new ConstraintViolationException("Validation fail: " + reasons, violations);
        }

        return entity;
    }

    public <T> T[] check(T... entities) {
        Set<ConstraintViolation<T>> violations = Arrays.stream(entities).
                map(entity -> validator.validate(entity)).
                reduce((a, b) -> {
                    Set<ConstraintViolation<T>> result = new HashSet<>(a);
                    result.addAll(b);
                    return result;
                }).
                orElse(Set.of());

        if(!violations.isEmpty()) {
            String reasons = violations.stream().
                    map(Object::toString).
                    reduce((a, b) -> String.join(", ", a, b)).
                    orElseThrow();
            throw new ConstraintViolationException("Validation fail: " + reasons, violations);
        }

        return entities;
    }

}
