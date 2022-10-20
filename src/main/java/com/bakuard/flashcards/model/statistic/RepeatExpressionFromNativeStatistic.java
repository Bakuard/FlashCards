package com.bakuard.flashcards.model.statistic;

import java.time.LocalDate;
import java.util.UUID;

public record RepeatExpressionFromNativeStatistic(UUID id,
                                                  UUID userId,
                                                  UUID expressionId,
                                                  LocalDate currentDate,
                                                  int interval,
                                                  boolean isRemember) {}
