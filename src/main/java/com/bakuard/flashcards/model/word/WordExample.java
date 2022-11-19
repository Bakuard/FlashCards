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

@Table("words_examples")
public class WordExample {

    @Column("origin")
    @NotBlank(message = "WordExample.origin.notBlank")
    private String origin;
    @Column("translate")
    @NotBlankOrNull(message = "WordExample.translate.notBlankOrNull")
    private String translate;
    @Column("note")
    @NotBlankOrNull(message = "WordExample.note.notBlankOrNull")
    private String note;
    @Transient
    private List<SourceInfo> sourceInfo;

    @PersistenceCreator
    public WordExample(String origin, String translate, String note) {
        this.origin = origin;
        this.translate = translate;
        this.note = note;
        this.sourceInfo = new ArrayList<>();
    }

    public String getOrigin() {
        return origin;
    }

    public String getTranslate() {
        return translate;
    }

    public String getNote() {
        return note;
    }

    public List<SourceInfo> getSourceInfo() {
        return sourceInfo;
    }

    public WordExample setAll(List<SourceInfo> sourceInfo) {
        this.sourceInfo.clear();
        if(sourceInfo != null) this.sourceInfo.addAll(sourceInfo);
        return this;
    }

    public WordExample addSourceInfo(SourceInfo info) {
        sourceInfo.add(info);
        return this;
    }

    public WordExample removeSourceInfo(String sourceName) {
        sourceInfo.removeIf(info -> Objects.equals(info.sourceName(), sourceName));
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        WordExample example = (WordExample) o;
        return Objects.equals(origin, example.origin) &&
                Objects.equals(translate, example.translate) &&
                Objects.equals(note, example.note) &&
                Objects.equals(sourceInfo, example.sourceInfo);
    }

    @Override
    public int hashCode() {
        return Objects.hash(origin, translate, note, sourceInfo);
    }

    @Override
    public String toString() {
        return "WordExample{" +
                "origin='" + origin + '\'' +
                ", translate='" + translate + '\'' +
                ", note='" + note + '\'' +
                ", sourceInfo=" + sourceInfo +
                '}';
    }

}
