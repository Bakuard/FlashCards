package com.bakuard.flashcards.dto.common;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.Objects;

@Schema(description = "Транскрипция слова.")
public class TranscriptionResponse {

    @Schema(description = "Значение транскрипции слова.")
    private String value;
    @Schema(description = "Примечание к транскрипции.")
    private String note;

    public TranscriptionResponse() {

    }

    public String getValue() {
        return value;
    }

    public TranscriptionResponse setValue(String value) {
        this.value = value;
        return this;
    }

    public String getNote() {
        return note;
    }

    public TranscriptionResponse setNote(String note) {
        this.note = note;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TranscriptionResponse that = (TranscriptionResponse) o;
        return Objects.equals(value, that.value) &&
                Objects.equals(note, that.note);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value, note);
    }

    @Override
    public String toString() {
        return "TranscriptionResponse{" +
                "value='" + value + '\'' +
                ", note='" + note + '\'' +
                '}';
    }

}
