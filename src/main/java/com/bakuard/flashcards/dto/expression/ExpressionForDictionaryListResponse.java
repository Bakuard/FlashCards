package com.bakuard.flashcards.dto.expression;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.Objects;
import java.util.UUID;

@Schema(description = "Данные устойчевого выражения для списка устойчевых выражений в словаре.")
public class ExpressionForDictionaryListResponse {

    @Schema(description = "Уникальный идентификатор устойчевого выражения.")
    private UUID expressionId;
    @Schema(description = "Уникальный идентификатор пользователя, к словарю которого относится это устойчевое выражение.")
    private UUID userId;
    @Schema(description = "Значение устойчевого выражения.")
    private String value;
    @Schema(description = """
            Указывает - помнит ли пользователь это устойчевое выражение или нет. <br/>
            Значение true указывает, что пользователь не смог вспомнить это устойчевое выражение во время повторения,
            или оно является новым.
            """)
    private boolean isHotRepeat;

    public ExpressionForDictionaryListResponse() {

    }

    public UUID getExpressionId() {
        return expressionId;
    }

    public ExpressionForDictionaryListResponse setExpressionId(UUID expressionId) {
        this.expressionId = expressionId;
        return this;
    }

    public UUID getUserId() {
        return userId;
    }

    public ExpressionForDictionaryListResponse setUserId(UUID userId) {
        this.userId = userId;
        return this;
    }

    public String getValue() {
        return value;
    }

    public ExpressionForDictionaryListResponse setValue(String value) {
        this.value = value;
        return this;
    }

    public boolean isHotRepeat() {
        return isHotRepeat;
    }

    public ExpressionForDictionaryListResponse setHotRepeat(boolean hotRepeat) {
        isHotRepeat = hotRepeat;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ExpressionForDictionaryListResponse that = (ExpressionForDictionaryListResponse) o;
        return isHotRepeat == that.isHotRepeat &&
                Objects.equals(expressionId, that.expressionId) &&
                Objects.equals(userId, that.userId) &&
                Objects.equals(value, that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(expressionId, userId, value, isHotRepeat);
    }

    @Override
    public String toString() {
        return "ExpressionForDictionaryListResponse{" +
                "expressionId=" + expressionId +
                ", userId=" + userId +
                ", value='" + value + '\'' +
                ", isHotRepeat=" + isHotRepeat +
                '}';
    }

}
