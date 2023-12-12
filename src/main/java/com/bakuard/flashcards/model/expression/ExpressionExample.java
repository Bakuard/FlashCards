package com.bakuard.flashcards.model.expression;

import com.bakuard.flashcards.validation.annotation.NotBlankOrNull;
import jakarta.validation.constraints.NotBlank;
import org.springframework.data.annotation.PersistenceCreator;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.util.Objects;

/**
 * Пример к устойчивому выражению.
 */
@Table("expressions_examples")
public class ExpressionExample {

    @Column("origin")
    @NotBlank(message = "ExpressionExample.origin.notBlank")
    private final String origin;
    @Column("translate")
    @NotBlank(message = "ExpressionExample.translate.notBlank")
    private final String translate;
    @Column("note")
    @NotBlankOrNull(message = "ExpressionExample.note.notBlankOrNull")
    private final String note;

    /**
     * Данный конструктор используется слоем доступа к данным для загрузки примера к устойчивому выражению.
     * @param origin пример к устойчивому выражению на английском языке.
     * @param translate перевод примера на родной язык пользователя.
     * @param note примечание к примеру добавляемое пользователем.
     */
    @PersistenceCreator
    public ExpressionExample(String origin, String translate, String note) {
        this.origin = origin;
        this.translate = translate;
        this.note = note;
    }

    /**
     * Возвращает пример к устойчивому выражению на английском языке.
     * @return пример к устойчивому выражению на английском языке.
     */
    public String getOrigin() {
        return origin;
    }

    /**
     * Возвращает перевод примера на родной язык пользователя.
     * @return перевод примера на родной язык пользователя.
     */
    public String getTranslate() {
        return translate;
    }

    /**
     * Возвращает примечание к примеру добавляемое пользователем.
     * @return примечание к примеру добавляемое пользователем.
     */
    public String getNote() {
        return note;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ExpressionExample that = (ExpressionExample) o;
        return Objects.equals(origin, that.origin) &&
                Objects.equals(translate, that.translate) &&
                Objects.equals(note, that.note);
    }

    @Override
    public int hashCode() {
        return Objects.hash(origin, translate, note);
    }

    @Override
    public String toString() {
        return "ExpressionExample{" +
                "origin='" + origin + '\'' +
                ", translate='" + translate + '\'' +
                ", note='" + note + '\'' +
                '}';
    }

}
