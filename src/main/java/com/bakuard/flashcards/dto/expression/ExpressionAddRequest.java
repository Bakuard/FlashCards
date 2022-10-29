package com.bakuard.flashcards.dto.expression;

import com.bakuard.flashcards.dto.common.ExampleRequest;
import com.bakuard.flashcards.dto.common.InterpretationRequest;
import com.bakuard.flashcards.dto.common.TranslateRequest;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Schema(description = "Данные добавляемого устойчевого выражения.")
public class ExpressionAddRequest {

    @Schema(description = """
            Идентификатор пользователя, в словарь которого будет добавленно устойчевое выражение. <br/>
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
    private List<ExampleRequest> examples;

    public ExpressionAddRequest() {

    }

    public UUID getUserID() {
        return userID;
    }

    public ExpressionAddRequest setUserID(UUID userID) {
        this.userID = userID;
        return this;
    }

    public String getValue() {
        return value;
    }

    public ExpressionAddRequest setValue(String value) {
        this.value = value;
        return this;
    }

    public String getNote() {
        return note;
    }

    public ExpressionAddRequest setNote(String note) {
        this.note = note;
        return this;
    }

    public List<InterpretationRequest> getInterpretations() {
        return interpretations;
    }

    public ExpressionAddRequest setInterpretations(List<InterpretationRequest> interpretations) {
        this.interpretations = interpretations;
        return this;
    }

    public List<TranslateRequest> getTranslates() {
        return translates;
    }

    public ExpressionAddRequest setTranslates(List<TranslateRequest> translates) {
        this.translates = translates;
        return this;
    }

    public List<ExampleRequest> getExamples() {
        return examples;
    }

    public ExpressionAddRequest setExamples(List<ExampleRequest> examples) {
        this.examples = examples;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ExpressionAddRequest that = (ExpressionAddRequest) o;
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
        return "ExpressionAddRequest{" +
                "userID=" + userID +
                ", value='" + value + '\'' +
                ", note='" + note + '\'' +
                ", interpretations=" + interpretations +
                ", translates=" + translates +
                ", examples=" + examples +
                '}';
    }

}
