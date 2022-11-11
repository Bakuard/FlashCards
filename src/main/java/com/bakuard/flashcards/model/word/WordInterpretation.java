package com.bakuard.flashcards.model.word;

import org.springframework.data.annotation.PersistenceCreator;
import org.springframework.data.annotation.Transient;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import javax.validation.constraints.NotBlank;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Table("words_interpretations")
public class WordInterpretation {

    @Column("value")
    @NotBlank(message = "WordInterpretation.value.notBlank")
    private String value;
    @Transient
    private List<SourceInfo> sourceInfo;

    @PersistenceCreator
    public WordInterpretation(String value) {
        this.value = value;
        this.sourceInfo = new ArrayList<>();
    }

    public String getValue() {
        return value;
    }

    public List<SourceInfo> getSourceInfo() {
        return sourceInfo;
    }

    public WordInterpretation setAll(List<SourceInfo> sourceInfo) {
        this.sourceInfo.clear();
        if(sourceInfo != null) this.sourceInfo.addAll(sourceInfo);
        return this;
    }

    public WordInterpretation addSourceInfo(SourceInfo info) {
        sourceInfo.add(info);
        return this;
    }

    public WordInterpretation removeSourceInfo(String sourceName) {
        sourceInfo.removeIf(info -> Objects.equals(info.sourceName(), sourceName));
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        WordInterpretation that = (WordInterpretation) o;
        return Objects.equals(value, that.value) && Objects.equals(sourceInfo, that.sourceInfo);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value, sourceInfo);
    }

    @Override
    public String toString() {
        return "WordInterpretation{" +
                "value='" + value + '\'' +
                ", sourceInfo=" + sourceInfo +
                '}';
    }

}
