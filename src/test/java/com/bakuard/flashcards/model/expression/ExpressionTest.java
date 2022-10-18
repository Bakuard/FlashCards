package com.bakuard.flashcards.model.expression;

import com.bakuard.flashcards.config.TestConfig;
import com.bakuard.flashcards.model.RepeatDataFromEnglish;
import com.bakuard.flashcards.model.RepeatDataFromNative;
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
import java.time.Clock;
import java.time.Duration;
import java.time.LocalDate;
import java.util.UUID;
import java.util.stream.Collectors;

@ExtendWith(SpringExtension.class)
@TestPropertySource(locations = "classpath:test.properties")
@Import(TestConfig.class)
class ExpressionTest {

    @Autowired
    private ValidatorUtil validator;
    @Autowired
    private Clock clock;

    @Test
    @DisplayName("""
            create expression:
             userId is null,
             value is null,
             note is blank,
             interpretations:
             - contains null,
             - interpretations value is null,
             translations:
             - value is null,
             - note is blank,
             examples contains items with:
             - origin is null,
             - translate is null,
             - note is blank
             repeatDataFromEnglish is null,
             repeatDataFromNative is null
             => exception
            """)
    public void createExpression1() {
        Assertions.
                assertThatExceptionOfType(ConstraintViolationException.class).
                isThrownBy(() -> Expression.newBuilder(validator).
                        setUserId(null).
                        setValue(null).
                        setNote("    ").
                        addInterpretation(null).
                        addInterpretation(new ExpressionInterpretation("     ")).
                        addTranslation(null).
                        addTranslation(new ExpressionTranslation(null, "    ")).
                        addExample(null).
                        addExample(new ExpressionExample(null, null, "   ")).
                        setRepeatData((RepeatDataFromEnglish) null).
                        setRepeatData((RepeatDataFromNative) null).
                        build()).
                extracting(ex -> ex.getConstraintViolations().stream().
                                map(ConstraintViolation::getMessage).
                                collect(Collectors.toList()),
                        InstanceOfAssertFactories.collection(String.class)).
                containsExactlyInAnyOrder(
                        "Expression.userId.notNull",
                        "Expression.value.notBlank",
                        "Expression.note.notBlankOrNull",

                        "Expression.interpretations.notContainsNull",
                        "Expression.translations.notContainsNull",
                        "Expression.examples.notContainsNull",

                        "ExpressionInterpretation.value.notBlank",
                        "ExpressionTranslation.value.notBlank",
                        "ExpressionTranslation.note.notBlankOrNull",
                        "ExpressionExample.origin.notBlank",
                        "ExpressionExample.translate.notBlank",
                        "ExpressionExample.note.notBlankOrNull",

                        "Expression.repeatDataFromEnglish.notNull",
                        "Expression.repeatDataFromNative.notNull"
                );
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
             repeatDataFromEnglish.interval < 1,
             repeatDataFromEnglish.lastDateOfRepeat is not present,
             repeatDataFromNative.interval < 1,
             repeatDataFromNative.lastDateOfRepeat is not present
             => exception
            """)
    public void createExpression2() {
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
                        setRepeatData(new RepeatDataFromEnglish(0, yesterday())).
                        setRepeatData(new RepeatDataFromNative(0, yesterday())).
                        build()).
                extracting(ex -> ex.getConstraintViolations().stream().
                                map(ConstraintViolation::getMessage).
                                collect(Collectors.toList()),
                        InstanceOfAssertFactories.collection(String.class)).
                containsExactlyInAnyOrder(
                        "Expression.interpretations.allUnique",
                        "Expression.translations.allUnique",
                        "Expression.examples.allUnique",
                        "RepeatDataFromEnglish.interval.min",
                        "RepeatDataFromEnglish.lastDateOfRepeat.present",
                        "RepeatDataFromNative.interval.min",
                        "RepeatDataFromNative.lastDateOfRepeat.present");
    }

    @Test
    @DisplayName("""
            create expression:
             userId (correct),
             value (correct),
             note (correct),
             interpretations contains items with:
             - value is blank,
             transcriptions contains items with:
             - value is blank,
             - note is null,
             translations contains items with:
             - value is blank,
             - note is null,
             examples contains items with:
             - origin is blank,
             - translate is blank,
             - note is null
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
                        addInterpretation(new ExpressionInterpretation("     ")).
                        addTranslation(new ExpressionTranslation("translation1", "note1")).
                        addTranslation(new ExpressionTranslation("     ", null)).
                        addExample(new ExpressionExample("example1", "translate1", "note1")).
                        addExample(new ExpressionExample("    ", "     ", null)).
                        setRepeatData(new RepeatDataFromEnglish(1, today())).
                        setRepeatData(new RepeatDataFromNative(1, today())).
                        build()).
                extracting(ex -> ex.getConstraintViolations().stream().
                                map(ConstraintViolation::getMessage).
                                collect(Collectors.toList()),
                        InstanceOfAssertFactories.collection(String.class)).
                containsExactlyInAnyOrder(
                        "ExpressionInterpretation.value.notBlank",
                        "ExpressionTranslation.value.notBlank",
                        "ExpressionExample.origin.notBlank",
                        "ExpressionExample.translate.notBlank");
    }

    @Test
    @DisplayName("""
            create expression:
             all data is correct
             => do throw nothing
            """)
    public void createExpression4() {
        Assertions.assertThatCode(() ->  Expression.newBuilder(validator).
                        setUserId(toUUID(1)).
                        setValue("value").
                        setNote("note").
                        addInterpretation(new ExpressionInterpretation("interpretation1")).
                        addTranslation(new ExpressionTranslation("translation1", "note1")).
                        addExample(new ExpressionExample("example1", "translate1", "note1")).
                        setRepeatData(new RepeatDataFromEnglish(1, today())).
                        setRepeatData(new RepeatDataFromNative(1, today())).
                        build()).
                doesNotThrowAnyException();
    }


    private UUID toUUID(int number) {
        return UUID.fromString("00000000-0000-0000-0000-" + String.format("%012d", number));
    }

    private LocalDate yesterday() {
        return LocalDate.now(Clock.offset(clock, Duration.ofDays(-1)));
    }

    private LocalDate today() {
        return LocalDate.now(clock);
    }

}