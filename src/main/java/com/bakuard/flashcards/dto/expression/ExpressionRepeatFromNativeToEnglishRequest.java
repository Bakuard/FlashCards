package com.bakuard.flashcards.dto.expression;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.Objects;
import java.util.UUID;

@Schema(description = """
        Запрос на повторение устойчевого выражения с родного языка пользователя на англиский.
        """)
public class ExpressionRepeatFromNativeToEnglishRequest {

    @Schema(description = """
            Идентификатор пользователя, с которым связано указанное выражение. <br/>
            Ограничения: не должен быть null.
            """)
    private UUID userId;
    @Schema(description = """
            Уникальный идентификатор выражения. <br/>
            Огрничения: не должен быть null.
            """)
    private UUID expressionId;
    @Schema(description = "Значение выражения на английском языке записанное пользователем по памяти.")
    private String inputValue;

    public ExpressionRepeatFromNativeToEnglishRequest() {

    }

    public UUID getUserId() {
        return userId;
    }

    public ExpressionRepeatFromNativeToEnglishRequest setUserId(UUID userId) {
        this.userId = userId;
        return this;
    }

    public UUID getExpressionId() {
        return expressionId;
    }

    public ExpressionRepeatFromNativeToEnglishRequest setExpressionId(UUID expressionId) {
        this.expressionId = expressionId;
        return this;
    }

    public String getInputValue() {
        return inputValue;
    }

    public ExpressionRepeatFromNativeToEnglishRequest setInputValue(String inputValue) {
        this.inputValue = inputValue;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ExpressionRepeatFromNativeToEnglishRequest that = (ExpressionRepeatFromNativeToEnglishRequest) o;
        return Objects.equals(userId, that.userId) &&
                Objects.equals(expressionId, that.expressionId) &&
                Objects.equals(inputValue, that.inputValue);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId, expressionId, inputValue);
    }

    @Override
    public String toString() {
        return "ExpressionRepeatFromNativeToEnglishRequest{" +
                "userId=" + userId +
                ", wordId=" + expressionId +
                ", inputTranslate='" + inputValue + '\'' +
                '}';
    }

}
