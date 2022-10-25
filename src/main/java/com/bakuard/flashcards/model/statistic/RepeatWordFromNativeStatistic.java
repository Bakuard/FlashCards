package com.bakuard.flashcards.model.statistic;

import java.time.LocalDate;
import java.util.UUID;

public record RepeatWordFromNativeStatistic(UUID userId,
                                            UUID wordId,
                                            LocalDate currentDate,
                                            boolean isRemember) {}
