package com.bakuard.flashcards.model;

import org.springframework.data.relational.core.mapping.Column;

import javax.validation.constraints.Min;
import java.time.LocalDate;

/**
 * Данные о последнем повторении слова или устойчевого выражения с родного языка пользователя на английский язык.
 * @param interval кол-во дней до слеущего повторения с родного языка пользователя на английский язык.
 * @param lastDateOfRepeat дата последнего повторения с родного языка пользователя на английский язык.
 */
public record RepeatDataFromNative(
        @Column("repeat_interval_from_native")
        @Min(value = 1, message = "RepeatDataFromNative.interval.min")
        int interval,
        @Column("last_date_of_repeat_from_native")
        LocalDate lastDateOfRepeat) {

    /**
     * Создает и возвращает точную копию переданного объекта.
     * @param data копируемый объект.
     * @return точная копия переданног объекта.
     */
    public static RepeatDataFromNative copy(RepeatDataFromNative data) {
        return new RepeatDataFromNative(data.interval, data.lastDateOfRepeat);
    }


    /**
     * Проверяет - является ли слово новым в словаре пользователя или было ли последнее повторение слова успешным.
     * @param lowestRepeatInterval наименьший из интервалов повторения пользователя.
     * @return true - если выполняется описанное выше условие, иначе - false.
     */
    public boolean isHotRepeat(int lowestRepeatInterval) {
        return lowestRepeatInterval == interval;
    }

}
