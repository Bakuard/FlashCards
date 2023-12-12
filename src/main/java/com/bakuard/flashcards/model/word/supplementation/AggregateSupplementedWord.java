package com.bakuard.flashcards.model.word.supplementation;

import com.bakuard.flashcards.model.word.Word;
import com.bakuard.flashcards.model.word.WordExample;
import com.bakuard.flashcards.model.word.WordInterpretation;
import com.bakuard.flashcards.model.word.WordTranscription;
import com.bakuard.flashcards.model.word.WordTranslation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Содержит результат дополнения заданного английского слова транскрипциями, толкованиями, переводами
 * или переводами примеров полученных из нескольких внешних сервисов. Данный объект также будет содержать
 * транскрипции, толкования, переводы и переводы примеров добавленные самим пользователем.
 */
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

    /**
     * Создает объект для агрегирования всех результатов дополнения указанного слова из разных
     * внешних источников.
     * @param word дополняемое слово.
     */
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
                collect(Collectors.toMap(WordInterpretation::getValue, i -> new ArrayList<>()));
        examplesOuterSource = examples.stream().
                collect(Collectors.toMap(WordExample::getOrigin, e -> new ArrayList<>()));
    }

    /**
     * Добавляет к результатам дополнения слова из разных внешних сервисов результат дополнения слова
     * из ещё одного внешнего сервиса.
     * @param word результат дополнения слова из ещё одного внешнего сервиса.
     * @return ссылку на этот же объект.
     */
    public AggregateSupplementedWord merge(SupplementedWord word) {
        mergeTranscriptions(word);
        mergeInterpretations(word);
        mergeTranslations(word);
        mergeExamples(word);
        return this;
    }

    /**
     * Возвращает ссылку на слово для которого было выполнено дополнение из внешних сервисов.
     * @see Word
     */
    public Word getWord() {
        return word;
    }

    /**
     * Возвращает список всех транскрипций к дополняемому слову.
     * @return список всех транскрипций к дополняемому слову.
     */
    public List<WordTranscription> getTranscriptions() {
        return Collections.unmodifiableList(transcriptions);
    }

    /**
     * Возвращает список всех интерпретаций к дополняемому слову.
     * @return список всех интерпретаций к дополняемому слову.
     */
    public List<WordInterpretation> getInterpretations() {
        return Collections.unmodifiableList(interpretations);
    }

    /**
     * Возвращает список всех перевод к дополняемому слову.
     * @return список всех перевод к дополняемому слову.
     */
    public List<WordTranslation> getTranslations() {
        return Collections.unmodifiableList(translations);
    }

    /**
     * Возвращает список всех примеров к дополняемому слову.
     * @return список всех примеров к дополняемому слову.
     */
    public List<WordExample> getExamples() {
        return Collections.unmodifiableList(examples);
    }

    /**
     * Возвращает список данных о всех внешних сервисах из которых была получена данная транскрипция.
     * Если заданная транскрипция не связана ни с одним из внешних сервисов - возвращает пустой список.
     * @param transcription одна из транскрипций этого слова.
     * @return список данных о всех внешних сервисах из которых было получено данная транскрипция.
     */
    public List<OuterSource> getOuterSource(WordTranscription transcription) {
        return transcriptionsOuterSource.get(transcription.getValue());
    }

    /**
     * Возвращает список данных о всех внешних сервисах из которых была получена данная интерпретация.
     * Если заданная интерпретация не связана ни с одним из внешних сервисов - возвращает пустой список.
     * @param interpretation одна из интерпретаций этого слова.
     * @return список данных о всех внешних сервисах из которых было получено данная интерпретация.
     */
    public List<OuterSource> getOuterSource(WordInterpretation interpretation) {
        return interpretationsOuterSource.get(interpretation.getValue());
    }

    /**
     * Возвращает список данных о всех внешних сервисах из которых был получен данный перевод.
     * Если заданный перевод не связана ни с одним из внешних сервисов - возвращает пустой список.
     * @param translation один из переводов этого слова.
     * @return список данных о всех внешних сервисах из которых был получен данный перевод.
     */
    public List<OuterSource> getOuterSource(WordTranslation translation) {
        return translationsOuterSource.get(translation.getValue());
    }

    /**
     * Возвращает список данных о всех внешних сервисах из которых был получен данный пример.
     * Если заданный пример не связан ни с одним из внешних сервисов - возвращает пустой список.
     * @param example один из примеров этого слова.
     * @return список данных о всех внешних сервисах из которых был получен данный пример.
     */
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
