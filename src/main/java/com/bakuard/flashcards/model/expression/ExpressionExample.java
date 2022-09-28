package com.bakuard.flashcards.model.expression;

import com.bakuard.flashcards.validation.NotBlankOrNull;
import org.springframework.data.annotation.PersistenceCreator;
import org.springframework.data.relational.core.mapping.Table;

import javax.validation.constraints.NotBlank;
import java.util.Objects;

@Table("expressions_examples")
public class ExpressionExample {

    @NotBlank(message = "ExpressionExample.origin.notBlank")
    private String origin;
    @NotBlank(message = "ExpressionExample.translate.notBlank")
    private String translate;
    @NotBlankOrNull(message = "ExpressionExample.note.notBlankOrNull")
    private String note;

    @PersistenceCreator
    public ExpressionExample(String origin, String translate, String note) {
        this.origin = origin;
        this.translate = translate;
        this.note = note;
    }

    public String getOrigin() {
        return origin;
    }

    public String getTranslate() {
        return translate;
    }

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
