package com.bakuard.flashcards.dto.common;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.Objects;

@Schema(description = "Пример использования слова или устойчевого выражения.")
public class ExampleRequest {

    @Schema(description = """
            Пример на английском языке. <br/>
            Ограничения: не должен быть пустой строкой.
            """)
    private String origin;
    @Schema(description = """
            Перевод примера на родной язык пользователя.<br/>
            Ограничения: не должен быть пустой строкой или должен быть null.
            """)
    private String translate;
    @Schema(description = """
            Примечание к примеру. <br/>
            Ограничения: не должно быть пустой строкой или должно быть null.
            """)
    private String note;

    public ExampleRequest() {
    }

    public String getOrigin() {
        return origin;
    }

    public ExampleRequest setOrigin(String origin) {
        this.origin = origin;
        return this;
    }

    public String getTranslate() {
        return translate;
    }

    public ExampleRequest setTranslate(String translate) {
        this.translate = translate;
        return this;
    }

    public String getNote() {
        return note;
    }

    public ExampleRequest setNote(String note) {
        this.note = note;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ExampleRequest that = (ExampleRequest) o;
        return Objects.equals(origin, that.origin) &&
                Objects.equals(translate, that.translate) &&
                Objects.equals(note, that.note);
    }

    @Override
    public int hashCode() {
        return Objects.hash(origin, translate, note);
    }

    @Override
    public String toString() {
        return "ExampleRequestResponse{" +
                "origin='" + origin + '\'' +
                ", translate='" + translate + '\'' +
                ", note='" + note + '\'' +
                '}';
    }

}
