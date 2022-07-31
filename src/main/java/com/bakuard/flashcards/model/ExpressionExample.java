package com.bakuard.flashcards.model;

import org.springframework.data.relational.core.mapping.Table;

@Table("expressions_examples")
public class ExpressionExample {

    private String origin;
    private String translate;
    private String note;

    public ExpressionExample(String origin, String translate, String note) {
        this.origin = origin;
        this.translate = translate;
        this.note = note;
    }

}
