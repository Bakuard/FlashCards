package com.bakuard.flashcards.service.wordSupplementation;

import com.bakuard.flashcards.dal.WordOuterSourceBuffer;
import com.bakuard.flashcards.model.word.Word;
import com.bakuard.flashcards.validation.ValidatorUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Clock;
import java.util.concurrent.TimeUnit;

public class WordSupplementationService implements WordSupplementation {

    private static final Logger logger = LoggerFactory.getLogger(WordSupplementationService.class.getName());


    private WordSupplementationFromBuffer wordSupplementationFromBuffer;
    private ValidatorUtil validator;
    private WordOuterSourceBuffer wordOuterSourceBuffer;

    public WordSupplementationService(WordOuterSourceBuffer wordOuterSourceBuffer,
                                      Clock clock,
                                      ObjectMapper mapper,
                                      ValidatorUtil validator) {
        this.wordOuterSourceBuffer = wordOuterSourceBuffer;
        this.validator = validator;
        wordSupplementationFromBuffer = new WordSupplementationFromBuffer(
                wordOuterSourceBuffer,
                new ReversoScrapper(mapper, clock),
                new YandexTranslateScrapper(mapper, clock),
                new OxfordDictionaryScrapper(clock)
        );
    }

    @Override
    public Word supplement(Word word) {
        validator.assertValid(word);
        return wordSupplementationFromBuffer.supplement(word);
    }

    public void scheduleDeleteUnusedExamples() {
        Thread thread = new Thread(() -> {
            while(!Thread.currentThread().isInterrupted()) {
                try {
                    wordOuterSourceBuffer.deleteUnusedOuterSourceExamples();
                    TimeUnit.HOURS.sleep(2);
                } catch (Exception e) {
                    logger.error("Fail to delete unused examples from outer source", e);
                }
            }
        });
        thread.setDaemon(true);
        thread.start();
    }

}
