package com.bakuard.flashcards.service;

import com.bakuard.flashcards.dal.IntervalsRepository;
import com.bakuard.flashcards.dal.WordsRepository;
import com.bakuard.flashcards.model.Word;
import com.google.common.collect.ImmutableList;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

public class WordService {

    private WordsRepository wordsRepository;
    private IntervalsRepository intervalsRepository;

    public WordService(WordsRepository wordsRepository, IntervalsRepository intervalsRepository) {
        this.wordsRepository = wordsRepository;
        this.intervalsRepository = intervalsRepository;
    }

    public void save(Word word) {
        wordsRepository.save(word);
    }

    public void deleteById(UUID userId, UUID wordId) {
        wordsRepository.deleteById(userId, wordId);
    }

    public boolean existsById(UUID userId, UUID wordId) {
        return wordsRepository.existsById(userId, wordId);
    }

    public Optional<Word> findById(UUID userId, UUID wordId) {
        return wordsRepository.findById(userId, wordId);
    }

    public Optional<Word> findByValue(UUID userId, String value) {
        return wordsRepository.findByValue(userId, value);
    }

    public long count(UUID userId) {
        return wordsRepository.count(userId);
    }

    public long countForRepeat(UUID userId, LocalDate date) {
        return wordsRepository.countForRepeat(userId, date);
    }

    public Page<Word> findByUserId(UUID userId, Pageable pageable) {
        return wordsRepository.findByUserId(userId, pageable);
    }

    public Page<Word> findAllForRepeat(UUID userId, LocalDate date, Pageable pageable) {
        return PageableExecutionUtils.getPage(
                wordsRepository.findAllForRepeat(userId, date, pageable.getPageSize(), pageable.getPageNumber()),
                pageable,
                () -> wordsRepository.countForRepeat(userId, date)
        );
    }

    @Transactional
    public void repeat(Word word, boolean isRemember) {
        word.repeat(isRemember, LocalDate.now(), intervalsRepository.findAll(word.getUserId()));
    }

    @Transactional
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

}
