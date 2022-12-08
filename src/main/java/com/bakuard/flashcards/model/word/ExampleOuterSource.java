package com.bakuard.flashcards.model.word;

import java.time.LocalDate;

/**
 * Данные о внешнем сервисе используемом для перевода одного из примеров английского слова.
 * @param url url внешнего сервиса напрямую ведущий к переводу примера слова.
 * @param sourceName наименование внешнего сервиса.
 * @param recentUpdateDate дата последнего обновления перевода примера к слову из этого сервиса.
 * @param translate перевод примера к слову.
 */
public record ExampleOuterSource(String url,
                                 String sourceName,
                                 LocalDate recentUpdateDate,
                                 String translate) {}
