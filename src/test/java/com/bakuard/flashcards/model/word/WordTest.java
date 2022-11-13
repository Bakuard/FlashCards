package com.bakuard.flashcards.model.word;

import com.bakuard.flashcards.config.TestConfig;
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
import java.util.UUID;
import java.util.stream.Collectors;

@ExtendWith(SpringExtension.class)
@TestPropertySource(locations = "classpath:test.properties")
@Import(TestConfig.class)
class WordTest {

    @Autowired
    private ValidatorUtil validator;
    @Autowired
    private Clock clock;

    @Test
    @DisplayName("""
            create word:
             userId is null,
             value is null,
             note is blank,
             interpretations:
             - contains null,
             - interpretations value is null,
             transcriptions:
             - contains null,
             - transcription value is null,
             - transcription note is blank
             translations:
             - value is null,
             - note is blank,
             examples contains items with:
             - origin is null,
             - translate is null,
             - note is blank
             repeat intervals:
             - lowest interval < 1
             => exception
            """)
    public void createWord1() {
        Assertions.
                assertThatExceptionOfType(ConstraintViolationException.class).
                isThrownBy(() -> validator.assertValid(
                        new Word(null, 0, 0, clock).
                        setValue(null).
                        setNote("    ").
                        addInterpretation(null).
                        addInterpretation(new WordInterpretation("     ")).
                        addTranscription(null).
                        addTranscription(new WordTranscription(null, "    ")).
                        addTranslation(null).
                        addTranslation(new WordTranslation(null, "    ")).
                        addExample(null).
                        addExample(new WordExample(null, null, "   "))
                )).
                extracting(ex -> ex.getConstraintViolations().stream().
                                map(ConstraintViolation::getMessage).
                                collect(Collectors.toList()),
                        InstanceOfAssertFactories.collection(String.class)).
                containsExactlyInAnyOrder(
                        "Word.userId.notNull",
                        "Word.value.notBlank",
                        "Word.note.notBlankOrNull",

                        "Word.interpretations.notContainsNull",
                        "Word.transcriptions.notContainsNull",
                        "Word.translations.notContainsNull",
                        "Word.examples.notContainsNull",

                        "WordInterpretation.value.notBlank",
                        "WordTranscription.value.notBlank",
                        "WordTranscription.note.notBlankOrNull",
                        "WordTranslation.value.notBlank",
                        "WordTranslation.note.notBlankOrNull",
                        "WordExample.origin.notBlank",
                        "WordExample.translate.notBlank",
                        "WordExample.note.notBlankOrNull",

                        "RepeatDataFromEnglish.interval.min",
                        "RepeatDataFromNative.interval.min"
                );
    }

    @Test
    @DisplayName("""
            create word:
             userId (correct),
             value (correct),
             note (correct),
             interpretations not contains unique items,
             transcriptions not contains unique items,
             translations not contains unique items,
             examples not contains unique items
             => exception
            """)
    public void createWord2() {
        Assertions.
                assertThatExceptionOfType(ConstraintViolationException.class).
                isThrownBy(() -> validator.assertValid(
                        new Word(toUUID(1), 1, 1, clock).
                        setValue("value").
                        setNote("note").
                        addInterpretation(new WordInterpretation("interpretation1")).
                        addInterpretation(new WordInterpretation("interpretation1")).
                        addTranscription(new WordTranscription("transcription1", "note1")).
                        addTranscription(new WordTranscription("transcription1", "note2")).
                        addTranslation(new WordTranslation("translation1", "note1")).
                        addTranslation(new WordTranslation("translation1", "note2")).
                        addExample(new WordExample("example1", "translate1", "note1")).
                        addExample(new WordExample("example1", "translate2", "note2"))
                )).
                extracting(ex -> ex.getConstraintViolations().stream().
                                map(ConstraintViolation::getMessage).
                                collect(Collectors.toList()),
                        InstanceOfAssertFactories.collection(String.class)).
                containsExactlyInAnyOrder(
                        "Word.interpretations.allUnique",
                        "Word.transcriptions.allUnique",
                        "Word.translations.allUnique",
                        "Word.examples.allUnique");
    }

    @Test
    @DisplayName("""
            create word:
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
    public void createWord3() {
        Assertions.
                assertThatExceptionOfType(ConstraintViolationException.class).
                isThrownBy(() -> validator.assertValid(
                        new Word(toUUID(1), 1, 1, clock).
                        setValue("value").
                        setNote("note").
                        addInterpretation(new WordInterpretation("interpretation1")).
                        addInterpretation(new WordInterpretation("     ")).
                        addTranscription(new WordTranscription("transcription1", "note1")).
                        addTranscription(new WordTranscription("     ", null)).
                        addTranslation(new WordTranslation("translation1", "note1")).
                        addTranslation(new WordTranslation("     ", null)).
                        addExample(new WordExample("example1", "translate1", "note1")).
                        addExample(new WordExample("    ", "     ", null))
                )).
                extracting(ex -> ex.getConstraintViolations().stream().
                                map(ConstraintViolation::getMessage).
                                collect(Collectors.toList()),
                        InstanceOfAssertFactories.collection(String.class)).
                containsExactlyInAnyOrder(
                        "WordInterpretation.value.notBlank",
                        "WordTranscription.value.notBlank",
                        "WordTranslation.value.notBlank",
                        "WordExample.origin.notBlank",
                        "WordExample.translate.notBlank");
    }

    @Test
    @DisplayName("""
            create word:
             all data is correct
             => do throw nothing
            """)
    public void createWord4() {
        Assertions.assertThatCode(() ->  validator.assertValid(
                new Word(toUUID(1), 1, 1, clock).
                        setValue("value").
                        setNote("note").
                        addInterpretation(new WordInterpretation("interpretation1")).
                        addTranscription(new WordTranscription("transcription1", "note1")).
                        addTranslation(new WordTranslation("translation1", "note1")).
                        addExample(new WordExample("example1", "translate1", "note1"))
                )).
                doesNotThrowAnyException();
    }


    private UUID toUUID(int number) {
        return UUID.fromString("00000000-0000-0000-0000-" + String.format("%012d", number));
    }

}