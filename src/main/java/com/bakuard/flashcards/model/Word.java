package com.bakuard.flashcards.model;

import com.google.common.collect.ImmutableList;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.PersistenceCreator;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Embedded;
import org.springframework.data.relational.core.mapping.MappedCollection;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDate;
import java.util.*;

@Table("words")
public class Word implements Entity<Word> {

    @Id
    @Column("word_id")
    private UUID id;
    @Column("user_id")
    private final UUID userId;
    @Column("value")
    private String value;
    @Column("note")
    private String note;
    @MappedCollection(idColumn = "word_id", keyColumn = "index")
    private List<WordInterpretation> interpretations;
    @MappedCollection(idColumn = "word_id", keyColumn = "index")
    private List<WordTranscription> transcriptions;
    @MappedCollection(idColumn = "word_id", keyColumn = "index")
    private List<WordTranslation> translations;
    @MappedCollection(idColumn = "word_id", keyColumn = "index")
    private List<WordExample> examples;
    @Embedded.Nullable
    private RepeatData repeatData;

    @PersistenceCreator
    public Word(UUID id,
                UUID userId,
                String value,
                String note,
                List<WordInterpretation> interpretations,
                List<WordTranscription> transcriptions,
                List<WordTranslation> translations,
                List<WordExample> examples,
                RepeatData repeatData) {
        this.id = id;
        this.userId = userId;
        this.value = value;
        this.note = note;
        this.interpretations = interpretations;
        this.transcriptions = transcriptions;
        this.translations = translations;
        this.examples = examples;
        this.repeatData = repeatData;
    }

    public Word(UUID userId,
                String value,
                String note,
                ImmutableList<Integer> intervals) {
        this.userId = userId;
        this.value = value;
        this.note = note;
        interpretations = new ArrayList<>();
        transcriptions = new ArrayList<>();
        translations = new ArrayList<>();
        examples = new ArrayList<>();
        repeatData = new RepeatData(intervals.get(0), LocalDate.now());
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

    public List<WordInterpretation> getInterpretations() {
        return Collections.unmodifiableList(interpretations);
    }

    public List<WordTranscription> getTranscriptions() {
        return Collections.unmodifiableList(transcriptions);
    }

    public List<WordTranslation> getTranslations() {
        return Collections.unmodifiableList(translations);
    }

    public List<WordExample> getExamples() {
        return Collections.unmodifiableList(examples);
    }

    public RepeatData getRepeatData() {
        return repeatData;
    }

    public boolean isHotRepeat(ImmutableList<Integer> intervals) {
        return repeatData.interval() == intervals.get(0);
    }

    @Override
    public void generateIdIfAbsent() {
        if(id == null) id = UUID.randomUUID();
    }

    public Word addInterpretations(WordInterpretation interpretation) {
        interpretations.add(interpretation);
        return this;
    }

    public Word addTranscriptions(WordTranscription transcription) {
        transcriptions.add(transcription);
        return this;
    }

    public Word addTranslations(WordTranslation translation) {
        translations.add(translation);
        return this;
    }

    public Word addExamples(WordExample example) {
        examples.add(example);
        return this;
    }

    public void repeat(boolean isRemember, LocalDate lastDateOfRepeat, ImmutableList<Integer> intervals) {
        int index = isRemember ?
                Math.min(intervals.indexOf(repeatData.interval()) + 1, intervals.size() - 1) : 0;

        repeatData = new RepeatData(intervals.get(index), lastDateOfRepeat);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Word word = (Word) o;
        return id.equals(word.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public String toString() {
        return "Word{" +
                "id=" + id +
                ", userId=" + userId +
                ", value='" + value + '\'' +
                ", note='" + note + '\'' +
                ", interpretations=" + interpretations +
                ", transcriptions=" + transcriptions +
                ", translations=" + translations +
                ", examples=" + examples +
                ", repeatData=" + repeatData +
                '}';
    }

}
