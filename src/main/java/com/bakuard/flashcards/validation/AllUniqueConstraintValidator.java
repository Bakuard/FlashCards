package com.bakuard.flashcards.validation;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

public class AllUniqueConstraintValidator implements ConstraintValidator<AllUnique, List<?>> {

    private String methodName;

    @Override
    public void initialize(AllUnique constraintAnnotation) {
        this.methodName = constraintAnnotation.nameOfGetterMethod();
    }

    @Override
    public boolean isValid(List<?> objects, ConstraintValidatorContext constraintValidatorContext) {
        if(objects == null) return false;

        long countUnique = objects.stream().
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

        return countUnique == objects.size();
    }

}
