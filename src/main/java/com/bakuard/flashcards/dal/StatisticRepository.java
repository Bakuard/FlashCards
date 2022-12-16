package com.bakuard.flashcards.dal;

import com.bakuard.flashcards.model.statistic.*;
import com.bakuard.flashcards.validation.InvalidParameter;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Отвечает за сохранение данных о каждом повторении каждого слова или устойчивого выражения, а также
 * за агрегирование этих данных.
 */
public interface StatisticRepository {

    /**
     * Добавляет к общей статистике данные об одном конкретном повторении слова wordId из словаря пользователя
     * userId. Данные относятся к повторению слова с английского на родной язык пользователя.
     * @param statistic данные о результате одного конкретного повторения слова.
     * @see RepeatWordFromEnglishStatistic
     */
    public void append(RepeatWordFromEnglishStatistic statistic);

    /**
     * Добавляет к общей статистике данные об одном конкретном повторении слова wordId из словаря пользователя
     * userId. Данные относятся к повторению слова с родного на английский язык.
     * @param statistic данные о результате одного конкретного повторения слова.
     * @see RepeatWordFromNativeStatistic
     */
    public void append(RepeatWordFromNativeStatistic statistic);

    /**
     * Добавляет к общей статистике данные об одном конкретном повторении устойчивого выражения expressionId
     * из словаря пользователя userId. Данные относятся к повторению устойчивого выражения с английского на
     * родной язык пользователя.
     * @param statistic данные о результате одного конкретного повторения устойчивого выражения.
     * @see RepeatExpressionFromEnglishStatistic
     */
    public void append(RepeatExpressionFromEnglishStatistic statistic);

    /**
     * Добавляет к общей статистике данные об одном конкретном повторении устойчивого выражения expressionId
     * из словаря пользователя userId. Данные относятся к повторению устойчивого выражения с родного на
     * английский язык пользователя.
     * @param statistic данные о результате одного конкретного повторения устойчивого выражения.
     * @see RepeatExpressionFromNativeStatistic
     */
    public void append(RepeatExpressionFromNativeStatistic statistic);

    /**
     * Возвращает статистические данные о результатах повторения слова wordId из словаря пользователя
     * userId за указанный период.
     * @param userId идентификатор пользователя, к словарю которого относится слово
     * @param wordId идентификатор слова
     * @param start дата начала периода за который собирается статистика
     * @param end дата конца периода за которые собирается статистика
     * @return статистические данные о результатах повторения слова wordId за указанный период.
     * @throws InvalidParameter если start > end
     * @see WordRepetitionByPeriodStatistic
     */
    public WordRepetitionByPeriodStatistic wordRepetitionByPeriod(
            UUID userId, UUID wordId, LocalDate start, LocalDate end);

    /**
     * Возвращает статистические данные о результатах повторения устойчивого выражения expressionId за указанный
     * период.
     * @param userId идентификатор пользователя, к словарю которого относится слово
     * @param expressionId идентификатор устойчивого выражения
     * @param start дата начала периода за который собирается статистика
     * @param end дата конца периода за которые собирается статистика
     * @return статистические данные о результатах повторения устойчивого выражения expressionId за указанный период.
     * @throws InvalidParameter если start > end
     * @see ExpressionRepetitionByPeriodStatistic
     */
    public ExpressionRepetitionByPeriodStatistic expressionRepetitionByPeriod(
            UUID userId, UUID expressionId, LocalDate start, LocalDate end);

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
     * @throws InvalidParameter если start > end
     * @see WordRepetitionByPeriodStatistic
     */
    public Page<WordRepetitionByPeriodStatistic> wordsRepetitionByPeriod(
            UUID userId, LocalDate start, LocalDate end, Pageable pageable);

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
     * @throws InvalidParameter если start > end
     * @return статистические данные о результатах повторения всех устойчивых выражений полученных за указанный
     *         период.
     * @see ExpressionRepetitionByPeriodStatistic
     */
    public Page<ExpressionRepetitionByPeriodStatistic> expressionsRepetitionByPeriod(
            UUID userId, LocalDate start, LocalDate end, Pageable pageable);

}
