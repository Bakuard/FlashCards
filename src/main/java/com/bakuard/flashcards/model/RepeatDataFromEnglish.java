package com.bakuard.flashcards.model;

import com.bakuard.flashcards.validation.Present;
import org.springframework.data.relational.core.mapping.Column;

import javax.validation.constraints.Min;
import java.time.LocalDate;

public record RepeatDataFromEnglish(
        @Column("repeat_interval_from_english")
        @Min(value = 1, message = "RepeatDataFromEnglish.interval.min")
        int interval,
        @Column("last_date_of_repeat_from_english")
        @Present(message = "RepeatDataFromEnglish.lastDateOfRepeat.present")
        LocalDate lastDateOfRepeat) {

    public static RepeatDataFromEnglish copy(RepeatDataFromEnglish data) {
        return new RepeatDataFromEnglish(data.interval, data.lastDateOfRepeat);
    }


    public LocalDate nextDateOfRepeat() {
        return lastDateOfRepeat.plusDays(interval);
    }

}
