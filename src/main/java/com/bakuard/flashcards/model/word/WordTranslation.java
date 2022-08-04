package com.bakuard.flashcards.model.word;

import org.springframework.data.relational.core.mapping.Table;

@Table("words_translations")
public class WordTranslation {

    private String value;
    private String note;

    public WordTranslation(String value, String note) {
        this.value = value;
        this.note = note;
    }

}
