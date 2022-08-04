package com.bakuard.flashcards.validation;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.FIELD, ElementType.ANNOTATION_TYPE, ElementType.CONSTRUCTOR, ElementType.PARAMETER, ElementType.TYPE_USE})
@Constraint(validatedBy = AllUniqueConstraintValidator.class)
public @interface AllUnique {

    String message() default "{AllUnique}";

    String nameOfGetterMethod();

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

}
