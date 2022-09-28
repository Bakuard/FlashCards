package com.bakuard.flashcards.model.expression;

import com.bakuard.flashcards.config.TestConfig;
import com.bakuard.flashcards.model.word.*;
import com.bakuard.flashcards.validation.ValidatorUtil;
import org.assertj.core.api.Assertions;
import org.assertj.core.api.InstanceOfAssertFactories;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import java.util.UUID;
import java.util.stream.Collectors;

@ExtendWith(SpringExtension.class)
@TestPropertySource(locations = "classpath:application.properties")
@Import(TestConfig.class)
class ExpressionTest {

    @Autowired
    private ValidatorUtil validator;

    @Test
    @DisplayName("""
            create expression:
             userId is null,
             value is null,
             note is blank,
             interpretations is null,
             translations is null,
             examples is null,
             repeatData is null
             => exception
            """)
    public void createExpression1() {
        Assertions.
                assertThatExceptionOfType(ConstraintViolationException.class).
                isThrownBy(() -> Expression.newBuilder(validator).
                        setUserId(null).
                        setValue(null).
                        setNote("    ").
                        setInterpretations(null).
                        setTranslations(null).
                        setExamples(null).
                        setRepeatData(null).
                        build()).
                extracting(ex -> ex.getConstraintViolations().stream().
                                map(ConstraintViolation::getMessage).
                                collect(Collectors.toList()),
                        InstanceOfAssertFactories.collection(String.class)).
                containsExactlyInAnyOrder(
                        "Expression.userId.notNull",
                        "Expression.value.notBlank",
                        "Expression.note.notBlankOrNull",
                        "Expression.interpretations.notNull",
                        "Expression.translations.notNull",
                        "Expression.examples.notNull");
    }

    @Test
    @DisplayName("""
            create expression:
             userId (correct),
             value is blank,
             note is null (correct),
             interpretations contains null,
             translations contains null,
             examples contains null,
             repeatData is null
             => exception
            """)
    public void createExpression2() {
        Assertions.
                assertThatExceptionOfType(ConstraintViolationException.class).
                isThrownBy(() -> Expression.newBuilder(validator).
                        setUserId(toUUID(1)).
                        setValue("   ").
                        setNote(null).
                        addInterpretation(null).
                        addInterpretation(new ExpressionInterpretation("     ")).
                        addTranslation(null).
                        addTranslation(new ExpressionTranslation("   ", "    ")).
                        addExample(null).
                        addExample(new ExpressionExample("   ", "   ", "   ")).
                        setRepeatData(null).
                        build()).
                extracting(ex -> ex.getConstraintViolations().stream().
                                map(ConstraintViolation::getMessage).
                                collect(Collectors.toList()),
                        InstanceOfAssertFactories.collection(String.class)).
                containsExactlyInAnyOrder(
                        "Expression.value.notBlank",
                        "Expression.interpretations.notContainsNull",
                        "Expression.translations.notContainsNull",
                        "Expression.examples.notContainsNull");
    }

    @Test
    @DisplayName("""
            create expression:
             userId (correct),
             value (correct),
             note (correct),
             interpretations not contains unique items,
             translations not contains unique items,
             examples not contains unique items,
             repeatData is null
             => exception
            """)
    public void createExpression3() {
        Assertions.
                assertThatExceptionOfType(ConstraintViolationException.class).
                isThrownBy(() -> Expression.newBuilder(validator).
                        setUserId(toUUID(1)).
                        setValue("value").
                        setNote("note").
                        addInterpretation(new ExpressionInterpretation("interpretation1")).
                        addInterpretation(new ExpressionInterpretation("interpretation1")).
                        addTranslation(new ExpressionTranslation("translation1", "note1")).
                        addTranslation(new ExpressionTranslation("translation1", "note2")).
                        addExample(new ExpressionExample("example1", "translate1", "note1")).
                        addExample(new ExpressionExample("example1", "translate2", "note2")).
                        setRepeatData(null).
                        build()).
                extracting(ex -> ex.getConstraintViolations().stream().
                                map(ConstraintViolation::getMessage).
                                collect(Collectors.toList()),
                        InstanceOfAssertFactories.collection(String.class)).
                containsExactlyInAnyOrder(
                        "Expression.interpretations.allUnique",
                        "Expression.translations.allUnique",
                        "Expression.examples.allUnique");
    }

    @Test
    @DisplayName("""
            create expression:
             userId (correct),
             value (correct),
             note (correct),
             interpretations contains items with:
             - value is null,
             translations contains items with:
             - value is null,
             - note is blank,
             examples contains items with:
             - origin is null,
             - translate is null,
             - note is blank,
             repeatData is null
             => exception
            """)
    public void createExpression4() {
        Assertions.
                assertThatExceptionOfType(ConstraintViolationException.class).
                isThrownBy(() -> Expression.newBuilder(validator).
                        setUserId(toUUID(1)).
                        setValue("value").
                        setNote("note").
                        addInterpretation(new ExpressionInterpretation("interpretation1")).
                        addInterpretation(new ExpressionInterpretation(null)).
                        addTranslation(new ExpressionTranslation("translation1", "note1")).
                        addTranslation(new ExpressionTranslation(null, "     ")).
                        addExample(new ExpressionExample("example1", "translate1", "note1")).
                        addExample(new ExpressionExample(null, null, "    ")).
                        setRepeatData(null).
                        build()).
                extracting(ex -> ex.getConstraintViolations().stream().
                                map(ConstraintViolation::getMessage).
                                collect(Collectors.toList()),
                        InstanceOfAssertFactories.collection(String.class)).
                containsExactlyInAnyOrder(
                        "ExpressionInterpretation.value.notBlank",
                        "ExpressionTranslation.value.notBlank",
                        "ExpressionTranslation.note.notBlankOrNull",
                        "ExpressionExample.origin.notBlank",
                        "ExpressionExample.translate.notBlank",
                        "ExpressionExample.note.notBlankOrNull");
    }

    @Test
    @DisplayName("""
            create expression:
             userId (correct),
             value (correct),
             note (correct),
             interpretations contains items with:
             - value is blank,
             translations contains items with:
             - value is blank,
             - note is blank,
             examples contains items with:
             - origin is null,
             - translate is null,
             - note is blank,
             repeatData is null
             => exception
            """)
    public void createExpression5() {
        Assertions.
                assertThatExceptionOfType(ConstraintViolationException.class).
                isThrownBy(() -> Expression.newBuilder(validator).
                        setUserId(toUUID(1)).
                        setValue("value").
                        setNote("note").
                        addInterpretation(new ExpressionInterpretation("interpretation1")).
                        addInterpretation(new ExpressionInterpretation("     ")).
                        addTranslation(new ExpressionTranslation("translation1", "note1")).
                        addTranslation(new ExpressionTranslation("     ", "     ")).
                        addExample(new ExpressionExample("example1", "translate1", "note1")).
                        addExample(new ExpressionExample("    ", "     ", "    ")).
                        setRepeatData(null).
                        build()).
                extracting(ex -> ex.getConstraintViolations().stream().
                                map(ConstraintViolation::getMessage).
                                collect(Collectors.toList()),
                        InstanceOfAssertFactories.collection(String.class)).
                containsExactlyInAnyOrder(
                        "ExpressionInterpretation.value.notBlank",
                        "ExpressionTranslation.value.notBlank",
                        "ExpressionTranslation.note.notBlankOrNull",
                        "ExpressionExample.origin.notBlank",
                        "ExpressionExample.translate.notBlank",
                        "ExpressionExample.note.notBlankOrNull");
    }


    private UUID toUUID(int number) {
        return UUID.fromString("00000000-0000-0000-0000-" + String.format("%012d", number));
    }

}