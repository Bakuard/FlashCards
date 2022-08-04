package com.bakuard.flashcards.model;

import com.bakuard.flashcards.validation.Present;
import org.springframework.data.relational.core.mapping.Column;

import javax.validation.constraints.Min;
import java.time.LocalDate;
import java.util.Objects;

public class RepeatData {

    @Column("repeat_interval")
    @Min(value = 1, message = "RepeatData.interval.min")
    private int interval;
    @Column("last_date_of_repeat")
    @Present(message = "RepeatData.lastDateOfRepeat.present")
    private LocalDate lastDateOfRepeat;

    public RepeatData(int interval, LocalDate lastDateOfRepeat) {
        this.interval = interval;
        this.lastDateOfRepeat = lastDateOfRepeat;
    }

    public int getInterval() {
        return interval;
    }

    public LocalDate getLastDateOfRepeat() {
        return lastDateOfRepeat;
    }

    public LocalDate nextDateOfRepeat() {
        return lastDateOfRepeat.plusDays(interval);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RepeatData that = (RepeatData) o;
        return interval == that.interval && Objects.equals(lastDateOfRepeat, that.lastDateOfRepeat);
    }

    @Override
    public int hashCode() {
        return Objects.hash(interval, lastDateOfRepeat);
    }

    @Override
    public String toString() {
        return "RepeatData{" +
                "interval=" + interval +
                ", lastDateOfRepeat=" + lastDateOfRepeat +
                '}';
    }

}
