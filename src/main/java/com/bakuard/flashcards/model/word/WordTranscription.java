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

@Table("words_transcriptions")
public class WordTranscription {

    @Column("value")
    @NotBlank(message = "WordTranscription.value.notBlank")
    private String value;
    @Column("note")
    @NotBlankOrNull(message = "WordTranscription.note.notBlankOrNull")
    private String note;
    @Transient
    private List<SourceInfo> sourceInfo;

    @PersistenceCreator
    public WordTranscription(String value, String note) {
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

    public WordTranscription setAll(List<SourceInfo> sourceInfo) {
        this.sourceInfo.clear();
        if(sourceInfo != null) this.sourceInfo.addAll(sourceInfo);
        return this;
    }

    public WordTranscription addSourceInfo(SourceInfo info) {
        sourceInfo.add(info);
        return this;
    }

    public WordTranscription removeSourceInfo(String sourceName) {
        sourceInfo.removeIf(info -> Objects.equals(info.sourceName(), sourceName));
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        WordTranscription that = (WordTranscription) o;
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
        return "WordTranscription{" +
                "value='" + value + '\'' +
                ", note='" + note + '\'' +
                ", sourceInfo=" + sourceInfo +
                '}';
    }

}
