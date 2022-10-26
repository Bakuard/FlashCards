package com.bakuard.flashcards.dto.word;

import com.bakuard.flashcards.dto.common.InterpretationRequestResponse;
import com.bakuard.flashcards.dto.common.TranslateRequestResponse;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Schema(description = "Данные слова для списка повторяемых слов с родного языка пользователя на английский язык.")
public class WordForRepetitionFromNativeResponse {

    @Schema(description = "Уникальный идентификатор слова.")
    private UUID wordId;
    @Schema(description = "Уникальный идентификатор пользователя, к словарю которого относится это слово.")
    private UUID userId;
    @Schema(description = "Переводы английского слова.")
    private List<TranslateRequestResponse> translations;
    @Schema(description = "Толкования английского слова.")
    private List<InterpretationRequestResponse> interpretations;

    public WordForRepetitionFromNativeResponse() {

    }

    public UUID getWordId() {
        return wordId;
    }

    public WordForRepetitionFromNativeResponse setWordId(UUID wordId) {
        this.wordId = wordId;
        return this;
    }

    public UUID getUserId() {
        return userId;
    }

    public WordForRepetitionFromNativeResponse setUserId(UUID userId) {
        this.userId = userId;
        return this;
    }

    public List<TranslateRequestResponse> getTranslations() {
        return translations;
    }

    public WordForRepetitionFromNativeResponse setTranslations(List<TranslateRequestResponse> translations) {
        this.translations = translations;
        return this;
    }

    public List<InterpretationRequestResponse> getInterpretations() {
        return interpretations;
    }

    public WordForRepetitionFromNativeResponse setInterpretations(List<InterpretationRequestResponse> interpretations) {
        this.interpretations = interpretations;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        WordForRepetitionFromNativeResponse that = (WordForRepetitionFromNativeResponse) o;
        return Objects.equals(wordId, that.wordId) &&
                Objects.equals(userId, that.userId) &&
                Objects.equals(translations, that.translations) &&
                Objects.equals(interpretations, that.interpretations);
    }

    @Override
    public int hashCode() {
        return Objects.hash(wordId, userId, translations, interpretations);
    }

    @Override
    public String toString() {
        return "WordForRepetitionNativeToEnglishResponse{" +
                "wordId=" + wordId +
                ", userId=" + userId +
                ", translations=" + translations +
                ", interpretations=" + interpretations +
                '}';
    }

}
