package com.bakuard.flashcards.model.word;

import com.bakuard.flashcards.config.TestConfig;
import com.bakuard.flashcards.validation.ValidatorUtil;
import org.assertj.core.api.Assertions;
import org.assertj.core.api.InstanceOfAssertFactories;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
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
class WordTest {

    @Autowired
    private ValidatorUtil validator;

    @Test
    @DisplayName("""
            create word:
             userId is null,
             value is null,
             note is blank,
             interpretations is null,
             transcriptions is null,
             translations is null,
             examples is null,
             repeatData is null
             => exception
            """)
    public void createWord1() {
        Assertions.
                assertThatExceptionOfType(ConstraintViolationException.class).
                isThrownBy(() -> Word.newBuilder(validator).
                        setUserId(null).
                        setValue(null).
                        setNote("    ").
                        setInterpretations(null).
                        setTranscriptions(null).
                        setTranslations(null).
                        setExamples(null).
                        setRepeatData(null).
                        build()).
                extracting(ex -> ex.getConstraintViolations().stream().
                                map(ConstraintViolation::getMessage).
                                collect(Collectors.toList()),
                        InstanceOfAssertFactories.collection(String.class)).
                containsExactlyInAnyOrder(
                        "Word.userId.notNull",
                        "Word.value.notBlank",
                        "Word.note.notBlankOrNull",
                        "Word.interpretations.notNull",
                        "Word.transcriptions.notNull",
                        "Word.translations.notNull",
                        "Word.examples.notNull");
    }

    @Test
    @DisplayName("""
            create word:
             userId (correct),
             value is blank,
             note is null (correct),
             interpretations contains null,
             transcriptions contains null,
             translations contains null,
             examples contains null,
             repeatData is null
             => exception
            """)
    public void createWord2() {
        Assertions.
                assertThatExceptionOfType(ConstraintViolationException.class).
                isThrownBy(() -> Word.newBuilder(validator).
                        setUserId(toUUID(1)).
                        setValue("   ").
                        setNote(null).
                        addInterpretation(null).
                        addInterpretation(new WordInterpretation("     ")).
                        addTranscription(null).
                        addTranscription(new WordTranscription("    ", "    ")).
                        addTranslation(null).
                        addTranslation(new WordTranslation("   ", "    ")).
                        addExample(null).
                        addExample(new WordExample("   ", "   ", "   ")).
                        setRepeatData(null).
                        build()).
                extracting(ex -> ex.getConstraintViolations().stream().
                                map(ConstraintViolation::getMessage).
                                collect(Collectors.toList()),
                        InstanceOfAssertFactories.collection(String.class)).
                containsExactlyInAnyOrder(
                        "Word.value.notBlank",
                        "Word.interpretations.notContainsNull",
                        "Word.transcriptions.notContainsNull",
                        "Word.translations.notContainsNull",
                        "Word.examples.notContainsNull");
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
             examples not contains unique items,
             repeatData is null
             => exception
            """)
    public void createWord3() {
        Assertions.
                assertThatExceptionOfType(ConstraintViolationException.class).
                isThrownBy(() -> Word.newBuilder(validator).
                        setUserId(toUUID(1)).
                        setValue("value").
                        setNote("note").
                        addInterpretation(new WordInterpretation("interpretation1")).
                        addInterpretation(new WordInterpretation("interpretation1")).
                        addTranscription(new WordTranscription("transcription1", "note1")).
                        addTranscription(new WordTranscription("transcription1", "note2")).
                        addTranslation(new WordTranslation("translation1", "note1")).
                        addTranslation(new WordTranslation("translation1", "note2")).
                        addExample(new WordExample("example1", "translate1", "note1")).
                        addExample(new WordExample("example1", "translate2", "note2")).
                        setRepeatData(null).
                        build()).
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
             - value is null,
             transcriptions contains items with:
             - value is null,
             - note is blank,
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
    public void createWord4() {
        Assertions.
                assertThatExceptionOfType(ConstraintViolationException.class).
                isThrownBy(() -> Word.newBuilder(validator).
                        setUserId(toUUID(1)).
                        setValue("value").
                        setNote("note").
                        addInterpretation(new WordInterpretation("interpretation1")).
                        addInterpretation(new WordInterpretation(null)).
                        addTranscription(new WordTranscription("transcription1", "note1")).
                        addTranscription(new WordTranscription(null, "     ")).
                        addTranslation(new WordTranslation("translation1", "note1")).
                        addTranslation(new WordTranslation(null, "     ")).
                        addExample(new WordExample("example1", "translate1", "note1")).
                        addExample(new WordExample(null, null, "    ")).
                        setRepeatData(null).
                        build()).
                extracting(ex -> ex.getConstraintViolations().stream().
                                map(ConstraintViolation::getMessage).
                                collect(Collectors.toList()),
                        InstanceOfAssertFactories.collection(String.class)).
                containsExactlyInAnyOrder(
                        "WordInterpretation.value.notBlank",
                        "WordTranscription.value.notBlank",
                        "WordTranscription.note.notBlankOrNull",
                        "WordTranslation.value.notBlank",
                        "WordTranslation.note.notBlankOrNull",
                        "WordExample.origin.notBlank",
                        "WordExample.translate.notBlank",
                        "WordExample.note.notBlankOrNull");
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
             - note is blank,
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
    public void createWord5() {
        Assertions.
                assertThatExceptionOfType(ConstraintViolationException.class).
                isThrownBy(() -> Word.newBuilder(validator).
                        setUserId(toUUID(1)).
                        setValue("value").
                        setNote("note").
                        addInterpretation(new WordInterpretation("interpretation1")).
                        addInterpretation(new WordInterpretation("     ")).
                        addTranscription(new WordTranscription("transcription1", "note1")).
                        addTranscription(new WordTranscription("     ", "     ")).
                        addTranslation(new WordTranslation("translation1", "note1")).
                        addTranslation(new WordTranslation("     ", "     ")).
                        addExample(new WordExample("example1", "translate1", "note1")).
                        addExample(new WordExample("    ", "     ", "    ")).
                        setRepeatData(null).
                        build()).
                extracting(ex -> ex.getConstraintViolations().stream().
                                map(ConstraintViolation::getMessage).
                                collect(Collectors.toList()),
                        InstanceOfAssertFactories.collection(String.class)).
                containsExactlyInAnyOrder(
                        "WordInterpretation.value.notBlank",
                        "WordTranscription.value.notBlank",
                        "WordTranscription.note.notBlankOrNull",
                        "WordTranslation.value.notBlank",
                        "WordTranslation.note.notBlankOrNull",
                        "WordExample.origin.notBlank",
                        "WordExample.translate.notBlank",
                        "WordExample.note.notBlankOrNull");
    }


    private UUID toUUID(int number) {
        return UUID.fromString("00000000-0000-0000-0000-" + String.format("%012d", number));
    }

}