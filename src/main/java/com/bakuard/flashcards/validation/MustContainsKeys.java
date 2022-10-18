package com.bakuard.flashcards.validation;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.FIELD, ElementType.ANNOTATION_TYPE, ElementType.CONSTRUCTOR, ElementType.PARAMETER, ElementType.TYPE_USE})
@Constraint(validatedBy = MustContainsValidator.class)
public @interface MustContainsKeys {

    String message() default "{MustContains}";

    String[] keys();

    String nameOfContainsKeyMethod();

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

}
