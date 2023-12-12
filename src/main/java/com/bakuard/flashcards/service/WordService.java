package com.bakuard.flashcards.service;

import com.bakuard.flashcards.config.configData.ConfigData;
import com.bakuard.flashcards.dal.IntervalRepository;
import com.bakuard.flashcards.dal.WordRepository;
import com.bakuard.flashcards.model.RepetitionResult;
import com.bakuard.flashcards.model.word.Word;
import com.bakuard.flashcards.validation.ValidatorUtil;
import com.bakuard.flashcards.validation.exception.NotUniqueEntityException;
import com.bakuard.flashcards.validation.exception.UnknownEntityException;
import com.google.common.collect.ImmutableList;
import jakarta.validation.ConstraintViolationException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Набор операций над словами в словаре пользователя требующие обращения к внешнему хранилищу или другим
 * сервисам. Каждый метод этого класса выполняется в отдельной транзакции.
 */
@Transactional
public class WordService {

    private WordRepository wordRepository;
    private IntervalRepository intervalRepository;
    private Clock clock;
    private ConfigData configData;
    private ValidatorUtil validator;

    /**
     * Создает новый сервис для слов.
     * @param wordRepository репозиторий слов
     * @param intervalRepository репозиторий интервалов повторения
     * @param clock часы используемые для получения текущей даты (параметр добавлен для удобства тестирования)
     * @param configData общие данные конфигурации приложения
     * @param validator объект отвечающий за валидация входных данных пользователя
     */
    public WordService(WordRepository wordRepository,
                       IntervalRepository intervalRepository,
                       Clock clock,
                       ConfigData configData,
                       ValidatorUtil validator) {
        this.wordRepository = wordRepository;
        this.intervalRepository = intervalRepository;
        this.clock = clock;
        this.configData = configData;
        this.validator = validator;
    }

    /**
     * Делегирует вызов одноименному методу {@link WordRepository} добавляя предварительную валидацию
     * данных слова.
     * @throws NotUniqueEntityException если среди слов пользователя уже есть другое слово с таким значением
     *                                  {@link NotUniqueEntityException#getMessageKey()} вернет Word.value.unique
     * @throws ConstraintViolationException если нарушен хотя бы один из инвариантов {@link Word}
     * @see <a href="https://docs.spring.io/spring-data/commons/docs/current/api/org/springframework/data/repository/CrudRepository.html#save(S)">Документация к CrudRepository#save(entity)</a>
     */
    public Word save(Word word) {
        try {
            validator.assertValid(word);
            return wordRepository.save(word);
        } catch (Exception e) {
            if(e.getCause() instanceof DuplicateKeyException) {
                throw new NotUniqueEntityException(
                        "Word with value '" + word.getValue() + "' already exists",
                        "Word.value.unique");
            }
            throw e;
        }
    }

    /**
     * Делегирует вызов одноименному методу {@link WordRepository}.
     * Если оборачиваемый метод вернул false - выбрасывает исключение.
     * @throws UnknownEntityException если оборачиваемый метод вернул false.
     *                                {@link UnknownEntityException#getMessageKey()} вернет Word.unknownIdOrUserId
     */
    public void tryDeleteById(UUID userId, UUID wordId) {
        boolean wasDeleted = wordRepository.deleteById(userId, wordId);
        if(!wasDeleted) {
            throw new UnknownEntityException(
                    "User with id=" + userId + " not exists or hasn't word with id=" + wordId,
                    "Word.unknownIdOrUserId");
        }
    }

    /**
     * Делегирует вызов методу {@link WordRepository#existsById(UUID, UUID)}.
     */
    public boolean existsById(UUID userId, UUID wordId) {
        return wordRepository.existsById(userId, wordId);
    }

    /**
     * Делегирует вызов методу {@link WordRepository#findById(UUID, UUID)}.
     */
    public Optional<Word> findById(UUID userId, UUID wordId) {
        return wordRepository.findById(userId, wordId);
    }

    /**
     * Делегирует вызов методу {@link WordRepository#findByValue(UUID, String, int, long, long)}.
     * Оборачивает возвращаемое значение в объект Page.
     */
    public Page<Word> findByValue(UUID userId, String value, int maxDistance, Pageable pageable) {
        maxDistance = Math.max(maxDistance, 1);
        maxDistance = Math.min(configData.levenshteinMaxDistance(), maxDistance);
        final int distance = maxDistance;

        return PageableExecutionUtils.getPage(
                wordRepository.findByValue(userId, value, maxDistance, pageable.getPageSize(), pageable.getOffset()),
                pageable,
                () -> wordRepository.countForValue(userId, value, distance)
        );
    }

    /**
     * Делегирует вызов методу {@link WordRepository#findByTranslate(UUID, String, long, long)}.
     * Оборачивает возвращаемое значение в объект Page.
     */
    public Page<Word> findByTranslate(UUID userId, String translate, Pageable pageable) {
        return PageableExecutionUtils.getPage(
                wordRepository.findByTranslate(userId, translate, pageable.getPageSize(), pageable.getOffset()),
                pageable,
                () -> wordRepository.countForTranslate(userId, translate)
        );
    }

    /**
     * Разбивает выборку из всех слов пользователя с идентификатором userId на страницы размером size,
     * а затем возвращает ту страницу, на которой встречается первое из слов (на выборке задан лексикографический
     * порядок) начинающееся на букву wordFirstCharacter. Если у пользователя нет слов начинающихся на указанную
     * букву - возвращает пустую страницу.
     * @param userId идентификатор пользователя, из слов которого делается выборка
     * @param wordFirstCharacter первая буква слов, для которых возвращается страница
     * @param size размер страницы слов
     * @return страница слов
     */
    public Page<Word> jumpToCharacter(UUID userId, String wordFirstCharacter, int size) {
        Page<Word> result = Page.empty();

        long count = wordRepository.count(userId);
        if(count > 0) {
            long pageNumber = wordRepository.getWordIndexByFirstCharacter(userId, wordFirstCharacter) / count;
            if(pageNumber >= 0) {
                size = size == 0 ? configData.pagination().defaultPageSize() :
                        Math.max(Math.min(size, configData.pagination().maxPageSize()), configData.pagination().minPageSize());
                Pageable pageable = PageRequest.of((int) pageNumber, size);
                result = PageableExecutionUtils.getPage(
                        wordRepository.findByTranslate(userId, wordFirstCharacter, pageable.getPageSize(), pageable.getOffset()),
                        pageable,
                        () -> count
                );
            }
        }

        return result;
    }

    /**
     * Делегирует вызов методу {@link WordRepository#findById(UUID, UUID)}. Если оборачиваемый метод возвращает
     * пустой Optional - данный метод генерирует исключение.
     * @throws UnknownEntityException если оборачиваемый метод возвращает пустой Optional.
     *                                {@link UnknownEntityException#getMessageKey()} вернет Word.unknownIdOrUserId
     */
    public Word tryFindById(UUID userId, UUID wordId) {
        return findById(userId, wordId).
                orElseThrow(
                        () -> new UnknownEntityException(
                                "User with id=" + userId + " not exists or hasn't word with id=" + wordId,
                                "Word.unknownIdOrUserId"
                        )
                );
    }

    /**
     * Делегирует вызов методу {@link WordRepository#findByUserId(UUID, Pageable)}.
     */
    public Page<Word> findByUserId(UUID userId, Pageable pageable) {
        return wordRepository.findByUserId(userId, pageable);
    }

    /**
     * Делегирует вызов методу {@link WordRepository#findAllForRepeatFromEnglish(UUID, LocalDate, long, long)}.
     * Оборачивает возвращаемое значение в объект Page.
     */
    public Page<Word> findAllForRepeatFromEnglish(UUID userId, Pageable pageable) {
        LocalDate date = LocalDate.now(clock);

        return PageableExecutionUtils.getPage(
                wordRepository.findAllForRepeatFromEnglish(userId, date, pageable.getPageSize(), pageable.getPageNumber()),
                pageable,
                () -> wordRepository.countForRepeatFromEnglish(userId, date)
        );
    }

    /**
     * Делегирует вызов методу {@link WordRepository#findAllForRepeatFromNative(UUID, LocalDate, long, long)}.
     * Оборачивает возвращаемое значение в объект Page.
     */
    public Page<Word> findAllForRepeatFromNative(UUID userId, Pageable pageable) {
        LocalDate date = LocalDate.now(clock);

        return PageableExecutionUtils.getPage(
                wordRepository.findAllForRepeatFromNative(userId, date, pageable.getPageSize(), pageable.getPageNumber()),
                pageable,
                () -> wordRepository.countForRepeatFromNative(userId, date)
        );
    }

    /**
     * Делегирует вызов методу {@link Word#repeatFromEnglish(boolean, LocalDate, ImmutableList)} слова,
     * имеющего идентификатор wordId, сохраняет указанное слово, а затем возвращает его.
     * @return слово с идентификатором wordId.
     */
    public Word repeatFromEnglish(UUID userId, UUID wordId, boolean isRemember) {
        Word word = tryFindById(userId, wordId);
        word.repeatFromEnglish(isRemember, LocalDate.now(clock), intervalRepository.findAll(userId));
        save(word);
        return word;
    }

    /**
     * Делегирует вызов методу {@link Word#repeatFromNative(String, LocalDate, ImmutableList)} слова,
     * имеющего идентификатор wordId, сохраняет указанное слово, а затем возвращает его.
     * @return слово с идентификатором wordId.
     */
    public RepetitionResult<Word> repeatFromNative(UUID userId, UUID wordId, String inputWordValue) {
        Word word = tryFindById(userId, wordId);
        boolean isRemember = word.repeatFromNative(inputWordValue, LocalDate.now(clock), intervalRepository.findAll(userId));
        save(word);
        return new RepetitionResult<>(word, isRemember);
    }

    /**
     * Делегирует вызов методу {@link Word#markForRepetitionFromEnglish(LocalDate, int)} слова,
     * имеющего идентификатор wordId, сохраняет указанное слово, а затем возвращает его.
     * @return слово с идентификатором wordId.
     */
    public Word markForRepetitionFromEnglish(UUID userId, UUID wordId) {
        Word word = tryFindById(userId, wordId);
        word.markForRepetitionFromEnglish(LocalDate.now(clock), intervalRepository.findAll(userId).get(0));
        save(word);
        return word;
    }

    /**
     * Делегирует вызов методу {@link Word#markForRepetitionFromNative(LocalDate, int)} слова,
     * имеющего идентификатор wordId, сохраняет указанное слово, а затем возвращает его.
     * @return слово с идентификатором wordId.
     */
    public Word markForRepetitionFromNative(UUID userId, UUID wordId) {
        Word word = tryFindById(userId, wordId);
        word.markForRepetitionFromNative(LocalDate.now(clock), intervalRepository.findAll(userId).get(0));
        save(word);
        return word;
    }

    /**
     * Делегирует вызов методу {@link com.bakuard.flashcards.model.RepeatDataFromEnglish#isHotRepeat(int)},
     * объекта {@link Word#getRepeatDataFromEnglish()}.
     */
    public boolean isHotRepeatFromEnglish(Word word) {
        List<Integer> intervals = intervalRepository.findAll(word.getUserId());
        return word.getRepeatDataFromEnglish().isHotRepeat(intervals.get(0));
    }

    /**
     * Делегирует вызов методу {@link com.bakuard.flashcards.model.RepeatDataFromNative#isHotRepeat(int)},
     * объекта {@link Word#getRepeatDataFromNative()}.
     */
    public boolean isHotRepeatFromNative(Word word) {
        List<Integer> intervals = intervalRepository.findAll(word.getUserId());
        return word.getRepeatDataFromNative().isHotRepeat(intervals.get(0));
    }


}
