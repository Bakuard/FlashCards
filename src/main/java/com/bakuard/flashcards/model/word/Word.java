package com.bakuard.flashcards.model.word;

import com.bakuard.flashcards.model.Entity;
import com.bakuard.flashcards.model.RepeatDataFromEnglish;
import com.bakuard.flashcards.model.RepeatDataFromNative;
import com.bakuard.flashcards.validation.AllUnique;
import com.bakuard.flashcards.validation.NotBlankOrNull;
import com.bakuard.flashcards.validation.NotContainsNull;
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
import java.util.*;
import java.util.stream.Collectors;

@Table("words")
public class Word implements Entity {

    @Id
    @Column("word_id")
    private UUID id;
    @Column("user_id")
    @NotNull(message = "Word.userId.notNull")
    private final UUID userId;
    @Column("value")
    @NotBlank(message = "Word.value.notBlank")
    private String value;
    @Column("note")
    @NotBlankOrNull(message = "Word.note.notBlankOrNull")
    private String note;
    @MappedCollection(idColumn = "word_id", keyColumn = "index")
    @NotContainsNull(message = "Word.interpretations.notContainsNull")
    @AllUnique(nameOfGetterMethod = "getValue", message = "Word.interpretations.allUnique")
    private List<@Valid WordInterpretation> interpretations;
    @MappedCollection(idColumn = "word_id", keyColumn = "index")
    @NotContainsNull(message = "Word.transcriptions.notContainsNull")
    @AllUnique(nameOfGetterMethod = "getValue", message = "Word.transcriptions.allUnique")
    private final List<@Valid WordTranscription> transcriptions;
    @MappedCollection(idColumn = "word_id", keyColumn = "index")
    @NotContainsNull(message = "Word.translations.notContainsNull")
    @AllUnique(nameOfGetterMethod = "getValue", message = "Word.translations.allUnique")
    private final List<@Valid WordTranslation> translations;
    @MappedCollection(idColumn = "word_id", keyColumn = "index")
    @NotContainsNull(message = "Word.examples.notContainsNull")
    @AllUnique(nameOfGetterMethod = "getOrigin", message = "Word.examples.allUnique")
    private final List<@Valid WordExample> examples;
    @Embedded.Nullable
    @Valid
    private RepeatDataFromEnglish repeatDataFromEnglish;
    @Embedded.Nullable
    @Valid
    private RepeatDataFromNative repeatDataFromNative;
    @Transient
    private boolean isNew;

    @PersistenceCreator
    public Word(UUID id,
                UUID userId,
                String value,
                String note,
                List<WordInterpretation> interpretations,
                List<WordTranscription> transcriptions,
                List<WordTranslation> translations,
                List<WordExample> examples,
                RepeatDataFromEnglish repeatDataFromEnglish,
                RepeatDataFromNative repeatDataFromNative) {
        this.id = id;
        this.userId = userId;
        this.value = value;
        this.note = note;
        this.interpretations = interpretations;
        this.transcriptions = transcriptions;
        this.translations = translations;
        this.examples = examples;
        this.repeatDataFromEnglish = repeatDataFromEnglish;
        this.repeatDataFromNative = repeatDataFromNative;
        this.isNew = false;
    }

    public Word(UUID userId, int lowestIntervalForEnglish, int lowestIntervalForNative, Clock clock) {
        this.userId = userId;
        this.id = UUID.randomUUID();
        this.interpretations = new ArrayList<>();
        this.transcriptions = new ArrayList<>();
        this.translations = new ArrayList<>();
        this.examples = new ArrayList<>();
        this.repeatDataFromEnglish = new RepeatDataFromEnglish(lowestIntervalForEnglish, LocalDate.now(clock));
        this.repeatDataFromNative = new RepeatDataFromNative(lowestIntervalForNative, LocalDate.now(clock));
        this.isNew = true;
    }

    public Word(Word other) {
        this.id = other.id;
        this.userId = other.userId;
        this.value = other.value;
        this.note = other.note;
        this.interpretations = other.interpretations.stream().
                map(WordInterpretation::new).
                collect(Collectors.toCollection(ArrayList::new));
        this.transcriptions = other.transcriptions.stream().
                map(WordTranscription::new).
                collect(Collectors.toCollection(ArrayList::new));
        this.translations = other.translations.stream().
                map(WordTranslation::new).
                collect(Collectors.toCollection(ArrayList::new));
        this.examples = other.examples.stream().
                map(WordExample::new).
                collect(Collectors.toCollection(ArrayList::new));
        this.repeatDataFromEnglish = RepeatDataFromEnglish.copy(other.repeatDataFromEnglish);
        this.repeatDataFromNative = RepeatDataFromNative.copy(other.repeatDataFromNative);
        this.isNew = other.isNew;
    }

    @Override
    public UUID getId() {
        return id;
    }

    @Override
    public boolean isNew() {
        return isNew;
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

    public List<WordInterpretation> getInterpretationsBy(String outerSourceName) {
        return interpretations.stream().
                filter(interpretation -> interpretation.getSourceInfo().stream().
                        anyMatch(sourceInfo -> sourceInfo.sourceName().equals(outerSourceName))).
                collect(Collectors.toCollection(ArrayList::new));
    }

    public List<WordTranscription> getTranscriptions() {
        return Collections.unmodifiableList(transcriptions);
    }

    public List<WordTranscription> getTranscriptionsBy(String outerSourceName) {
        return transcriptions.stream().
                filter(transcription -> transcription.getSourceInfo().stream().
                        anyMatch(sourceInfo -> sourceInfo.sourceName().equals(outerSourceName))).
                collect(Collectors.toCollection(ArrayList::new));
    }

    public List<WordTranslation> getTranslations() {
        return Collections.unmodifiableList(translations);
    }

    public List<WordTranslation> getTranslationsBy(String outerSourceName) {
        return translations.stream().
                filter(translation -> translation.getSourceInfo().stream().
                        anyMatch(sourceInfo -> sourceInfo.sourceName().equals(outerSourceName))).
                collect(Collectors.toCollection(ArrayList::new));
    }

    public List<WordExample> getExamples() {
        return Collections.unmodifiableList(examples);
    }

    public List<WordExample> getExamplesBy(String outerSourceName) {
        return examples.stream().
                filter(example -> example.getSourceInfo().stream().
                        anyMatch(sourceInfo -> sourceInfo.sourceName().equals(outerSourceName))).
                collect(Collectors.toCollection(ArrayList::new));
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
    public void markAsSaved() {
        isNew = false;
    }

    public Word setValue(String value) {
        this.value = value;
        return this;
    }

    public Word setNote(String note) {
        this.note = note;
        return this;
    }

    public Word setInterpretations(List<WordInterpretation> interpretations) {
        this.interpretations.clear();
        if(interpretations != null) this.interpretations.addAll(interpretations);
        return this;
    }

    public Word setTranscriptions(List<WordTranscription> transcriptions) {
        this.transcriptions.clear();
        if(transcriptions != null) this.transcriptions.addAll(transcriptions);
        return this;
    }

    public Word setTranslations(List<WordTranslation> translations) {
        this.translations.clear();
        if(translations != null) this.translations.addAll(translations);
        return this;
    }

    public Word setExamples(List<WordExample> examples) {
        this.examples.clear();
        if(examples != null) this.examples.addAll(examples);
        return this;
    }

    public Word mergeInterpretation(WordInterpretation interpretation) {
        boolean isMerged = false;
        for(int i = 0; i < interpretations.size() && !isMerged; i++) {
            isMerged = interpretations.get(i).merge(interpretation);
        }
        if(!isMerged) interpretations.add(interpretation);
        return this;
    }

    public Word mergeTranscription(WordTranscription transcription) {
        boolean isMerged = false;
        for(int i = 0; i < transcriptions.size() && !isMerged; i++) {
            isMerged = transcriptions.get(i).merge(transcription);
        }
        if(!isMerged) transcriptions.add(transcription);
        return this;
    }

    public Word mergeTranslation(WordTranslation translation) {
        boolean isMerged = false;
        for(int i = 0; i < translations.size() && !isMerged; i++) {
            isMerged = translations.get(i).merge(translation);
        }
        if(!isMerged) translations.add(translation);
        return this;
    }

    public Word mergeExample(WordExample example) {
        boolean isMerged = false;
        for(int i = 0; i < examples.size() && !isMerged; i++) {
            isMerged = examples.get(i).merge(example);
        }
        return this;
    }

    public Word addInterpretation(WordInterpretation interpretation) {
        interpretations.add(interpretation);
        return this;
    }

    public Word addTranscription(WordTranscription transcription) {
        transcriptions.add(transcription);
        return this;
    }

    public Word addTranslation(WordTranslation translation) {
        translations.add(translation);
        return this;
    }

    public Word addExample(WordExample example) {
        examples.add(example);
        return this;
    }

    public void repeatFromEnglish(boolean isRemember, LocalDate lastDateOfRepeat, ImmutableList<Integer> intervals) {
        int index = isRemember ?
                Math.min(intervals.indexOf(repeatDataFromEnglish.interval()) + 1, intervals.size() - 1) : 0;

        repeatDataFromEnglish = new RepeatDataFromEnglish(intervals.get(index), lastDateOfRepeat);
    }

    public boolean repeatFromNative(String inputValue, LocalDate lastDateOfRepeat, ImmutableList<Integer> intervals) {
        boolean isRemember = inputValue.equalsIgnoreCase(value);
        int index = isRemember ?
                Math.min(intervals.indexOf(repeatDataFromNative.interval()) + 1, intervals.size() - 1) : 0;

        repeatDataFromNative = new RepeatDataFromNative(intervals.get(index), lastDateOfRepeat);

        return isRemember;
    }

    public void markForRepetitionFromEnglish(LocalDate lastDateOfRepeat, int lowestInterval) {
        repeatDataFromEnglish = new RepeatDataFromEnglish(lowestInterval, lastDateOfRepeat);
    }

    public void markForRepetitionFromNative(LocalDate lastDateOfRepeat, int lowestInterval) {
        repeatDataFromNative = new RepeatDataFromNative(lowestInterval, lastDateOfRepeat);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Word word = (Word) o;
        return Objects.equals(id, word.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
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
                ", repeatDataFromEnglish=" + repeatDataFromEnglish +
                ", repeatDataFromNative=" + repeatDataFromNative +
                ", isNew=" + isNew +
                '}';
    }

}
