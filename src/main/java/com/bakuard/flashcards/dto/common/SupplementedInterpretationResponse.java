package com.bakuard.flashcards.dto.common;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;
import java.util.Objects;

@Schema(description = "Толкование слова полученное из внешнего сервиса")
public class SupplementedInterpretationResponse {

    @Schema(description = "Подробное описание значения и употребления слова.")
    private String value;
    @Schema(description = "Данные всех внешних источников из которых получено данное толкование.")
    private List<OuterSourceResponse> outerSource;

    public SupplementedInterpretationResponse() {

    }

    public String getValue() {
        return value;
    }

    public SupplementedInterpretationResponse setValue(String value) {
        this.value = value;
        return this;
    }

    public List<OuterSourceResponse> getOuterSource() {
        return outerSource;
    }

    public SupplementedInterpretationResponse setOuterSource(List<OuterSourceResponse> outerSource) {
        this.outerSource = outerSource;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SupplementedInterpretationResponse that = (SupplementedInterpretationResponse) o;
        return Objects.equals(value, that.value) && Objects.equals(outerSource, that.outerSource);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value, outerSource);
    }

    @Override
    public String toString() {
        return "SupplementedInterpretationResponse{" +
                "value='" + value + '\'' +
                ", outerSource=" + outerSource +
                '}';
    }

}
