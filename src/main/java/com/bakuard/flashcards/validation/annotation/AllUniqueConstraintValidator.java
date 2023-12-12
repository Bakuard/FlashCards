package com.bakuard.flashcards.validation.annotation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class AllUniqueConstraintValidator implements ConstraintValidator<AllUnique, List<?>> {

    private String methodName;

    @Override
    public void initialize(AllUnique constraintAnnotation) {
        this.methodName = constraintAnnotation.nameOfGetterMethod();
    }

    @Override
    public boolean isValid(List<?> objects, ConstraintValidatorContext constraintValidatorContext) {
        if(objects == null) return false;

        long countNotNull = objects.stream().filter(Objects::nonNull).count();
        long countUnique = objects.stream().
                filter(Objects::nonNull).
                map(obj -> {
                    Class<?> c = obj.getClass();
                    Method m = Arrays.stream(c.getDeclaredMethods()).
                            filter(method -> method.getName().equals(methodName) &&
                                    method.getParameterCount() == 0).
                            findAny().
                            orElseThrow();
                    try {
                        return m.invoke(obj);
                    } catch(InvocationTargetException | IllegalAccessException e) {
                        throw new RuntimeException(e);
                    }
                }).
                distinct().
                count();

        return countUnique == countNotNull;
    }

}
