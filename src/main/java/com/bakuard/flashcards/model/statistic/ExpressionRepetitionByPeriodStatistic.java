package com.bakuard.flashcards.model.statistic;

import java.util.UUID;

/**
 * Содержит данные о результатах повторения определенного устойчивого выражения за определенный период.
 * @param userId идентификатор пользователя, к словарю которого относится устойчивое выражение.
 * @param expressionId идентификатор устойчивого выражения, по которому собирается статистика.
 * @param value значение устойчивого выражения, по которому собирается статистика.
 * @param rememberFromEnglish кол-во успешных повторений с английского языка на родной язык пользователя.
 * @param notRememberFromEnglish кол-во не успешных повторений с английского языка на родной язык пользователя.
 * @param rememberFromNative кол-во успешных повторений с родного языка пользователя на английский язык.
 * @param notRememberFromNative кол-во не успешных повторений с родного языка пользователя на английский язык.
 */
public record ExpressionRepetitionByPeriodStatistic(UUID userId,
                                                    UUID expressionId,
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
