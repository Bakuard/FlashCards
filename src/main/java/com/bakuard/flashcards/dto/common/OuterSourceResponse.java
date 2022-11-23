package com.bakuard.flashcards.dto.common;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.Objects;

@Schema(description = """
        Данные об одном из внешних источников, из которого был получен перевод слова,
         транскрипция или толкование.
        """)
public class OuterSourceResponse {

    @Schema(description = "URL внешнего источника.")
    private String outerSourceUrl;
    @Schema(description = "Имя внешнего источника.")
    private String outerSourceName;

    public OuterSourceResponse() {

    }

    public String getOuterSourceUrl() {
        return outerSourceUrl;
    }

    public OuterSourceResponse setOuterSourceUrl(String outerSourceUrl) {
        this.outerSourceUrl = outerSourceUrl;
        return this;
    }

    public String getOuterSourceName() {
        return outerSourceName;
    }

    public OuterSourceResponse setOuterSourceName(String outerSourceName) {
        this.outerSourceName = outerSourceName;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        OuterSourceResponse that = (OuterSourceResponse) o;
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
