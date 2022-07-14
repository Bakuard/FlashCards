package com.bakuard.flashcards.model;

import org.springframework.data.relational.core.mapping.Table;

@Table("words_translations")
public class WordTranslation {

    private String value;
    private String note;

}
