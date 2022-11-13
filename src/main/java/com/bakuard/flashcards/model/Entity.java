package com.bakuard.flashcards.model;

import java.util.UUID;

public interface Entity {

    public UUID getId();

    public boolean isNew();

    public void generateIdIfAbsent();

}
