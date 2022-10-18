package com.bakuard.flashcards.model;

public record RepetitionResult<T>(T payload, boolean isRemember) {}
