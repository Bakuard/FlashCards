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
            Указывает - помнит ли пользователь перевод этого слова с английского на свой родной язык. <br/>
            Значение true указывает, что пользователь не смог вспомнить это слово во время повторения,
            или оно является новым.
            """)
    private boolean isHotRepeatFromEnglish;
    @Schema(description = """
            Указывает - помнит ли пользователь перевод этого слова с родного языка на английский. <br/>
            Значение true указывает, что пользователь не смог вспомнить это слово во время повторения,
            или оно является новым.
            """)
    private boolean isHotRepeatFromNative;

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

    public boolean isHotRepeatFromEnglish() {
        return isHotRepeatFromEnglish;
    }

    public WordForDictionaryListResponse setHotRepeatFromEnglish(boolean hotRepeatFromEnglish) {
        isHotRepeatFromEnglish = hotRepeatFromEnglish;
        return this;
    }

    public boolean isHotRepeatFromNative() {
        return isHotRepeatFromNative;
    }

    public WordForDictionaryListResponse setHotRepeatFromNative(boolean hotRepeatFromNative) {
        isHotRepeatFromNative = hotRepeatFromNative;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        WordForDictionaryListResponse that = (WordForDictionaryListResponse) o;
        return isHotRepeatFromEnglish == that.isHotRepeatFromEnglish &&
                isHotRepeatFromNative == that.isHotRepeatFromNative &&
                Objects.equals(wordId, that.wordId) &&
                Objects.equals(userId, that.userId) &&
                Objects.equals(value, that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(wordId, userId, value, isHotRepeatFromEnglish, isHotRepeatFromNative);
    }

    @Override
    public String toString() {
        return "WordForDictionaryListResponse{" +
                "wordId=" + wordId +
                ", userId=" + userId +
                ", value='" + value + '\'' +
                ", isHotRepeatFromEnglish=" + isHotRepeatFromEnglish +
                ", isHotRepeatFromNative=" + isHotRepeatFromNative +
                '}';
    }

}
