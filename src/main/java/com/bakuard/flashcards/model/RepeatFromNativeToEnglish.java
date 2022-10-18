package com.bakuard.flashcards.model;

import com.bakuard.flashcards.validation.Present;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import javax.validation.constraints.Min;
import java.time.LocalDate;
import java.util.UUID;

@Table("repeat_from_native_to_english")
public record RepeatFromNativeToEnglish(
        @Id
        @Column("repeat_id")
        UUID id,
        @Column("repetition_date")
        @Present(message = "RepeatFromNativeToEnglish.lastDateOfRepeat.present")
        LocalDate currentDate,
        @Column("repetition_interval")
        @Min(value = 1, message = "RepeatFromNativeToEnglish.interval.min")
        int interval,
        @Column("is_remember")
        boolean isRemember) {}
