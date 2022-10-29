package com.bakuard.flashcards.dto.common;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.Objects;

@Schema(description = "Толкование слова или устойчевого выражения")
public class InterpretationResponse {

    @Schema(description = """
            Подробное описание значения и употребления слова или устойчевого выражения. <br/>
            Должно представлять собой не пустую строку.
            """)
    private String value;
    @Schema(description = """
            Указывает - была ли добавлена данная интерпретация автоматически при дополнении слова.
             Если это так - возвращает true.
            """)
    private boolean isNew;

    public InterpretationResponse() {

    }

    public String getValue() {
        return value;
    }

    public InterpretationResponse setValue(String value) {
        this.value = value;
        return this;
    }

    public boolean isNew() {
        return isNew;
    }

    public InterpretationResponse setNew(boolean aNew) {
        isNew = aNew;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        InterpretationResponse that = (InterpretationResponse) o;
        return isNew == that.isNew && Objects.equals(value, that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value, isNew);
    }

    @Override
    public String toString() {
        return "InterpretationResponse{" +
                "value='" + value + '\'' +
                ", isNew=" + isNew +
                '}';
    }

}
