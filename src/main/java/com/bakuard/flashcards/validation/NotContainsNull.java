package com.bakuard.flashcards.validation;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.FIELD, ElementType.ANNOTATION_TYPE, ElementType.CONSTRUCTOR, ElementType.PARAMETER, ElementType.TYPE_USE})
@Constraint(validatedBy = NotContainsNullValidator.class)
@Documented
public @interface NotContainsNull {

    String message() default "{NotContainsNull}";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

}
