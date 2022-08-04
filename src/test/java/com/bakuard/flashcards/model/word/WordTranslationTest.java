package com.bakuard.flashcards.model.word;

import com.bakuard.flashcards.config.SpringConfig;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import java.util.Set;

@SpringBootTest(classes = SpringConfig.class)
@TestPropertySource(locations = "classpath:application.properties")
class WordTranslationTest {

    @Autowired
    private Validator validator;

    @Test
    @DisplayName("""
            WordTranslation(value, note):
             note is null
             => successful validation
            """)
    public void constructor1() {
        WordTranslation wordTranslation = new WordTranslation("value", null);

        Set<ConstraintViolation<WordTranslation>> actual = validator.validate(wordTranslation);

        Assertions.assertThat(actual.isEmpty()).isTrue();
    }

    @Test
    @DisplayName("""
            WordTranslation(value, note):
             note is blank
             => fail validate
            """)
    public void constructor2() {
        WordTranslation wordTranslation = new WordTranslation("value", "       ");

        Set<ConstraintViolation<WordTranslation>> actual = validator.validate(wordTranslation);

        Assertions.assertThat(actual).
                extracting(ConstraintViolation::getMessage).
                containsExactlyInAnyOrder("WordTranslation.note.notBlankOrNull");
    }

}