package com.bakuard.flashcards.dal;

import com.bakuard.flashcards.model.statistic.ExpressionRepetitionByPeriodStatistic;
import com.bakuard.flashcards.model.statistic.RepeatExpressionFromEnglishStatistic;
import com.bakuard.flashcards.model.statistic.RepeatExpressionFromNativeStatistic;
import com.bakuard.flashcards.model.statistic.RepeatWordFromEnglishStatistic;
import com.bakuard.flashcards.model.statistic.RepeatWordFromNativeStatistic;
import com.bakuard.flashcards.model.statistic.WordRepetitionByPeriodStatistic;
import com.bakuard.flashcards.validation.exception.InvalidParameter;
import com.bakuard.flashcards.validation.exception.NotUniqueEntityException;
import com.bakuard.flashcards.validation.exception.UnknownEntityException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.Optional;
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
     * @throws NullPointerException если statistic равен null
     * @throws UnknownEntityException если не существует пользователя с идентификатором {@link RepeatWordFromEnglishStatistic#userId()},
     *                                или слова с идентификатором {@link RepeatWordFromEnglishStatistic#wordId()}.
     *                                {@link UnknownEntityException#getMessageKey()} вернет Statistic.unknownUserIdAndWordId
     * @throws NotUniqueEntityException если результат повторения указанного слова указанным пользователем в указанную
     *                                  дату уже сохранен. {@link NotUniqueEntityException#getMessageKey()} вернет Statistic.unique
     * @see RepeatWordFromEnglishStatistic
     */
    public void append(RepeatWordFromEnglishStatistic statistic);

    /**
     * Добавляет к общей статистике данные об одном конкретном повторении слова wordId из словаря пользователя
     * userId. Данные относятся к повторению слова с родного на английский язык.
     * @param statistic данные о результате одного конкретного повторения слова.
     * @throws NullPointerException если statistic равен null
     * @throws UnknownEntityException если не существует пользователя с идентификатором {@link RepeatWordFromEnglishStatistic#userId()},
     *                                или слова с идентификатором {@link RepeatWordFromEnglishStatistic#wordId()}.
     *                                {@link UnknownEntityException#getMessageKey()} вернет Statistic.unknownUserIdAndWordId
     * @throws NotUniqueEntityException если результат повторения указанного слова указанным пользователем в указанную
     *                                  дату уже сохранен. {@link NotUniqueEntityException#getMessageKey()} вернет Statistic.unique
     * @see RepeatWordFromNativeStatistic
     */
    public void append(RepeatWordFromNativeStatistic statistic);

    /**
     * Добавляет к общей статистике данные об одном конкретном повторении устойчивого выражения expressionId
     * из словаря пользователя userId. Данные относятся к повторению устойчивого выражения с английского на
     * родной язык пользователя.
     * @param statistic данные о результате одного конкретного повторения устойчивого выражения.
     * @throws NullPointerException если statistic равен null
     * @throws UnknownEntityException если не существует пользователя с идентификатором {@link RepeatExpressionFromEnglishStatistic#userId()},
     *                                или выражения с идентификатором {@link RepeatExpressionFromEnglishStatistic#expressionId()}.
     *                                {@link UnknownEntityException#getMessageKey()} вернет Statistic.unknownUserIdOrExpressionId
     * @throws NotUniqueEntityException если результат повторения указанного выражения указанным пользователем в указанную
     *                                  дату уже сохранен. {@link NotUniqueEntityException#getMessageKey()} вернет Statistic.unique
     * @see RepeatExpressionFromEnglishStatistic
     */
    public void append(RepeatExpressionFromEnglishStatistic statistic);

    /**
     * Добавляет к общей статистике данные об одном конкретном повторении устойчивого выражения expressionId
     * из словаря пользователя userId. Данные относятся к повторению устойчивого выражения с родного на
     * английский язык пользователя.
     * @param statistic данные о результате одного конкретного повторения устойчивого выражения.
     * @throws NullPointerException если statistic равен null
     * @throws UnknownEntityException если не существует пользователя с идентификатором {@link RepeatExpressionFromEnglishStatistic#userId()},
     *                                или выражения с идентификатором {@link RepeatExpressionFromEnglishStatistic#expressionId()}.
     *                                {@link UnknownEntityException#getMessageKey()} вернет Statistic.unknownUserIdOrExpressionId
     * @throws NotUniqueEntityException если результат повторения указанного выражения указанным пользователем в указанную
     *                                  дату уже сохранен. {@link NotUniqueEntityException#getMessageKey()} вернет Statistic.unique
     * @see RepeatExpressionFromNativeStatistic
     */
    public void append(RepeatExpressionFromNativeStatistic statistic);

    /**
     * Возвращает статистические данные о результатах повторения слова wordId из словаря пользователя
     * userId за указанный период. Если не существует пользователя с идентификатором userId или
     * в словаре пользователя нет слова с идентификатором wordId - возвращает пустой Optional.
     * @param userId идентификатор пользователя, к словарю которого относится слово
     * @param wordId идентификатор слова
     * @param start дата начала периода за который собирается статистика
     * @param end дата конца периода за которые собирается статистика
     * @return статистические данные о результатах повторения слова wordId за указанный период.
     * @throws InvalidParameter если start > end. {@link InvalidParameter#getMessageKey()} вернет StatisticRepository.invalidPeriodBorder
     * @throws NullPointerException если хотя бы один из параметров равен null
     * @see WordRepetitionByPeriodStatistic
     */
    public Optional<WordRepetitionByPeriodStatistic> wordRepetitionByPeriod(
            UUID userId, UUID wordId, LocalDate start, LocalDate end);

    /**
     * Возвращает статистические данные о результатах повторения устойчивого выражения expressionId за указанный
     * период. Если не существует пользователя с идентификатором userId или в словаре пользователя нет
     * устойчивого выражения с идентификатором expressionId - возвращает пустой Optional.
     * @param userId идентификатор пользователя, к словарю которого относится слово
     * @param expressionId идентификатор устойчивого выражения
     * @param start дата начала периода за который собирается статистика
     * @param end дата конца периода за которые собирается статистика
     * @return статистические данные о результатах повторения устойчивого выражения expressionId за указанный период.
     * @throws InvalidParameter если start > end. {@link InvalidParameter#getMessageKey()} вернет StatisticRepository.invalidPeriodBorder
     * @throws NullPointerException если хотя бы один из параметров равен null
     * @see ExpressionRepetitionByPeriodStatistic
     */
    public Optional<ExpressionRepetitionByPeriodStatistic> expressionRepetitionByPeriod(
            UUID userId, UUID expressionId, LocalDate start, LocalDate end);

    /**
     * Возвращает статистические данные о результатах повторения всех слов из словаря пользователя userId
     * полученных за указанный период. Статистические данные для каждого отдельного слова собираются в виде
     * одного объекта {@link WordRepetitionByPeriodStatistic}, которые затем сортируются в порядке возрастания
     * значений слов. Если за указанный период пользователь не повторял слов или нет пользователя с
     * таким userId - возвращает пустую страницу.
     * @param userId идентификатор пользователя, из слов которого делается выборка
     * @param start дата начала периода за который собирается статистика
     * @param end дата конца периода за которые собирается статистика
     * @param pageable параметры пагинации
     * @return статистические данные о результатах повторения всех слов полученных за указанный период.
     * @throws InvalidParameter если start > end. {@link InvalidParameter#getMessageKey()} вернет StatisticRepository.invalidPeriodBorder
     * @throws NullPointerException если хотя бы один из параметров равен null
     * @see WordRepetitionByPeriodStatistic
     */
    public Page<WordRepetitionByPeriodStatistic> wordsRepetitionByPeriod(
            UUID userId, LocalDate start, LocalDate end, Pageable pageable);

    /**
     * Возвращает статистические данные о результатах повторения всех устойчивых выражений из словаря
     * пользователя userId полученных за указанный период. Статистические данные для каждого отдельного выражения
     * собираются в виде одного объекта {@link ExpressionRepetitionByPeriodStatistic}, которые затем сортируются в
     * порядке возрастания значений устойчивых выражений. Если за указанный период пользователь не повторял выражений
     * или нет пользователя с таким userId - возвращает пустую страницу.
     * @param userId идентификатор пользователя, из устойчивых выражений которого делается выборка
     * @param start дата начала периода за который собирается статистика
     * @param end дата конца периода за которые собирается статистика
     * @param pageable параметры пагинации
     * @throws InvalidParameter если start > end. {@link InvalidParameter#getMessageKey()} вернет StatisticRepository.invalidPeriodBorder
     * @throws NullPointerException если хотя бы один из параметров равен null
     * @return статистические данные о результатах повторения всех устойчивых выражений полученных за указанный
     *         период.
     * @see ExpressionRepetitionByPeriodStatistic
     */
    public Page<ExpressionRepetitionByPeriodStatistic> expressionsRepetitionByPeriod(
            UUID userId, LocalDate start, LocalDate end, Pageable pageable);

}
