package com.bakuard.flashcards.service;

import com.bakuard.flashcards.dal.ExpressionRepository;
import com.bakuard.flashcards.dal.IntervalsRepository;
import com.bakuard.flashcards.model.RepeatData;
import com.bakuard.flashcards.model.expression.Expression;
import com.bakuard.flashcards.validation.UnknownEntityException;
import com.google.common.collect.ImmutableList;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.transaction.annotation.Transactional;

import java.rmi.server.UID;
import java.time.Clock;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Transactional
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

    public RepeatData initialRepeatData(UUID userId) {
        List<Integer> intervals = intervalsRepository.findAll(userId);
        return new RepeatData(intervals.get(0), LocalDate.now(clock));
    }

    public Expression save(Expression expression) {
        return expressionRepository.save(expression);
    }

    public void tryDeleteById(UUID userId, UUID expressionId) {
        if(!existsById(userId, expressionId)) {
            throw new UnknownEntityException(
                    "Unknown expression with id=" + expressionId + " userId=" + userId,
                    "Expression.unknown");
        }
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

    public Expression tryFindById(UUID userId, UUID expressionId) {
        return findById(userId, expressionId).
                orElseThrow(
                        () -> new UnknownEntityException(
                                "Unknown expression with id=" + expressionId + " userId=" + userId,
                                "Expression.unknownId"
                        )
                );
    }

    public Expression tryFindByValue(UUID userId, String value) {
        return findByValue(userId, value).
                orElseThrow(
                        () -> new UnknownEntityException(
                                "Unknown expression with value=" + value + " userId=" + userId,
                                "Expression.unknownValue"
                        )
                );
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

    public void repeat(Expression expression, boolean isRemember) {
        expression.repeat(isRemember, LocalDate.now(clock), intervalsRepository.findAll(expression.getUserId()));
    }

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

    public boolean isHotRepeat(Expression expression) {
        List<Integer> intervals = intervalsRepository.findAll(expression.getUserId());
        return expression.isHotRepeat(intervals.get(0));
    }

}
