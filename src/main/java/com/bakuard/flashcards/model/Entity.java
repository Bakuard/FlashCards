package com.bakuard.flashcards.model;

import com.bakuard.flashcards.validation.ValidatorUtil;

import java.util.UUID;

public interface Entity {

    public UUID getId();

    public void generateIdIfAbsent();

    public void setValidator(ValidatorUtil validator);

}
