package com.bakuard.flashcards.service;

import com.bakuard.flashcards.dal.IntervalsRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Transactional
public class IntervalService {

    private IntervalsRepository intervalsRepository;

    public IntervalService(IntervalsRepository intervalsRepository) {
        this.intervalsRepository = intervalsRepository;
    }

    public void add(UUID userId, int interval) {
        intervalsRepository.add(userId, interval);
    }

    public void replace(UUID userId, int oldInterval, int newInterval) {
        intervalsRepository.replace(userId, oldInterval, newInterval);
    }

}
