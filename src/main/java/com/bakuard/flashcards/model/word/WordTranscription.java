package com.bakuard.flashcards.model.word;

import org.springframework.data.relational.core.mapping.Table;

@Table("words_transcriptions")
public class WordTranscription {

    private String value;
    private String note;

}
