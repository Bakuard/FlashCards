package com.bakuard.flashcards.model;

import jakarta.validation.constraints.Min;
import org.springframework.data.relational.core.mapping.Column;

import java.time.LocalDate;

/**
 * Данные о последнем повторении слова или устойчивого выражения с английского языка на родной язык пользователя.
 * @param interval кол-во дней до следующего повторения с английского языка на родной язык пользователя.
 * @param lastDateOfRepeat дата последнего повторения с английского языка на родной язык пользователя.
 */
public record RepeatDataFromEnglish(
        @Column("repeat_interval_from_english")
        @Min(value = 1, message = "RepeatDataFromEnglish.interval.min")
        int interval,
        @Column("last_date_of_repeat_from_english")
        LocalDate lastDateOfRepeat) {

    /**
     * Создает и возвращает точную копию переданного объекта.
     * @param data копируемый объект.
     * @return точная копия переданного объекта.
     */
    public static RepeatDataFromEnglish copy(RepeatDataFromEnglish data) {
        return new RepeatDataFromEnglish(data.interval, data.lastDateOfRepeat);
    }


    /**
     * Проверяет - нужно ли повторять слово или устойчивое выражение, к которому относится данный объект.
     * Если интервал повторения равен наименьшему из возможных интервалов этого пользователя - то слово
     * или устойчивое выражение должно быть повторено в ближайшее время.
     * @param lowestRepeatInterval наименьший из интервалов повторения пользователя.
     * @return true - если выполняется описанное выше условие, иначе - false.
     */
    public boolean isHotRepeat(int lowestRepeatInterval) {
        return lowestRepeatInterval == interval;
    }

}
