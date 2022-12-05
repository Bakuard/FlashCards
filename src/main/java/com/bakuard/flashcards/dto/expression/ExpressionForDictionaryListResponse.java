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
            Указывает - помнит ли пользователь перевод этого выражения с английского на свой родной язык. <br/>
            Значение true указывает, что пользователь не смог вспомнить это выражение во время повторения,
            или оно является новым.
            """)
    private boolean hotRepeatFromEnglish;
    @Schema(description = """
            Указывает - помнит ли пользователь перевод этого выражения с родного языка на английский. <br/>
            Значение true указывает, что пользователь не смог вспомнить это выражение во время повторения,
            или оно является новым.
            """)
    private boolean hotRepeatFromNative;

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

    public boolean isHotRepeatFromEnglish() {
        return hotRepeatFromEnglish;
    }

    public ExpressionForDictionaryListResponse setHotRepeatFromEnglish(boolean hotRepeatFromEnglish) {
        this.hotRepeatFromEnglish = hotRepeatFromEnglish;
        return this;
    }

    public boolean isHotRepeatFromNative() {
        return hotRepeatFromNative;
    }

    public ExpressionForDictionaryListResponse setHotRepeatFromNative(boolean hotRepeatFromNative) {
        this.hotRepeatFromNative = hotRepeatFromNative;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ExpressionForDictionaryListResponse that = (ExpressionForDictionaryListResponse) o;
        return hotRepeatFromEnglish == that.hotRepeatFromEnglish &&
                hotRepeatFromNative == that.hotRepeatFromNative &&
                Objects.equals(expressionId, that.expressionId) &&
                Objects.equals(userId, that.userId) &&
                Objects.equals(value, that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(expressionId, userId, value, hotRepeatFromEnglish, hotRepeatFromNative);
    }

    @Override
    public String toString() {
        return "ExpressionForDictionaryListResponse{" +
                "expressionId=" + expressionId +
                ", userId=" + userId +
                ", value='" + value + '\'' +
                ", isHotRepeatFromEnglish=" + hotRepeatFromEnglish +
                ", isHotRepeatFromNative=" + hotRepeatFromNative +
                '}';
    }

}
