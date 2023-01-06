package com.bakuard.flashcards.model.word.supplementation;

import com.bakuard.flashcards.model.Entity;
import com.bakuard.flashcards.model.word.WordExample;
import com.bakuard.flashcards.model.word.WordInterpretation;
import com.bakuard.flashcards.model.word.WordTranscription;
import com.bakuard.flashcards.model.word.WordTranslation;

import java.net.URI;
import java.time.Clock;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.IntStream;

public class SupplementedWord implements Entity {

    private UUID id;
    private final UUID examplesOwnerId;
    private String outerSourceName;
    private String value;
    private LocalDate recentUpdateDate;
    private URI outerSourceUri;
    private final List<WordInterpretation> interpretations;
    private final List<WordTranscription> transcriptions;
    private final List<WordTranslation> translations;
    private final List<SupplementedWordExample> examples;

    public SupplementedWord(UUID id,
                            UUID examplesOwnerId,
                            String value,
                            String outerSourceName,
                            LocalDate recentUpdateDate,
                            URI outerSourceUri) {
        this.id = id;
        this.examplesOwnerId = examplesOwnerId;
        this.value = value;
        this.outerSourceName = outerSourceName;
        this.recentUpdateDate = recentUpdateDate;
        this.outerSourceUri = outerSourceUri;
        this.interpretations = new ArrayList<>();
        this.transcriptions = new ArrayList<>();
        this.translations = new ArrayList<>();
        this.examples = new ArrayList<>();
    }

    public SupplementedWord(UUID examplesOwnerId,
                            String value,
                            String outerSourceName,
                            LocalDate recentUpdateDate,
                            URI outerSourceUrl) {
        this(null,
                examplesOwnerId,
                value,
                outerSourceName,
                recentUpdateDate,
                outerSourceUrl);
    }

    public SupplementedWord(SupplementedWord word) {
        this(
                word.getId(),
                word.getExamplesOwnerId(),
                word.getValue(),
                word.getOuterSourceName(),
                word.getRecentUpdateDate(),
                word.getOuterSourceUri()
        );
    }

    /**
     * см. {@link Entity#getId()}
     */
    @Override
    public UUID getId() {
        return id;
    }

    /**
     * Возвращает идентификатор пользователя, к слову из словаря которого относятся примеры {@link #getExamples()}.
     * @return идентификатор пользователя.
     */
    public UUID getExamplesOwnerId() {
        return examplesOwnerId;
    }

    public String getValue() {
        return value;
    }

    public String getOuterSourceName() {
        return outerSourceName;
    }

    public LocalDate getRecentUpdateDate() {
        return recentUpdateDate;
    }

    public URI getOuterSourceUri() {
        return outerSourceUri;
    }

    public List<WordInterpretation> getInterpretations() {
        return Collections.unmodifiableList(interpretations);
    }

    public List<WordTranscription> getTranscriptions() {
        return Collections.unmodifiableList(transcriptions);
    }

    public List<WordTranslation> getTranslations() {
        return Collections.unmodifiableList(translations);
    }

    public List<SupplementedWordExample> getExamples() {
        return Collections.unmodifiableList(examples);
    }

    /**
     * Находит и возвращает все примеры данного слова, которые отсутствуют в заданном списке.
     * Все примеры сравниваются по их значению ({@link WordExample#getOrigin()}).
     * @param examples список примеров к слову
     * @return все примеры, которые отсутствуют в заданном списке.
     */
    public List<WordExample> getMissingExamples(List<WordExample> examples) {
        return examples.stream().
                filter(example -> !containsExampleBy(example.getOrigin())).
                toList();
    }

    public boolean outerSourceNameIs(String outerSourceName) {
        return outerSourceName == this.outerSourceName ||
                outerSourceName != null && outerSourceName.equalsIgnoreCase(this.outerSourceName);
    }

    public long getDaysAfterRecentUpdateDate(Clock clock) {
        return ChronoUnit.DAYS.between(recentUpdateDate, LocalDate.now(clock));
    }

    public long getMonthsAfterRecentUpdateDate(Clock clock) {
        return ChronoUnit.MONTHS.between(recentUpdateDate, LocalDate.now(clock));
    }

    /**
     * Проверяет - содержит ли данное слово примеры без переводов. Если слово вообще не содержит
     * никаких примеров - возвращает false.
     * @return true - если условие выше выполняется, иначе - false.
     */
    public boolean containsExamplesWithoutTranslate() {
        return !examples.isEmpty() &&
                examples.stream().allMatch(example -> example.getTranslate() != null);
    }

    /**
     * Проверяет - содержит ли данное слово пример, метод {@link WordExample#getOrigin()}
     * возвращает значение равное exampleOrigin.
     * @param exampleOrigin значение искомого примера
     * @return true - если описанное выше условие выполняется, иначе - false.
     */
    public boolean containsExampleBy(String exampleOrigin) {
        return examples.stream().
                anyMatch(example -> example.getOrigin().equalsIgnoreCase(exampleOrigin));
    }

    /**
     * см. {@link Entity#generateIdIfAbsent()}
     */
    @Override
    public void generateIdIfAbsent() {
        if(id == null) id = UUID.randomUUID();
    }

    public SupplementedWord setValue(String value) {
        this.value = value;
        return this;
    }

    public SupplementedWord setOuterSourceName(String outerSourceName) {
        this.outerSourceName = outerSourceName;
        return this;
    }

    public SupplementedWord setRecentUpdateDate(LocalDate recentUpdateDate) {
        this.recentUpdateDate = recentUpdateDate;
        return this;
    }

    public SupplementedWord setOuterSourceUri(URI outerSourceUri) {
        this.outerSourceUri = outerSourceUri;
        return this;
    }

    public SupplementedWord addInterpretation(WordInterpretation interpretation) {
        interpretations.add(interpretation);
        return this;
    }

    public SupplementedWord addInterpretations(List<WordInterpretation> interpretations) {
        this.interpretations.addAll(interpretations);
        return this;
    }

    public SupplementedWord addTranscription(WordTranscription transcription) {
        transcriptions.add(transcription);
        return this;
    }

    public SupplementedWord addTranscriptions(List<WordTranscription> transcriptions) {
        this.transcriptions.addAll(transcriptions);
        return this;
    }

    public SupplementedWord addTranslation(WordTranslation translation) {
        translations.add(translation);
        return this;
    }

    public SupplementedWord addTranslations(List<WordTranslation> translations) {
        this.translations.addAll(translations);
        return this;
    }

    public SupplementedWord addExample(SupplementedWordExample example) {
        examples.add(example);
        return this;
    }

    public SupplementedWord addExamples(List<SupplementedWordExample> examples) {
        this.examples.addAll(examples);
        return this;
    }

    /**
     * Удаляет из данного слова все примеры, которые отсутствуют в заданном списке. Все примеры
     * сравниваются по их значению ({@link WordExample#getOrigin()}).
     * @param examples список примеров к слову
     * @return ссылку на этот же объект.
     */
    public SupplementedWord removeRedundantExamples(List<WordExample> examples) {
        this.examples.removeIf(example -> !examples.contains(example));
        return this;
    }

    /**
     * Находит пример со значением exampleOrigin и заменяет его на newExample. Если среди примеров
     * данного слова нет примера с таким значением - метод ничего не делает.
     * @param exampleOrigin значение ({@link WordExample#getOrigin()}) заменяемого примера
     * @param newExample новый пример
     * @return ссылку на этот же объект.
     */
    public SupplementedWord replaceExample(String exampleOrigin, SupplementedWordExample newExample) {
        int index = IntStream.range(0, examples.size()).
                filter(i -> examples.get(i).getOrigin().equalsIgnoreCase(exampleOrigin)).
                findFirst().
                orElse(-1);

        if(index != -1) examples.set(index, newExample);

        return this;
    }

    /**
     * Удаляет интерпретацию по её значению. Если такой интерпретации нет - ничего не делает.
     * @param interpretationValue значение удаляемой интерпретации
     * @return ссылку на этот же объект
     */
    public SupplementedWord removeInterpretationBy(String interpretationValue) {
        interpretations.removeIf(interpretation -> interpretation.getValue().equalsIgnoreCase(interpretationValue));
        return this;
    }

    /**
     * Удаляет транскрипцию по её значению. Если такой транскрипции нет - ничего не делает.
     * @param transcriptionValue значение удаляемой транскрипции
     * @return ссылку на этот же объект
     */
    public SupplementedWord removeTranscriptionBy(String transcriptionValue) {
        transcriptions.removeIf(transcription -> transcription.getValue().equalsIgnoreCase(transcriptionValue));
        return this;
    }

    /**
     * Удаляет перевод по его значению. Если такого перевода нет - ничего не делает.
     * @param translateValue значение удаляемого перевода
     * @return ссылку на этот же объект
     */
    public SupplementedWord removeTranslateBy(String translateValue) {
        translations.removeIf(translation -> translation.getValue().equalsIgnoreCase(translateValue));
        return this;
    }

    /**
     * Удаляет пример по его значению на английском языке. Если такого примера нет - ничего не делает.
     * @param exampleOrigin значение удаляемого примера
     * @return ссылку на этот же объект
     */
    public SupplementedWord removeExampleBy(String exampleOrigin) {
        examples.removeIf(example -> example.getOrigin().equalsIgnoreCase(exampleOrigin));
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SupplementedWord that = (SupplementedWord) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "SupplementedWord{" +
                "id=" + id +
                ", examplesOwnerId=" + examplesOwnerId +
                ", outerSourceName='" + outerSourceName + '\'' +
                ", value='" + value + '\'' +
                ", recentUpdateDate=" + recentUpdateDate +
                ", outerSourceUri=" + outerSourceUri +
                ", interpretations=" + interpretations +
                ", transcriptions=" + transcriptions +
                ", translations=" + translations +
                ", examples=" + examples +
                '}';
    }

}
