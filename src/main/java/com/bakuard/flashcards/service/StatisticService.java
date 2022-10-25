package com.bakuard.flashcards.service;

import com.bakuard.flashcards.dal.StatisticRepository;
import com.bakuard.flashcards.model.statistic.ExpressionRepetitionByPeriodStatistic;
import com.bakuard.flashcards.model.statistic.WordRepetitionByPeriodStatistic;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.UUID;

@Transactional
public class StatisticService {

    private StatisticRepository statisticRepository;

    public StatisticService(StatisticRepository statisticRepository) {
        this.statisticRepository = statisticRepository;
    }

    public WordRepetitionByPeriodStatistic wordRepetitionByPeriod(
            UUID userId, UUID wordId, LocalDate start, LocalDate end) {
        return statisticRepository.wordRepetitionByPeriod(userId, wordId, start, end);
    }

    public ExpressionRepetitionByPeriodStatistic expressionRepetitionByPeriod(
            UUID userId, UUID expressionId, LocalDate start, LocalDate end) {
        return statisticRepository.expressionRepetitionByPeriod(userId, expressionId, start, end);
    }

    public Page<WordRepetitionByPeriodStatistic> wordsRepetitionByPeriod(
            UUID userId, LocalDate start, LocalDate end, Pageable pageable) {
        return statisticRepository.wordsRepetitionByPeriod(userId, start, end, pageable);
    }

    public Page<ExpressionRepetitionByPeriodStatistic> expressionsRepetitionByPeriod(
            UUID userId, LocalDate start, LocalDate end, Pageable pageable) {
        return statisticRepository.expressionsRepetitionByPeriod(userId, start, end, pageable);
    }

}
