package com.bakuard.flashcards.config.configData;

public record Pagination(int maxPageSize,
                         int defaultPageSize,
                         int minPageSize) {}
