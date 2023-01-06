package com.bakuard.flashcards.model;

import org.springframework.data.domain.Persistable;

import java.util.UUID;

/**
 * Общий интерфейс для всех сущностей.
 */
public interface Entity extends Persistable<UUID> {

    /**
     * Возвращает уникальный идентификатор сущности. Если сущность является новой ({@link  #isNew()}) -
     * возвращает null.
     * @return уникальный идентификатор сущности или null.
     */
    public UUID getId();

    /**
     * Проверяет - является ли данная сущность новой. Сущность считается новой до первого сохранения в БД.
     * @return true - если сущность новая, иначе - false.
     */
    public default boolean isNew() {
        return getId() == null;
    }

    /**
     * Данный метод используется слоем доступа к данным для генерации идентификатор сущности перед её первым
     * сохранением
     */
    public void generateIdIfAbsent();

}
