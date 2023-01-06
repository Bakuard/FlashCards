package com.bakuard.flashcards.model.word.supplementation;

import com.bakuard.flashcards.model.word.WordExample;

import java.net.URI;
import java.util.Objects;

/**
 * Пример к слову переведенный из внешнего источника.
 */
public class SupplementedWordExample extends WordExample {

    private URI outerSourceUri;

    /**
     * Создает новый пример к слову.
     * @param origin текст примера на английском языке
     * @param translate перевод к примеру
     * @param note примечание к примеру
     * @param outerSourceUri ссылка на внешний сервис для получения перевода этого примера
     */
    public SupplementedWordExample(String origin,
                                   String translate,
                                   String note,
                                   URI outerSourceUri) {
        super(origin, translate, note);
        this.outerSourceUri = outerSourceUri;
    }

    /**
     * Выполняет глубокое копирование примера к слову
     * @param other копируемый пример
     */
    public SupplementedWordExample(SupplementedWordExample other) {
        super(other);
        this.outerSourceUri = other.outerSourceUri;
    }

    /**
     * Возвращает ссылку на внешний сервис для получения перевода этого примера.
     */
    public URI getOuterSourceUri() {
        return outerSourceUri;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        SupplementedWordExample that = (SupplementedWordExample) o;
        return Objects.equals(outerSourceUri, that.outerSourceUri);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), outerSourceUri);
    }

    @Override
    public String toString() {
        return "SupplementedWordExample{" +
                "outerSourceUri=" + outerSourceUri +
                ", origin='" + getOrigin() + '\'' +
                ", translate='" + getTranslate() + '\'' +
                ", note='" + getNote() + '\'' +
                '}';
    }

}
