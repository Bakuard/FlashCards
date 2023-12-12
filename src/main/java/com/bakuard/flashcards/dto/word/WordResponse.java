package com.bakuard.flashcards.dto.word;

import com.bakuard.flashcards.dto.common.ExampleResponse;
import com.bakuard.flashcards.dto.common.InterpretationResponse;
import com.bakuard.flashcards.dto.common.TranscriptionResponse;
import com.bakuard.flashcards.dto.common.TranslateResponse;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Schema(description = "Подробные данные об одном слове.")
public class WordResponse {

    @Schema(description = "Уникальный идентификатор слова.")
    private UUID wordId;
    @Schema(description = "Уникальный идентификатор пользователя, к словарю которого относится это слово.")
    private UUID userId;
    @Schema(description = "Значение слова.")
    private String value;
    @Schema(description = "Примечание к слову.")
    private String note;
    @Schema(description = "Список транскрипций слова.")
    private List<TranscriptionResponse> transcriptions;
    @Schema(description = "Список толкований слова.")
    private List<InterpretationResponse> interpretations;
    @Schema(description = "Список переводов слова.")
    private List<TranslateResponse> translates;
    @Schema(description = "Список примеров слова.")
    private List<ExampleResponse> examples;

    public WordResponse() {

    }

    public UUID getWordId() {
        return wordId;
    }

    public WordResponse setWordId(UUID wordId) {
        this.wordId = wordId;
        return this;
    }

    public UUID getUserId() {
        return userId;
    }

    public WordResponse setUserId(UUID userId) {
        this.userId = userId;
        return this;
    }

    public String getValue() {
        return value;
    }

    public WordResponse setValue(String value) {
        this.value = value;
        return this;
    }

    public String getNote() {
        return note;
    }

    public WordResponse setNote(String note) {
        this.note = note;
        return this;
    }

    public List<TranscriptionResponse> getTranscriptions() {
        return transcriptions;
    }

    public WordResponse setTranscriptions(List<TranscriptionResponse> transcriptions) {
        this.transcriptions = transcriptions;
        return this;
    }

    public List<InterpretationResponse> getInterpretations() {
        return interpretations;
    }

    public WordResponse setInterpretations(List<InterpretationResponse> interpretations) {
        this.interpretations = interpretations;
        return this;
    }

    public List<TranslateResponse> getTranslates() {
        return translates;
    }

    public WordResponse setTranslates(List<TranslateResponse> translates) {
        this.translates = translates;
        return this;
    }

    public List<ExampleResponse> getExamples() {
        return examples;
    }

    public WordResponse setExamples(List<ExampleResponse> examples) {
        this.examples = examples;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        WordResponse that = (WordResponse) o;
        return Objects.equals(wordId, that.wordId) &&
                Objects.equals(userId, that.userId) &&
                Objects.equals(value, that.value) &&
                Objects.equals(note, that.note) &&
                Objects.equals(transcriptions, that.transcriptions) &&
                Objects.equals(interpretations, that.interpretations) &&
                Objects.equals(translates, that.translates) &&
                Objects.equals(examples, that.examples);
    }

    @Override
    public int hashCode() {
        return Objects.hash(wordId, userId, value, note, transcriptions, interpretations, translates, examples);
    }

    @Override
    public String toString() {
        return "WordResponse{" +
                "wordId=" + wordId +
                ", userId=" + userId +
                ", value='" + value + '\'' +
                ", note='" + note + '\'' +
                ", transcriptions=" + transcriptions +
                ", interpretations=" + interpretations +
                ", translates=" + translates +
                ", examples=" + examples +
                '}';
    }

}
