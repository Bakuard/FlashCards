package com.bakuard.flashcards.dal;

import com.bakuard.flashcards.model.statistic.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.UUID;

public interface StatisticRepository {

    public void append(RepeatWordFromEnglishStatistic statistic);

    public void append(RepeatWordFromNativeStatistic statistic);

    public void append(RepeatExpressionFromEnglishStatistic statistic);

    public void append(RepeatExpressionFromNativeStatistic statistic);

    public WordRepetitionByPeriodStatistic wordRepetitionByPeriod(
            UUID userId, UUID wordId, LocalDate start, LocalDate end);

    public ExpressionRepetitionByPeriodStatistic expressionRepetitionByPeriod(
            UUID userId, UUID expressionId, LocalDate start, LocalDate end);

    public Page<WordRepetitionByPeriodStatistic> wordsRepetitionByPeriod(
            UUID userId, LocalDate start, LocalDate end, Pageable pageable);

    public Page<ExpressionRepetitionByPeriodStatistic> expressionsRepetitionByPeriod(
            UUID userId, LocalDate start, LocalDate end, Pageable pageable);

}
