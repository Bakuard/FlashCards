package com.bakuard.flashcards.dto.common;

import java.util.Objects;

public class ExampleRequestResponse {

    private String origin;
    private String translate;
    private String note;

    public ExampleRequestResponse() {
    }

    public String getOrigin() {
        return origin;
    }

    public ExampleRequestResponse setOrigin(String origin) {
        this.origin = origin;
        return this;
    }

    public String getTranslate() {
        return translate;
    }

    public ExampleRequestResponse setTranslate(String translate) {
        this.translate = translate;
        return this;
    }

    public String getNote() {
        return note;
    }

    public ExampleRequestResponse setNote(String note) {
        this.note = note;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ExampleRequestResponse that = (ExampleRequestResponse) o;
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
        return "ExampleRequestResponse{" +
                "origin='" + origin + '\'' +
                ", translate='" + translate + '\'' +
                ", note='" + note + '\'' +
                '}';
    }

}
