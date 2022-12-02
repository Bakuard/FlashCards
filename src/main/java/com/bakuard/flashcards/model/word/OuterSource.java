package com.bakuard.flashcards.model.word;

import java.time.LocalDate;

public record OuterSource(String url,
                          String sourceName,
                          LocalDate recentUpdateDate) {}
