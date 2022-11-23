package com.bakuard.flashcards.model.word;

import com.bakuard.flashcards.validation.NotBlankOrNull;
import org.springframework.data.annotation.PersistenceCreator;
import org.springframework.data.annotation.Transient;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import javax.validation.constraints.NotBlank;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Table("words_translations")
public class WordTranslation {

    @Column("value")
    @NotBlank(message = "WordTranslation.value.notBlank")
    private String value;

    @Column("note")
    @NotBlankOrNull(message = "WordTranslation.note.notBlankOrNull")
    private String note;
    @Transient
    private List<SourceInfo> sourceInfo;

    @PersistenceCreator
    public WordTranslation(String value, String note) {
        this.value = value;
        this.note = note;
        this.sourceInfo = new ArrayList<>();
    }

    public String getValue() {
        return value;
    }

    public String getNote() {
        return note;
    }

    public List<SourceInfo> getSourceInfo() {
        return sourceInfo;
    }

    public WordTranslation setValue(String value) {
        this.value = value;
        return this;
    }

    public WordTranslation setNote(String note) {
        this.note = note;
        return this;
    }

    public boolean merge(WordTranslation other) {
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

    public WordTranslation addSourceInfo(SourceInfo info) {
        sourceInfo.add(info);
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        WordTranslation that = (WordTranslation) o;
        return Objects.equals(value, that.value) &&
                Objects.equals(note, that.note) &&
                Objects.equals(sourceInfo, that.sourceInfo);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value, note, sourceInfo);
    }

    @Override
    public String toString() {
        return "WordTranslation{" +
                "value='" + value + '\'' +
                ", note='" + note + '\'' +
                ", sourceInfo=" + sourceInfo +
                '}';
    }

}
