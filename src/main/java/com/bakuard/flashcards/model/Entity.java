package com.bakuard.flashcards.model;

import java.util.UUID;

public interface Entity<T> {

    public UUID getId();

    public void generateIdIfAbsent();

}
