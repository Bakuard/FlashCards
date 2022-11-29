package com.bakuard.flashcards.model.word;

import com.bakuard.flashcards.validation.NotBlankOrNull;
import org.springframework.data.annotation.PersistenceCreator;
import org.springframework.data.annotation.Transient;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import javax.validation.constraints.NotBlank;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

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
    private List<ExampleOuterSource> outerSource;

    @PersistenceCreator
    public WordExample(String origin, String translate, String note) {
        this.origin = origin;
        this.translate = translate;
        this.note = note;
        this.outerSource = new ArrayList<>();
    }

    public WordExample(WordExample other) {
        this.origin = other.origin;
        this.translate = other.translate;
        this.note = other.note;
        this.outerSource = new ArrayList<>(other.outerSource);
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

    public List<ExampleOuterSource> getSourceInfo() {
        return outerSource;
    }

    public Optional<LocalDate> getRecentUpdateDate(String outerSourceName) {
        return outerSource.stream().
                filter(outerSource -> outerSource.sourceName().equals(outerSourceName)).
                findFirst().
                map(ExampleOuterSource::recentUpdateDate);
    }

    public boolean hasOuterSource(String outerSourceName) {
        return outerSource.stream().
                anyMatch(outerSource -> outerSource.sourceName().equals(outerSourceName));
    }

    public WordExample setOrigin(String origin) {
        this.origin = origin;
        return this;
    }

    public WordExample setTranslate(String translate) {
        this.translate = translate;
        return this;
    }

    public WordExample setNote(String note) {
        this.note = note;
        return this;
    }

    public boolean merge(WordExample other) {
        boolean isMerged = origin.equals(other.origin);
        if(isMerged) {
            for(int i = 0; i < other.outerSource.size(); i++) {
                ExampleOuterSource otherInfo = other.outerSource.get(i);
                boolean isFind = false;
                int index = 0;
                for(int j = 0; j < outerSource.size() && !isFind; j++) {
                    isFind = outerSource.get(j).sourceName().equals(otherInfo.sourceName());
                    index = j;
                }
                if(isFind) outerSource.set(index, otherInfo);
                else outerSource.add(otherInfo);
            }
            if(translate == null && !outerSource.isEmpty()) {
                translate = outerSource.get(0).translate();
            }
        }
        return isMerged;
    }

    public WordExample addSourceInfo(ExampleOuterSource info) {
        outerSource.add(info);
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
                Objects.equals(outerSource, example.outerSource);
    }

    @Override
    public int hashCode() {
        return Objects.hash(origin, translate, note, outerSource);
    }

    @Override
    public String toString() {
        return "WordExample{" +
                "origin='" + origin + '\'' +
                ", translate='" + translate + '\'' +
                ", note='" + note + '\'' +
                ", sourceInfo=" + outerSource +
                '}';
    }

}
