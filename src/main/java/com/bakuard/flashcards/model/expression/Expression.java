package com.bakuard.flashcards.model.expression;

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

/**
 * Подробные данные об устойчевом выражении в словаре пользователя.
 */
@Table("expressions")
public class Expression implements Entity {

    @Id
    @Column("expression_id")
    private UUID id;
    @Column("user_id")
    @NotNull(message = "Expression.userId.notNull")
    private final UUID userId;
    @Column("value")
    @NotBlank(message = "Expression.value.notBlank")
    private String value;
    @Column("note")
    @NotBlankOrNull(message = "Expression.note.notBlankOrNull")
    private String note;
    @MappedCollection(idColumn = "expression_id", keyColumn = "index")
    @NotContainsNull(message = "Expression.interpretations.notContainsNull")
    @AllUnique(nameOfGetterMethod = "getValue", message = "Expression.interpretations.allUnique")
    private final List<@Valid ExpressionInterpretation> interpretations;
    @MappedCollection(idColumn = "expression_id", keyColumn = "index")
    @NotContainsNull(message = "Expression.translations.notContainsNull")
    @AllUnique(nameOfGetterMethod = "getValue", message = "Expression.translations.allUnique")
    private final List<@Valid ExpressionTranslation> translations;
    @MappedCollection(idColumn = "expression_id", keyColumn = "index")
    @NotContainsNull(message = "Expression.examples.notContainsNull")
    @AllUnique(nameOfGetterMethod = "getOrigin", message = "Expression.examples.allUnique")
    private final List<@Valid ExpressionExample> examples;
    @Embedded.Nullable
    @Valid
    private RepeatDataFromEnglish repeatDataFromEnglish;
    @Embedded.Nullable
    @Valid
    private RepeatDataFromNative repeatDataFromNative;

    /**
     * Данный конструктор используется слоем доступа к данным для загрузки устойчевого выражения.
     * @param id уникальный идентификатор устойчевого выражения.
     * @param userId уникальный идентификатор пользователя к словарю которого относится это устойчевое выражение.
     * @param value устойчевое выражение на английском языке
     * @param note примечание к устоцчевому выражению.
     * @param interpretations список интерпритаций устойчевого выражения.
     * @param translations список переводов устойчевого выражения.
     * @param examples список примеров к устойчевому выражению.
     * @param repeatDataFromEnglish данные последнего повторения устойчевого выражения с английского языка.
     * @param repeatDataFromNative данные последнего повторения устойчевого выражения с родного языка пользователя.
     */
    @PersistenceCreator
    public Expression(UUID id,
                      UUID userId,
                      String value,
                      String note,
                      List<ExpressionInterpretation> interpretations,
                      List<ExpressionTranslation> translations,
                      List<ExpressionExample> examples,
                      RepeatDataFromEnglish repeatDataFromEnglish,
                      RepeatDataFromNative repeatDataFromNative) {
        this.id = id;
        this.userId = userId;
        this.value = value;
        this.note = note;
        this.interpretations = interpretations;
        this.translations = translations;
        this.examples = examples;
        this.repeatDataFromEnglish = repeatDataFromEnglish;
        this.repeatDataFromNative = repeatDataFromNative;
    }

    /**
     * Создает новое устойчевое выражение для словаря пользователя.
     * @param userId уникальный идентификатор пользователя к словарю которого относится это устойчевое выражение.
     * @param lowestIntervalForEnglish наименьший из всех интервалов повторения данного пользователя.
     *                                 Подробнее см. {@link RepeatDataFromEnglish}.
     * @param lowestIntervalForNative наименьший из всех интервалов повторения данного пользователя.
     *                                Подробнее см. {@link RepeatDataFromNative}.
     * @param clock используется для получения текущей даты и возможности её опредления в тестах.
     */
    public Expression(UUID userId,
                      int lowestIntervalForEnglish,
                      int lowestIntervalForNative,
                      Clock clock) {
        this.userId = userId;
        this.interpretations = new ArrayList<>();
        this.translations = new ArrayList<>();
        this.examples = new ArrayList<>();
        this.repeatDataFromEnglish = new RepeatDataFromEnglish(lowestIntervalForEnglish, LocalDate.now(clock));
        this.repeatDataFromNative = new RepeatDataFromNative(lowestIntervalForNative, LocalDate.now(clock));
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
     * Возвращает уникальный идентификатор пользователя к словарю которого отностися устойчевое выражение.
     * @return уникальный идентификатор пользователя к словарю которого отностися устойчевое выражение.
     */
    public UUID getUserId() {
        return userId;
    }

    /**
     * Возвращает устойчевое выражение на английском языке
     * @return устойчевое выражение на английском языке
     */
    public String getValue() {
        return value;
    }

    /**
     * Возвращает примечание к устойчевому выражению добавленное пользователем.
     * @return примечание к устойчевому выражению.
     */
    public String getNote() {
        return note;
    }

    /**
     * Возвращает список всех интерпритация устойчевого выражения.
     * @return список всех интерпритация устойчевого выражения.
     */
    public List<ExpressionInterpretation> getInterpretations() {
        return Collections.unmodifiableList(interpretations);
    }

    /**
     * Возвращает список переводов устойчевого выражения.
     * @return список переводов устойчевого выражения.
     */
    public List<ExpressionTranslation> getTranslations() {
        return Collections.unmodifiableList(translations);
    }

    /**
     * Возвращает список примеров устойчевого выражения.
     * @return список примеров устойчевого выражения.
     */
    public List<ExpressionExample> getExamples() {
        return Collections.unmodifiableList(examples);
    }

    /**
     * Возвращает данные последнего повторения устойчевого выражения с английского языка на родной
     * язык пользователя.
     * @return данные последнего повторения устойчевого выражения с английского языка.
     */
    public RepeatDataFromEnglish getRepeatDataFromEnglish() {
        return repeatDataFromEnglish;
    }

    /**
     * Возвращает данные последнего повторения устойчевого выражения с родного языка пользователя
     * на английский язык.
     * @return данные последнего повторения устойчевого выражения с родного языка пользователя.
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

    public Expression setValue(String value) {
        this.value = value;
        return this;
    }

    public Expression setNote(String note) {
        this.note = note;
        return this;
    }

    public Expression setInterpretations(List<ExpressionInterpretation> interpretations) {
        this.interpretations.clear();
        if(interpretations != null) this.interpretations.addAll(interpretations);
        return this;
    }

    public Expression setTranslations(List<ExpressionTranslation> translations) {
        this.translations.clear();
        if(translations != null) this.translations.addAll(translations);
        return this;
    }

    public Expression setExamples(List<ExpressionExample> examples) {
        this.examples.clear();
        if(examples != null) this.examples.addAll(examples);
        return this;
    }

    public Expression addInterpretation(ExpressionInterpretation interpretation) {
        interpretations.add(interpretation);
        return this;
    }

    public Expression addTranslation(ExpressionTranslation translation) {
        translations.add(translation);
        return this;
    }

    public Expression addExample(ExpressionExample example) {
        examples.add(example);
        return this;
    }

    /**
     * Задает результат последнего повторения этого устойчевого выражения с английского языка
     * на родной язык пользователя.
     * @param isRemember true - если пользователь правильно вспомнил переводы, произношение и толкования
     *                   устойчевого выражения, иначе - false.
     * @param lastDateOfRepeat дата текущего повторения.
     * @param intervals все интервалы повторения (подробнее см. {@link com.bakuard.flashcards.service.IntervalService})
     *                  пользователя.
     */
    public void repeatFromEnglish(boolean isRemember, LocalDate lastDateOfRepeat, ImmutableList<Integer> intervals) {
        int index = isRemember ?
                Math.min(intervals.indexOf(repeatDataFromEnglish.interval()) + 1, intervals.size() - 1) : 0;

        repeatDataFromEnglish = new RepeatDataFromEnglish(intervals.get(index), lastDateOfRepeat);
    }

    /**
     * Проверяет указанное пользователем значение устойчевого выражения при его повторении с родного языка пользователя
     * на английский язык. Если заданное значение равняется значению текущего устойчевого выражения - повторения
     * считается успешным.
     * @param inputValue значение устойчевого выражения на английском языке.
     * @param lastDateOfRepeat дата текущего повторения.
     * @param intervals все интервалы повторения (подробнее см. {@link com.bakuard.flashcards.service.IntervalService})
     *                  пользователя.
     * @return true - если повторение выполнено успешно, иначе - false.
     */
    public boolean repeatFromNative(String inputValue, LocalDate lastDateOfRepeat, ImmutableList<Integer> intervals) {
        boolean isRemember = inputValue.equalsIgnoreCase(value);
        int index = inputValue.equalsIgnoreCase(value) ?
                Math.min(intervals.indexOf(repeatDataFromNative.interval()) + 1, intervals.size() - 1) : 0;

        repeatDataFromNative = new RepeatDataFromNative(intervals.get(index), lastDateOfRepeat);

        return isRemember;
    }

    /**
     * Указывает, что пользователь забыл перевод данного устойчевого выражения с английского на родной язык и его
     * требуется повторить в ближайшее время. Метод отметит текущую дату, как дату последнего повторения,
     * установит наименьший из интервалов повторения пользователя.
     * @param lastDateOfRepeat текущая дата.
     * @param lowestInterval наименьший из интервалов повторения пользователя
     *                       (подробнее см. {@link com.bakuard.flashcards.service.IntervalService})
     */
    public void markForRepetitionFromEnglish(LocalDate lastDateOfRepeat, int lowestInterval) {
        repeatDataFromEnglish = new RepeatDataFromEnglish(lowestInterval, lastDateOfRepeat);
    }

    /**
     * Указывает, что пользователь забыл перевод данного устойчевого выражения с родного языка на английский и его
     * требуется повторить в ближайшее время. Метод отметит текущую дату, как дату последнего повторения,
     * установит наименьший из интервалов повторения пользователя.
     * @param lastDateOfRepeat текущая дата.
     * @param lowestInterval наименьший из интервалов повторения пользователя
     *                       (подробнее см. {@link com.bakuard.flashcards.service.IntervalService})
     */
    public void markForRepetitionFromNative(LocalDate lastDateOfRepeat, int lowestInterval) {
        repeatDataFromNative = new RepeatDataFromNative(lowestInterval, lastDateOfRepeat);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Expression that = (Expression) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Expression{" +
                "id=" + id +
                ", userId=" + userId +
                ", value='" + value + '\'' +
                ", note='" + note + '\'' +
                ", interpretations=" + interpretations +
                ", translations=" + translations +
                ", examples=" + examples +
                ", repeatDataFromEnglish=" + repeatDataFromEnglish +
                ", repeatDataFromNative=" + repeatDataFromNative +
                '}';
    }

}
