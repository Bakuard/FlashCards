package com.bakuard.flashcards.service;

import com.bakuard.flashcards.config.configData.ConfigData;
import com.bakuard.flashcards.dal.ExpressionRepository;
import com.bakuard.flashcards.dal.IntervalRepository;
import com.bakuard.flashcards.model.RepeatDataFromEnglish;
import com.bakuard.flashcards.model.RepeatDataFromNative;
import com.bakuard.flashcards.model.RepetitionResult;
import com.bakuard.flashcards.model.expression.Expression;
import com.bakuard.flashcards.validation.ValidatorUtil;
import com.bakuard.flashcards.validation.exception.NotUniqueEntityException;
import com.bakuard.flashcards.validation.exception.UnknownEntityException;
import jakarta.validation.ConstraintViolationException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.relational.core.conversion.DbActionExecutionException;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Набор операций над устойчивыми выражениями в словаре пользователя требующие обращения к внешнему хранилищу
 * или другим сервисам. Каждый метод этого класса выполняется в отдельной транзакции.
 */
@Transactional
public class ExpressionService {

    private ExpressionRepository expressionRepository;
    private IntervalRepository intervalRepository;
    private Clock clock;
    private ConfigData configData;
    private ValidatorUtil validator;

    /**
     * Создает новый сервис для устойчивых выражений.
     * @param expressionRepository репозиторий устойчивых выражений
     * @param intervalRepository репозиторий интервалов повторения
     * @param clock часы используемые для получения текущей даты (параметр добавлен для удобства тестирования)
     * @param configData общие данные конфигурации приложения
     * @param validator объект отвечающий за валидация входных данных пользователя
     */
    public ExpressionService(ExpressionRepository expressionRepository,
                             IntervalRepository intervalRepository,
                             Clock clock,
                             ConfigData configData,
                             ValidatorUtil validator) {
        this.expressionRepository = expressionRepository;
        this.intervalRepository = intervalRepository;
        this.clock = clock;
        this.configData = configData;
        this.validator = validator;
    }

    /**
     * Делегирует вызов одноименному методу {@link ExpressionRepository} добавляя предварительную валидацию
     * данных устойчивого выражения.
     * @throws NotUniqueEntityException если среди устойчивых выражений пользователя уже есть другое выражение
     *                                  с таким значением. {@link NotUniqueEntityException#getMessageKey()}
     *                                  вернет Expression.value.unique
     * @throws ConstraintViolationException если нарушен хотя бы один из инвариантов {@link Expression}
     * @see <a href="https://docs.spring.io/spring-data/commons/docs/current/api/org/springframework/data/repository/CrudRepository.html#save(S)">Документация к CrudRepository#save(entity)</a>
     */
    public Expression save(Expression expression) {
        try {
            validator.assertValid(expression);
            return expressionRepository.save(expression);
        } catch (DbActionExecutionException e) {
            if(e.getCause() instanceof DuplicateKeyException) {
                throw new NotUniqueEntityException(
                        "Expression with value '" + expression.getValue() + "' already exists",
                        "Expression.value.unique");
            }
            throw e;
        }
    }

    /**
     * Делегирует вызов одноименному методу {@link ExpressionRepository}.
     * Если оборачиваемый метод вернул false - выбрасывает исключение.
     * @throws UnknownEntityException если оборачиваемый метод вернул false.
     *                                {@link UnknownEntityException#getMessageKey()} вернет Expression.unknownIdOrUserId
     */
    public void tryDeleteById(UUID userId, UUID expressionId) {
        boolean wasDeleted = expressionRepository.deleteById(userId, expressionId);
        if(!wasDeleted) {
            throw new UnknownEntityException(
                    "User with id=" + userId + " not exists or hasn't expression with id=" + expressionId,
                    "Expression.unknownIdOrUserId");
        }
    }

    /**
     * Делегирует вызов методу {@link ExpressionRepository#existsById(UUID, UUID)}.
     */
    public boolean existsById(UUID userId, UUID expressionId) {
        return expressionRepository.existsById(userId, expressionId);
    }

    /**
     * Делегирует вызов методу {@link ExpressionRepository#findById(UUID, UUID)}.
     */
    public Optional<Expression> findById(UUID userId, UUID expressionId) {
        return expressionRepository.findById(userId, expressionId);
    }

    /**
     * Делегирует вызов методу {@link ExpressionRepository#findByValue(UUID, String, int, long, long)}.
     * Оборачивает возвращаемое значение в объект Page.
     */
    public Page<Expression> findByValue(UUID userId, String value, int maxDistance, Pageable pageable) {
        maxDistance = Math.max(maxDistance, 1);
        maxDistance = Math.min(configData.levenshteinMaxDistance(), maxDistance);
        final int distance = maxDistance;

        return PageableExecutionUtils.getPage(
                expressionRepository.findByValue(userId, value, maxDistance, pageable.getPageSize(), pageable.getOffset()),
                pageable,
                () -> expressionRepository.countForValue(userId, value, distance)
        );
    }

    /**
     * Делегирует вызов методу {@link ExpressionRepository#findByTranslate(UUID, String, long, long)}.
     * Оборачивает возвращаемое значение в объект Page.
     */
    public Page<Expression> findByTranslate(UUID userId, String translate, Pageable pageable) {
        return PageableExecutionUtils.getPage(
                expressionRepository.findByTranslate(userId, translate, pageable.getPageSize(), pageable.getOffset()),
                pageable,
                () -> expressionRepository.countForTranslate(userId, translate)
        );
    }

    /**
     * Делегирует вызов методу {@link ExpressionRepository#findById(UUID, UUID)}. Если оборачиваемый метод
     * возвращает пустой Optional - данный метод генерирует исключение.
     * @throws UnknownEntityException если оборачиваемый метод возвращает пустой Optional.
     *                                {@link UnknownEntityException#getMessageKey()} вернет Expression.unknownIdOrUserId
     */
    public Expression tryFindById(UUID userId, UUID expressionId) {
        return findById(userId, expressionId).
                orElseThrow(
                        () -> new UnknownEntityException(
                                "Unknown expression with id=" + expressionId + " userId=" + userId,
                                "Expression.unknownIdOrUserId"
                        )
                );
    }

    /**
     * Делегирует вызов методу {@link ExpressionRepository#findByUserId(UUID, Pageable)}.
     */
    public Page<Expression> findByUserId(UUID userId, Pageable pageable) {
        return expressionRepository.findByUserId(userId, pageable);
    }

    /**
     * Делегирует вызов методу {@link ExpressionRepository#findAllForRepeatFromEnglish(UUID, LocalDate, long, long)}.
     * Оборачивая возвращаемое значение в объект Page.
     */
    public Page<Expression> findAllForRepeatFromEnglish(UUID userId, Pageable pageable) {
        LocalDate date = LocalDate.now(clock);

        return PageableExecutionUtils.getPage(
                expressionRepository.findAllForRepeatFromEnglish(userId, date, pageable.getPageSize(), pageable.getOffset()),
                pageable,
                () -> expressionRepository.countForRepeatFromEnglish(userId, date)
        );
    }

    /**
     * Делегирует вызов методу {@link ExpressionRepository#findAllForRepeatFromNative(UUID, LocalDate, long, long)}.
     * Оборачивает возвращаемое значение в объект Page.
     */
    public Page<Expression> findAllForRepeatFromNative(UUID userId, Pageable pageable) {
        LocalDate date = LocalDate.now(clock);

        return PageableExecutionUtils.getPage(
                expressionRepository.findAllForRepeatFromNative(userId, date, pageable.getPageSize(), pageable.getOffset()),
                pageable,
                () -> expressionRepository.countForRepeatFromNative(userId, date)
        );
    }

    /**
     * Задает результат последнего повторения этого устойчивого выражения с английского языка
     * на родной язык пользователя. Метод изменяет данные о последнем повторении устойчивого выражения,
     * а именно: <br/>
     * 1. В качестве даты последнего повторения устанавливается текущая дата. <br/>
     * 2. Если повторение было успешно, то будет выбран ближайший больший интервал повторения (если таковые есть)
     *    относительно текущего.<br/>
     * 3. Если повторение не было успешно (isRemember = false), то будет выбран наименьший интервал
     *    из списка intervals.
     * @param userId идентификатор пользователя, для устойчивого выражения которого выполняется повторение.
     * @param expressionId идентификатор повторяемого устойчивого выражения.
     * @param isRemember true - если пользователь правильно вспомнил переводы, произношение и толкования
     *                   устойчивого выражения, иначе - false.
     * @return устойчивое выражение с идентификатором expressionId.
     */
    public Expression repeatFromEnglish(UUID userId, UUID expressionId, boolean isRemember) {
        Expression expression = tryFindById(userId, expressionId);
        List<Integer> allIntervals =  intervalRepository.findAll(userId);
        int intervalIndex = findNextRepeatInterval(
                allIntervals,
                expression.getRepeatDataFromEnglish().interval(),
                isRemember
        );
        LocalDate lastDateOfRepeat = LocalDate.now(clock);
        expression.setRepeatDataFromEnglish(
                new RepeatDataFromEnglish(allIntervals.get(intervalIndex), lastDateOfRepeat)
        );
        save(expression);
        return expression;
    }

    /**
     * Проверяет указанное пользователем значение устойчивого выражения при его повторении с родного языка пользователя
     * на английский язык. Если заданное значение равняется значению текущего устойчивого выражения - повторение
     * считается успешным. Метод изменяет данные о последнем повторении устойчивого выражения, а именно: <br/>
     * 1. В качестве даты последнего повторения устанавливается текущая дата. <br/>
     * 2. Если повторение было успешно, то будет выбран ближайший больший интервал повторения (если таковые есть)
     *    относительно текущего.<br/>
     * 3. Если повторение не было успешно, то будет выбран наименьший интервал из списка intervals.
     * @param userId идентификатор пользователя, для слова которого выполняется повторение.
     * @param expressionId идентификатор повторяемого устойчивого выражения.
     * @param inputExpressionValue значения устойчивого выражения на английском языке указанное при проверке.
     * @return устойчивое выражение с идентификатором expressionId.
     */
    public RepetitionResult<Expression> repeatFromNative(UUID userId, UUID expressionId, String inputExpressionValue) {
        Expression expression = tryFindById(userId, expressionId);
        boolean isRemember = expression.getValue().equals(inputExpressionValue);
        List<Integer> allIntervals =  intervalRepository.findAll(userId);
        int intervalIndex = findNextRepeatInterval(
                allIntervals,
                expression.getRepeatDataFromEnglish().interval(),
                isRemember
        );
        LocalDate lastDateOfRepeat = LocalDate.now(clock);
        expression.setRepeatDataFromNative(
                new RepeatDataFromNative(allIntervals.get(intervalIndex), lastDateOfRepeat)
        );
        save(expression);
        return new RepetitionResult<>(expression, isRemember);
    }

    /**
     * Указывает, что пользователь забыл перевод данного устойчивого выражения с английского на родной язык и его
     * требуется повторить в ближайшее время. Метод отметит текущую дату, как дату последнего повторения и
     * установит наименьший из интервалов повторения пользователя.
     * @return устойчивое выражение с идентификатором expressionId.
     */
    public Expression markForRepetitionFromEnglish(UUID userId, UUID expressionId) {
        Expression expression = tryFindById(userId, expressionId);
        LocalDate lastDateOfRepeat = LocalDate.now(clock);
        List<Integer> allIntervals =  intervalRepository.findAll(userId);
        expression.setRepeatDataFromEnglish(new RepeatDataFromEnglish(allIntervals.getFirst(), lastDateOfRepeat));
        save(expression);
        return expression;
    }

    /**
     * Указывает, что пользователь забыл перевод данного устойчивого выражения с родного языка на английский и его
     * требуется повторить в ближайшее время. Метод отметит текущую дату, как дату последнего повторения и
     * установит наименьший из интервалов повторения пользователя.
     * @return устойчивое выражение с идентификатором expressionId.
     */
    public Expression markForRepetitionFromNative(UUID userId, UUID expressionId) {
        Expression expression = tryFindById(userId, expressionId);
        LocalDate lastDateOfRepeat = LocalDate.now(clock);
        List<Integer> allIntervals =  intervalRepository.findAll(userId);
        expression.setRepeatDataFromNative(new RepeatDataFromNative(allIntervals.getFirst(), lastDateOfRepeat));
        save(expression);
        return expression;
    }

    /**
     * Делегирует вызов методу {@link com.bakuard.flashcards.model.RepeatDataFromEnglish#isHotRepeat(int)},
     * объекта {@link Expression#getRepeatDataFromEnglish()}.
     */
    public boolean isHotRepeatFromEnglish(Expression expression) {
        List<Integer> intervals = intervalRepository.findAll(expression.getUserId());
        return expression.getRepeatDataFromEnglish().isHotRepeat(intervals.get(0));
    }

    /**
     * Делегирует вызов методу {@link com.bakuard.flashcards.model.RepeatDataFromNative#isHotRepeat(int)},
     * объекта {@link Expression#getRepeatDataFromNative()}.
     */
    public boolean isHotRepeatFromNative(Expression expression) {
        List<Integer> intervals = intervalRepository.findAll(expression.getUserId());
        return expression.getRepeatDataFromNative().isHotRepeat(intervals.get(0));
    }


    private int findNextRepeatInterval(List<Integer> allIntervals, int lastInterval, boolean isRemember) {
        return isRemember ? Math.min(allIntervals.indexOf(lastInterval) + 1, allIntervals.size() - 1) : 0;
    }
}
