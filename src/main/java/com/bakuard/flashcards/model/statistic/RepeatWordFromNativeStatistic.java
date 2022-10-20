package com.bakuard.flashcards.model.statistic;

import java.time.LocalDate;
import java.util.UUID;

public record RepeatWordFromNativeStatistic(UUID id,
                                            UUID userId,
                                            UUID wordId,
                                            LocalDate currentDate,
                                            int interval,
                                            boolean isRemember) {}
