package com.bakuard.flashcards.model.word.supplementation;

import java.net.URI;

/**
 * Данные о внешнем сервисе используемом для перевода одного из примеров английского слова.
 * @param uri uri внешнего сервиса напрямую ведущий к переводу примера слова.
 * @param sourceName наименование внешнего сервиса.
 * @param translate перевод примера к слову.
 */
public record ExampleOuterSource(URI uri,
                                 String sourceName,
                                 String translate) {}
