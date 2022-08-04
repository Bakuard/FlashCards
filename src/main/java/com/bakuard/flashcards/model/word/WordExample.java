package com.bakuard.flashcards.model.word;

import org.springframework.data.relational.core.mapping.Table;

@Table("words_examples")
public class WordExample {

    private String origin;
    private String translate;
    private String note;

    public WordExample(String origin, String translate, String note) {
        this.origin = origin;
        this.translate = translate;
        this.note = note;
    }

}
