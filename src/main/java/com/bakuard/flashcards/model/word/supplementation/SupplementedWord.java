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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.IntStream;

/**
 * Содержит результат дополнения заданного английского слова транскрипциями, толкованиями, переводами
 * или переводами примеров полученных из одного заданного внешнего сервиса. Внешний сервис
 * необязательно должен возвращать и транскрипциями, и толкованиями, и переводы, и переводы примеров. Он
 * может вернуть, например, только транскрипции.
 */
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

    /**
     * Данный конструктор используется слоем доступа к данным для загрузки результата дополнения английского слова
     * из внешнего сервиса.
     * @param id уникальный идентификатор результата дополнения английского слова из внешнего сервиса.
     * @param examplesOwnerId идентификатор пользователя, примеры к слову которого были переведены из внешнего
     *                        сервиса.
     * @param value значение дополняемого слова.
     * @param outerSourceName наименование внешнего сервиса.
     * @param recentUpdateDate последняя дата, когда указанный источник использовался для дополнения или
     *                         обновления дополнений к указанному слову.
     * @param outerSourceUri ссылка на внешний сервис.
     */
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

    /**
     * Создает новый объект для хранения результата дополнения английского слова из внешнего сервиса.
     * @param examplesOwnerId идентификатор пользователя, примеры к слову которого были переведены из внешнего
     *                        сервиса.
     * @param value значение дополняемого слова.
     * @param outerSourceName наименование внешнего сервиса.
     * @param recentUpdateDate последняя дата, когда указанный источник использовался для дополнения или
     *                         обновления дополнений к указанному слову.
     * @param outerSourceUri ссылка на внешний сервис.
     */
    public SupplementedWord(UUID examplesOwnerId,
                            String value,
                            String outerSourceName,
                            LocalDate recentUpdateDate,
                            URI outerSourceUri) {
        this(null,
                examplesOwnerId,
                value,
                outerSourceName,
                recentUpdateDate,
                outerSourceUri);
    }

    /**
     * Конструктор глубокого копирования
     */
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

    /**
     * Возвращает значение дополняемого слова.
     * @return значение дополняемого слова.
     */
    public String getValue() {
        return value;
    }

    /**
     * Возвращает наименование внешнего сервиса из которого были получены транскрипции, переводы,
     * толкования или переводы примеров к слову.
     */
    public String getOuterSourceName() {
        return outerSourceName;
    }

    /**
     * Возвращает последнюю дату, когда данное слово было дополнено транскрипциями, переводами,
     * переводами или переводами примеров к этому слову из данного внешнего источника.
     */
    public LocalDate getRecentUpdateDate() {
        return recentUpdateDate;
    }

    /**
     * Ссылку на внешний сервис.
     */
    public URI getOuterSourceUri() {
        return outerSourceUri;
    }

    /**
     * Возвращает все интерпретации к данному слову полученные из внешнего сервиса. Список может быть
     * пустым.
     * @return интерпретации к данному слову.
     */
    public List<WordInterpretation> getInterpretations() {
        return Collections.unmodifiableList(interpretations);
    }

    /**
     * Возвращает все транскрипции к данному слову полученные из внешнего сервиса. Список может быть
     * пустым.
     * @return транскрипции к данному слову.
     */
    public List<WordTranscription> getTranscriptions() {
        return Collections.unmodifiableList(transcriptions);
    }

    /**
     * Возвращает все переводы к данному слову полученные из внешнего сервиса. Список может быть
     * пустым.
     * @return переводы к данному слову.
     */
    public List<WordTranslation> getTranslations() {
        return Collections.unmodifiableList(translations);
    }

    /**
     * Возвращает все переводы примеров к данному слову полученные из внешнего сервиса. Список может быть
     * пустым.
     * @return переводы примеров к данному слову.
     */
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

    /**
     * Проверяет - равняется ли переданное значение наименованию внешнего сервиса без учета регистра
     * символов. Если это не так, или переданное значение равно null - метод вернет false.
     * @param outerSourceName предполагаемое наименование внешнего сервиса
     * @return true - если переданное значение равняется наименованию внешнего сервиса без учета регистра
     *         символов, иначе - false.
     */
    public boolean outerSourceNameIs(String outerSourceName) {
        return outerSourceName == this.outerSourceName ||
                outerSourceName != null && outerSourceName.equalsIgnoreCase(this.outerSourceName);
    }

    /**
     * Возвращает кол-во дней прошедшее с последнего дополнения данного слова из данного источника.
     * @param clock используется для получения текущей даты
     */
    public long getDaysAfterRecentUpdateDate(Clock clock) {
        return ChronoUnit.DAYS.between(recentUpdateDate, LocalDate.now(clock));
    }

    /**
     * Возвращает кол-во месяцев прошедшее с последнего дополнения данного слова из данного источника.
     * @param clock используется для получения текущей даты
     */
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

    /**
     * Устанавливает значение дополняемого слова.
     * @param value значение дополняемого слова.
     * @return ссылку на этот же объект.
     */
    public SupplementedWord setValue(String value) {
        this.value = value;
        return this;
    }

    /**
     * Устанавливает наименование внешнего сервиса используемого для дополнения данного слова
     * транскрипциями, толкованиями, переводами или переводами примеров.
     * @param outerSourceName наименование внешнего сервиса используемого для дополнения данного слова.
     * @return ссылку на этот же объект.
     */
    public SupplementedWord setOuterSourceName(String outerSourceName) {
        this.outerSourceName = outerSourceName;
        return this;
    }

    /**
     * Устанавливает последнюю дату обращения к заданному внешнему сервису для дополнения слова
     * транскрипциями, толкованиями, переводами или переводами примеров.
     * @param recentUpdateDate дата последнего обращения к заданному внешнему сервису.
     * @return ссылку на этот же объект.
     */
    public SupplementedWord setRecentUpdateDate(LocalDate recentUpdateDate) {
        this.recentUpdateDate = recentUpdateDate;
        return this;
    }

    /**
     * Устанавливает ссылку на внешний сервис используемый для дополнения слова транскрипциями,
     * толкованиями, переводами или переводами примеров.
     * @param outerSourceUri ссылку на внешний сервис.
     * @return ссылку на этот же объект.
     */
    public SupplementedWord setOuterSourceUri(URI outerSourceUri) {
        this.outerSourceUri = outerSourceUri;
        return this;
    }

    /**
     * Добавляет полученную из внешнего сервиса интерпретацию к данному слову. Если интерпретация
     * с таким значением уже есть у данного слова - метод ничего не делает.
     * @param interpretation добавляемая интерпретация
     * @return ссылку на этот же объект.
     */
    public SupplementedWord addInterpretation(WordInterpretation interpretation) {
        if(interpretations.stream().noneMatch(i -> i.getValue().equalsIgnoreCase(interpretation.getValue()))) {
            interpretations.add(interpretation);
        }
        return this;
    }

    /**
     * Добавляет все полученные из внешнего сервиса интерпретации к данному слову. Метод исключит все
     * дублируемые интерпретации (сравнение производится по их значению).
     * @param interpretations добавляемые интерпретации
     * @return ссылку на этот же объект
     */
    public SupplementedWord addInterpretations(List<WordInterpretation> interpretations) {
        interpretations.forEach(this::addInterpretation);
        return this;
    }

    /**
     * Добавляет полученную из внешнего сервиса транскрипцию к данному слову. Если транскрипция
     * с таким же значением уже есть у данного слова - метод ничего не делает.
     * @param transcription добавляемая транскрипция
     * @return ссылку на этот же объект.
     */
    public SupplementedWord addTranscription(WordTranscription transcription) {
        if(transcriptions.stream().noneMatch(t -> t.getValue().equalsIgnoreCase(transcription.getValue()))) {
            transcriptions.add(transcription);
        }
        return this;
    }

    /**
     * Добавляет все полученные из внешнего сервиса транскрипции к данному слову. Метод исключит все
     * дублируемые транскрипции (сравнение производится по их значению).
     * @param transcriptions добавляемые транскрипции
     * @return ссылку на этот же объект
     */
    public SupplementedWord addTranscriptions(List<WordTranscription> transcriptions) {
        transcriptions.forEach(this::addTranscription);
        return this;
    }

    /**
     * Добавляет полученный из внешнего сервиса перевод к данному слову. Если перевод
     * с таким же значением уже есть у данного слова - метод ничего не делает.
     * @param translation добавляемый перевод слова
     * @return ссылку на этот же объект.
     */
    public SupplementedWord addTranslation(WordTranslation translation) {
        if(translations.stream().noneMatch(t -> t.getValue().equalsIgnoreCase(translation.getValue()))) {
            translations.add(translation);
        }
        return this;
    }

    /**
     * Добавляет все полученные из внешнего сервиса переводы к данному слову. Метод исключит все
     * дублируемые переводы (сравнение производится по их значению).
     * @param translations добавляемые переводы
     * @return ссылку на этот же объект
     */
    public SupplementedWord addTranslations(List<WordTranslation> translations) {
        translations.forEach(this::addTranslation);
        return this;
    }

    /**
     * Добавляет полученный из внешнего сервиса перевод примера к данному слову. Если перевод примера
     * с таким же значением уже есть у данного слова - метод ничего не делает.
     * @param example добавляемый перевод примера к слову
     * @return ссылку на этот же объект.
     */
    public SupplementedWord addExample(SupplementedWordExample example) {
        if(examples.stream().noneMatch(e -> e.getOrigin().equalsIgnoreCase(example.getOrigin()))) {
            examples.add(example);
        }
        return this;
    }

    /**
     * Добавляет все полученные из внешнего сервиса переводы примеров к данному слову.
     * Метод исключит все дублируемые переводы примеров (сравнение производится по их значению).
     * @param examples добавляемые переводы примеров
     * @return ссылку на этот же объект
     */
    public SupplementedWord addExamples(List<SupplementedWordExample> examples) {
        examples.forEach(this::addExample);
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
