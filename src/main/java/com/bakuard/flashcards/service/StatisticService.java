package com.bakuard.flashcards.service;

import com.bakuard.flashcards.dal.StatisticRepository;
import com.bakuard.flashcards.model.statistic.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDate;
import java.util.UUID;

@Transactional
public class StatisticService {

    private StatisticRepository statisticRepository;
    private Clock clock;

    public StatisticService(StatisticRepository statisticRepository, Clock clock) {
        this.statisticRepository = statisticRepository;
        this.clock = clock;
    }

    public void appendWordFromEnglish(UUID userId, UUID wordId, boolean isRemember) {
        statisticRepository.append(
                new RepeatWordFromEnglishStatistic(
                        userId,
                        wordId,
                        LocalDate.now(clock),
                        isRemember)
        );
    }

    public void appendWordFromNative(UUID userId, UUID wordId, boolean isRemember) {
        statisticRepository.append(
                new RepeatWordFromNativeStatistic(
                        userId,
                        wordId,
                        LocalDate.now(clock),
                        isRemember)
        );
    }

    public void appendExpressionFromEnglish(UUID userId, UUID expressionId, boolean isRemember) {
        statisticRepository.append(
                new RepeatExpressionFromEnglishStatistic(
                        userId,
                        expressionId,
                        LocalDate.now(clock),
                        isRemember)
        );
    }

    public void appendExpressionFromNative(UUID userId, UUID expressionId, boolean isRemember) {
        statisticRepository.append(
                new RepeatExpressionFromNativeStatistic(
                        userId,
                        expressionId,
                        LocalDate.now(clock),
                        isRemember)
        );
    }

    public WordRepetitionByPeriodStatistic wordRepetitionByPeriod(
            UUID userId, UUID wordId, String start, String end) {
        return statisticRepository.wordRepetitionByPeriod(
                userId, wordId, LocalDate.parse(start), LocalDate.parse(end)
        );
    }

    public ExpressionRepetitionByPeriodStatistic expressionRepetitionByPeriod(
            UUID userId, UUID expressionId, String start, String end) {
        return statisticRepository.expressionRepetitionByPeriod(
                userId, expressionId, LocalDate.parse(start), LocalDate.parse(end)
        );
    }

    public Page<WordRepetitionByPeriodStatistic> wordsRepetitionByPeriod(
            UUID userId, String start, String end, Pageable pageable) {
        return statisticRepository.wordsRepetitionByPeriod(
                userId, LocalDate.parse(start), LocalDate.parse(end), pageable
        );
    }

    public Page<ExpressionRepetitionByPeriodStatistic> expressionsRepetitionByPeriod(
            UUID userId, String start, String end, Pageable pageable) {
        return statisticRepository.expressionsRepetitionByPeriod(
                userId, LocalDate.parse(start), LocalDate.parse(end), pageable
        );
    }

}
