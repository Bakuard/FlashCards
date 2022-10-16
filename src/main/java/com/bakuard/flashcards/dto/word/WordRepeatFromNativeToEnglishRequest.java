package com.bakuard.flashcards.dto.word;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.Objects;
import java.util.UUID;

@Schema(description = "Запрос на повторение слова с родного языка пользователя на англиский.")
public class WordRepeatFromNativeToEnglishRequest {

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
    private String inputTranslate;

    public WordRepeatFromNativeToEnglishRequest() {

    }

    public UUID getUserId() {
        return userId;
    }

    public WordRepeatFromNativeToEnglishRequest setUserId(UUID userId) {
        this.userId = userId;
        return this;
    }

    public UUID getWordId() {
        return wordId;
    }

    public WordRepeatFromNativeToEnglishRequest setWordId(UUID wordId) {
        this.wordId = wordId;
        return this;
    }

    public String getInputTranslate() {
        return inputTranslate;
    }

    public WordRepeatFromNativeToEnglishRequest setInputTranslate(String inputTranslate) {
        this.inputTranslate = inputTranslate;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        WordRepeatFromNativeToEnglishRequest that = (WordRepeatFromNativeToEnglishRequest) o;
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
        return "WordRepeatFromNativeToEnglishRequest{" +
                "userId=" + userId +
                ", wordId=" + wordId +
                ", inputTranslate='" + inputTranslate + '\'' +
                '}';
    }

}
