package com.bakuard.flashcards.dto.expression;

import com.bakuard.flashcards.dto.common.ExampleSupplementRequest;
import com.bakuard.flashcards.dto.common.InterpretationRequest;
import com.bakuard.flashcards.dto.common.TranslateRequest;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Schema(description = """
        Данные выражения, которые необходимо дополнить из внешних источников (например, web переводчиков и толковых словарей).
        """)
public class ExpressionSupplementRequest {

    @Schema(description = """
            Идентификатор пользователя, выражение которого необходимо дополнить. <br/>
            Ограничения: не должен быть null.
            """)
    private UUID userID;
    @Schema(description = """
            Значение устойчевого выражения. <br/>
            Должно представлять собой не пустую строку.
            """)
    private String value;
    @Schema(description = """
            Примечане к устойчевому выражению. <br/>
            Ограничения: не должно быть пустой строкой или должно быть null.
            """)
    private String note;
    @Schema(description = """
            Список интерпретаций устойчевого выражения. <br/>
            Ограничения: <br/>
            1. Не должен содержать null <br/>
            2. Не должен содержать дубликатов <br/>
            Сам список может принимать значение null либо быть пустым.
            """)
    private List<InterpretationRequest> interpretations;
    @Schema(description = """
            Список переводов устойчевого выражения. <br/>
            Ограничения: <br/>
            1. Не должен содержать null <br/>
            2. Не должен содержать дубликатов <br/>
            Сам список может принимать значение null либо быть пустым.
            """)
    private List<TranslateRequest> translates;
    @Schema(description = """
            Список примеров устойчевого выражения. <br/>
            Ограничения: <br/>
            1. Не должен содержать null <br/>
            2. Не должен содержать дубликатов <br/>
            Сам список может принимать значение null либо быть пустым.
            """)
    private List<ExampleSupplementRequest> examples;

    public ExpressionSupplementRequest() {

    }

    public UUID getUserID() {
        return userID;
    }

    public ExpressionSupplementRequest setUserID(UUID userID) {
        this.userID = userID;
        return this;
    }

    public String getValue() {
        return value;
    }

    public ExpressionSupplementRequest setValue(String value) {
        this.value = value;
        return this;
    }

    public String getNote() {
        return note;
    }

    public ExpressionSupplementRequest setNote(String note) {
        this.note = note;
        return this;
    }

    public List<InterpretationRequest> getInterpretations() {
        return interpretations;
    }

    public ExpressionSupplementRequest setInterpretations(List<InterpretationRequest> interpretations) {
        this.interpretations = interpretations;
        return this;
    }

    public List<TranslateRequest> getTranslates() {
        return translates;
    }

    public ExpressionSupplementRequest setTranslates(List<TranslateRequest> translates) {
        this.translates = translates;
        return this;
    }

    public List<ExampleSupplementRequest> getExamples() {
        return examples;
    }

    public ExpressionSupplementRequest setExamples(List<ExampleSupplementRequest> examples) {
        this.examples = examples;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ExpressionSupplementRequest that = (ExpressionSupplementRequest) o;
        return Objects.equals(userID, that.userID) &&
                Objects.equals(value, that.value) &&
                Objects.equals(note, that.note) &&
                Objects.equals(interpretations, that.interpretations) &&
                Objects.equals(translates, that.translates) &&
                Objects.equals(examples, that.examples);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userID, value, note, interpretations, translates, examples);
    }

    @Override
    public String toString() {
        return "ExpressionSupplementRequest{" +
                "userID=" + userID +
                ", value='" + value + '\'' +
                ", note='" + note + '\'' +
                ", interpretations=" + interpretations +
                ", translates=" + translates +
                ", examples=" + examples +
                '}';
    }

}
