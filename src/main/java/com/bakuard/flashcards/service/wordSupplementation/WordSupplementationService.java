package com.bakuard.flashcards.service.wordSupplementation;

import com.bakuard.flashcards.dal.WordRepository;
import com.bakuard.flashcards.model.word.Word;
import com.bakuard.flashcards.service.util.Transaction;
import com.bakuard.flashcards.validation.ValidatorUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import java.time.Clock;
import java.util.concurrent.TimeUnit;

public class WordSupplementationService implements WordSupplementation {

    private static Logger logger = LoggerFactory.getLogger(WordSupplementationService.class.getName());


    private WordSupplementationFromBuffer wordSupplementationFromBuffer;
    private ValidatorUtil validator;
    private WordRepository wordRepository;
    private Transaction transaction;

    public WordSupplementationService(WordRepository wordRepository,
                                      Clock clock,
                                      ObjectMapper mapper,
                                      ValidatorUtil validator,
                                      Transaction transaction) {
        this.wordRepository = wordRepository;
        this.validator = validator;
        this.transaction = transaction;
        wordSupplementationFromBuffer = new WordSupplementationFromBuffer(
                wordRepository,
                transaction,
                new ReversoScrapper(mapper, clock)
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
                    transaction.commit(() -> wordRepository.deleteUnusedOuterSourceExamples());
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
