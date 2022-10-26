package com.bakuard.flashcards.model.statistic;

import java.time.LocalDate;
import java.util.UUID;

public record RepeatWordFromEnglishStatistic(UUID userId,
                                             UUID wordId,
                                             LocalDate currentDate,
                                             boolean isRemember) {}
