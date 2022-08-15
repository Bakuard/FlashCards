package com.bakuard.flashcards.model.expression;

import com.bakuard.flashcards.config.SpringConfig;
import com.bakuard.flashcards.model.RepeatData;
import com.bakuard.flashcards.model.word.*;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@SpringBootTest(classes = SpringConfig.class)
@TestPropertySource(locations = "classpath:application.properties")
class ExpressionTest {

    @Autowired
    private Validator validator;

    @Test
    @DisplayName("""
            create expression:
             userId is null,
             value is null,
             note is blank,
             there are interpretation duplicates,
             several translations is not valid,
             several examples is not valid,
             there are examples duplicates,
             repeat interval < 1,
             date of repeat < current day
             => fail validate
            """)
    public void createExpression1() {
        Expression expression = new Expression(
                toUUID(1),
                null,
                null,
                "      ",
                List.of(
                        new ExpressionInterpretation("value"),
                        new ExpressionInterpretation("value")
                ),
                List.of(
                        new ExpressionTranslation(null, "note"),
                        new ExpressionTranslation("value", "    ")
                ),
                List.of(
                        new ExpressionExample("value", null, null),
                        new ExpressionExample("value", "translate", "      "),
                        new ExpressionExample(null, "translate", "note")
                ),
                new RepeatData(0, LocalDate.of(1900, 1, 1))
        );

        Set<ConstraintViolation<Expression>> actual = validator.validate(expression);

        Assertions.assertThat(actual).
                extracting(ConstraintViolation::getMessage).
                containsExactlyInAnyOrder("Expression.userId.notNull",
                        "Expression.value.notBlank",
                        "Expression.note.notBlankOrNull",
                        "Expression.interpretations.allUnique",
                        "ExpressionTranslation.value.notBlank",
                        "ExpressionTranslation.note.notBlankOrNull",
                        "ExpressionExample.translate.notBlank",
                        "ExpressionExample.note.notBlankOrNull",
                        "ExpressionExample.origin.notBlank",
                        "Expression.examples.allUnique",
                        "RepeatData.interval.min",
                        "RepeatData.lastDateOfRepeat.present");
    }

    @Test
    @DisplayName("""
            create expression:
             all data is correct
             => successful validate
            """)
    public void createExpression2() {
        Expression expression = new Expression(
                toUUID(1),
                toUUID(1),
                "value",
                null,
                List.of(
                        new ExpressionInterpretation("value1"),
                        new ExpressionInterpretation("value2")
                ),
                List.of(
                        new ExpressionTranslation("value1", "note"),
                        new ExpressionTranslation("value2", null)
                ),
                List.of(
                        new ExpressionExample("value1", "translate", null),
                        new ExpressionExample("value2", "translate", "note"),
                        new ExpressionExample("value3", "translate", "note")
                ),
                new RepeatData(1, LocalDate.now())
        );

        Set<ConstraintViolation<Expression>> actual = validator.validate(expression);

        Assertions.assertThat(actual.isEmpty()).isTrue();
    }


    private UUID toUUID(int number) {
        return UUID.fromString("00000000-0000-0000-0000-" + String.format("%012d", number));
    }

}