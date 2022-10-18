package com.bakuard.flashcards.model.word;

import com.bakuard.flashcards.model.Entity;
import com.bakuard.flashcards.model.RepeatDataFromEnglish;
import com.bakuard.flashcards.model.RepeatDataFromNative;
import com.bakuard.flashcards.validation.AllUnique;
import com.bakuard.flashcards.validation.NotBlankOrNull;
import com.bakuard.flashcards.validation.NotContainsNull;
import com.bakuard.flashcards.validation.ValidatorUtil;
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
    @NotNull(message = "Word.repeatDataFromEnglish.notNull")
    @Embedded.Nullable
    @Valid
    private RepeatDataFromEnglish repeatDataFromEnglish;
    @NotNull(message = "Word.repeatDataFromNative.notNull")
    @Embedded.Nullable
    @Valid
    private RepeatDataFromNative repeatDataFromNative;
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
    }

    private Word(UUID id,
                 UUID userId,
                 String value,
                 String note,
                 List<WordInterpretation> interpretations,
                 List<WordTranscription> transcriptions,
                 List<WordTranslation> translations,
                 List<WordExample> examples,
                 RepeatDataFromEnglish repeatDataFromEnglish,
                 RepeatDataFromNative repeatDataFromNative,
                 ValidatorUtil validator) {
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
        this.validator = validator;

        validator.assertValid(this);
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
                setRepeatData(repeatDataFromEnglish);
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
                ", repeatDataFromEnglish=" + repeatDataFromEnglish +
                ", repeatDataFromNative=" + repeatDataFromNative +
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
        private RepeatDataFromEnglish repeatDataFromEnglish;
        private RepeatDataFromNative repeatDataFromNative;
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

        public Builder setRepeatData(RepeatDataFromEnglish repeatDataFromEnglish) {
            this.repeatDataFromEnglish = repeatDataFromEnglish;
            return this;
        }

        public Builder setRepeatData(RepeatDataFromNative repeatDataFromNative) {
            this.repeatDataFromNative = repeatDataFromNative;
            return this;
        }

        public Builder setInitialRepeatData(int lowestInterval, Clock clock) {
            repeatDataFromEnglish = new RepeatDataFromEnglish(lowestInterval, LocalDate.now(clock));
            repeatDataFromNative = new RepeatDataFromNative(lowestInterval, LocalDate.now(clock));
            return this;
        }

        public Builder setInterpretations(List<WordInterpretation> interpretations) {
            this.interpretations.clear();
            if(interpretations != null) this.interpretations.addAll(interpretations);
            return this;
        }

        public Builder setTranscriptions(List<WordTranscription> transcriptions) {
            this.transcriptions.clear();
            if(transcriptions != null) this.transcriptions.addAll(transcriptions);
            return this;
        }

        public Builder setTranslations(List<WordTranslation> translations) {
            this.translations.clear();
            if(translations != null) this.translations.addAll(translations);
            return this;
        }

        public Builder setExamples(List<WordExample> examples) {
            this.examples.clear();
            if(examples != null) this.examples.addAll(examples);
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
                    repeatDataFromEnglish,
                    repeatDataFromNative,
                    validator
            );
        }

    }

}
