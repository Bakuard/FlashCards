package com.bakuard.flashcards.service.wordSupplementation;

import com.bakuard.flashcards.dal.WordOuterSourceBuffer;
import com.bakuard.flashcards.model.word.Word;
import com.bakuard.flashcards.service.util.Transaction;

public class WordSupplementationFromBuffer implements WordSupplementation {

    private WordSupplementation[] outerSource;
    private WordOuterSourceBuffer wordOuterSourceBuffer;
    private Transaction transaction;

    public WordSupplementationFromBuffer(WordOuterSourceBuffer wordOuterSourceBuffer,
                                         Transaction transaction,
                                         WordSupplementation... outerSource) {
        this.outerSource = outerSource;
        this.transaction = transaction;
        this.wordOuterSourceBuffer = wordOuterSourceBuffer;
    }

    @Override
    public Word supplement(Word word) {
        transaction.commit(() -> wordOuterSourceBuffer.mergeFromOuterSource(word));
        for(WordSupplementation wordSupplementation : outerSource) wordSupplementation.supplement(word);
        transaction.commit(() -> wordOuterSourceBuffer.saveDataFromOuterSource(word));
        return word;
    }

}
