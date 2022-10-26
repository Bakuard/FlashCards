package com.bakuard.flashcards.service;

import com.bakuard.flashcards.dal.IntervalRepository;
import com.google.common.collect.ImmutableList;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Transactional
public class IntervalService {

    private IntervalRepository intervalRepository;

    public IntervalService(IntervalRepository intervalRepository) {
        this.intervalRepository = intervalRepository;
    }

    public void add(UUID userId, int interval) {
        intervalRepository.add(userId, interval);
    }

    public void replace(UUID userId, int oldInterval, int newInterval) {
        intervalRepository.replace(userId, oldInterval, newInterval);
    }

    public ImmutableList<Integer> findAll(UUID userId) {
        return intervalRepository.findAll(userId);
    }

}
