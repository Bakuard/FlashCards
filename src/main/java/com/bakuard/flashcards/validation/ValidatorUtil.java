package com.bakuard.flashcards.validation;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.Validator;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class ValidatorUtil {

    private final Validator validator;

    public ValidatorUtil(Validator validator) {
        this.validator = validator;
    }

    public <T> Set<ConstraintViolation<T>> check(T entity, Class<?>... groups) {
        return Arrays.stream(groups).
                map(g -> validator.validate(entity, g)).
                filter(s -> !s.isEmpty()).
                findFirst().
                orElse(Set.of());
    }

    public <T> T assertAllEmpty(T entity, Set<ConstraintViolation<T>>... constraints) {
        Set<ConstraintViolation<T>> violations = new HashSet<>();
        Arrays.stream(constraints).forEach(violations::addAll);

        assertEmpty(violations);

        return entity;
    }

    public <T> T assertValid(T entity) {
        Set<ConstraintViolation<T>> violations = validator.validate(entity);

        assertEmpty(violations);

        return entity;
    }

    public <T> T[] assertValid(T... entities) {
        Set<ConstraintViolation<T>> violations = Arrays.stream(entities).
                map(entity -> validator.validate(entity)).
                reduce((a, b) -> {
                    Set<ConstraintViolation<T>> result = new HashSet<>(a);
                    result.addAll(b);
                    return result;
                }).
                orElse(Set.of());

        assertEmpty(violations);

        return entities;
    }


    private <T> void assertEmpty(Set<ConstraintViolation<T>> violations) {
        if(!violations.isEmpty()) {
            String reasons = violations.stream().
                    map(ConstraintViolation::getMessage).
                    reduce((a, b) -> String.join(", ", a, b)).
                    orElseThrow();
            throw new ConstraintViolationException("Validation fail: " + reasons, violations);
        }
    }

}
