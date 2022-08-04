package com.bakuard.flashcards.model.word;

import org.springframework.data.relational.core.mapping.Table;

@Table("words_interpretations")
public class WordInterpretation {

    private String value;

    public WordInterpretation(String value) {
        this.value = value;
    }

}
