package com.bakuard.flashcards.model;

import org.springframework.data.relational.core.mapping.Table;

@Table("words_examples")
public class WordExample {

    private String origin;
    private String translate;
    private String note;

}
