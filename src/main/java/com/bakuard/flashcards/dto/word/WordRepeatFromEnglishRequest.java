package com.bakuard.flashcards.dto.word;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.Objects;
import java.util.UUID;

@Schema(description = "Запрос на повторение слова с английского на родной язык пользователя.")
public class WordRepeatFromEnglishRequest {

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
    @Schema(description = """
            Указывает - помнит ли пользователь данное слово или нет.
            """)
    private boolean isRemember;

    public WordRepeatFromEnglishRequest() {

    }

    public UUID getUserId() {
        return userId;
    }

    public WordRepeatFromEnglishRequest setUserId(UUID userId) {
        this.userId = userId;
        return this;
    }

    public UUID getWordId() {
        return wordId;
    }

    public WordRepeatFromEnglishRequest setWordId(UUID wordId) {
        this.wordId = wordId;
        return this;
    }

    public boolean isRemember() {
        return isRemember;
    }

    public WordRepeatFromEnglishRequest setRemember(boolean remember) {
        isRemember = remember;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        WordRepeatFromEnglishRequest that = (WordRepeatFromEnglishRequest) o;
        return isRemember == that.isRemember &&
                Objects.equals(userId, that.userId) &&
                Objects.equals(wordId, that.wordId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId, wordId, isRemember);
    }

    @Override
    public String toString() {
        return "WordRepeatRequest{" +
                "userId=" + userId +
                ", wordId=" + wordId +
                ", isRemember=" + isRemember +
                '}';
    }

}
