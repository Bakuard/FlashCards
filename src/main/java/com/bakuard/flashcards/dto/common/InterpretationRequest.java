package com.bakuard.flashcards.dto.common;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.Objects;

@Schema(description = "Толкование слова или устойчевого выражения")
public class InterpretationRequest {

    @Schema(description = """
            Подробное описание значения и употребления слова или устойчевого выражения. <br/>
            Должно представлять собой не пустую строку.
            """)
    private String value;

    public InterpretationRequest() {
    }

    public String getValue() {
        return value;
    }

    public InterpretationRequest setValue(String value) {
        this.value = value;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        InterpretationRequest that = (InterpretationRequest) o;
        return Objects.equals(value, that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    @Override
    public String toString() {
        return "InterpretationRequestResponse{" +
                "value='" + value + '\'' +
                '}';
    }

}
