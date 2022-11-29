package com.bakuard.flashcards.dto.word;

import com.bakuard.flashcards.dto.common.ExampleRequest;
import com.bakuard.flashcards.dto.common.InterpretationRequest;
import com.bakuard.flashcards.dto.common.TranscriptionRequest;
import com.bakuard.flashcards.dto.common.TranslateRequest;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Schema(description = "Данные добавляемого слова.")
public class WordAddRequest {

    @Schema(description = """
            Идентификатор пользователя, в словарь которого будет добавленно слово. <br/>
            Ограничения: не должен быть null.
            """)
    private UUID userId;
    @Schema(description = """
            Значение слова. <br/>
            Должно представлять собой не пустую строку.
            """)
    private String value;
    @Schema(description = """
            Примечание к слову. <br/>
            Ограничения: не должно быть пустой строкой или должно быть null.
            """)
    private String note;
    @Schema(description = """
            Список транскрипций слова. <br/>
            Ограничения: <br/>
            1. Не должен содержать null <br/>
            2. Не должен содержать дубликатов <br/>
            Сам список может принимать значение null либо быть пустым.
            """)
    private List<TranscriptionRequest> transcriptions;
    @Schema(description = """
            Список интерпретаций слова. <br/>
            Ограничения: <br/>
            1. Не должен содержать null <br/>
            2. Не должен содержать дубликатов <br/>
            Сам список может принимать значение null либо быть пустым.
            """)
    private List<InterpretationRequest> interpretations;
    @Schema(description = """
            Список переводов слова. <br/>
            Ограничения: <br/>
            1. Не должен содержать null <br/>
            2. Не должен содержать дубликатов <br/>
            Сам список может принимать значение null либо быть пустым.
            """)
    private List<TranslateRequest> translates;
    @Schema(description = """
            Список примеров слова. <br/>
            Ограничения: <br/>
            1. Не должен содержать null <br/>
            2. Не должен содержать дубликатов <br/>
            Сам список может принимать значение null либо быть пустым.
            """)
    private List<ExampleRequest> examples;

    public WordAddRequest() {

    }

    public UUID getUserId() {
        return userId;
    }

    public WordAddRequest setUserId(UUID userId) {
        this.userId = userId;
        return this;
    }

    public String getValue() {
        return value;
    }

    public WordAddRequest setValue(String value) {
        this.value = value;
        return this;
    }

    public String getNote() {
        return note;
    }

    public WordAddRequest setNote(String note) {
        this.note = note;
        return this;
    }

    public List<TranscriptionRequest> getTranscriptions() {
        return transcriptions;
    }

    public WordAddRequest setTranscriptions(List<TranscriptionRequest> transcriptions) {
        this.transcriptions = transcriptions;
        return this;
    }

    public List<InterpretationRequest> getInterpretations() {
        return interpretations;
    }

    public WordAddRequest setInterpretations(List<InterpretationRequest> interpretations) {
        this.interpretations = interpretations;
        return this;
    }

    public List<TranslateRequest> getTranslates() {
        return translates;
    }

    public WordAddRequest setTranslates(List<TranslateRequest> translates) {
        this.translates = translates;
        return this;
    }

    public List<ExampleRequest> getExamples() {
        return examples;
    }

    public WordAddRequest setExamples(List<ExampleRequest> examples) {
        this.examples = examples;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        WordAddRequest that = (WordAddRequest) o;
        return Objects.equals(userId, that.userId) &&
                Objects.equals(value, that.value) &&
                Objects.equals(note, that.note) &&
                Objects.equals(transcriptions, that.transcriptions) &&
                Objects.equals(interpretations, that.interpretations) &&
                Objects.equals(translates, that.translates) &&
                Objects.equals(examples, that.examples);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId, value, note, transcriptions, interpretations, translates, examples);
    }

    @Override
    public String toString() {
        return "WordAddRequest{" +
                "userId=" + userId +
                ", value='" + value + '\'' +
                ", note='" + note + '\'' +
                ", transcriptions=" + transcriptions +
                ", interpretations=" + interpretations +
                ", translates=" + translates +
                ", examples=" + examples +
                '}';
    }

}
