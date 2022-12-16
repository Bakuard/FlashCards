package com.bakuard.flashcards.service.wordSupplementation;

import com.bakuard.flashcards.model.word.*;

/**
 * Отвечает за заполнение переданного слова транскрипциями, толкованиями, переводами или переводами примеров
 * полученных из одного конкретного внешнего сервиса или другого источника. Каждая из реализаций этого
 * интерфейса должна самостоятельно решить - когда нужно загружать данные для переданного слова,
 * нужны ли какие-либо транзакции.
 */
public interface WordSupplementation {

    /**
     * Слово заполняемое транскрипциями, толкованиями, переводами или переводами примеров из некоторого
     * внешнего сервиса или другого источника. При этом, каждая транскрипция, перевод и т.д. слова
     * должны добавляться в него с помощью одного из соответствующих методов mergeXXX() класса {@link Word}.
     * @param word слово дополняемое данными из одного или нескольких перечисленных выше типов.
     * @return это же слово
     * @see Word#mergeTranslation(WordTranslation)
     * @see Word#mergeTranscription(WordTranscription)
     * @see Word#mergeInterpretation(WordInterpretation)
     * @see Word#mergeExampleIfPresent(WordExample)
     */
    public Word supplement(Word word);

}
