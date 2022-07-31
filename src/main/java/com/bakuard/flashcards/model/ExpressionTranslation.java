package com.bakuard.flashcards.model;

import org.springframework.data.relational.core.mapping.Table;

@Table("expressions_translations")
public class ExpressionTranslation {

    private String value;
    private String note;

    public ExpressionTranslation(String value, String note) {
        this.value = value;
        this.note = note;
    }

}
