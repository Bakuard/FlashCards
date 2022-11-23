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

    public WordInterpretation setValue(String value) {
        this.value = value;
        return this;
    }

    public boolean merge(WordInterpretation other) {
        boolean isMerged = value.equals(other.value);
        if(isMerged) {
            for(int i = 0; i < other.sourceInfo.size(); i++) {
                SourceInfo otherInfo = other.sourceInfo.get(i);
                boolean isFind = false;
                int index = 0;
                for(int j = 0; j < sourceInfo.size() && !isFind; j++) {
                    isFind = sourceInfo.get(j).sourceName().equals(otherInfo.sourceName());
                    index = j;
                }
                if(isFind) sourceInfo.set(index, otherInfo);
                else sourceInfo.add(otherInfo);
            }
        }
        return isMerged;
    }

    public WordInterpretation addSourceInfo(SourceInfo info) {
        sourceInfo.add(info);
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
