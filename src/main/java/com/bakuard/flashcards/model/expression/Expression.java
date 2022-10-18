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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Table("expressions")
public class Expression implements Entity {

    public static Builder newBuilder(ValidatorUtil validator) {
        return new Builder(validator);
    }


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
    @NotNull(message = "Expression.repeatDataFromNative.notNull")
    @Embedded.Nullable
    @Valid
    private RepeatDataFromNative repeatDataFromNative;
    @Transient
    private ValidatorUtil validator;

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

    private Expression(UUID id,
                       UUID userId,
                       String value,
                       String note,
                       List<ExpressionInterpretation> interpretations,
                       List<ExpressionTranslation> translations,
                       List<ExpressionExample> examples,
                       RepeatDataFromEnglish repeatDataFromEnglish,
                       RepeatDataFromNative repeatDataFromNative,
                       ValidatorUtil validator) {
        this.id = id;
        this.userId = userId;
        this.value = value;
        this.note = note;
        this.interpretations = interpretations;
        this.translations = translations;
        this.examples = examples;
        this.repeatDataFromEnglish = repeatDataFromEnglish;
        this.repeatDataFromNative = repeatDataFromNative;
        this.validator = validator;

        validator.assertValid(this);
    }

    @Override
    public UUID getId() {
        return id;
    }

    @Override
    public boolean isNew() {
        return id == null;
    }

    @Override
    public void setValidator(ValidatorUtil validator) {
        this.validator = validator;
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
    public void generateIdIfAbsent() {
        if(id == null) id = UUID.randomUUID();
    }

    public Builder builder() {
        return newBuilder(validator).
                setOrGenerateId(id).
                setUserId(userId).
                setValue(value).
                setNote(note).
                setInterpretations(interpretations).
                setTranslations(translations).
                setExamples(examples).
                setRepeatData(repeatDataFromEnglish);
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Expression that = (Expression) o;
        return id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
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
                ", repeatData=" + repeatDataFromEnglish +
                '}';
    }


    public static class Builder {

        private UUID id;
        private UUID userId;
        private String value;
        private String note;
        private List<ExpressionInterpretation> interpretations;
        private List<ExpressionTranslation> translations;
        private List<ExpressionExample> examples;
        private RepeatDataFromEnglish repeatDataFromEnglish;
        private RepeatDataFromNative repeatDataFromNative;
        private final ValidatorUtil validator;

        private Builder(ValidatorUtil validator) {
            this.interpretations = new ArrayList<>();
            this.translations = new ArrayList<>();
            this.examples = new ArrayList<>();
            this.validator = validator;
        }

        public Builder setOrGenerateId(UUID id) {
            this.id = id == null ? UUID.randomUUID() : id;
            return this;
        }

        public Builder setUserId(UUID userId) {
            this.userId = userId;
            return this;
        }

        public Builder setValue(String value) {
            this.value = value;
            return this;
        }

        public Builder setNote(String note) {
            this.note = note;
            return this;
        }

        public Builder setRepeatData(RepeatDataFromEnglish repeatDataFromEnglish) {
            this.repeatDataFromEnglish = repeatDataFromEnglish;
            return this;
        }

        public Builder setRepeatData(RepeatDataFromNative repeatDataFromNative) {
            this.repeatDataFromNative = repeatDataFromNative;
            return this;
        }

        public Builder setInitialRepeatData(int lowestInterval, Clock clock) {
            repeatDataFromEnglish = new RepeatDataFromEnglish(lowestInterval, LocalDate.now(clock));
            repeatDataFromNative = new RepeatDataFromNative(lowestInterval, LocalDate.now(clock));
            return this;
        }

        public Builder setInterpretations(List<ExpressionInterpretation> interpretations) {
            this.interpretations.clear();
            if(interpretations != null) this.interpretations.addAll(interpretations);
            return this;
        }

        public Builder setTranslations(List<ExpressionTranslation> translations) {
            this.translations.clear();
            if(translations != null) this.translations.addAll(translations);
            return this;
        }

        public Builder setExamples(List<ExpressionExample> examples) {
            this.examples.clear();
            if(examples != null) this.examples.addAll(examples);
            return this;
        }

        public Builder addInterpretation(ExpressionInterpretation interpretation) {
            interpretations.add(interpretation);
            return this;
        }

        public Builder addTranslation(ExpressionTranslation translation) {
            translations.add(translation);
            return this;
        }

        public Builder addExample(ExpressionExample example) {
            examples.add(example);
            return this;
        }

        public Expression build() {
            return new Expression(
                    id,
                    userId,
                    value,
                    note,
                    interpretations,
                    translations,
                    examples,
                    repeatDataFromEnglish,
                    repeatDataFromNative,
                    validator
            );
        }

    }

}
