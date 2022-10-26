package com.bakuard.flashcards.dto.expression;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.Objects;
import java.util.UUID;

@Schema(description = "Данные выражения которое необходимо повторить в ближайшее время.")
public class ExpressionMarkForRepetitionRequest {

    @Schema(description = """
            Уникальный идентификатор пользователя, для выражения которого устанавливается повторение. <br/>
            Ограничение: не должен быть null.
            """)
    private UUID userId;
    @Schema(description = """
            Уникальный идентификатор выражения.
            Ограничение: не должен быть null.
            """)
    private UUID expressionId;

    public ExpressionMarkForRepetitionRequest() {

    }

    public UUID getUserId() {
        return userId;
    }

    public ExpressionMarkForRepetitionRequest setUserId(UUID userId) {
        this.userId = userId;
        return this;
    }

    public UUID getExpressionId() {
        return expressionId;
    }

    public ExpressionMarkForRepetitionRequest setExpressionId(UUID expressionId) {
        this.expressionId = expressionId;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ExpressionMarkForRepetitionRequest that = (ExpressionMarkForRepetitionRequest) o;
        return Objects.equals(userId, that.userId) && Objects.equals(expressionId, that.expressionId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId, expressionId);
    }

    @Override
    public String toString() {
        return "ExpressionMarkForRepetitionRequest{" +
                "userId=" + userId +
                ", expressionId=" + expressionId +
                '}';
    }

}
