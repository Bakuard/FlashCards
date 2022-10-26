package com.bakuard.flashcards.model.statistic;

import java.util.UUID;

public record WordRepetitionByPeriodStatistic(UUID userId,
                                              UUID wordId,
                                              String value,
                                              long rememberFromEnglish,
                                              long notRememberFromEnglish,
                                              long rememberFromNative,
                                              long notRememberFromNative) {


    public long totalRepetitionNumbersFromEnglish() {
        return rememberFromEnglish + notRememberFromEnglish;
    }

    public long totalRepetitionNumbersFromNative() {
        return rememberFromNative + notRememberFromNative;
    }

}
