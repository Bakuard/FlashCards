package com.bakuard.flashcards.service.wordSupplementation;

import com.bakuard.flashcards.dal.WordOuterSourceBuffer;
import com.bakuard.flashcards.model.word.Word;
import com.bakuard.flashcards.model.word.supplementation.AggregateSupplementedWord;
import com.bakuard.flashcards.validation.ValidatorUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.Clock;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Отвечает за заполнение переданного слова транскрипциями, толкованиями, переводами и переводами примеров
 * из нескольких разных внешних сервисов или других источников.
 */
public class WordSupplementationService {

    private static final Logger logger = LoggerFactory.getLogger(WordSupplementationService.class.getName());


    private ValidatorUtil validator;
    private WordOuterSourceBuffer wordOuterSourceBuffer;
    private List<WordSupplementation> outerServices;
    private Thread thread;
    private final TransactionTemplate transaction;

    /**
     * Создает и возвращает новый сервис дополнения данных слова из внешних сервисов и других источников.
     * @param wordOuterSourceBuffer см. {@link WordOuterSourceBuffer}
     * @param clock часы используемые для получения текущего времени (параметр добавлен для удобства тестирования).
     * @param mapper используется для сериализации и десериализации Java объектов в JSON, а также парсинга JSON.
     *              Требуется некоторым реализациям {@link WordSupplementation}.
     * @param validator объект для валидации слова передаваемого методу {@link #supplement(Word)}.
     */
    public WordSupplementationService(WordOuterSourceBuffer wordOuterSourceBuffer,
                                      Clock clock,
                                      ObjectMapper mapper,
                                      ValidatorUtil validator,
                                      TransactionTemplate transaction) {
        this.wordOuterSourceBuffer = wordOuterSourceBuffer;
        this.validator = validator;
        this.transaction = transaction;

        outerServices = List.of(
                new OxfordDictionaryScrapper(clock, wordOuterSourceBuffer, transaction),
                new YandexTranslateScrapper(mapper, clock, wordOuterSourceBuffer, transaction),
                new ReversoScrapper(mapper, clock, wordOuterSourceBuffer, transaction)
        );
    }

    /**
     * Возвращает для переданного слова транскрипции, толковании, переводы или переводы к его примерам,
     * полученные из разных внешних источников.
     * @param word см. {@link Word}
     * @see Word
     * @see AggregateSupplementedWord
     */
    public AggregateSupplementedWord supplement(Word word) {
        AggregateSupplementedWord result = new AggregateSupplementedWord(word);
        outerServices.forEach(outerService -> result.merge(outerService.supplement(word)));
        return result;
    }

    /**
     * Создает (если ещё не создан) отдельный поток, отвечающий за периодическое удаление всех переводов
     * не используемых примеров.
     */
    public void scheduleDeleteUnusedExamples() {
        thread = new Thread(() -> {
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    int deletedRowsNumber = transaction.execute(status -> wordOuterSourceBuffer.deleteUnusedExamples());
                    logger.info("Delete unused examples from outer source. {} rows was deleted.", deletedRowsNumber);
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
