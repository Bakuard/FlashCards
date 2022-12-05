package com.bakuard.flashcards.dto.common;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Schema(description = "Толкование слова или устойчевого выражения")
public class InterpretationResponse {

    @Schema(description = """
            Подробное описание значения и употребления слова или устойчевого выражения. <br/>
            Должно представлять собой не пустую строку.
            """)
    private String value;
    @Schema(description = "Данные всех внешних источников из которых получено данное толкование.")
    private List<OuterSourceResponse> outerSource;

    public InterpretationResponse() {
        outerSource = new ArrayList<>();
    }

    public String getValue() {
        return value;
    }

    public InterpretationResponse setValue(String value) {
        this.value = value;
        return this;
    }

    public List<OuterSourceResponse> getOuterSource() {
        return outerSource;
    }

    public InterpretationResponse setOuterSource(List<OuterSourceResponse> outerSource) {
        this.outerSource = outerSource;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        InterpretationResponse that = (InterpretationResponse) o;
        return Objects.equals(value, that.value) && Objects.equals(outerSource, that.outerSource);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value, outerSource);
    }

    @Override
    public String toString() {
        return "InterpretationResponse{" +
                "value='" + value + '\'' +
                ", outerSource=" + outerSource +
                '}';
    }

}
