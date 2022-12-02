package com.bakuard.flashcards.model.word;

import java.time.LocalDate;

public record ExampleOuterSource(String url,
                                 String sourceName,
                                 LocalDate recentUpdateDate,
                                 String translate) {}
