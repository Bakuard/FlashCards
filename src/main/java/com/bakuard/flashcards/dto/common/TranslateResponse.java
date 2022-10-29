package com.bakuard.flashcards.dto.common;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.Objects;

@Schema(description = "Один из переводов слова или устойчевого выражения.")
public class TranslateResponse {

    @Schema(description = """
            Значение перевода. <br/>
            Должно представлять собой не пустую строку.
            """)
    private String value;
    @Schema(description = """
            Примечание к переводу. <br/>
            Должно представлять собой не пустую строку или иметь значение null.
            """)
    private String note;
    @Schema(description = """
            Указывает - был ли добавлен данный перевод автоматически при дополнении слова или выражения.
             Если это так - возвращает true.
            """)
    private boolean isNew;

    public TranslateResponse() {

    }

    public String getValue() {
        return value;
    }

    public TranslateResponse setValue(String value) {
        this.value = value;
        return this;
    }

    public String getNote() {
        return note;
    }

    public TranslateResponse setNote(String note) {
        this.note = note;
        return this;
    }

    public boolean isNew() {
        return isNew;
    }

    public TranslateResponse setNew(boolean aNew) {
        isNew = aNew;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TranslateResponse that = (TranslateResponse) o;
        return isNew == that.isNew &&
                Objects.equals(value, that.value) &&
                Objects.equals(note, that.note);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value, note, isNew);
    }

    @Override
    public String toString() {
        return "TranslateResponse{" +
                "value='" + value + '\'' +
                ", note='" + note + '\'' +
                ", isNew=" + isNew +
                '}';
    }

}
