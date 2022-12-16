package com.bakuard.flashcards.service.wordSupplementation;

import com.bakuard.flashcards.dal.WordOuterSourceBuffer;
import com.bakuard.flashcards.model.word.Word;

/**
 * Отвечает за дополнение слова транскрипциями, толкованиями, переводами и переводами примеров из буфера,
 * а также за обновление буфера соответствующими данными полученными из внешних сервисов.
 */
public class WordSupplementationFromBuffer implements WordSupplementation {

    private WordSupplementation[] outerSource;
    private WordOuterSourceBuffer wordOuterSourceBuffer;

    public WordSupplementationFromBuffer(WordOuterSourceBuffer wordOuterSourceBuffer,
                                         WordSupplementation... outerSource) {
        this.outerSource = outerSource;
        this.wordOuterSourceBuffer = wordOuterSourceBuffer;
    }

    /**
     * Дополняет слово транскрипциями, толкованиями, переводами и переводами примеров из буфера, далее
     * передает вызов другим реализациям см. {@link WordSupplementation}, а затем обновляет буфер
     * данными полученными из внешних сервисов.
     * @see WordSupplementation
     */
    @Override
    public Word supplement(Word word) {
        wordOuterSourceBuffer.mergeFromOuterSource(word);
        for(WordSupplementation wordSupplementation : outerSource) wordSupplementation.supplement(word);
        wordOuterSourceBuffer.saveDataFromOuterSource(word);
        return word;
    }

}
