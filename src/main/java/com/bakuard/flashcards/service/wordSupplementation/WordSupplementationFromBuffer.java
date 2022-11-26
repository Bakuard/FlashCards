package com.bakuard.flashcards.service.wordSupplementation;

import com.bakuard.flashcards.dal.WordRepository;
import com.bakuard.flashcards.model.word.Word;
import com.bakuard.flashcards.service.util.Transaction;

public class WordSupplementationFromBuffer implements WordSupplementation {

    private WordSupplementation[] outerSource;
    private WordRepository wordRepository;
    private Transaction transaction;

    public WordSupplementationFromBuffer(WordRepository wordRepository,
                                         Transaction transaction,
                                         WordSupplementation... outerSource) {
        this.outerSource = outerSource;
        this.transaction = transaction;
        this.wordRepository = wordRepository;
    }

    @Override
    public Word supplement(Word word) {
        transaction.commit(() -> wordRepository.mergeFromOuterSource(word));
        for(WordSupplementation wordSupplementation : outerSource) wordSupplementation.supplement(word);
        transaction.commit(() -> wordRepository.saveDataFromOuterSource(word));
        return word;
    }

}
