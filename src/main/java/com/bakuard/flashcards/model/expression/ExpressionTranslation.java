package com.bakuard.flashcards.model.expression;

import com.bakuard.flashcards.validation.NotBlankOrNull;
import org.springframework.data.annotation.PersistenceCreator;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import javax.validation.constraints.NotBlank;
import java.util.Objects;

@Table("expressions_translations")
public class ExpressionTranslation {

    @Column("value")
    @NotBlank(message = "ExpressionTranslation.value.notBlank")
    private String value;
    @Column("note")
    @NotBlankOrNull(message = "ExpressionTranslation.note.notBlankOrNull")
    private String note;

    @PersistenceCreator
    public ExpressionTranslation(String value, String note) {
        this.value = value;
        this.note = note;
    }

    public String getValue() {
        return value;
    }

    public String getNote() {
        return note;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ExpressionTranslation that = (ExpressionTranslation) o;
        return Objects.equals(value, that.value) &&
                Objects.equals(note, that.note);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value, note);
    }

    @Override
    public String toString() {
        return "ExpressionTranslation{" +
                "value='" + value + '\'' +
                ", note='" + note + '\'' +
                '}';
    }

}
