package com.bakuard.flashcards.service;

import com.bakuard.flashcards.dal.IntervalsRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

public class IntervalService {

    private IntervalsRepository intervalsRepository;

    public IntervalService(IntervalsRepository intervalsRepository) {
        this.intervalsRepository = intervalsRepository;
    }

    @Transactional
    public void add(UUID userId, int interval) {
        if(interval <= 0) {
            throw new IllegalArgumentException("interval can't be less then 1. Actual: " + interval);
        }

        intervalsRepository.add(userId, interval);
    }

}
