package com.bakuard.flashcards.dto.common;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;
import java.util.Objects;

@Schema(description = "Один из переводов слова полученный из внешнего сервиса.")
public class SupplementedTranslateResponse {

    @Schema(description = "Значение перевода.")
    private String value;
    @Schema(description = " Примечание к переводу.")
    private String note;
    @Schema(description = "Данные всех внешних источников из которых получен данный перевод.")
    private List<OuterSourceResponse> outerSource;

    public SupplementedTranslateResponse() {

    }

    public String getValue() {
        return value;
    }

    public SupplementedTranslateResponse setValue(String value) {
        this.value = value;
        return this;
    }

    public String getNote() {
        return note;
    }

    public SupplementedTranslateResponse setNote(String note) {
        this.note = note;
        return this;
    }

    public List<OuterSourceResponse> getOuterSource() {
        return outerSource;
    }

    public SupplementedTranslateResponse setOuterSource(List<OuterSourceResponse> outerSource) {
        this.outerSource = outerSource;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SupplementedTranslateResponse that = (SupplementedTranslateResponse) o;
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
        return "SupplementedTranslateResponse{" +
                "value='" + value + '\'' +
                ", note='" + note + '\'' +
                ", outerSource=" + outerSource +
                '}';
    }

}
