package com.bakuard.flashcards.dto.word;

import java.util.Objects;
import java.util.UUID;

public class WordRepeatRequest {

    private UUID wordId;
    private UUID userId;
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

    public UUID getUserId() {
        return userId;
    }

    public WordRepeatRequest setUserId(UUID userId) {
        this.userId = userId;
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
                Objects.equals(wordId, that.wordId) &&
                Objects.equals(userId, that.userId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(wordId, userId, isRemember);
    }

    @Override
    public String toString() {
        return "WordRepeatRequest{" +
                "wordId=" + wordId +
                ", userId=" + userId +
                ", isRemember=" + isRemember +
                '}';
    }

}
