package com.bakuard.flashcards.dto.common;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.Objects;

@Schema(description = """
        Данные об одном из внешних источников, из которого был получен перевод примера к слову.
        """)
public class ExampleOuterSourceResponse {

    @Schema(description = "URL внешнего источника.")
    private String outerSourceUrl;
    @Schema(description = "Имя внешнего источника.")
    private String outerSourceName;
    @Schema(description = "Перевод примера полученный из данного внешнего источника.")
    private String exampleTranslate;

    public ExampleOuterSourceResponse() {

    }

    public String getOuterSourceUrl() {
        return outerSourceUrl;
    }

    public ExampleOuterSourceResponse setOuterSourceUrl(String outerSourceUrl) {
        this.outerSourceUrl = outerSourceUrl;
        return this;
    }

    public String getOuterSourceName() {
        return outerSourceName;
    }

    public ExampleOuterSourceResponse setOuterSourceName(String outerSourceName) {
        this.outerSourceName = outerSourceName;
        return this;
    }

    public String getExampleTranslate() {
        return exampleTranslate;
    }

    public ExampleOuterSourceResponse setExampleTranslate(String exampleTranslate) {
        this.exampleTranslate = exampleTranslate;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ExampleOuterSourceResponse that = (ExampleOuterSourceResponse) o;
        return Objects.equals(outerSourceUrl, that.outerSourceUrl) &&
                Objects.equals(outerSourceName, that.outerSourceName) &&
                Objects.equals(exampleTranslate, that.exampleTranslate);
    }

    @Override
    public int hashCode() {
        return Objects.hash(outerSourceUrl, outerSourceName, exampleTranslate);
    }

    @Override
    public String toString() {
        return "ExampleOuterSourceResponse{" +
                "outerSourceUrl='" + outerSourceUrl + '\'' +
                ", outerSourceName='" + outerSourceName + '\'' +
                ", exampleTranslate='" + exampleTranslate + '\'' +
                '}';
    }

}
