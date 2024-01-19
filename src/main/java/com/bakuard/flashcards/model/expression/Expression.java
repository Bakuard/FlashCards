package com.bakuard.flashcards.model.expression;

import com.bakuard.flashcards.model.Entity;
import com.bakuard.flashcards.model.RepeatDataFromEnglish;
import com.bakuard.flashcards.model.RepeatDataFromNative;
import com.bakuard.flashcards.validation.annotation.AllUnique;
import com.bakuard.flashcards.validation.annotation.NotBlankOrNull;
import com.bakuard.flashcards.validation.annotation.NotContainsNull;
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

/**
 * Подробные данные об устойчивом выражении в словаре пользователя.
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
    private List<@Valid ExpressionInterpretation> interpretations;
    @MappedCollection(idColumn = "expression_id", keyColumn = "index")
    @NotContainsNull(message = "Expression.translations.notContainsNull")
    @AllUnique(nameOfGetterMethod = "getValue", message = "Expression.translations.allUnique")
    private List<@Valid ExpressionTranslation> translations;
    @MappedCollection(idColumn = "expression_id", keyColumn = "index")
    @NotContainsNull(message = "Expression.examples.notContainsNull")
    @AllUnique(nameOfGetterMethod = "getOrigin", message = "Expression.examples.allUnique")
    private List<@Valid ExpressionExample> examples;
    @Embedded.Nullable
    @Valid
    private RepeatDataFromEnglish repeatDataFromEnglish;
    @Embedded.Nullable
    @Valid
    private RepeatDataFromNative repeatDataFromNative;

    /**
     * Данный конструктор используется слоем доступа к данным для загрузки устойчивого выражения.
     * @param id уникальный идентификатор устойчивого выражения.
     * @param userId уникальный идентификатор пользователя к словарю которого относится это устойчивое выражение.
     * @param value устойчивое выражение на английском языке
     * @param note примечание к устойчивому выражению.
     * @param interpretations список интерпретаций устойчивого выражения.
     * @param translations список переводов устойчивого выражения.
     * @param examples список примеров к устойчивому выражению.
     * @param repeatDataFromEnglish данные последнего повторения устойчивого выражения с английского языка.
     * @param repeatDataFromNative данные последнего повторения устойчивого выражения с родного языка пользователя.
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
     * Создает новое устойчивое выражение для словаря пользователя.
     * @param userId уникальный идентификатор пользователя к словарю которого относится это устойчивое выражение.
     * @param lowestIntervalForEnglish Наименьший из всех интервалов повторения данного пользователя.
     *                                 Подробнее см. {@link RepeatDataFromEnglish}.
     * @param lowestIntervalForNative Наименьший из всех интервалов повторения данного пользователя.
     *                                Подробнее см. {@link RepeatDataFromNative}.
     * @param clock используется для получения текущей даты и возможности её определения в тестах.
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
     * Возвращает уникальный идентификатор пользователя к словарю которого относится устойчивое выражение.
     * @return уникальный идентификатор пользователя к словарю которого относится устойчивое выражение.
     */
    public UUID getUserId() {
        return userId;
    }

    /**
     * Возвращает устойчивое выражение на английском языке
     * @return устойчивое выражение на английском языке
     */
    public String getValue() {
        return value;
    }

    /**
     * Возвращает примечание к устойчивому выражению добавленное пользователем.
     * @return примечание к устойчивому выражению.
     */
    public String getNote() {
        return note;
    }

    /**
     * Возвращает список всех интерпретация устойчивого выражения.
     * @return список всех интерпретация устойчивого выражения.
     */
    public List<ExpressionInterpretation> getInterpretations() {
        return interpretations;
    }

    /**
     * Возвращает список переводов устойчивого выражения.
     * @return список переводов устойчивого выражения.
     */
    public List<ExpressionTranslation> getTranslations() {
        return translations;
    }

    /**
     * Возвращает список примеров устойчивого выражения.
     * @return список примеров устойчивого выражения.
     */
    public List<ExpressionExample> getExamples() {
        return examples;
    }

    /**
     * Возвращает данные последнего повторения устойчивого выражения с английского языка на родной
     * язык пользователя.
     * @return данные последнего повторения устойчивого выражения с английского языка.
     */
    public RepeatDataFromEnglish getRepeatDataFromEnglish() {
        return repeatDataFromEnglish;
    }

    /**
     * Возвращает данные последнего повторения устойчивого выражения с родного языка пользователя
     * на английский язык.
     * @return данные последнего повторения устойчивого выражения с родного языка пользователя.
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

    /**
     * Устанавливает значение устойчивого выражения
     * @param value значение устойчивого выражения
     * @return ссылку на этот же объект
     */
    public Expression setValue(String value) {
        this.value = value;
        return this;
    }

    /**
     * Устанавливает примечание к устойчивому выражению
     * @param note примечание к устойчивому выражению
     * @return ссылку на этот же объект
     */
    public Expression setNote(String note) {
        this.note = note;
        return this;
    }

    /**
     * Устанавливает список интерпретаций к устойчивому выражению.
     * @param interpretations список интерпретаций к устойчивому выражению
     * @return ссылку на этот же объект
     */
    public Expression setInterpretations(List<ExpressionInterpretation> interpretations) {
        this.interpretations = interpretations;
        return this;
    }

    /**
     * Устанавливает список переводов устойчивого выражения.
     * @param translations список переводов устойчивого выражения
     * @return ссылку на этот же объект
     */
    public Expression setTranslations(List<ExpressionTranslation> translations) {
        this.translations = translations;
        return this;
    }

    /**
     * Устанавливает список примеров к устойчивому выражению.
     * @param examples список примеров к устойчивому выражению
     * @return ссылку на этот же объект
     */
    public Expression setExamples(List<ExpressionExample> examples) {
        this.examples = examples;
        return this;
    }

    /**
     * Добавляет указанную интерпретацию к данному устойчивому выражению.
     * @param interpretation добавляемая интерпретация
     * @return ссылку на этот же объект
     */
    public Expression addInterpretation(ExpressionInterpretation interpretation) {
        interpretations.add(interpretation);
        return this;
    }

    /**
     * Добавляет указанный перевод к данному устойчивому выражению.
     * @param translation добавляемый перевод
     * @return ссылку на этот же объект
     */
    public Expression addTranslation(ExpressionTranslation translation) {
        translations.add(translation);
        return this;
    }

    /**
     * Добавляет указанный пример к данному устойчивому выражению.
     * @param example добавляемый пример
     * @return ссылку на этот же объект
     */
    public Expression addExample(ExpressionExample example) {
        examples.add(example);
        return this;
    }

    /**
     * Задает результат последнего повторения этого устойчивого выражения с английского языка
     * на родной язык пользователя.
     * @param repeatDataFromEnglish (См. {@link RepeatDataFromEnglish}).
     * @return ссылку на этот же объект
     */
    public Expression setRepeatDataFromEnglish(RepeatDataFromEnglish repeatDataFromEnglish) {
        this.repeatDataFromEnglish = repeatDataFromEnglish;
        return this;
    }

    /**
     * Задает результат последнего повторения этого устойчивого выражения с родного языка пользователя
     * на английский язык.
     * @param repeatDataFromNative (См. {@link RepeatDataFromNative}).
     * @return ссылку на этот же объект
     */
    public Expression setRepeatDataFromNative(RepeatDataFromNative repeatDataFromNative) {
        this.repeatDataFromNative = repeatDataFromNative;
        return this;
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
