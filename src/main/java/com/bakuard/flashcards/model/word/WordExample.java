package com.bakuard.flashcards.model.word;

import com.bakuard.flashcards.validation.annotation.NotBlankOrNull;
import org.springframework.data.annotation.PersistenceCreator;
import org.springframework.data.annotation.Transient;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import javax.validation.constraints.NotBlank;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Пример к слову.
 */
@Table("words_examples")
public class WordExample {

    @Column("origin")
    @NotBlank(message = "WordExample.origin.notBlank")
    private String origin;
    @Column("translate")
    @NotBlankOrNull(message = "WordExample.translate.notBlankOrNull")
    private String translate;
    @Column("note")
    @NotBlankOrNull(message = "WordExample.note.notBlankOrNull")
    private String note;

    /**
     * Создает пример к слову.
     * @param origin текст примера к слову на английском языке.
     * @param translate перевод примера на родной язык пользователя.
     * @param note примечание к примеру добавляемое пользователем.
     */
    @PersistenceCreator
    public WordExample(String origin, String translate, String note) {
        this.origin = origin;
        this.translate = translate;
        this.note = note;
    }

    /**
     * Выполняет глубокое копирование переданного примера к слову.
     * @param other копируемый пример к слову.
     */
    public WordExample(WordExample other) {
        this.origin = other.origin;
        this.translate = other.translate;
        this.note = other.note;
    }

    /**
     * Возвращает текст примера к слову на английском языке.
     * @return текст примера к слову на английском языке.
     */
    public String getOrigin() {
        return origin;
    }

    /**
     * Возвращает перевод к примеру слова.
     * @return перевод к примеру слова.
     */
    public String getTranslate() {
        return translate;
    }

    /**
     * Возвращает примечание к примеру заданное пользователем.
     * @return примечание к примеру.
     */
    public String getNote() {
        return note;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        WordExample example = (WordExample) o;
        return Objects.equals(origin, example.origin) &&
                Objects.equals(translate, example.translate) &&
                Objects.equals(note, example.note);
    }

    @Override
    public int hashCode() {
        return Objects.hash(origin, translate, note);
    }

    @Override
    public String toString() {
        return "WordExample{" +
                "origin='" + origin + '\'' +
                ", translate='" + translate + '\'' +
                ", note='" + note + '\'' +
                '}';
    }

}
