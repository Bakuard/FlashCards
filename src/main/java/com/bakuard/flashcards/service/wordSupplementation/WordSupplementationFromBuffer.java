package com.bakuard.flashcards.service.wordSupplementation;

import com.bakuard.flashcards.dal.WordOuterSourceBuffer;
import com.bakuard.flashcards.model.word.Word;

public class WordSupplementationFromBuffer implements WordSupplementation {

    private WordSupplementation[] outerSource;
    private WordOuterSourceBuffer wordOuterSourceBuffer;

    public WordSupplementationFromBuffer(WordOuterSourceBuffer wordOuterSourceBuffer,
                                         WordSupplementation... outerSource) {
        this.outerSource = outerSource;
        this.wordOuterSourceBuffer = wordOuterSourceBuffer;
    }

    @Override
    public Word supplement(Word word) {
        wordOuterSourceBuffer.mergeFromOuterSource(word);
        for(WordSupplementation wordSupplementation : outerSource) wordSupplementation.supplement(word);
        wordOuterSourceBuffer.saveDataFromOuterSource(word);
        return word;
    }

}
