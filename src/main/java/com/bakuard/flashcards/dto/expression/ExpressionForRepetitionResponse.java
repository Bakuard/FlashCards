package com.bakuard.flashcards.dto.expression;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class ExpressionForRepetitionResponse {

    private UUID expressionId;
    private UUID userId;
    private String value;
    private List<String> examples;

    public ExpressionForRepetitionResponse() {

    }

    public UUID getExpressionId() {
        return expressionId;
    }

    public ExpressionForRepetitionResponse setExpressionId(UUID expressionId) {
        this.expressionId = expressionId;
        return this;
    }

    public UUID getUserId() {
        return userId;
    }

    public ExpressionForRepetitionResponse setUserId(UUID userId) {
        this.userId = userId;
        return this;
    }

    public String getValue() {
        return value;
    }

    public ExpressionForRepetitionResponse setValue(String value) {
        this.value = value;
        return this;
    }

    public List<String> getExamples() {
        return examples;
    }

    public ExpressionForRepetitionResponse setExamples(List<String> examples) {
        this.examples = examples;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ExpressionForRepetitionResponse that = (ExpressionForRepetitionResponse) o;
        return Objects.equals(expressionId, that.expressionId) &&
                Objects.equals(userId, that.userId) &&
                Objects.equals(value, that.value) &&
                Objects.equals(examples, that.examples);
    }

    @Override
    public int hashCode() {
        return Objects.hash(expressionId, userId, value, examples);
    }

    @Override
    public String toString() {
        return "ExpressionForRepetitionResponse{" +
                "expressionId=" + expressionId +
                ", userId=" + userId +
                ", value='" + value + '\'' +
                ", examples=" + examples +
                '}';
    }

}
