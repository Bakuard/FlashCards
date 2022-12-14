package com.bakuard.flashcards.service;

import com.bakuard.flashcards.dal.StatisticRepository;
import com.bakuard.flashcards.model.statistic.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Сервис по сбору и предоставлению статистики связанной с повторением слов и устойчивых выражений.
 */
@Transactional
public class StatisticService {

    private StatisticRepository statisticRepository;
    private Clock clock;

    public StatisticService(StatisticRepository statisticRepository, Clock clock) {
        this.statisticRepository = statisticRepository;
        this.clock = clock;
    }

    /**
     * Добавляет к общей статистике данные об одном конкретном повторении слова wordId из словаря пользователя
     * userId. Данные относятся к повторению слова с английского на родной язык пользователя.
     * @param userId идентификатор пользователя, к словарю которого относится слово
     * @param wordId идентификатор слова
     * @param isRemember true - если пользователь успешно повторил слово с английского языка, иначе - false.
     */
    public void appendWordFromEnglish(UUID userId, UUID wordId, boolean isRemember) {
        statisticRepository.append(
                new RepeatWordFromEnglishStatistic(
                        userId,
                        wordId,
                        LocalDate.now(clock),
                        isRemember)
        );
    }

    /**
     * Добавляет к общей статистике данные об одном конкретном повторении слова wordId из словаря пользователя
     * userId. Данные относятся к повторению слова с родного на английский язык.
     * @param userId идентификатор пользователя, к словарю которого относится слово
     * @param wordId идентификатор слова
     * @param isRemember true - если пользователь успешно повторил слово с родного языка, иначе - false.
     */
    public void appendWordFromNative(UUID userId, UUID wordId, boolean isRemember) {
        statisticRepository.append(
                new RepeatWordFromNativeStatistic(
                        userId,
                        wordId,
                        LocalDate.now(clock),
                        isRemember)
        );
    }

    /**
     * Добавляет к общей статистике данные об одном конкретном повторении устойчивого выражения expressionId
     * из словаря пользователя userId. Данные относятся к повторению устойчивого выражения с английского на
     * родной язык пользователя.
     * @param userId идентификатор пользователя, к словарю которого относится слово
     * @param expressionId идентификатор устойчивого выражения
     * @param isRemember true - если пользователь успешно повторил устойчивое выражение с английского языка,
     *                   иначе - false.
     */
    public void appendExpressionFromEnglish(UUID userId, UUID expressionId, boolean isRemember) {
        statisticRepository.append(
                new RepeatExpressionFromEnglishStatistic(
                        userId,
                        expressionId,
                        LocalDate.now(clock),
                        isRemember)
        );
    }

    /**
     * Добавляет к общей статистике данные об одном конкретном повторении устойчивого выражения expressionId
     * из словаря пользователя userId. Данные относятся к повторению устойчивого выражения с родного на
     * английский язык.
     * @param userId идентификатор пользователя, к словарю которого относится слово
     * @param expressionId идентификатор устойчивого выражения
     * @param isRemember true - если пользователь успешно повторил устойчивое выражение с английского языка,
     *                   иначе - false.
     */
    public void appendExpressionFromNative(UUID userId, UUID expressionId, boolean isRemember) {
        statisticRepository.append(
                new RepeatExpressionFromNativeStatistic(
                        userId,
                        expressionId,
                        LocalDate.now(clock),
                        isRemember)
        );
    }

    /**
     * Возвращает статистические данные о результатах повторения слова wordId за указанный период.
     * @param userId идентификатор пользователя, к словарю которого относится слово
     * @param wordId идентификатор слова
     * @param start дата начала периода за который собирается статистика
     * @param end дата конца периода за которые собирается статистика
     * @return статистические данные о результатах повторения слова wordId за указанный период.
     * @see WordRepetitionByPeriodStatistic
     */
    public WordRepetitionByPeriodStatistic wordRepetitionByPeriod(
            UUID userId, UUID wordId, String start, String end) {
        return statisticRepository.wordRepetitionByPeriod(
                userId, wordId, LocalDate.parse(start), LocalDate.parse(end)
        );
    }

    /**
     * Возвращает статистические данные о результатах повторения устойчивого выражения expressionId за указанный
     * период.
     * @param userId идентификатор пользователя, к словарю которого относится слово
     * @param expressionId идентификатор устойчивого выражения
     * @param start дата начала периода за который собирается статистика
     * @param end дата конца периода за которые собирается статистика
     * @return статистические данные о результатах повторения устойчивого выражения expressionId за указанный период.
     * @see ExpressionRepetitionByPeriodStatistic
     */
    public ExpressionRepetitionByPeriodStatistic expressionRepetitionByPeriod(
            UUID userId, UUID expressionId, String start, String end) {
        return statisticRepository.expressionRepetitionByPeriod(
                userId, expressionId, LocalDate.parse(start), LocalDate.parse(end)
        );
    }

    /**
     * Возвращает статистические данные о результатах повторения всех слов из словаря пользователя userId
     * полученных за указанный период. Статистические данные для каждого отдельного слова собираются в виде
     * одного объекта {@link WordRepetitionByPeriodStatistic}. Данные отсортированы в порядке возрастания
     * значений слов. Если за указанный период пользователь не повторял слов - возвращает пустую страницу.
     * @param userId идентификатор пользователя, из слов которого делается выборка
     * @param start дата начала периода за который собирается статистика
     * @param end дата конца периода за которые собирается статистика
     * @param pageable параметры пагинации
     * @return статистические данные о результатах повторения всех слов полученных за указанный период.
     * @see WordRepetitionByPeriodStatistic
     */
    public Page<WordRepetitionByPeriodStatistic> wordsRepetitionByPeriod(
            UUID userId, String start, String end, Pageable pageable) {
        return statisticRepository.wordsRepetitionByPeriod(
                userId, LocalDate.parse(start), LocalDate.parse(end), pageable
        );
    }

    /**
     * Возвращает статистические данные о результатах повторения всех устойчивых выражений из словаря
     * пользователя userId полученных за указанный период. Статистические данные для каждого отдельного выражения
     * собираются в виде одного объекта {@link ExpressionRepetitionByPeriodStatistic}. Данные отсортированы в
     * порядке возрастания значений устойчивых выражений. Если за указанный период пользователь не повторял
     * устойчивых выражений - возвращает пустую страницу.
     * @param userId идентификатор пользователя, из устойчивых выражений которого делается выборка
     * @param start дата начала периода за который собирается статистика
     * @param end дата конца периода за которые собирается статистика
     * @param pageable параметры пагинации
     * @return статистические данные о результатах повторения всех устойчивых выражений полученных за указанный
     *         период.
     * @see ExpressionRepetitionByPeriodStatistic
     */
    public Page<ExpressionRepetitionByPeriodStatistic> expressionsRepetitionByPeriod(
            UUID userId, String start, String end, Pageable pageable) {
        return statisticRepository.expressionsRepetitionByPeriod(
                userId, LocalDate.parse(start), LocalDate.parse(end), pageable
        );
    }

}
