package com.bakuard.flashcards.dto.word;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.Objects;
import java.util.UUID;

@Schema(description = "Запрос на повторение слова с родного языка пользователя на англиский.")
public class WordRepeatFromNativeRequest {

    @Schema(description = """
            Идентификатор пользователя, с которым связано указанное слово. <br/>
            Ограничения: не должен быть null.
            """)
    private UUID userId;
    @Schema(description = """
            Уникальный идентификатор слова. <br/>
            Огрничения: не должен быть null.
            """)
    private UUID wordId;
    @Schema(description = "Значение слова на английском языке записанное пользователем по памяти.")
    private String inputValue;

    public WordRepeatFromNativeRequest() {

    }

    public UUID getUserId() {
        return userId;
    }

    public WordRepeatFromNativeRequest setUserId(UUID userId) {
        this.userId = userId;
        return this;
    }

    public UUID getWordId() {
        return wordId;
    }

    public WordRepeatFromNativeRequest setWordId(UUID wordId) {
        this.wordId = wordId;
        return this;
    }

    public String getInputValue() {
        return inputValue;
    }

    public WordRepeatFromNativeRequest setInputValue(String inputValue) {
        this.inputValue = inputValue;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        WordRepeatFromNativeRequest that = (WordRepeatFromNativeRequest) o;
        return Objects.equals(userId, that.userId) &&
                Objects.equals(wordId, that.wordId) &&
                Objects.equals(inputValue, that.inputValue);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId, wordId, inputValue);
    }

    @Override
    public String toString() {
        return "WordRepeatFromNativeToEnglishRequest{" +
                "userId=" + userId +
                ", wordId=" + wordId +
                ", inputTranslate='" + inputValue + '\'' +
                '}';
    }

}
