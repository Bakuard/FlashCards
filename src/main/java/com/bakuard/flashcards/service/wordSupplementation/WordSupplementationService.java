package com.bakuard.flashcards.service.wordSupplementation;

import com.bakuard.flashcards.dal.WordRepository;
import com.bakuard.flashcards.model.word.Word;
import com.bakuard.flashcards.validation.ValidatorUtil;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.time.Clock;

public class WordSupplementationService implements WordSupplementation {

    private WordSupplementationFromBuffer wordSupplementationFromBuffer;
    private ValidatorUtil validator;

    public WordSupplementationService(WordRepository wordRepository,
                                      Clock clock,
                                      ObjectMapper mapper,
                                      ValidatorUtil validator) {
        this.validator = validator;
        wordSupplementationFromBuffer = new WordSupplementationFromBuffer(
                wordRepository,
                new ReversoScrapper(mapper, clock)
        );
    }

    @Override
    public Word supplement(Word word) {
        validator.assertValid(word);
        return wordSupplementationFromBuffer.supplement(word);
    }

}
