package com.bakuard.flashcards.model.word.supplementation;

import com.bakuard.flashcards.model.word.*;

import java.util.*;
import java.util.stream.Collectors;

public class AggregateSupplementedWord {

    private final Word word;
    private List<WordTranscription> transcriptions;
    private List<WordTranslation> translations;
    private List<WordInterpretation> interpretations;
    private List<WordExample> examples;
    private Map<String, List<OuterSource>> transcriptionsOuterSource;
    private Map<String, List<OuterSource>> translationsOuterSource;
    private Map<String, List<OuterSource>> interpretationsOuterSource;
    private Map<String, List<ExampleOuterSource>> examplesOuterSource;

    public AggregateSupplementedWord(Word word) {
        this.word = word;
        transcriptions = new ArrayList<>(word.getTranscriptions());
        translations = new ArrayList<>(word.getTranslations());
        interpretations = new ArrayList<>(word.getInterpretations());
        examples = new ArrayList<>(word.getExamples());
        transcriptionsOuterSource = transcriptions.stream().
                collect(Collectors.toMap(WordTranscription::getValue, t -> new ArrayList<>()));
        translationsOuterSource = translations.stream().
                collect(Collectors.toMap(WordTranslation::getValue, t -> new ArrayList<>()));
        interpretationsOuterSource = interpretations.stream().
                collect(Collectors.toMap(WordInterpretation::getValue, t -> new ArrayList<>()));
        examplesOuterSource = examples.stream().
                collect(Collectors.toMap(WordExample::getOrigin, t -> new ArrayList<>()));
    }

    public AggregateSupplementedWord merge(SupplementedWord word) {
        mergeTranscriptions(word);
        mergeInterpretations(word);
        mergeTranslations(word);
        mergeExamples(word);
        return this;
    }

    public Word getWord() {
        return word;
    }

    public List<WordTranscription> getTranscriptions() {
        return Collections.unmodifiableList(transcriptions);
    }

    public List<WordInterpretation> getInterpretations() {
        return Collections.unmodifiableList(interpretations);
    }

    public List<WordTranslation> getTranslations() {
        return Collections.unmodifiableList(translations);
    }

    public List<WordExample> getExamples() {
        return Collections.unmodifiableList(examples);
    }

    public List<OuterSource> getOuterSource(WordTranscription transcription) {
        return translationsOuterSource.get(transcription.getValue());
    }

    public List<OuterSource> getOuterSource(WordInterpretation interpretation) {
        return interpretationsOuterSource.get(interpretation.getValue());
    }

    public List<OuterSource> getOuterSource(WordTranslation translation) {
        return translationsOuterSource.get(translation.getValue());
    }

    public List<ExampleOuterSource> getOuterSource(WordExample example) {
        return examplesOuterSource.get(example.getOrigin());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AggregateSupplementedWord that = (AggregateSupplementedWord) o;
        return Objects.equals(word, that.word) &&
                Objects.equals(transcriptions, that.transcriptions) &&
                Objects.equals(translations, that.translations) &&
                Objects.equals(interpretations, that.interpretations) &&
                Objects.equals(examples, that.examples) &&
                Objects.equals(transcriptionsOuterSource, that.transcriptionsOuterSource) &&
                Objects.equals(translationsOuterSource, that.translationsOuterSource) &&
                Objects.equals(interpretationsOuterSource, that.interpretationsOuterSource) &&
                Objects.equals(examplesOuterSource, that.examplesOuterSource);
    }

    @Override
    public int hashCode() {
        return Objects.hash(word, transcriptions, translations, interpretations, examples,
                transcriptionsOuterSource, translationsOuterSource,
                interpretationsOuterSource, examplesOuterSource);
    }

    @Override
    public String toString() {
        return "AggregateSupplementedWord{" +
                "word=" + word +
                ", transcriptions=" + transcriptions +
                ", translations=" + translations +
                ", interpretations=" + interpretations +
                ", examples=" + examples +
                ", transcriptionsOuterSource=" + transcriptionsOuterSource +
                ", translationsOuterSource=" + translationsOuterSource +
                ", interpretationsOuterSource=" + interpretationsOuterSource +
                ", examplesOuterSource=" + examplesOuterSource +
                '}';
    }


    private void mergeTranscriptions(SupplementedWord word) {
        word.getTranscriptions().forEach(transcription -> {
            if(!transcriptionsOuterSource.containsKey(transcription.getValue())) {
                transcriptions.add(transcription);
                transcriptionsOuterSource.put(transcription.getValue(), new ArrayList<>());
            }
            transcriptionsOuterSource.get(transcription.getValue()).add(toOuterSource(word));
        });
    }

    private void mergeInterpretations(SupplementedWord word) {
        word.getInterpretations().forEach(interpretation -> {
            if(!interpretationsOuterSource.containsKey(interpretation.getValue())) {
                interpretations.add(interpretation);
                interpretationsOuterSource.put(interpretation.getValue(), new ArrayList<>());
            }
            interpretationsOuterSource.get(interpretation.getValue()).add(toOuterSource(word));
        });
    }

    private void mergeTranslations(SupplementedWord word) {
        word.getTranslations().forEach(translation -> {
            if(!translationsOuterSource.containsKey(translation.getValue())) {
                translations.add(translation);
                translationsOuterSource.put(translation.getValue(), new ArrayList<>());
            }
            translationsOuterSource.get(translation.getValue()).add(toOuterSource(word));
        });
    }

    private void mergeExamples(SupplementedWord word) {
        word.getExamples().forEach(example ->
            examplesOuterSource.get(example.getOrigin()).add(toExampleOuterSource(word, example))
        );
    }

    private OuterSource toOuterSource(SupplementedWord word) {
        return new OuterSource(word.getOuterSourceName(), word.getOuterSourceUri());
    }

    private ExampleOuterSource toExampleOuterSource(SupplementedWord word,
                                                    SupplementedWordExample example) {
        return new ExampleOuterSource(
                example.getOuterSourceUri(),
                word.getOuterSourceName(),
                example.getTranslate()
        );
    }

}
