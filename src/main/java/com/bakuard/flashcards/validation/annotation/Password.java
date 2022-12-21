package com.bakuard.flashcards.validation.annotation;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.FIELD, ElementType.ANNOTATION_TYPE, ElementType.CONSTRUCTOR, ElementType.PARAMETER, ElementType.TYPE_USE})
@Constraint(validatedBy = PasswordConstraintValidator.class)
@Documented
public @interface Password {

    String message() default "{password.incorrectFormat}";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

}
