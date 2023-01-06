package com.bakuard.flashcards.model.word;

import org.springframework.data.annotation.PersistenceCreator;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import javax.validation.constraints.NotBlank;
import java.util.Objects;

/**
 * Интерпретация к слову.
 */
@Table("words_interpretations")
public class WordInterpretation {

    @Column("value")
    @NotBlank(message = "WordInterpretation.value.notBlank")
    private String value;

    /**
     * Создает интерпретацию к слову.
     * @param value значение интерпретации к слову.
     */
    @PersistenceCreator
    public WordInterpretation(String value) {
        this.value = value;
    }

    /**
     * Выполняет глубокое копирование интерпретации к слову.
     * @param other копируемая интерпретация к слову.
     */
    public WordInterpretation(WordInterpretation other) {
        this.value = other.value;
    }

    /**
     * Возвращает значение интерпретации к слову.
     * @return значение интерпретации к слову.
     */
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
