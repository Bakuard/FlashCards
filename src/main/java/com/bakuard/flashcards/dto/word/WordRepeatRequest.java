package com.bakuard.flashcards.dto.word;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.Objects;
import java.util.UUID;

@Schema(description = "Запрос на повторение слова.")
public class WordRepeatRequest {

    @Schema(description = """
            Уникальный идентификатор слова. <br/>
            Огрничения: не должен быть null.
            """)
    private UUID wordId;
    @Schema(description = """
            Указывает - помнит ли пользователь данное слово или нет.
            """)
    private boolean isRemember;

    public WordRepeatRequest() {
    }

    public UUID getWordId() {
        return wordId;
    }

    public WordRepeatRequest setWordId(UUID wordId) {
        this.wordId = wordId;
        return this;
    }

    public boolean isRemember() {
        return isRemember;
    }

    public WordRepeatRequest setRemember(boolean remember) {
        isRemember = remember;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        WordRepeatRequest that = (WordRepeatRequest) o;
        return isRemember == that.isRemember &&
                Objects.equals(wordId, that.wordId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(wordId, isRemember);
    }

    @Override
    public String toString() {
        return "WordRepeatRequest{" +
                "wordId=" + wordId +
                ", isRemember=" + isRemember +
                '}';
    }

}
