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
    private UUID wordId;
    @Schema(description = "Значение выражения на английском языке записанное пользователем по памяти.")
    private String inputTranslate;

    public ExpressionRepeatFromNativeToEnglishRequest() {

    }

    public UUID getUserId() {
        return userId;
    }

    public ExpressionRepeatFromNativeToEnglishRequest setUserId(UUID userId) {
        this.userId = userId;
        return this;
    }

    public UUID getWordId() {
        return wordId;
    }

    public ExpressionRepeatFromNativeToEnglishRequest setWordId(UUID wordId) {
        this.wordId = wordId;
        return this;
    }

    public String getInputTranslate() {
        return inputTranslate;
    }

    public ExpressionRepeatFromNativeToEnglishRequest setInputTranslate(String inputTranslate) {
        this.inputTranslate = inputTranslate;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ExpressionRepeatFromNativeToEnglishRequest that = (ExpressionRepeatFromNativeToEnglishRequest) o;
        return Objects.equals(userId, that.userId) &&
                Objects.equals(wordId, that.wordId) &&
                Objects.equals(inputTranslate, that.inputTranslate);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId, wordId, inputTranslate);
    }

    @Override
    public String toString() {
        return "ExpressionRepeatFromNativeToEnglishRequest{" +
                "userId=" + userId +
                ", wordId=" + wordId +
                ", inputTranslate='" + inputTranslate + '\'' +
                '}';
    }

}
