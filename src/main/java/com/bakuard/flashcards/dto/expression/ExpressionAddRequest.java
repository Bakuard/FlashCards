package com.bakuard.flashcards.dto.expression;

import com.bakuard.flashcards.dto.common.ExampleRequestResponse;
import com.bakuard.flashcards.dto.common.InterpretationRequestResponse;
import com.bakuard.flashcards.dto.common.TranslateRequestResponse;

import java.util.List;
import java.util.Objects;

public class ExpressionAddRequest {

    private String value;
    private String note;
    private List<InterpretationRequestResponse> interpretations;
    private List<TranslateRequestResponse> translates;
    private List<ExampleRequestResponse> examples;

    public ExpressionAddRequest() {

    }

    public String getValue() {
        return value;
    }

    public ExpressionAddRequest setValue(String value) {
        this.value = value;
        return this;
    }

    public String getNote() {
        return note;
    }

    public ExpressionAddRequest setNote(String note) {
        this.note = note;
        return this;
    }

    public List<InterpretationRequestResponse> getInterpretations() {
        return interpretations;
    }

    public ExpressionAddRequest setInterpretations(List<InterpretationRequestResponse> interpretations) {
        this.interpretations = interpretations;
        return this;
    }

    public List<TranslateRequestResponse> getTranslates() {
        return translates;
    }

    public ExpressionAddRequest setTranslates(List<TranslateRequestResponse> translates) {
        this.translates = translates;
        return this;
    }

    public List<ExampleRequestResponse> getExamples() {
        return examples;
    }

    public ExpressionAddRequest setExamples(List<ExampleRequestResponse> examples) {
        this.examples = examples;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ExpressionAddRequest that = (ExpressionAddRequest) o;
        return Objects.equals(value, that.value) &&
                Objects.equals(note, that.note) &&
                Objects.equals(interpretations, that.interpretations) &&
                Objects.equals(translates, that.translates) &&
                Objects.equals(examples, that.examples);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value, note, interpretations, translates, examples);
    }

    @Override
    public String toString() {
        return "ExpressionAddRequest{" +
                "value='" + value + '\'' +
                ", note='" + note + '\'' +
                ", interpretations=" + interpretations +
                ", translates=" + translates +
                ", examples=" + examples +
                '}';
    }

}
