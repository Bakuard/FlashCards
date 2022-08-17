package com.bakuard.flashcards.dto.common;

import java.util.Objects;

public class TranscriptionRequestResponse {

    private String value;
    private String note;

    public TranscriptionRequestResponse() {
    }

    public String getValue() {
        return value;
    }

    public TranscriptionRequestResponse setValue(String value) {
        this.value = value;
        return this;
    }

    public String getNote() {
        return note;
    }

    public TranscriptionRequestResponse setNote(String note) {
        this.note = note;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TranscriptionRequestResponse that = (TranscriptionRequestResponse) o;
        return Objects.equals(value, that.value) && Objects.equals(note, that.note);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value, note);
    }

    @Override
    public String toString() {
        return "TranscriptionRequestResponse{" +
                "value='" + value + '\'' +
                ", note='" + note + '\'' +
                '}';
    }

}
