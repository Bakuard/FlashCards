package com.bakuard.flashcards.dto.common;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.Objects;

@Schema(description = "Один из переводов слова или устойчевого выражения.")
public class TranslateRequestResponse {

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

    public TranslateRequestResponse() {
    }

    public String getValue() {
        return value;
    }

    public TranslateRequestResponse setValue(String value) {
        this.value = value;
        return this;
    }

    public String getNote() {
        return note;
    }

    public TranslateRequestResponse setNote(String note) {
        this.note = note;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TranslateRequestResponse that = (TranslateRequestResponse) o;
        return Objects.equals(value, that.value) && Objects.equals(note, that.note);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value, note);
    }

    @Override
    public String toString() {
        return "TranslateRequestResponse{" +
                "value='" + value + '\'' +
                ", note='" + note + '\'' +
                '}';
    }

}
