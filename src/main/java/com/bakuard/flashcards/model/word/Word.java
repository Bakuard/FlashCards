package com.bakuard.flashcards.model.word;

import com.bakuard.flashcards.model.Entity;
import com.bakuard.flashcards.model.RepeatDataFromEnglish;
import com.bakuard.flashcards.model.RepeatDataFromNative;
import com.bakuard.flashcards.validation.annotation.AllUnique;
import com.bakuard.flashcards.validation.annotation.NotBlankOrNull;
import com.bakuard.flashcards.validation.annotation.NotContainsNull;
import com.google.common.collect.ImmutableList;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.PersistenceCreator;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Embedded;
import org.springframework.data.relational.core.mapping.MappedCollection;
import org.springframework.data.relational.core.mapping.Table;

import java.time.Clock;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
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
    private List<@Valid WordInterpretation> interpretations;
    @MappedCollection(idColumn = "word_id", keyColumn = "index")
    @NotContainsNull(message = "Word.transcriptions.notContainsNull")
    @AllUnique(nameOfGetterMethod = "getValue", message = "Word.transcriptions.allUnique")
    private List<@Valid WordTranscription> transcriptions;
    @MappedCollection(idColumn = "word_id", keyColumn = "index")
    @NotContainsNull(message = "Word.translations.notContainsNull")
    @AllUnique(nameOfGetterMethod = "getValue", message = "Word.translations.allUnique")
    private List<@Valid WordTranslation> translations;
    @MappedCollection(idColumn = "word_id", keyColumn = "index")
    @NotContainsNull(message = "Word.examples.notContainsNull")
    @AllUnique(nameOfGetterMethod = "getOrigin", message = "Word.examples.allUnique")
    private List<@Valid WordExample> examples;
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
     * См. {@link Entity#getId()}
     */
    @Override
    public UUID getId() {
        return id;
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
     * Возвращает список всех интерпретаций слова.
     * @return список всех интерпретация слова.
     */
    public List<WordInterpretation> getInterpretations() {
        return interpretations;
    }

    /**
     * Возвращает список всех транскрипций слова.
     * @return список всех транскрипций слова.
     */
    public List<WordTranscription> getTranscriptions() {
        return transcriptions;
    }

    /**
     * Возвращает все переводы данного слова.
     * @return все переводы данного слова.
     */
    public List<WordTranslation> getTranslations() {
        return translations;
    }

    /**
     * Возвращает список всех примеров к слову.
     * @return список всех примеров к слову.
     */
    public List<WordExample> getExamples() {
        return examples;
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
     * См. {@link Entity#generateIdIfAbsent()}
     */
    @Override
    public void generateIdIfAbsent() {
        if(id == null) id = UUID.randomUUID();
    }

    /**
     * Устанавливает значение слова.
     * @param value значение слова
     * @return ссылку на этот же объект
     */
    public Word setValue(String value) {
        this.value = value;
        return this;
    }

    /**
     * Устанавливает примечание к слову.
     * @param note примечание к слову
     * @return ссылку на этот же объект
     */
    public Word setNote(String note) {
        this.note = note;
        return this;
    }

    /**
     * Устанавливает список интерпретаций к слову.
     * @param interpretations список интерпретаций к слову
     * @return ссылку на этот же объект
     */
    public Word setInterpretations(List<WordInterpretation> interpretations) {
        this.interpretations = interpretations;
        return this;
    }

    /**
     * Устанавливает список транскрипций к слову.
     * @param transcriptions список транскрипций к слову
     * @return ссылку на этот же объект
     */
    public Word setTranscriptions(List<WordTranscription> transcriptions) {
        this.transcriptions = transcriptions;
        return this;
    }

    /**
     * Устанавливает список переводов к слову.
     * @param translations список переводов к слову
     * @return ссылку на этот же объект
     */
    public Word setTranslations(List<WordTranslation> translations) {
        this.translations = translations;
        return this;
    }

    /**
     * Устанавливает список примеров к слову.
     * @param examples список примеров к слову
     * @return ссылку на этот же объект
     */
    public Word setExamples(List<WordExample> examples) {
        this.examples = examples;
        return this;
    }

    /**
     * Добавляет указанную интерпретацию к данному слову.
     * @param interpretation добавляемая интерпретация
     * @return ссылку на этот же объект
     */
    public Word addInterpretation(WordInterpretation interpretation) {
        interpretations.add(interpretation);
        return this;
    }

    /**
     * Добавляет указанную транскрипцию к данному слову.
     * @param transcription добавляемая транскрипция
     * @return ссылку на этот же объект
     */
    public Word addTranscription(WordTranscription transcription) {
        transcriptions.add(transcription);
        return this;
    }

    /**
     * Добавляет указанный перевод к данному слову.
     * @param translation добавляемый перевод
     * @return ссылку на этот же объект
     */
    public Word addTranslation(WordTranslation translation) {
        translations.add(translation);
        return this;
    }

    /**
     * Добавляет указанный пример к данному слову.
     * @param example добавляемый пример
     * @return ссылку на этот же объект
     */
    public Word addExample(WordExample example) {
        examples.add(example);
        return this;
    }

    /**
     * Удаляет интерпретацию по её значению. Если такой интерпретации нет - ничего не делает.
     * @param interpretationValue значение удаляемой интерпретации
     * @return ссылку на этот же объект
     */
    public Word removeInterpretationBy(String interpretationValue) {
        interpretations.removeIf(interpretation -> interpretation.getValue().equalsIgnoreCase(interpretationValue));
        return this;
    }

    /**
     * Удаляет транскрипцию по её значению. Если такой транскрипции нет - ничего не делает.
     * @param transcriptionValue значение удаляемой транскрипции
     * @return ссылку на этот же объект
     */
    public Word removeTranscriptionBy(String transcriptionValue) {
        transcriptions.removeIf(transcription -> transcription.getValue().equalsIgnoreCase(transcriptionValue));
        return this;
    }

    /**
     * Удаляет перевод по его значению. Если такого перевода нет - ничего не делает.
     * @param translateValue значение удаляемого перевода
     * @return ссылку на этот же объект
     */
    public Word removeTranslateBy(String translateValue) {
        translations.removeIf(translation -> translation.getValue().equalsIgnoreCase(translateValue));
        return this;
    }

    /**
     * Удаляет пример по его значению на английском языке. Если такого примера нет - ничего не делает.
     * @param exampleOrigin значение удаляемого примера
     * @return ссылку на этот же объект
     */
    public Word removeExampleBy(String exampleOrigin) {
        examples.removeIf(example -> example.getOrigin().equalsIgnoreCase(exampleOrigin));
        return this;
    }

    /**
     * Задает результат последнего повторения этого слова с английского языка на родной язык пользователя.
     * @param repeatDataFromEnglish (См. {@link RepeatDataFromEnglish}).
     * @return ссылку на этот же объект
     */
    public Word setRepeatDataFromEnglish(RepeatDataFromEnglish repeatDataFromEnglish) {
        this.repeatDataFromEnglish = repeatDataFromEnglish;
        return this;
    }

    /**
     * Задает результат последнего повторения этого слова с родного языка пользователя на английский язык.
     * @param repeatDataFromNative (См. {@link RepeatDataFromNative}).
     * @return ссылку на этот же объект
     */
    public Word setRepeatDataFromNative(RepeatDataFromNative repeatDataFromNative) {
        this.repeatDataFromNative = repeatDataFromNative;
        return this;
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
