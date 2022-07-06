package com.bakuard.flashcards.model;

import org.springframework.data.relational.core.mapping.Column;

import java.time.LocalDate;

public record RepeatData(@Column("interval") int interval,
                         @Column("last_date_of_repeat") LocalDate lastDateOfRepeat) {}
