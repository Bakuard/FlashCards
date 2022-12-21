package com.bakuard.flashcards.dal;

import com.bakuard.flashcards.validation.exception.InvalidParameter;
import com.bakuard.flashcards.validation.exception.NotUniqueEntityException;
import com.bakuard.flashcards.validation.exception.UnknownEntityException;
import com.google.common.collect.ImmutableList;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Отвечает за редактирование списка всех интервалов повторения конкретного пользователя находящихся в
 * постоянном хранилище. <br/><br/>
 * Интервал повторения - это кол-во дней через которое нужно будет повторить слово(или устойчивое выражение)
 * с момента его последнего повторения. У пользователя, как правило, есть несколько интервалов повторений
 * разной длины. После каждого успешного повторения слова или устойчивого выражения - выбирается следующий
 * интервал повторения, который больше текущего (если таких несколько, то выбирается наименьший из них). <br/>
 * Например: <br/>
 * 1. У пользователя есть интервалы повторения 1, 3, 5 и 11. <br/>
 * 2. Также у пользователя есть слово wordA, текущий интервал повторения которого равен 3. <br/>
 * В случае успешного повторения слова wordA, в качестве нового текущего интервала повторения будет взято
 * значение 5.
 * @see com.bakuard.flashcards.model.word.Word#repeatFromEnglish(boolean, LocalDate, ImmutableList)
 * @see com.bakuard.flashcards.model.word.Word#repeatFromNative(String, LocalDate, ImmutableList)
 * @see com.bakuard.flashcards.model.expression.Expression#repeatFromEnglish(boolean, LocalDate, ImmutableList)
 * @see com.bakuard.flashcards.model.expression.Expression#repeatFromNative(String, LocalDate, ImmutableList)
 */
public interface IntervalRepository {

    /**
     * Добавляет новый интервал повторения для пользователя userId.
     * @param userId идентификатор пользователя
     * @param interval интервал повторения
     * @throws InvalidParameter если interval < 1.
     *                          {@link InvalidParameter#getMessageKey()} вернет RepeatInterval.notNegative
     * @throws NullPointerException если userId равен null
     * @throws NotUniqueEntityException если у пользователя уже есть интервал повторения с таким значением.
     *                                  {@link NotUniqueEntityException#getMessageKey()} вернет RepeatInterval.unique
     * @throws UnknownEntityException если пользователя с таким userId не существует.
     *                                {@link UnknownEntityException#getMessageKey()} вернет User.unknownId
     */
    public void add(UUID userId, int interval);

    /**
     * Добавляет новые интервалы повторения для пользователя userId.
     * @param userId идентификатор пользователя
     * @param intervals добавляемые интервалы
     * @throws InvalidParameter если хотя бы один из интервалов < 1.
     *                          {@link InvalidParameter#getMessageKey()} вернет RepeatInterval.notNegative
     * @throws NullPointerException если userId или intervals равны null
     * @throws NotUniqueEntityException если у пользователя уже есть интервалы повторения с такими значениями
     *                                  {@link NotUniqueEntityException#getMessageKey()} вернет RepeatInterval.unique
     * @throws UnknownEntityException если пользователя с таким userId не существует.
     *                                {@link UnknownEntityException#getMessageKey()} вернет User.unknownId
     */
    public void addAll(UUID userId, int... intervals);

    /**
     * Заменяет интервал повторения равный oldInterval на интервал повторения равный newInterval. У всех
     * слов и устойчивых выражений, текущий интервал повторения которых равен oldInterval, значение интервала
     * будет заменено на newInterval. Метод выполняется над интервалами повторения пользователя userId.
     * <br/><br/>
     * Особый случай: если oldInterval равен newInterval - метод ничего не делает.
     * @param userId идентификатор пользователя
     * @param oldInterval заменяемый интервал повторения
     * @param newInterval новый интервал повторения
     * @throws InvalidParameter если среди интервалов повторения пользователя userId нет интервала со значением
     *                          oldInterval. {@link InvalidParameter#getMessageKey()} вернет RepeatInterval.notNegative
     *                          или RepeatInterval.intervalNotExists
     */
    public void replace(UUID userId, int oldInterval, int newInterval);

    /**
     * Находит и возвращает все интервалы повторения пользователя
     * @param userId идентификатор пользователя
     * @return все интервалы повторения пользователя
     */
    public ImmutableList<Integer> findAll(UUID userId);

}
