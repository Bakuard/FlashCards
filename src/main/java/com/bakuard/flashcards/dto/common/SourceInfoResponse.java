package com.bakuard.flashcards.dto.common;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.Objects;

@Schema(description = """
        Данные об одном из внешних источников, из которого был получен перевод слова,
         транскрипция, толкование или перевод примера к слову.
        """)
public class SourceInfoResponse {

    @Schema(description = "URL внешнего источника.")
    private String outerSourceUrl;
    @Schema(description = "Имя внешнего источника.")
    private String outerSourceName;

    public SourceInfoResponse() {

    }

    public String getOuterSourceUrl() {
        return outerSourceUrl;
    }

    public SourceInfoResponse setOuterSourceUrl(String outerSourceUrl) {
        this.outerSourceUrl = outerSourceUrl;
        return this;
    }

    public String getOuterSourceName() {
        return outerSourceName;
    }

    public SourceInfoResponse setOuterSourceName(String outerSourceName) {
        this.outerSourceName = outerSourceName;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SourceInfoResponse that = (SourceInfoResponse) o;
        return Objects.equals(outerSourceUrl, that.outerSourceUrl) &&
                Objects.equals(outerSourceName, that.outerSourceName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(outerSourceUrl, outerSourceName);
    }

    @Override
    public String toString() {
        return "SourceInfoResponse{" +
                "outerSourceUrl='" + outerSourceUrl + '\'' +
                ", outerSourceName='" + outerSourceName + '\'' +
                '}';
    }

}
