package com.bakuard.flashcards.model;

/**
 * Содержит данные о результате повторения слова или устойчивого выражения с родного языка пользователя на английский
 * язык.
 * @param payload слово или устойчивое выражение.
 * @param isRemember true - если пользователь верно вспомнил нужный перевод на английский язык, false - в противном случае.
 * @param <T>
 */
public record RepetitionResult<T>(T payload, boolean isRemember) {}
