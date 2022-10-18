package com.bakuard.flashcards.model;

import com.bakuard.flashcards.validation.Present;
import org.springframework.data.relational.core.mapping.Column;

import javax.validation.constraints.Min;
import java.time.LocalDate;

public record RepeatDataFromNative(
        @Column("repeat_interval_from_native")
        @Min(value = 1, message = "RepeatDataFromNative.interval.min")
        int interval,
        @Column("last_date_of_repeat_from_native")
        @Present(message = "RepeatDataFromNative.lastDateOfRepeat.present")
        LocalDate lastDateOfRepeat) {

    public LocalDate nextDateOfRepeat() {
        return lastDateOfRepeat.plusDays(interval);
    }

}
