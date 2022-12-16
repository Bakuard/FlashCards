package com.bakuard.flashcards.service.wordSupplementation;

import com.bakuard.flashcards.dal.WordOuterSourceBuffer;
import com.bakuard.flashcards.model.word.Word;
import com.bakuard.flashcards.validation.ValidatorUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Clock;
import java.util.concurrent.TimeUnit;

/**
 * Отвечает за заполнение переданного слова транскрипциями, толкованиями, переводами и переводами примеров
 * из нескольких разных внешних сервисов или других источников.
 */
public class WordSupplementationService implements WordSupplementation {

    private static final Logger logger = LoggerFactory.getLogger(WordSupplementationService.class.getName());


    private WordSupplementationFromBuffer wordSupplementationFromBuffer;
    private ValidatorUtil validator;
    private WordOuterSourceBuffer wordOuterSourceBuffer;
    private volatile Thread thread;

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

    /**
     * Делегирует вызов реализациям {@link WordSupplementation} добавляя предварительную
     * валидацию полей переданного слова.
     */
    @Override
    public Word supplement(Word word) {
        validator.assertValid(word);
        return wordSupplementationFromBuffer.supplement(word);
    }

    /**
     * Создает (если ещё не создан) отдельный поток, отвечающий за периодическое удаление всех переводов
     * не используемых примеров.
     */
    public void scheduleDeleteUnusedExamples() {
        if(thread == null) {
            thread = new Thread(() -> {
                while (!Thread.currentThread().isInterrupted()) {
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

}
