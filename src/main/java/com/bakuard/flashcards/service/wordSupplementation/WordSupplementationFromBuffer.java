package com.bakuard.flashcards.service.wordSupplementation;

import com.bakuard.flashcards.dal.WordRepository;
import com.bakuard.flashcards.model.word.Word;

public class WordSupplementationFromBuffer implements WordSupplementation {

    private WordSupplementation[] outerSource;
    private WordRepository wordRepository;

    public WordSupplementationFromBuffer(WordRepository wordRepository,
                                         WordSupplementation... outerSource) {
        this.outerSource = outerSource;
        this.wordRepository = wordRepository;
    }

    @Override
    public Word supplement(Word word) {
        wordRepository.mergeFromOuterSource(word);
        for(WordSupplementation wordSupplementation : outerSource) wordSupplementation.supplement(word);
        wordRepository.saveDataFromOuterSourceExcludeExamples(word);
        return word;
    }

}
