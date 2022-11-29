package com.bakuard.flashcards.dto.expression;

import com.bakuard.flashcards.dto.common.ExampleResponse;
import com.bakuard.flashcards.dto.common.InterpretationResponse;
import com.bakuard.flashcards.dto.common.TranslateResponse;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Schema(description = "Подробные данные об одном устойчевом выражении.")
public class ExpressionResponse {

    @Schema(description = "Уникальный идентификатор устойчевого выражения.")
    private UUID expressionId;
    @Schema(description = "Уникальный идентификатор пользователя, к словарю которого относится это устойчевое выражение.")
    private UUID userId;
    @Schema(description = "Значение устойчевого выражения.")
    private String value;
    @Schema(description = "Примечание к устойчевому выражению.")
    private String note;
    @Schema(description = "Список толкований устойчевого выражения.")
    private List<InterpretationResponse> interpretations;
    @Schema(description = "Список переводов устойчевого выражения.")
    private List<TranslateResponse> translates;
    @Schema(description = "Список примеров устойчевого выражения.")
    private List<ExampleResponse> examples;

    public ExpressionResponse() {

    }

    public UUID getExpressionId() {
        return expressionId;
    }

    public ExpressionResponse setExpressionId(UUID expressionId) {
        this.expressionId = expressionId;
        return this;
    }

    public UUID getUserId() {
        return userId;
    }

    public ExpressionResponse setUserId(UUID userId) {
        this.userId = userId;
        return this;
    }

    public String getValue() {
        return value;
    }

    public ExpressionResponse setValue(String value) {
        this.value = value;
        return this;
    }

    public String getNote() {
        return note;
    }

    public ExpressionResponse setNote(String note) {
        this.note = note;
        return this;
    }

    public List<InterpretationResponse> getInterpretations() {
        return interpretations;
    }

    public ExpressionResponse setInterpretations(List<InterpretationResponse> interpretations) {
        this.interpretations = interpretations;
        return this;
    }

    public List<TranslateResponse> getTranslates() {
        return translates;
    }

    public ExpressionResponse setTranslates(List<TranslateResponse> translates) {
        this.translates = translates;
        return this;
    }

    public List<ExampleResponse> getExamples() {
        return examples;
    }

    public ExpressionResponse setExamples(List<ExampleResponse> examples) {
        this.examples = examples;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ExpressionResponse that = (ExpressionResponse) o;
        return Objects.equals(expressionId, that.expressionId) &&
                Objects.equals(userId, that.userId) &&
                Objects.equals(value, that.value) &&
                Objects.equals(note, that.note) &&
                Objects.equals(interpretations, that.interpretations) &&
                Objects.equals(translates, that.translates) &&
                Objects.equals(examples, that.examples);
    }

    @Override
    public int hashCode() {
        return Objects.hash(expressionId, userId, value, note, interpretations, translates, examples);
    }

    @Override
    public String toString() {
        return "ExpressionResponse{" +
                "expressionId=" + expressionId +
                ", userId=" + userId +
                ", value='" + value + '\'' +
                ", note='" + note + '\'' +
                ", interpretations=" + interpretations +
                ", translates=" + translates +
                ", examples=" + examples +
                '}';
    }

}
