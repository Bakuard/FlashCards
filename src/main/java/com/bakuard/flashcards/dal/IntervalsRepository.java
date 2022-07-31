package com.bakuard.flashcards.dal;

import com.google.common.collect.ImmutableList;

import java.util.UUID;

public interface IntervalsRepository {

    public void add(UUID userId, int interval);

    public void removeUnused(UUID userId);

    public ImmutableList<Integer> findAll(UUID userId);

}
