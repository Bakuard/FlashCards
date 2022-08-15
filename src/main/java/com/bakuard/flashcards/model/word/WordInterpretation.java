package com.bakuard.flashcards.model.word;

import org.springframework.data.relational.core.mapping.Table;

import javax.validation.constraints.NotBlank;
import java.util.Objects;

@Table("words_interpretations")
public class WordInterpretation {

    @NotBlank(message = "WordInterpretation.value.notBlank")
    private String value;

    public WordInterpretation(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        WordInterpretation that = (WordInterpretation) o;
        return Objects.equals(value, that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    @Override
    public String toString() {
        return "WordInterpretation{" +
                "value='" + value + '\'' +
                '}';
    }

}
