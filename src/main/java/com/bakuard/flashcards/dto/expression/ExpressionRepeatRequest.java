package com.bakuard.flashcards.dto.expression;

import java.util.Objects;
import java.util.UUID;

public class ExpressionRepeatRequest {

    private UUID expressionId;
    private boolean isRemember;

    public ExpressionRepeatRequest() {

    }

    public UUID getExpressionId() {
        return expressionId;
    }

    public ExpressionRepeatRequest setExpressionId(UUID expressionId) {
        this.expressionId = expressionId;
        return this;
    }

    public boolean isRemember() {
        return isRemember;
    }

    public ExpressionRepeatRequest setRemember(boolean remember) {
        isRemember = remember;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ExpressionRepeatRequest that = (ExpressionRepeatRequest) o;
        return isRemember == that.isRemember &&
                Objects.equals(expressionId, that.expressionId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(expressionId, isRemember);
    }

    @Override
    public String toString() {
        return "ExpressionRepeatRequest{" +
                "expressionId=" + expressionId +
                ", isRemember=" + isRemember +
                '}';
    }

}
