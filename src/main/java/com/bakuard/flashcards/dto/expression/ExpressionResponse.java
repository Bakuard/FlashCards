package com.bakuard.flashcards.dto.expression;

import com.bakuard.flashcards.dto.common.ExampleRequestResponse;
import com.bakuard.flashcards.dto.common.InterpretationRequestResponse;
import com.bakuard.flashcards.dto.common.TranslateRequestResponse;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class ExpressionResponse {

    private UUID expressionId;
    private UUID userId;
    private String value;
    private String note;
    private List<InterpretationRequestResponse> interpretations;
    private List<TranslateRequestResponse> translates;
    private List<ExampleRequestResponse> examples;

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

    public List<InterpretationRequestResponse> getInterpretations() {
        return interpretations;
    }

    public ExpressionResponse setInterpretations(List<InterpretationRequestResponse> interpretations) {
        this.interpretations = interpretations;
        return this;
    }

    public List<TranslateRequestResponse> getTranslates() {
        return translates;
    }

    public ExpressionResponse setTranslates(List<TranslateRequestResponse> translates) {
        this.translates = translates;
        return this;
    }

    public List<ExampleRequestResponse> getExamples() {
        return examples;
    }

    public ExpressionResponse setExamples(List<ExampleRequestResponse> examples) {
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
