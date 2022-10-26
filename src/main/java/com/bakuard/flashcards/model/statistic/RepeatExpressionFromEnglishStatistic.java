package com.bakuard.flashcards.model.statistic;

import java.time.LocalDate;
import java.util.UUID;

public record RepeatExpressionFromEnglishStatistic(UUID userId,
                                                   UUID expressionId,
                                                   LocalDate currentDate,
                                                   boolean isRemember) {}
