package com.bakuard.flashcards.model;

import org.springframework.data.domain.Persistable;

import java.util.UUID;

public interface Entity extends Persistable<UUID> {

    public UUID getId();

    public boolean isNew();

    public void generateIdIfAbsent();

}
