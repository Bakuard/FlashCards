package com.bakuard.flashcards.dal;

import com.bakuard.flashcards.model.word.*;

/**
 * Отвечает за буферизацию данных слова (транскрипции, толкования, переводы и переводы примеров)
 * полученных из внешних сервисов.
 */
public interface WordOuterSourceBuffer {

    /**
     * Сохраняет все транскрипции, толкования, переводы и переводы примеров слова, которые были
     * получены из внешних сервисов в постоянное хранилище. Чтобы определить, какие из вышеперечисленных
     * данных получены из внешних сервисов, для каждой транскрипции, перевода и т.д. проверяются одно из
     * соответствующих условий: <br/>
     * 1. {@link WordTranscription#getOuterSource()} возвращает не пустой список. <br/>
     * 2. {@link WordTranslation#getOuterSource()} возвращает не пустой список. <br/>
     * 3. {@link WordInterpretation#getOuterSource()} возвращает не пустой список. <br/>
     * 4. {@link WordExample#getOuterSource()} возвращает не пустой список. <br/>
     * @param word слово, у которого сохраняются транскрипции, толкования, переводы и переводы примеров
     *             полученные из внешних сервисов.
     */
    public void saveDataFromOuterSource(Word word);

    /**
     * Загружает из буфера для выбранного слова все подходящие транскрипции, толкования, переводы и
     * переводы примеров. Далее, каждая транскрипция, перевод и т.д. будут добавлены в слово с помощью
     * одного из соответствующих методов mergeXXX() класса {@link Word}.
     * @param word слово, для которого загружаются транскрипции, толкования, переводы и переводы примеров.
     * @see Word#mergeTranslation(WordTranslation)
     * @see Word#mergeTranscription(WordTranscription)
     * @see Word#mergeInterpretation(WordInterpretation)
     * @see Word#mergeExampleIfPresent(WordExample)
     */
    public void mergeFromOuterSource(Word word);

    /**
     * Удаляет из буфера все переводы к примерам, которые больше не используются ни для одного слова.
     * Если таких нет - ничего не делает.
     */
    public void deleteUnusedOuterSourceExamples();

}
