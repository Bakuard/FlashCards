package com.bakuard.flashcards.model.word;

import com.bakuard.flashcards.model.Entity;
import com.bakuard.flashcards.model.RepeatDataFromEnglish;
import com.bakuard.flashcards.model.RepeatDataFromNative;
import com.bakuard.flashcards.validation.AllUnique;
import com.bakuard.flashcards.validation.NotBlankOrNull;
import com.bakuard.flashcards.validation.NotContainsNull;
import com.google.common.collect.ImmutableList;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.PersistenceCreator;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Embedded;
import org.springframework.data.relational.core.mapping.MappedCollection;
import org.springframework.data.relational.core.mapping.Table;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.Clock;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Подробные данные об одном слове английского языка в словаре пользователя.
 */
@Table("words")
public class Word implements Entity {

    @Id
    @Column("word_id")
    private UUID id;
    @Column("user_id")
    @NotNull(message = "Word.userId.notNull")
    private final UUID userId;
    @Column("value")
    @NotBlank(message = "Word.value.notBlank")
    private String value;
    @Column("note")
    @NotBlankOrNull(message = "Word.note.notBlankOrNull")
    private String note;
    @MappedCollection(idColumn = "word_id", keyColumn = "index")
    @NotContainsNull(message = "Word.interpretations.notContainsNull")
    @AllUnique(nameOfGetterMethod = "getValue", message = "Word.interpretations.allUnique")
    private final List<@Valid WordInterpretation> interpretations;
    @MappedCollection(idColumn = "word_id", keyColumn = "index")
    @NotContainsNull(message = "Word.transcriptions.notContainsNull")
    @AllUnique(nameOfGetterMethod = "getValue", message = "Word.transcriptions.allUnique")
    private final List<@Valid WordTranscription> transcriptions;
    @MappedCollection(idColumn = "word_id", keyColumn = "index")
    @NotContainsNull(message = "Word.translations.notContainsNull")
    @AllUnique(nameOfGetterMethod = "getValue", message = "Word.translations.allUnique")
    private final List<@Valid WordTranslation> translations;
    @MappedCollection(idColumn = "word_id", keyColumn = "index")
    @NotContainsNull(message = "Word.examples.notContainsNull")
    @AllUnique(nameOfGetterMethod = "getOrigin", message = "Word.examples.allUnique")
    private final List<@Valid WordExample> examples;
    @Embedded.Nullable
    @Valid
    private RepeatDataFromEnglish repeatDataFromEnglish;
    @Embedded.Nullable
    @Valid
    private RepeatDataFromNative repeatDataFromNative;

    /**
     * Данный конструктор используется слоем доступа к данным для загрузки слова.
     * @param id уникальный идентификатор слова.
     * @param userId уникальный идентификатор пользователя к словарю которого относится это слово.
     * @param value значение слова на английском языке.
     * @param note примечание к слову добавляемое пользователем.
     * @param interpretations интерпретация слова.
     * @param transcriptions транскрипции слова.
     * @param translations переводы слова.
     * @param examples примеры слова.
     * @param repeatDataFromEnglish данные последнего повторения слова с английского языка.
     * @param repeatDataFromNative данные последнего повторения слова с родного языка пользователя.
     */
    @PersistenceCreator
    public Word(UUID id,
                UUID userId,
                String value,
                String note,
                List<WordInterpretation> interpretations,
                List<WordTranscription> transcriptions,
                List<WordTranslation> translations,
                List<WordExample> examples,
                RepeatDataFromEnglish repeatDataFromEnglish,
                RepeatDataFromNative repeatDataFromNative) {
        this.id = id;
        this.userId = userId;
        this.value = value;
        this.note = note;
        this.interpretations = interpretations;
        this.transcriptions = transcriptions;
        this.translations = translations;
        this.examples = examples;
        this.repeatDataFromEnglish = repeatDataFromEnglish;
        this.repeatDataFromNative = repeatDataFromNative;
    }

    /**
     * Создает новое слово для словаря указанного пользователя.
     * @param userId уникальный идентификатор пользователя к словарю которого относится это слово.
     * @param lowestIntervalForEnglish Наименьший из всех интервалов повторения данного пользователя.
     *                                 Подробнее см. {@link RepeatDataFromEnglish}.
     * @param lowestIntervalForNative Наименьший из всех интервалов повторения данного пользователя.
     *                                Подробнее см. {@link RepeatDataFromNative}.
     * @param clock используется для получения текущей даты и возможности её определения в тестах.
     */
    public Word(UUID userId, int lowestIntervalForEnglish, int lowestIntervalForNative, Clock clock) {
        this.userId = userId;
        this.interpretations = new ArrayList<>();
        this.transcriptions = new ArrayList<>();
        this.translations = new ArrayList<>();
        this.examples = new ArrayList<>();
        this.repeatDataFromEnglish = new RepeatDataFromEnglish(lowestIntervalForEnglish, LocalDate.now(clock));
        this.repeatDataFromNative = new RepeatDataFromNative(lowestIntervalForNative, LocalDate.now(clock));
    }

    /**
     * Выполняет глубокое копирование для указанного слова.
     * @param other копируемое слово.
     */
    public Word(Word other) {
        this.id = other.id;
        this.userId = other.userId;
        this.value = other.value;
        this.note = other.note;
        this.interpretations = other.interpretations.stream().
                map(WordInterpretation::new).
                collect(Collectors.toCollection(ArrayList::new));
        this.transcriptions = other.transcriptions.stream().
                map(WordTranscription::new).
                collect(Collectors.toCollection(ArrayList::new));
        this.translations = other.translations.stream().
                map(WordTranslation::new).
                collect(Collectors.toCollection(ArrayList::new));
        this.examples = other.examples.stream().
                map(WordExample::new).
                collect(Collectors.toCollection(ArrayList::new));
        this.repeatDataFromEnglish = RepeatDataFromEnglish.copy(other.repeatDataFromEnglish);
        this.repeatDataFromNative = RepeatDataFromNative.copy(other.repeatDataFromNative);
    }

    /**
     * см. {@link Entity#getId()}
     */
    @Override
    public UUID getId() {
        return id;
    }

    /**
     * см. {@link Entity#isNew()}
     */
    @Override
    public boolean isNew() {
        return id == null;
    }

    /**
     * Возвращает уникальный идентификатор пользователя к словарю которого относится слово.
     * @return уникальный идентификатор пользователя к словарю которого относится слово.
     */
    public UUID getUserId() {
        return userId;
    }

    /**
     * Возвращает значение слова на английском языке.
     * @return значение слова на английском языке.
     */
    public String getValue() {
        return value;
    }

    /**
     * Возвращает примечание к слову добавленное пользователем.
     * @return примечание к слову.
     */
    public String getNote() {
        return note;
    }

    /**
     * Возвращает список всех интерпретация слова.
     * @return список всех интерпретация слова.
     */
    public List<WordInterpretation> getInterpretations() {
        return Collections.unmodifiableList(interpretations);
    }

    /**
     * Возвращает дату последнего обновления интерпретаций к слову из внешнего сервиса с указанным именем. Если
     * среди интерпретаций слова нет интерпретаций полученных из указанного внешнего источника, то метод возвращает
     * пустой Optional.
     * @param outerSourceName имя внешнего сервиса используемого для получения интерпретаций к слову.
     * @return дата последнего обновления интерпретаций к слову.
     */
    public Optional<LocalDate> getInterpretationsRecentUpdateDate(String outerSourceName) {
        return interpretations.stream().
                filter(interpretation -> interpretation.hasOuterSource(outerSourceName)).
                findFirst().
                flatMap(interpretation -> interpretation.getRecentUpdateDate(outerSourceName));
    }

    /**
     * Возвращает список всех транскрипций слова.
     * @return список всех транскрипций слова.
     */
    public List<WordTranscription> getTranscriptions() {
        return Collections.unmodifiableList(transcriptions);
    }

    /**
     * Возвращает дату последнего обновления транскрипций к слову из внешнего сервиса с указанным именем. Если
     * среди транскрипций слова нет транскрипций полученных из указанного внешнего источника, то метод возвращает
     * пустой Optional.
     * @param outerSourceName имя внешнего сервиса используемого для получения транскрипций к слову.
     * @return дата последнего обновления транскрипций к слову.
     */
    public Optional<LocalDate> getTranscriptionsRecentUpdateDate(String outerSourceName) {
        return transcriptions.stream().
                filter(transcription -> transcription.hasOuterSource(outerSourceName)).
                findFirst().
                flatMap(transcription -> transcription.getRecentUpdateDate(outerSourceName));
    }

    /**
     * Проверяет - имеет ли слово транскрипции полученные из внешнего сервиса с указанным именем.
     * @param outerSourceName имя внешнего сервиса.
     * @return true - если слово имеет транскрипции полученные из указанного внешнего сервиса, иначе - false.
     */
    public boolean hasTranscriptionsOfOuterSource(String outerSourceName) {
        return transcriptions.stream().
                anyMatch(transcription -> transcription.hasOuterSource(outerSourceName));
    }

    /**
     * Возвращает все переводы данного слова.
     * @return все переводы данного слова.
     */
    public List<WordTranslation> getTranslations() {
        return Collections.unmodifiableList(translations);
    }

    /**
     * Возвращает дату последнего обновления переводов слова из внешнего сервиса с указанным именем. Если
     * среди переводов слова нет переводов полученных из указанного внешнего источника, то метод возвращает
     * пустой Optional.
     * @param outerSourceName имя внешнего сервиса используемого для получения переводов к слову.
     * @return дата последнего обновления переводов к слову.
     */
    public Optional<LocalDate> getTranslationsRecentUpdateDate(String outerSourceName) {
        return translations.stream().
                filter(translation -> translation.hasOuterSource(outerSourceName)).
                findFirst().
                flatMap(translation -> translation.getRecentUpdateDate(outerSourceName));
    }

    /**
     * Возвращает список всех примеров к слову.
     * @return список всех примеров к слову.
     */
    public List<WordExample> getExamples() {
        return Collections.unmodifiableList(examples);
    }

    /**
     * Возвращает данные последнего повторения слова с английского языка на родной язык пользователя.
     * @return данные последнего повторения слова с английского языка.
     */
    public RepeatDataFromEnglish getRepeatDataFromEnglish() {
        return repeatDataFromEnglish;
    }

    /**
     * Возвращает данные последнего повторения слова с родного языка пользователя на английский язык.
     * @return данные последнего повторения слова с родного языка пользователя.
     */
    public RepeatDataFromNative getRepeatDataFromNative() {
        return repeatDataFromNative;
    }

    /**
     * см. {@link Entity#generateIdIfAbsent()}
     */
    @Override
    public void generateIdIfAbsent() {
        if(id == null) id = UUID.randomUUID();
    }

    public Word setValue(String value) {
        this.value = value;
        return this;
    }

    public Word setNote(String note) {
        this.note = note;
        return this;
    }

    public Word setInterpretations(List<WordInterpretation> interpretations) {
        this.interpretations.clear();
        if(interpretations != null) this.interpretations.addAll(interpretations);
        return this;
    }

    public Word setTranscriptions(List<WordTranscription> transcriptions) {
        this.transcriptions.clear();
        if(transcriptions != null) this.transcriptions.addAll(transcriptions);
        return this;
    }

    public Word setTranslations(List<WordTranslation> translations) {
        this.translations.clear();
        if(translations != null) this.translations.addAll(translations);
        return this;
    }

    public Word setExamples(List<WordExample> examples) {
        this.examples.clear();
        if(examples != null) this.examples.addAll(examples);
        return this;
    }

    /**
     * Ищет среди интерпретаций слова такую, значение ({@link WordInterpretation#getValue()}) которой
     * равняется значению указанной интерпретации. Если такая интерпретация есть, то для неё будет вызван
     * метод {@link WordInterpretation#merge(WordInterpretation)}. Если такой интерпретации нет, то заданная
     * интерпретация будет добавлена в список интерпретаций этого слова.
     * @param interpretation интерпретация, данные которой копируются в список интерпретаций этого слова.
     * @return ссылку на этот же объект.
     */
    public Word mergeInterpretation(WordInterpretation interpretation) {
        boolean isMerged = false;
        for(int i = 0; i < interpretations.size() && !isMerged; i++) {
            isMerged = interpretations.get(i).merge(interpretation);
        }
        if(!isMerged) interpretations.add(interpretation);
        return this;
    }

    /**
     * Ищет среди транскрипций слова такую, значение ({@link WordTranscription#getValue()}) которой
     * равняется значению указанной транскрипции. Если такая транскрипция есть, то для неё будет вызван
     * метод {@link WordTranscription#merge(WordTranscription)}. Если такой транскрипции нет, то заданная
     * транскрипция будет добавлена в список транскрипций этого слова.
     * @param transcription транскрипция, данные которой копируются в список транскрипций этого слова.
     * @return ссылку на этот же объект.
     */
    public Word mergeTranscription(WordTranscription transcription) {
        boolean isMerged = false;
        for(int i = 0; i < transcriptions.size() && !isMerged; i++) {
            isMerged = transcriptions.get(i).merge(transcription);
        }
        if(!isMerged) transcriptions.add(transcription);
        return this;
    }

    /**
     * Ищет среди переводов слова такое, значение ({@link WordTranslation#getValue()}) которого
     * равняется значению указанного перевода. Если такой перевод есть, то для него будет вызван
     * метод {@link WordTranslation#merge(WordTranslation)}. Если такого перевода нет, то заданный
     * перевод будет добавлен в список переводов этого слова.
     * @param translation перевод, данные которого копируются в список переводов этого слова.
     * @return ссылку на этот же объект.
     */
    public Word mergeTranslation(WordTranslation translation) {
        boolean isMerged = false;
        for(int i = 0; i < translations.size() && !isMerged; i++) {
            isMerged = translations.get(i).merge(translation);
        }
        if(!isMerged) translations.add(translation);
        return this;
    }

    /**
     * Ищет среди примеров слова такое, значение ({@link WordTranslation#getValue()}) которого
     * равняется значению указанного примера. Если такой пример есть, то для него будет вызван
     * метод {@link WordTranslation#merge(WordTranslation)}. Если такого перевода нет, то метод
     * ничего не делает.
     * @param example пример, данные которого копируются в список примеров этого слова.
     * @return ссылку на этот же объект.
     */
    public Word mergeExampleIfPresent(WordExample example) {
        boolean isMerged = false;
        for(int i = 0; i < examples.size() && !isMerged; i++) {
            isMerged = examples.get(i).mergeIfHasSameValue(example);
        }
        return this;
    }

    public Word addInterpretation(WordInterpretation interpretation) {
        interpretations.add(interpretation);
        return this;
    }

    public Word addTranscription(WordTranscription transcription) {
        transcriptions.add(transcription);
        return this;
    }

    public Word addTranslation(WordTranslation translation) {
        translations.add(translation);
        return this;
    }

    public Word addExample(WordExample example) {
        examples.add(example);
        return this;
    }

    /**
     * Задает результат последнего повторения этого слова с английского языка на родной язык пользователя.
     * @param isRemember true - если пользователь правильно вспомнил переводы, произношение и толкования слова,
     *                   иначе - false.
     * @param lastDateOfRepeat дата текущего повторения.
     * @param intervals Все интервалы повторения (подробнее см. {@link com.bakuard.flashcards.service.IntervalService})
     *                  пользователя.
     */
    public void repeatFromEnglish(boolean isRemember, LocalDate lastDateOfRepeat, ImmutableList<Integer> intervals) {
        int index = isRemember ?
                Math.min(intervals.indexOf(repeatDataFromEnglish.interval()) + 1, intervals.size() - 1) : 0;

        repeatDataFromEnglish = new RepeatDataFromEnglish(intervals.get(index), lastDateOfRepeat);
    }

    /**
     * Проверяет указанное пользователем значение английского слова при его повторении с родного языка пользователя
     * на английский язык. Если заданное значение равняется значению текущего слова - повторения считается успешным.
     * @param inputValue значение слова на английском языке.
     * @param lastDateOfRepeat дата текущего повторения.
     * @param intervals Все интервалы повторения (подробнее см. {@link com.bakuard.flashcards.service.IntervalService})
     *                  пользователя.
     * @return true - если повторение выполнено успешно, иначе - false.
     */
    public boolean repeatFromNative(String inputValue, LocalDate lastDateOfRepeat, ImmutableList<Integer> intervals) {
        boolean isRemember = inputValue.equalsIgnoreCase(value);
        int index = isRemember ?
                Math.min(intervals.indexOf(repeatDataFromNative.interval()) + 1, intervals.size() - 1) : 0;

        repeatDataFromNative = new RepeatDataFromNative(intervals.get(index), lastDateOfRepeat);

        return isRemember;
    }

    /**
     * Указывает, что пользователь забыл перевод данного слова с английского на родной язык и его требуется повторить
     * в ближайшее время. Метод отметит текущую дату, как дату последнего повторения, установит наименьший из интервалов
     * повторения пользователя.
     * @param lastDateOfRepeat текущая дата.
     * @param lowestInterval Наименьший из интервалов повторения пользователя
     *                       (подробнее см. {@link com.bakuard.flashcards.service.IntervalService})
     */
    public void markForRepetitionFromEnglish(LocalDate lastDateOfRepeat, int lowestInterval) {
        repeatDataFromEnglish = new RepeatDataFromEnglish(lowestInterval, lastDateOfRepeat);
    }

    /**
     * Указывает, что пользователь забыл перевод данного слова с родного языка на английский и его требуется повторить
     * в ближайшее время. Метод отметит текущую дату, как дату последнего повторения, установит наименьший из интервалов
     * повторения пользователя.
     * @param lastDateOfRepeat текущая дата.
     * @param lowestInterval Наименьший из интервалов повторения пользователя
     *                       (подробнее см. {@link com.bakuard.flashcards.service.IntervalService})
     */
    public void markForRepetitionFromNative(LocalDate lastDateOfRepeat, int lowestInterval) {
        repeatDataFromNative = new RepeatDataFromNative(lowestInterval, lastDateOfRepeat);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Word word = (Word) o;
        return Objects.equals(id, word.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    @Override
    public String toString() {
        return "Word{" +
                "id=" + id +
                ", userId=" + userId +
                ", value='" + value + '\'' +
                ", note='" + note + '\'' +
                ", interpretations=" + interpretations +
                ", transcriptions=" + transcriptions +
                ", translations=" + translations +
                ", examples=" + examples +
                ", repeatDataFromEnglish=" + repeatDataFromEnglish +
                ", repeatDataFromNative=" + repeatDataFromNative +
                '}';
    }

}
