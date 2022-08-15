package com.bakuard.flashcards.model.word;

import com.bakuard.flashcards.config.SpringConfig;
import com.bakuard.flashcards.model.RepeatData;
import com.google.common.collect.ImmutableList;
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
class WordTest {

    @Autowired
    private Validator validator;

    @Test
    @DisplayName("""
            create word:
             userId is null,
             value is null,
             note is blank,
             there are interpretation duplicates,
             several transcriptions is not valid,
             several translations is not valid,
             several examples is not valid,
             there are examples duplicates,
             repeat interval < 1,
             date of repeat < current day
             => fail validate
            """)
    public void createWord1() {
        Word word = new Word(
                toUUID(1),
                null,
                null,
                "      ",
                List.of(
                        new WordInterpretation("value"),
                        new WordInterpretation("value")
                ),
                List.of(
                        new WordTranscription(null, "note"),
                        new WordTranscription("value", "    ")
                ),
                List.of(
                        new WordTranslation(null, "note"),
                        new WordTranslation("value", "    ")
                ),
                List.of(
                        new WordExample("value", null, null),
                        new WordExample("value", "translate", "      "),
                        new WordExample(null, "translate", "note")
                ),
                new RepeatData(0, LocalDate.of(1900, 1, 1))
        );

        Set<ConstraintViolation<Word>> actual = validator.validate(word);

        Assertions.assertThat(actual).
                extracting(ConstraintViolation::getMessage).
                containsExactlyInAnyOrder("Word.userId.notNull",
                        "Word.value.notBlank",
                        "Word.note.notBlankOrNull",
                        "Word.interpretations.allUnique",
                        "WordTranscription.value.notBlank",
                        "WordTranscription.note.notBlankOrNull",
                        "WordTranslation.value.notBlank",
                        "WordTranslation.note.notBlankOrNull",
                        "WordExample.translate.notBlank",
                        "WordExample.note.notBlankOrNull",
                        "WordExample.origin.notBlank",
                        "Word.examples.allUnique",
                        "RepeatData.interval.min",
                        "RepeatData.lastDateOfRepeat.present");
    }

    @Test
    @DisplayName("""
            create word:
             all data is correct
             => successful validate
            """)
    public void createWord2() {
        Word word = new Word(
                toUUID(1),
                toUUID(1),
                "value",
                null,
                List.of(
                        new WordInterpretation("value1"),
                        new WordInterpretation("value2")
                ),
                List.of(
                        new WordTranscription("value1", "note"),
                        new WordTranscription("value2", null)
                ),
                List.of(
                        new WordTranslation("value1", "note"),
                        new WordTranslation("value2", null)
                ),
                List.of(
                        new WordExample("value1", "translate", null),
                        new WordExample("value2", "translate", "note"),
                        new WordExample("value3", "translate", "note")
                ),
                new RepeatData(1, LocalDate.now())
        );

        Set<ConstraintViolation<Word>> actual = validator.validate(word);

        Assertions.assertThat(actual.isEmpty()).isTrue();
    }


    private UUID toUUID(int number) {
        return UUID.fromString("00000000-0000-0000-0000-" + String.format("%012d", number));
    }

}