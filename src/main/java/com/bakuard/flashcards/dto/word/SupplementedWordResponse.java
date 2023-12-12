package com.bakuard.flashcards.dto.word;

import com.bakuard.flashcards.dto.common.SupplementedExampleResponse;
import com.bakuard.flashcards.dto.common.SupplementedInterpretationResponse;
import com.bakuard.flashcards.dto.common.SupplementedTranscriptionResponse;
import com.bakuard.flashcards.dto.common.SupplementedTranslateResponse;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Schema(description = """
        Подробные данные об одном слове, для которого транскрипции, интерпретации, переводы и переводы
        примеров были получены из внешних сервисов.
        """)
public class SupplementedWordResponse {

    @Schema(description = "Уникальный идентификатор слова.")
    private UUID wordId;
    @Schema(description = "Уникальный идентификатор пользователя, к словарю которого относится это слово.")
    private UUID userId;
    @Schema(description = "Значение слова.")
    private String value;
    @Schema(description = "Примечание к слову.")
    private String note;
    @Schema(description = "Список транскрипций слова.")
    private List<SupplementedTranscriptionResponse> transcriptions;
    @Schema(description = "Список толкований слова.")
    private List<SupplementedInterpretationResponse> interpretations;
    @Schema(description = "Список переводов слова.")
    private List<SupplementedTranslateResponse> translates;
    @Schema(description = "Список примеров слова.")
    private List<SupplementedExampleResponse> examples;

    public SupplementedWordResponse() {

    }

    public UUID getWordId() {
        return wordId;
    }

    public SupplementedWordResponse setWordId(UUID wordId) {
        this.wordId = wordId;
        return this;
    }

    public UUID getUserId() {
        return userId;
    }

    public SupplementedWordResponse setUserId(UUID userId) {
        this.userId = userId;
        return this;
    }

    public String getValue() {
        return value;
    }

    public SupplementedWordResponse setValue(String value) {
        this.value = value;
        return this;
    }

    public String getNote() {
        return note;
    }

    public SupplementedWordResponse setNote(String note) {
        this.note = note;
        return this;
    }

    public List<SupplementedTranscriptionResponse> getTranscriptions() {
        return transcriptions;
    }

    public SupplementedWordResponse setTranscriptions(List<SupplementedTranscriptionResponse> transcriptions) {
        this.transcriptions = transcriptions;
        return this;
    }

    public List<SupplementedInterpretationResponse> getInterpretations() {
        return interpretations;
    }

    public SupplementedWordResponse setInterpretations(List<SupplementedInterpretationResponse> interpretations) {
        this.interpretations = interpretations;
        return this;
    }

    public List<SupplementedTranslateResponse> getTranslates() {
        return translates;
    }

    public SupplementedWordResponse setTranslates(List<SupplementedTranslateResponse> translates) {
        this.translates = translates;
        return this;
    }

    public List<SupplementedExampleResponse> getExamples() {
        return examples;
    }

    public SupplementedWordResponse setExamples(List<SupplementedExampleResponse> examples) {
        this.examples = examples;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SupplementedWordResponse that = (SupplementedWordResponse) o;
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
        return Objects.hash(wordId, userId, value, note,
                transcriptions, interpretations, translates, examples);
    }

    @Override
    public String toString() {
        return "SupplementedWordResponse{" +
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
