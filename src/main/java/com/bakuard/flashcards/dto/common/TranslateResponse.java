package com.bakuard.flashcards.dto.common;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.ArrayList;
import java.util.List;
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
    @Schema(description = "Данные всех внешних источников из которых получен данный перевод.")
    private List<OuterSourceResponse> outerSource;

    public TranslateResponse() {
        outerSource = new ArrayList<>();
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

    public List<OuterSourceResponse> getOuterSource() {
        return outerSource;
    }

    public TranslateResponse setOuterSource(List<OuterSourceResponse> outerSource) {
        this.outerSource = outerSource;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TranslateResponse that = (TranslateResponse) o;
        return Objects.equals(value, that.value) &&
                Objects.equals(note, that.note) &&
                Objects.equals(outerSource, that.outerSource);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value, note, outerSource);
    }

    @Override
    public String toString() {
        return "TranslateResponse{" +
                "value='" + value + '\'' +
                ", note='" + note + '\'' +
                ", sourceInfo=" + outerSource +
                '}';
    }

}
