package com.bakuard.flashcards.dto.word;

import java.util.Objects;
import java.util.UUID;

public class WordForDictionaryListResponse {

    private UUID wordId;
    private UUID userId;
    private String value;
    private boolean isHotRepeat;

    public WordForDictionaryListResponse() {
    }

    public UUID getWordId() {
        return wordId;
    }

    public WordForDictionaryListResponse setWordId(UUID wordId) {
        this.wordId = wordId;
        return this;
    }

    public UUID getUserId() {
        return userId;
    }

    public WordForDictionaryListResponse setUserId(UUID userId) {
        this.userId = userId;
        return this;
    }

    public String getValue() {
        return value;
    }

    public WordForDictionaryListResponse setValue(String value) {
        this.value = value;
        return this;
    }

    public boolean isHotRepeat() {
        return isHotRepeat;
    }

    public WordForDictionaryListResponse setHotRepeat(boolean hotRepeat) {
        isHotRepeat = hotRepeat;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        WordForDictionaryListResponse that = (WordForDictionaryListResponse) o;
        return isHotRepeat == that.isHotRepeat &&
                Objects.equals(wordId, that.wordId) &&
                Objects.equals(userId, that.userId) &&
                Objects.equals(value, that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(wordId, userId, value, isHotRepeat);
    }

    @Override
    public String toString() {
        return "WordForDictionaryListResponse{" +
                "wordId=" + wordId +
                ", userId=" + userId +
                ", value='" + value + '\'' +
                ", isHotRepeat=" + isHotRepeat +
                '}';
    }

}
