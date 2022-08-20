package com.bakuard.flashcards.config.security;

import java.util.UUID;

public interface QueryContext {

    /**
     * Возвращает, а затем удаляет из контекста текущего запроса данные об идентификаторе пользователя,
     * который сделал этот запрос.
     * @return уникальный идентификатор пользователя.
     */
    public UUID getAndClearUserId();

}
