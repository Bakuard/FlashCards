package com.bakuard.flashcards.model.word;

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

@Table("words")
public class Word implements Entity {

    public static Builder newBuilder(ValidatorUtil validator) {
        return new Builder(validator);
    }


    @Id
    @Column("word_id")
    private UUID id;
    @Column("user_id")
    @NotNull(message = "Word.userId.notNull", groups = Groups.M.class)
    private final UUID userId;
    @Column("value")
    @NotBlank(message = "Word.value.notBlank", groups = Groups.M.class)
    private String value;
    @Column("note")
    @NotBlankOrNull(message = "Word.note.notBlankOrNull", groups = Groups.M.class)
    private String note;
    @MappedCollection(idColumn = "word_id", keyColumn = "index")
    @NotNull(message = "Word.interpretations.notNull", groups = Groups.A.class)
    @NotContainsNull(message = "Word.interpretations.notContainsNull", groups = Groups.B.class)
    @AllUnique(nameOfGetterMethod = "getValue", message = "Word.interpretations.allUnique", groups = Groups.C.class)
    private List<@Valid WordInterpretation> interpretations;
    @MappedCollection(idColumn = "word_id", keyColumn = "index")
    @NotNull(message = "Word.transcriptions.notNull", groups = Groups.D.class)
    @NotContainsNull(message = "Word.transcriptions.notContainsNull", groups = Groups.E.class)
    @AllUnique(nameOfGetterMethod = "getValue", message = "Word.transcriptions.allUnique", groups = Groups.F.class)
    private final List<@Valid WordTranscription> transcriptions;
    @MappedCollection(idColumn = "word_id", keyColumn = "index")
    @NotNull(message = "Word.translations.notNull", groups = Groups.G.class)
    @NotContainsNull(message = "Word.translations.notContainsNull", groups = Groups.H.class)
    @AllUnique(nameOfGetterMethod = "getValue", message = "Word.translations.allUnique", groups = Groups.I.class)
    private final List<@Valid WordTranslation> translations;
    @MappedCollection(idColumn = "word_id", keyColumn = "index")
    @NotNull(message = "Word.examples.notNull", groups = Groups.J.class)
    @NotContainsNull(message = "Word.examples.notContainsNull", groups = Groups.K.class)
    @AllUnique(nameOfGetterMethod = "getOrigin", message = "Word.examples.allUnique", groups = Groups.L.class)
    private final List<@Valid WordExample> examples;
    @Embedded.Nullable
    @Valid
    private RepeatData repeatData;
    @Transient
    private ValidatorUtil validator;

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

    private Word(UUID id,
                 UUID userId,
                 String value,
                 String note,
                 List<WordInterpretation> interpretations,
                 List<WordTranscription> transcriptions,
                 List<WordTranslation> translations,
                 List<WordExample> examples,
                 RepeatData repeatData,
                 ValidatorUtil validator) {
        this.id = id;
        this.userId = userId;
        this.value = value;
        this.note = note;
        this.interpretations = interpretations;
        this.transcriptions = transcriptions;
        this.translations = translations;
        this.examples = examples;
        this.repeatData = repeatData;
        this.validator = validator;

        validator.assertAllEmpty(this,
                validator.check(this, Groups.M.class),
                validator.check(this, Groups.A.class, Groups.B.class, Groups.C.class, Default.class),
                validator.check(this, Groups.D.class, Groups.E.class, Groups.F.class, Default.class),
                validator.check(this, Groups.G.class, Groups.H.class, Groups.I.class, Default.class),
                validator.check(this, Groups.J.class, Groups.K.class, Groups.L.class, Default.class)
        );
    }

    @Override
    public UUID getId() {
        return id;
    }

    @Override
    public boolean isNew() {
        return id == null;
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
                setTranscriptions(transcriptions).
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
                ", validator=" + validator +
                '}';
    }


    public static class Builder {

        private UUID wordId;
        private UUID userId;
        private String value;
        private String note;
        private List<WordInterpretation> interpretations;
        private List<WordTranscription> transcriptions;
        private List<WordTranslation> translations;
        private List<WordExample> examples;
        private RepeatData repeatData;
        private final ValidatorUtil validator;

        private Builder(ValidatorUtil validator) {
            interpretations = new ArrayList<>();
            transcriptions = new ArrayList<>();
            translations = new ArrayList<>();
            examples = new ArrayList<>();
            this.validator = validator;
        }

        public Builder setOrGenerateId(UUID wordId) {
            this.wordId = wordId == null ? UUID.randomUUID() : wordId;
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

        public Builder setInterpretations(List<WordInterpretation> interpretations) {
            this.interpretations = interpretations != null ? new ArrayList<>(interpretations) : null;
            return this;
        }

        public Builder setTranscriptions(List<WordTranscription> transcriptions) {
            this.transcriptions = transcriptions != null ? new ArrayList<>(transcriptions) : null;
            return this;
        }

        public Builder setTranslations(List<WordTranslation> translations) {
            this.translations = translations != null ? new ArrayList<>(translations) : null;
            return this;
        }

        public Builder setExamples(List<WordExample> examples) {
            this.examples = examples != null ? new ArrayList<>(examples) : null;
            return this;
        }

        public Builder addInterpretation(WordInterpretation interpretation) {
            interpretations.add(interpretation);
            return this;
        }

        public Builder addTranscription(WordTranscription transcription) {
            transcriptions.add(transcription);
            return this;
        }

        public Builder addTranslation(WordTranslation translation) {
            translations.add(translation);
            return this;
        }

        public Builder addExample(WordExample example) {
            examples.add(example);
            return this;
        }

        public Word build() {
            return new Word(
                    wordId,
                    userId,
                    value,
                    note,
                    interpretations,
                    transcriptions,
                    translations,
                    examples,
                    repeatData,
                    validator
            );
        }

    }

}
