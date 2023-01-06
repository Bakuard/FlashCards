package com.bakuard.flashcards.model.word.supplementation;

import java.net.URI;

/**
 * Данные о внешнем сервисе используемом для перевода, получения транскрипции или толкования английского слова.
 * @param uri uri внешнего сервиса напрямую ведущий к переводу, транскрипции или толкованию слова.
 * @param name наименование внешнего сервиса.
 */
public record OuterSource(String name, URI uri) {}
