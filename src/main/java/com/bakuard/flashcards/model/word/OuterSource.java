package com.bakuard.flashcards.model.word;

import java.time.LocalDate;

/**
 * Данные о внешнем сервисе используемом для перевода, получения транскрипции или толкования английского слова.
 * @param url url внешнего сервиса напрямую ведущий к переводу, транскрипции или толкованию слова.
 * @param sourceName наименование внешнего сервиса.
 * @param recentUpdateDate дата последнего обновления перевода, транскрипции или толкования слова.
 */
public record OuterSource(String url,
                          String sourceName,
                          LocalDate recentUpdateDate) {}
