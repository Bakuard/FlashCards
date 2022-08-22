package com.bakuard.flashcards.dto.word;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.Objects;
import java.util.UUID;

@Schema(description = "Данные слова для списка слов в словаре.")
public class WordForDictionaryListResponse {

    @Schema(description = "Уникальный идентификатор слова.")
    private UUID wordId;
    @Schema(description = "Уникальный идентификатор пользователя, к словарю которого относится это слово.")
    private UUID userId;
    @Schema(description = "Значение слова.")
    private String value;
    @Schema(description = """
            Указывает - помнит ли пользователь это слово или нет. <br/>
            Значение true указывает, что пользователь не смог вспомнить это слово во время повторения,
            или оно является новым.
            """)
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
