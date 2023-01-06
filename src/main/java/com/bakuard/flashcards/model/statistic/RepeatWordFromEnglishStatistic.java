package com.bakuard.flashcards.model.statistic;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Сохраняемые данные о результате одного из повторений слова с английского языка на родной язык пользователя.
 * @param userId идентификатор пользователя, к словарю которого относится слово.
 * @param wordId идентификатор слова, по которому собирается статистика.
 * @param currentDate дата повторения слова.
 * @param isRemember true - если повторение было успешно, иначе - false.
 */
public record RepeatWordFromEnglishStatistic(UUID userId,
                                             UUID wordId,
                                             LocalDate currentDate,
                                             boolean isRemember) {}
