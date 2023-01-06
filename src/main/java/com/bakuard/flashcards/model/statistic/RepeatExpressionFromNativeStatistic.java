package com.bakuard.flashcards.model.statistic;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Сохраняемые данные о результате одного из повторений устойчивого выражения с родного языка
 * пользователя на английский язык.
 * @param userId идентификатор пользователя, к словарю которого относится устойчивое выражение.
 * @param expressionId идентификатор устойчивого выражения, по которому собирается статистика.
 * @param currentDate дата повторения устойчивого выражения.
 * @param isRemember true - если повторение было успешно, иначе - false.
 */
public record RepeatExpressionFromNativeStatistic(UUID userId,
                                                  UUID expressionId,
                                                  LocalDate currentDate,
                                                  boolean isRemember) {}
