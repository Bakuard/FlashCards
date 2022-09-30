package com.bakuard.flashcards.dto.word;

import com.bakuard.flashcards.dto.common.ExampleRequestResponse;
import com.bakuard.flashcards.dto.common.InterpretationRequestResponse;
import com.bakuard.flashcards.dto.common.TranscriptionRequestResponse;
import com.bakuard.flashcards.dto.common.TranslateRequestResponse;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;
import java.util.Objects;

@Schema(description = "Данные добавляемого слова.")
public class WordAddRequest {

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
            Сам список может принимать значение null.
            """)
    private List<TranscriptionRequestResponse> transcriptions;
    @Schema(description = """
            Список интерпретаций слова. <br/>
            Ограничения: <br/>
            1. Не должен содержать null <br/>
            2. Не должен содержать дубликатов <br/>
            Сам список может принимать значение null.
            """)
    private List<InterpretationRequestResponse> interpretations;
    @Schema(description = """
            Список переводов слова. <br/>
            Ограничения: <br/>
            1. Не должен содержать null <br/>
            2. Не должен содержать дубликатов <br/>
            Сам список может принимать значение null.
            """)
    private List<TranslateRequestResponse> translates;
    @Schema(description = """
            Список примеров слова. <br/>
            Ограничения: <br/>
            1. Не должен содержать null <br/>
            2. Не должен содержать дубликатов <br/>
            Сам список может принимать значение null.
            """)
    private List<ExampleRequestResponse> examples;

    public WordAddRequest() {

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

    public List<TranscriptionRequestResponse> getTranscriptions() {
        return transcriptions;
    }

    public WordAddRequest setTranscriptions(List<TranscriptionRequestResponse> transcriptions) {
        this.transcriptions = transcriptions;
        return this;
    }

    public List<InterpretationRequestResponse> getInterpretations() {
        return interpretations;
    }

    public WordAddRequest setInterpretations(List<InterpretationRequestResponse> interpretations) {
        this.interpretations = interpretations;
        return this;
    }

    public List<TranslateRequestResponse> getTranslates() {
        return translates;
    }

    public WordAddRequest setTranslates(List<TranslateRequestResponse> translates) {
        this.translates = translates;
        return this;
    }

    public List<ExampleRequestResponse> getExamples() {
        return examples;
    }

    public WordAddRequest setExamples(List<ExampleRequestResponse> examples) {
        this.examples = examples;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        WordAddRequest that = (WordAddRequest) o;
        return Objects.equals(value, that.value) &&
                Objects.equals(note, that.note) &&
                Objects.equals(transcriptions, that.transcriptions) &&
                Objects.equals(interpretations, that.interpretations) &&
                Objects.equals(translates, that.translates) &&
                Objects.equals(examples, that.examples);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value, note, transcriptions, interpretations, translates, examples);
    }

    @Override
    public String toString() {
        return "WordAddRequest{" +
                ", value='" + value + '\'' +
                ", note='" + note + '\'' +
                ", transcriptions=" + transcriptions +
                ", interpretations=" + interpretations +
                ", translates=" + translates +
                ", examples=" + examples +
                '}';
    }

}
