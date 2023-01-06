package com.bakuard.flashcards.service.wordSupplementation;

import com.bakuard.flashcards.model.word.*;
import com.bakuard.flashcards.model.word.supplementation.SupplementedWord;

/**
 * Отвечает за поиск транскрипций, толкований, переводов или переводов примеров к указанному слову.
 */
public interface WordSupplementation {

    /**
     * Возвращает для переданного слова транскрипции, толковании, переводы или переводы к его примерам,
     * полученные из некоторого источника.
     * @param word см. {@link Word}
     * @see SupplementedWord
     * @see Word
     */
    public SupplementedWord supplement(Word word);

}
