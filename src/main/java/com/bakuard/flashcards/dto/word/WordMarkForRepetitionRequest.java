package com.bakuard.flashcards.dto.word;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.Objects;
import java.util.UUID;

@Schema(description = "Данные слова которое необходимо отметить для повторения в ближайшее время.")
public class WordMarkForRepetitionRequest {

    @Schema(description = """
            Уникальный идентификатор пользователя, для слова которого устанавливается повторение. <br/>
            Ограничение: не должен быть null.
            """)
    private UUID userId;
    @Schema(description = """
            Уникальный идентификатор слова.
            Ограничение: не должен быть null.
            """)
    private UUID wordId;

    public WordMarkForRepetitionRequest() {

    }

    public UUID getUserId() {
        return userId;
    }

    public WordMarkForRepetitionRequest setUserId(UUID userId) {
        this.userId = userId;
        return this;
    }

    public UUID getWordId() {
        return wordId;
    }

    public WordMarkForRepetitionRequest setWordId(UUID wordId) {
        this.wordId = wordId;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        WordMarkForRepetitionRequest that = (WordMarkForRepetitionRequest) o;
        return Objects.equals(userId, that.userId) && Objects.equals(wordId, that.wordId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId, wordId);
    }

    @Override
    public String toString() {
        return "WordMarkForRepetitionRequest{" +
                "userId=" + userId +
                ", wordId=" + wordId +
                '}';
    }

}
