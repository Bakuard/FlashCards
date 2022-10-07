package com.bakuard.flashcards.validation;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

public class MustContainsValidator implements ConstraintValidator<MustContainsKeys, Object> {

    private String methodName;
    private String[] keys;

    @Override
    public void initialize(MustContainsKeys constraintAnnotation) {
        this.keys = constraintAnnotation.keys();
        this.methodName = constraintAnnotation.nameOfContainsKeyMethod();
    }

    @Override
    public boolean isValid(Object container, ConstraintValidatorContext constraintValidatorContext) {
        if(container == null) return false;

        Method m = Arrays.stream(container.getClass().getDeclaredMethods()).
                filter(method -> method.getName().equals(methodName)).
                findAny().
                orElseThrow();

        try {
            return (boolean)m.invoke(container, keys);
        } catch(InvocationTargetException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

}
