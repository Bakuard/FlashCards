package com.bakuard.flashcards.dto.expression;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.Objects;
import java.util.UUID;

@Schema(description = "Запрос на повторение устойчевого выражения с английского на родной язык пользователя.")
public class ExpressionRepeatEnglishToNativeRequest {

    @Schema(description = """
            Идентификатор пользователя, с которым связано указанное устойчевое выражение. <br/>
            Ограничения: не должно быть null.
            """)
    private UUID userId;
    @Schema(description = """
            Уникальный идентификатор устойчевого выражения. <br/>
            Огрничения: не должен быть null.
            """)
    private UUID expressionId;
    @Schema(description = """
            Указывает - помнит ли пользователь данное устойчевое выражение или нет.
            """)
    private boolean isRemember;

    public ExpressionRepeatEnglishToNativeRequest() {

    }

    public UUID getUserId() {
        return userId;
    }

    public ExpressionRepeatEnglishToNativeRequest setUserId(UUID userId) {
        this.userId = userId;
        return this;
    }

    public UUID getExpressionId() {
        return expressionId;
    }

    public ExpressionRepeatEnglishToNativeRequest setExpressionId(UUID expressionId) {
        this.expressionId = expressionId;
        return this;
    }

    public boolean isRemember() {
        return isRemember;
    }

    public ExpressionRepeatEnglishToNativeRequest setRemember(boolean remember) {
        isRemember = remember;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ExpressionRepeatEnglishToNativeRequest that = (ExpressionRepeatEnglishToNativeRequest) o;
        return isRemember == that.isRemember &&
                Objects.equals(userId, that.userId) &&
                Objects.equals(expressionId, that.expressionId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId, expressionId, isRemember);
    }

    @Override
    public String toString() {
        return "ExpressionRepeatRequest{" +
                "userId=" + userId +
                ", expressionId=" + expressionId +
                ", isRemember=" + isRemember +
                '}';
    }

}
