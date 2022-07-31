package com.bakuard.flashcards.model;

import org.springframework.data.relational.core.mapping.Table;

@Table("expressions_interpretations")
public class ExpressionInterpretation {

    private String value;

    public ExpressionInterpretation(String value) {
        this.value = value;
    }

}
