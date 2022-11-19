package com.bakuard.flashcards.dto.common;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Schema(description = "Пример использования слова или устойчевого выражения.")
public class ExampleResponse {

    @Schema(description = "Пример на английском языке.")
    private String origin;
    @Schema(description = "Перевод примера на родной язык пользователя.")
    private String translate;
    @Schema(description = "Примечание к примеру.")
    private String note;
    @Schema(description = "Данные всех внешних источников из которых получен перевод для данного примера.")
    private List<SourceInfoResponse> sourceInfo;

    public ExampleResponse() {
        sourceInfo = new ArrayList<>();
    }

    public String getOrigin() {
        return origin;
    }

    public ExampleResponse setOrigin(String origin) {
        this.origin = origin;
        return this;
    }

    public String getTranslate() {
        return translate;
    }

    public ExampleResponse setTranslate(String translate) {
        this.translate = translate;
        return this;
    }

    public String getNote() {
        return note;
    }

    public ExampleResponse setNote(String note) {
        this.note = note;
        return this;
    }

    public List<SourceInfoResponse> getSourceInfo() {
        return sourceInfo;
    }

    public ExampleResponse setSourceInfo(List<SourceInfoResponse> sourceInfo) {
        this.sourceInfo = sourceInfo;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ExampleResponse that = (ExampleResponse) o;
        return Objects.equals(origin, that.origin) &&
                Objects.equals(translate, that.translate) &&
                Objects.equals(note, that.note) &&
                Objects.equals(sourceInfo, that.sourceInfo);
    }

    @Override
    public int hashCode() {
        return Objects.hash(origin, translate, note, sourceInfo);
    }

    @Override
    public String toString() {
        return "ExampleResponse{" +
                "origin='" + origin + '\'' +
                ", translate='" + translate + '\'' +
                ", note='" + note + '\'' +
                ", sourceInfo=" + sourceInfo +
                '}';
    }

}
