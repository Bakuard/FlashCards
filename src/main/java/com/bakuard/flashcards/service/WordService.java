package com.bakuard.flashcards.service;

import com.bakuard.flashcards.config.ConfigData;
import com.bakuard.flashcards.dal.IntervalsRepository;
import com.bakuard.flashcards.dal.WordsRepository;
import com.bakuard.flashcards.model.RepeatData;
import com.bakuard.flashcards.model.word.Word;
import com.bakuard.flashcards.validation.UnknownEntityException;
import com.google.common.collect.ImmutableList;
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
public class WordService {

    private WordsRepository wordsRepository;
    private IntervalsRepository intervalsRepository;
    private Clock clock;
    private ConfigData configData;

    public WordService(WordsRepository wordsRepository,
                       IntervalsRepository intervalsRepository,
                       Clock clock,
                       ConfigData configData) {
        this.wordsRepository = wordsRepository;
        this.intervalsRepository = intervalsRepository;
        this.clock = clock;
    }

    public RepeatData initialRepeatData(UUID userId) {
        List<Integer> intervals = intervalsRepository.findAll(userId);
        return new RepeatData(intervals.get(0), LocalDate.now(clock));
    }

    public Word save(Word word) {
        return wordsRepository.save(word);
    }

    public void tryDeleteById(UUID userId, UUID wordId) {
        if(!existsById(userId, wordId)) {
            throw new UnknownEntityException(
                    "Unknown word with id=" + wordId + " userId=" + userId,
                    "Word.unknownId");
        }
        wordsRepository.deleteById(userId, wordId);
    }

    public boolean existsById(UUID userId, UUID wordId) {
        return wordsRepository.existsById(userId, wordId);
    }

    public Optional<Word> findById(UUID userId, UUID wordId) {
        return wordsRepository.findById(userId, wordId);
    }

    public Page<Word> findByValue(UUID userId, String value, int maxDistance, Pageable pageable) {
        maxDistance = Math.max(maxDistance, 1);
        maxDistance = Math.min(configData.levenshteinMaxDistance(), maxDistance);
        final int distance = maxDistance;

        return PageableExecutionUtils.getPage(
                wordsRepository.findByValue(userId, value, maxDistance, pageable.getPageSize(), pageable.getPageNumber()),
                pageable,
                () -> wordsRepository.countForValue(userId, value, distance)
        );
    }

    public Word tryFindById(UUID userId, UUID wordId) {
        return findById(userId, wordId).
                orElseThrow(
                        () -> new UnknownEntityException(
                                "Unknown word with id=" + wordId + " userId=" + userId,
                                "Word.unknownId"
                        )
                );
    }

    public long count(UUID userId) {
        return wordsRepository.count(userId);
    }

    public long countForRepeat(UUID userId) {
        return wordsRepository.countForRepeat(userId, LocalDate.now(clock));
    }

    public Page<Word> findByUserId(UUID userId, Pageable pageable) {
        return wordsRepository.findByUserId(userId, pageable);
    }

    public Page<Word> findAllForRepeat(UUID userId, Pageable pageable) {
        LocalDate date = LocalDate.now(clock);

        return PageableExecutionUtils.getPage(
                wordsRepository.findAllForRepeat(userId, date, pageable.getPageSize(), pageable.getPageNumber()),
                pageable,
                () -> wordsRepository.countForRepeat(userId, date)
        );
    }

    public Word repeat(UUID userId, UUID wordId, boolean isRemember) {
        Word word = tryFindById(userId, wordId);
        word.repeat(isRemember, LocalDate.now(clock), intervalsRepository.findAll(word.getUserId()));
        return word;
    }

    public void replaceRepeatInterval(UUID userId, int oldInterval, int newInterval) {
        ImmutableList<Integer> intervals = intervalsRepository.findAll(userId);
        if(!intervals.contains(oldInterval)) {
            throw new IllegalArgumentException("Unknown oldInterval=" + oldInterval + " for user=" + userId);
        } else if(!intervals.contains(newInterval)) {
            throw new IllegalArgumentException("Unknown newInterval=" + newInterval + " for user=" + userId);
        } else if(oldInterval != newInterval) {
            wordsRepository.replaceRepeatInterval(userId, oldInterval, newInterval);
            intervalsRepository.removeUnused(userId);
        }
    }

    public boolean isHotRepeat(Word word) {
        List<Integer> intervals = intervalsRepository.findAll(word.getUserId());
        return word.isHotRepeat(intervals.get(0));
    }

}
