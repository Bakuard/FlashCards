package com.bakuard.flashcards.dto.word;

import com.bakuard.flashcards.dto.common.ExampleRequestResponse;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class WordForRepetitionResponse {

    private UUID wordId;
    private UUID userId;
    private String value;
    private List<String> examples;

    public WordForRepetitionResponse() {

    }

    public UUID getWordId() {
        return wordId;
    }

    public WordForRepetitionResponse setWordId(UUID wordId) {
        this.wordId = wordId;
        return this;
    }

    public UUID getUserId() {
        return userId;
    }

    public WordForRepetitionResponse setUserId(UUID userId) {
        this.userId = userId;
        return this;
    }

    public String getValue() {
        return value;
    }

    public WordForRepetitionResponse setValue(String value) {
        this.value = value;
        return this;
    }

    public List<String> getExamples() {
        return examples;
    }

    public WordForRepetitionResponse setExamples(List<String> examples) {
        this.examples = examples;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        WordForRepetitionResponse that = (WordForRepetitionResponse) o;
        return Objects.equals(wordId, that.wordId) &&
                Objects.equals(userId, that.userId) &&
                Objects.equals(value, that.value) &&
                Objects.equals(examples, that.examples);
    }

    @Override
    public int hashCode() {
        return Objects.hash(wordId, userId, value, examples);
    }

    @Override
    public String toString() {
        return "WordForRepetitionResponse{" +
                "wordId=" + wordId +
                ", userId=" + userId +
                ", value='" + value + '\'' +
                ", examples=" + examples +
                '}';
    }

}
