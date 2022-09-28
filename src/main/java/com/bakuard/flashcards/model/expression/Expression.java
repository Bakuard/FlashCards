package com.bakuard.flashcards.model.expression;

import com.bakuard.flashcards.model.Entity;
import com.bakuard.flashcards.model.RepeatData;
import com.bakuard.flashcards.validation.*;
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
import javax.validation.groups.Default;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Table("expressions")
public class Expression implements Entity<Expression> {

    public static Builder newBuilder(ValidatorUtil validator) {
        return new Builder(validator);
    }


    @Id
    @Column("expression_id")
    private UUID id;
    @Column("user_id")
    @NotNull(message = "Expression.userId.notNull", groups = Groups.M.class)
    private final UUID userId;
    @Column("value")
    @NotBlank(message = "Expression.value.notBlank", groups = Groups.M.class)
    private String value;
    @Column("note")
    @NotBlankOrNull(message = "Expression.note.notBlankOrNull", groups = Groups.M.class)
    private String note;
    @MappedCollection(idColumn = "expression_id", keyColumn = "index")
    @NotNull(message = "Expression.interpretations.notNull", groups = Groups.A.class)
    @NotContainsNull(message = "Expression.interpretations.notContainsNull", groups = Groups.B.class)
    @AllUnique(nameOfGetterMethod = "getValue", message = "Expression.interpretations.allUnique", groups = Groups.C.class)
    private List<@Valid ExpressionInterpretation> interpretations;
    @MappedCollection(idColumn = "expression_id", keyColumn = "index")
    @NotNull(message = "Expression.translations.notNull", groups = Groups.D.class)
    @NotContainsNull(message = "Expression.translations.notContainsNull", groups = Groups.E.class)
    @AllUnique(nameOfGetterMethod = "getValue", message = "Expression.translations.allUnique", groups = Groups.F.class)
    private List<@Valid ExpressionTranslation> translations;
    @MappedCollection(idColumn = "expression_id", keyColumn = "index")
    @NotNull(message = "Expression.examples.notNull", groups = Groups.G.class)
    @NotContainsNull(message = "Expression.examples.notContainsNull", groups = Groups.H.class)
    @AllUnique(nameOfGetterMethod = "getOrigin", message = "Expression.examples.allUnique", groups = Groups.I.class)
    private List<@Valid ExpressionExample> examples;
    @Embedded.Nullable
    @Valid
    private RepeatData repeatData;
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
                      RepeatData repeatData) {
        this.id = id;
        this.userId = userId;
        this.value = value;
        this.note = note;
        this.interpretations = interpretations;
        this.translations = translations;
        this.examples = examples;
        this.repeatData = repeatData;
    }

    private Expression(UUID id,
                       UUID userId,
                       String value,
                       String note,
                       List<ExpressionInterpretation> interpretations,
                       List<ExpressionTranslation> translations,
                       List<ExpressionExample> examples,
                       RepeatData repeatData,
                       ValidatorUtil validator) {
        this.id = id;
        this.userId = userId;
        this.value = value;
        this.note = note;
        this.interpretations = interpretations;
        this.translations = translations;
        this.examples = examples;
        this.repeatData = repeatData;
        this.validator = validator;

        validator.assertAllEmpty(this,
                validator.check(this, Groups.M.class),
                validator.check(this, Groups.A.class, Groups.B.class, Groups.C.class, Default.class),
                validator.check(this, Groups.D.class, Groups.E.class, Groups.F.class, Default.class),
                validator.check(this, Groups.G.class, Groups.H.class, Groups.I.class, Default.class)
        );
    }

    @Override
    public UUID getId() {
        return id;
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

    public RepeatData getRepeatData() {
        return repeatData;
    }

    public boolean isHotRepeat(int lowestInterval) {
        return repeatData.getInterval() == lowestInterval;
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
                setRepeatData(repeatData);
    }

    public void repeat(boolean isRemember, LocalDate lastDateOfRepeat, ImmutableList<Integer> intervals) {
        int index = isRemember ?
                Math.min(intervals.indexOf(repeatData.getInterval()) + 1, intervals.size() - 1) : 0;

        repeatData = new RepeatData(intervals.get(index), lastDateOfRepeat);
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
                ", repeatData=" + repeatData +
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
        private RepeatData repeatData;
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

        public Builder setRepeatData(RepeatData repeatData) {
            this.repeatData = repeatData;
            return this;
        }

        public Builder setInterpretations(List<ExpressionInterpretation> interpretations) {
            this.interpretations = interpretations != null ? new ArrayList<>(interpretations) : null;
            return this;
        }

        public Builder setTranslations(List<ExpressionTranslation> translations) {
            this.translations = translations != null ? new ArrayList<>(translations) : null;
            return this;
        }

        public Builder setExamples(List<ExpressionExample> examples) {
            this.examples = examples != null ? new ArrayList<>(examples) : null;
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
                    repeatData,
                    validator
            );
        }

    }

}
