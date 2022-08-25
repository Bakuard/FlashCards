package com.bakuard.flashcards.model.expression;

import com.bakuard.flashcards.model.Entity;
import com.bakuard.flashcards.model.RepeatData;
import com.bakuard.flashcards.validation.AllUnique;
import com.bakuard.flashcards.validation.NotBlankOrNull;
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
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Table("expressions")
public class Expression implements Entity<Expression> {

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
    @AllUnique(nameOfGetterMethod = "getValue", message = "Expression.interpretations.allUnique")
    private List<@Valid ExpressionInterpretation> interpretations;
    @MappedCollection(idColumn = "expression_id", keyColumn = "index")
    @AllUnique(nameOfGetterMethod = "getValue", message = "Expression.translations.allUnique")
    private List<@Valid ExpressionTranslation> translations;
    @MappedCollection(idColumn = "expression_id", keyColumn = "index")
    @AllUnique(nameOfGetterMethod = "getOrigin", message = "Expression.examples.allUnique")
    private List<@Valid ExpressionExample> examples;
    @Embedded.Nullable
    @Valid
    private RepeatData repeatData;

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

    public Expression(UUID userId,
                      String value,
                      String note,
                      int smallestInterval,
                      LocalDate today) {
        this.userId = userId;
        this.value = value;
        this.note = note;
        interpretations = new ArrayList<>();
        translations = new ArrayList<>();
        examples = new ArrayList<>();
        repeatData = new RepeatData(smallestInterval, today);
    }

    @Override
    public UUID getId() {
        return id;
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

    public boolean isHotRepeat(ImmutableList<Integer> intervals) {
        return repeatData.getInterval() == intervals.get(0);
    }

    @Override
    public void generateIdIfAbsent() {
        if(id == null) id = UUID.randomUUID();
    }

    public Expression addInterpretations(ExpressionInterpretation interpretation) {
        interpretations.add(interpretation);
        return this;
    }

    public Expression addTranslations(ExpressionTranslation translation) {
        translations.add(translation);
        return this;
    }

    public Expression addExamples(ExpressionExample example) {
        examples.add(example);
        return this;
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

}
