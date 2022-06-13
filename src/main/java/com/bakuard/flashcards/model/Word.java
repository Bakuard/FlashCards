package com.bakuard.flashcards.model;

import java.util.List;
import java.util.UUID;

public class Word {

    private UUID id;
    private User user;
    private String value;
    private String note;
    private List<Interpretation> interpretations;
    private List<Transcription> transcriptions;
    private List<Translation> translations;
    private List<Example> examples;

    private RepeatData repeatData;

}
