package com.bakuard.flashcards.model.expression;

import com.bakuard.flashcards.model.Entity;
import com.bakuard.flashcards.model.RepeatDataFromEnglish;
import com.bakuard.flashcards.model.RepeatDataFromNative;
import com.bakuard.flashcards.validation.AllUnique;
import com.bakuard.flashcards.validation.NotBlankOrNull;
import com.bakuard.flashcards.validation.NotContainsNull;
import com.bakuard.flashcards.validation.ValidatorUtil;
import com.google.common.collect.ImmutableList;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.PersistenceCreator;
import org.springframework.data.annotation.Transient;
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
    @NotNull(message = "Expression.interpretations.notNull")
    @NotContainsNull(message = "Expression.interpretations.notContainsNull")
    @AllUnique(nameOfGetterMethod = "getValue", message = "Expression.interpretations.allUnique")
    private List<@Valid ExpressionInterpretation> interpretations;
    @MappedCollection(idColumn = "expression_id", keyColumn = "index")
    @NotNull(message = "Expression.translations.notNull")
    @NotContainsNull(message = "Expression.translations.notContainsNull")
    @AllUnique(nameOfGetterMethod = "getValue", message = "Expression.translations.allUnique")
    private List<@Valid ExpressionTranslation> translations;
    @MappedCollection(idColumn = "expression_id", keyColumn = "index")
    @NotNull(message = "Expression.examples.notNull")
    @NotContainsNull(message = "Expression.examples.notContainsNull")
    @AllUnique(nameOfGetterMethod = "getOrigin", message = "Expression.examples.allUnique")
    private List<@Valid ExpressionExample> examples;
    @NotNull(message = "Expression.repeatDataFromEnglish.notNull")
    @Embedded.Nullable
    @Valid
    private RepeatDataFromEnglish repeatDataFromEnglish;
    @Embedded.Nullable
    @Valid
    private RepeatDataFromNative repeatDataFromNative;
    @Transient
    private boolean isNew;

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
        this.isNew = false;
    }

    public Expression(UUID userId,
                      int lowestIntervalForEnglish,
                      int lowestIntervalForNative,
                      Clock clock) {
        this.userId = userId;
        this.id = UUID.randomUUID();
        this.interpretations = new ArrayList<>();
        this.translations = new ArrayList<>();
        this.examples = new ArrayList<>();
        this.repeatDataFromEnglish = new RepeatDataFromEnglish(lowestIntervalForEnglish, LocalDate.now(clock));
        this.repeatDataFromNative = new RepeatDataFromNative(lowestIntervalForNative, LocalDate.now(clock));
        this.isNew = true;
    }

    @Override
    public UUID getId() {
        return id;
    }

    @Override
    public boolean isNew() {
        return isNew;
    }

    public UUID getUserId() {
        return userId;
    }

    public String getValue() {
        return value;
    }

    public String getNote() {
        return note;
    }

    public List<ExpressionInterpretation> getInterpretations() {
        return Collections.unmodifiableList(interpretations);
    }

    public List<ExpressionTranslation> getTranslations() {
        return Collections.unmodifiableList(translations);
    }

    public List<ExpressionExample> getExamples() {
        return Collections.unmodifiableList(examples);
    }

    public RepeatDataFromEnglish getRepeatDataFromEnglish() {
        return repeatDataFromEnglish;
    }

    public RepeatDataFromNative getRepeatDataFromNative() {
        return repeatDataFromNative;
    }

    public boolean isHotRepeatFromEnglish(int lowestInterval) {
        return repeatDataFromEnglish.interval() == lowestInterval;
    }

    public boolean isHotRepeatFromNative(int lowestInterval) {
        return repeatDataFromNative.interval() == lowestInterval;
    }

    @Override
    public void markAsSaved() {
        isNew = false;
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

    public void repeatFromEnglish(boolean isRemember, LocalDate lastDateOfRepeat, ImmutableList<Integer> intervals) {
        int index = isRemember ?
                Math.min(intervals.indexOf(repeatDataFromEnglish.interval()) + 1, intervals.size() - 1) : 0;

        repeatDataFromEnglish = new RepeatDataFromEnglish(intervals.get(index), lastDateOfRepeat);
    }

    public boolean repeatFromNative(String inputValue, LocalDate lastDateOfRepeat, ImmutableList<Integer> intervals) {
        boolean isRemember = inputValue.equalsIgnoreCase(value);
        int index = inputValue.equalsIgnoreCase(value) ?
                Math.min(intervals.indexOf(repeatDataFromNative.interval()) + 1, intervals.size() - 1) : 0;

        repeatDataFromNative = new RepeatDataFromNative(intervals.get(index), lastDateOfRepeat);

        return isRemember;
    }

    public void markForRepetitionFromEnglish(LocalDate lastDateOfRepeat, int lowestInterval) {
        repeatDataFromEnglish = new RepeatDataFromEnglish(lowestInterval, lastDateOfRepeat);
    }

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
                ", isNew=" + isNew +
                '}';
    }

}
