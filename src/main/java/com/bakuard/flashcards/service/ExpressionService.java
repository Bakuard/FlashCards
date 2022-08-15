package com.bakuard.flashcards.service;

import com.bakuard.flashcards.dal.ExpressionRepository;
import com.bakuard.flashcards.dal.IntervalsRepository;
import com.bakuard.flashcards.model.expression.Expression;
import com.google.common.collect.ImmutableList;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

public class ExpressionService {

    private ExpressionRepository expressionRepository;
    private IntervalsRepository intervalsRepository;
    private Clock clock;

    public ExpressionService(ExpressionRepository expressionRepository,
                             IntervalsRepository intervalsRepository,
                             Clock clock) {
        this.expressionRepository = expressionRepository;
        this.intervalsRepository = intervalsRepository;
        this.clock = clock;
    }

    public void save(Expression expression) {
        expressionRepository.save(expression);
    }

    public void deleteById(UUID userId, UUID expressionId) {
        expressionRepository.deleteById(userId, expressionId);
    }

    public boolean existsById(UUID userId, UUID expressionId) {
        return expressionRepository.existsById(userId, expressionId);
    }

    public Optional<Expression> findById(UUID userId, UUID expressionId) {
        return expressionRepository.findById(userId, expressionId);
    }

    public Optional<Expression> findByValue(UUID userId, String value) {
        return expressionRepository.findByValue(userId, value);
    }

    public long count(UUID userId) {
        return expressionRepository.count(userId);
    }

    public long countForRepeat(UUID userId) {
        return expressionRepository.countForRepeat(userId, LocalDate.now(clock));
    }

    public Page<Expression> findByUserId(UUID userId, Pageable pageable) {
        return expressionRepository.findByUserId(userId, pageable);
    }

    public Page<Expression> findAllForRepeat(UUID userId, Pageable pageable) {
        LocalDate date = LocalDate.now(clock);

        return PageableExecutionUtils.getPage(
                expressionRepository.findAllForRepeat(userId, date, pageable.getPageSize(), pageable.getPageNumber()),
                pageable,
                () -> expressionRepository.countForRepeat(userId, date)
        );
    }

    @Transactional
    public void repeat(Expression expression, boolean isRemember) {
        expression.repeat(isRemember, LocalDate.now(clock), intervalsRepository.findAll(expression.getUserId()));
    }

    @Transactional
    public void replaceRepeatInterval(UUID userId, int oldInterval, int newInterval) {
        ImmutableList<Integer> intervals = intervalsRepository.findAll(userId);
        if(!intervals.contains(oldInterval)) {
            throw new IllegalArgumentException("Unknown oldInterval=" + oldInterval + " for user=" + userId);
        } else if(!intervals.contains(newInterval)) {
            throw new IllegalArgumentException("Unknown newInterval=" + newInterval + " for user=" + userId);
        } else if(oldInterval != newInterval) {
            expressionRepository.replaceRepeatInterval(userId, oldInterval, newInterval);
            intervalsRepository.removeUnused(userId);
        }
    }

}
