package com.bakuard.flashcards.service;

import com.bakuard.flashcards.config.ConfigData;
import com.bakuard.flashcards.dal.ExpressionRepository;
import com.bakuard.flashcards.dal.IntervalsRepository;
import com.bakuard.flashcards.model.RepeatDataFromEnglish;
import com.bakuard.flashcards.model.RepetitionResult;
import com.bakuard.flashcards.model.expression.Expression;
import com.bakuard.flashcards.validation.UnknownEntityException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.transaction.annotation.Transactional;

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
    private ConfigData configData;

    public ExpressionService(ExpressionRepository expressionRepository,
                             IntervalsRepository intervalsRepository,
                             Clock clock,
                             ConfigData configData) {
        this.expressionRepository = expressionRepository;
        this.intervalsRepository = intervalsRepository;
        this.clock = clock;
        this.configData = configData;
    }

    public int getLowestRepeatInterval(UUID userId) {
        return intervalsRepository.findAll(userId).get(0);
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

    public Page<Expression> findByValue(UUID userId, String value, int maxDistance, Pageable pageable) {
        maxDistance = Math.max(maxDistance, 1);
        maxDistance = Math.min(configData.levenshteinMaxDistance(), maxDistance);
        final int distance = maxDistance;

        return PageableExecutionUtils.getPage(
                expressionRepository.findByValue(userId, value, maxDistance, pageable.getPageSize(), pageable.getPageNumber()),
                pageable,
                () -> expressionRepository.countForValue(userId, value, distance)
        );
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

    public long count(UUID userId) {
        return expressionRepository.count(userId);
    }

    public long countForRepeat(UUID userId) {
        return expressionRepository.countForRepeatFromEnglish(userId, LocalDate.now(clock));
    }

    public Page<Expression> findByUserId(UUID userId, Pageable pageable) {
        return expressionRepository.findByUserId(userId, pageable);
    }

    public Page<Expression> findAllForRepeatFromEnglish(UUID userId, Pageable pageable) {
        LocalDate date = LocalDate.now(clock);

        return PageableExecutionUtils.getPage(
                expressionRepository.findAllForRepeatFromEnglish(userId, date, pageable.getPageSize(), pageable.getPageNumber()),
                pageable,
                () -> expressionRepository.countForRepeatFromEnglish(userId, date)
        );
    }

    public Page<Expression> findAllForRepeatFromNative(UUID userId, Pageable pageable) {
        LocalDate date = LocalDate.now(clock);

        return PageableExecutionUtils.getPage(
                expressionRepository.findAllForRepeatFromNative(userId, date, pageable.getPageSize(), pageable.getPageNumber()),
                pageable,
                () -> expressionRepository.countForRepeatFromNative(userId, date)
        );
    }

    public Expression repeatFromEnglish(UUID userId, UUID expressionId, boolean isRemember) {
        Expression expression = tryFindById(userId, expressionId);
        expression.repeatFromEnglish(isRemember, LocalDate.now(clock), intervalsRepository.findAll(expression.getUserId()));
        return expression;
    }

    public RepetitionResult<Expression> repeatFromNative(UUID userId, UUID expressionId, String inputWordValue) {
        Expression expression = tryFindById(userId, expressionId);
        boolean isRemember = expression.repeatFromNative(
                inputWordValue, LocalDate.now(clock), intervalsRepository.findAll(userId));
        return new RepetitionResult<>(expression, isRemember);
    }

    public boolean isHotRepeatFromEnglish(Expression expression) {
        List<Integer> intervals = intervalsRepository.findAll(expression.getUserId());
        return expression.isHotRepeatFromEnglish(intervals.get(0));
    }

    public boolean isHotRepeatFromNative(Expression expression) {
        List<Integer> intervals = intervalsRepository.findAll(expression.getUserId());
        return expression.isHotRepeatFromNative(intervals.get(0));
    }

}
