package com.bakuard.flashcards.dto.common;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.Objects;

@Schema(description = "Один из переводов слова или устойчивого выражения.")
public class TranslateResponse {

    @Schema(description = "Значение перевода.")
    private String value;
    @Schema(description = "Примечание к переводу.")
    private String note;

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TranslateResponse that = (TranslateResponse) o;
        return Objects.equals(value, that.value) &&
                Objects.equals(note, that.note);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value, note);
    }

    @Override
    public String toString() {
        return "TranslateResponse{" +
                "value='" + value + '\'' +
                ", note='" + note + '\'' +
                '}';
    }

}
