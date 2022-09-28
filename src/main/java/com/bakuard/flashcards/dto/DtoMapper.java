package com.bakuard.flashcards.dto;

import com.bakuard.flashcards.config.ConfigData;
import com.bakuard.flashcards.dto.common.ExampleRequestResponse;
import com.bakuard.flashcards.dto.common.InterpretationRequestResponse;
import com.bakuard.flashcards.dto.common.TranscriptionRequestResponse;
import com.bakuard.flashcards.dto.common.TranslateRequestResponse;
import com.bakuard.flashcards.dto.word.*;
import com.bakuard.flashcards.model.filter.SortRules;
import com.bakuard.flashcards.model.word.*;
import com.bakuard.flashcards.service.ExpressionService;
import com.bakuard.flashcards.service.WordService;
import com.bakuard.flashcards.validation.ValidatorUtil;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.util.CollectionUtils;

import java.util.Collection;
import java.util.UUID;
import java.util.stream.Stream;

public class DtoMapper {

    private WordService wordService;
    private ExpressionService expressionService;
    private ConfigData configData;
    private SortRules sortRules;
    private ValidatorUtil validator;

    public DtoMapper(WordService wordService,
                     ExpressionService expressionService,
                     ConfigData configData,
                     SortRules sortRules,
                     ValidatorUtil validator) {
        this.wordService = wordService;
        this.expressionService = expressionService;
        this.configData = configData;
        this.sortRules = sortRules;
        this.validator = validator;
    }

    public WordResponse toWordResponse(Word word) {
        return new WordResponse().
                setWordId(word.getId()).
                setUserId(word.getUserId()).
                setValue(word.getValue()).
                setNote(word.getNote()).
                setTranscriptions(word.getTranscriptions().stream().
                        map(this::toTranscriptionRequestResponse).
                        toList()).
                setInterpretations(word.getInterpretations().stream().
                        map(this::toInterpretationRequestResponse).
                        toList()).
                setTranslates(word.getTranslations().stream().
                        map(this::toTranslateRequestResponse).
                        toList()).
                setExamples(word.getExamples().stream().
                        map(this::toExampleRequestResponse).
                        toList());
    }

    public Page<WordForDictionaryListResponse> toWordsForDictionaryListResponse(Page<Word> words) {
        return words.map(
                word -> new WordForDictionaryListResponse().
                        setWordId(word.getId()).
                        setUserId(word.getUserId()).
                        setValue(word.getValue()).
                        setHotRepeat(wordService.isHotRepeat(word))
        );
    }

    public Page<WordForRepetitionResponse> toWordsForRepetitionResponse(Page<Word> words) {
        return words.map(
                word -> new WordForRepetitionResponse().
                        setWordId(word.getId()).
                        setUserId(word.getUserId()).
                        setValue(word.getValue()).
                        setExamples(word.getExamples().stream().
                                map(WordExample::getOrigin).
                                toList())
        );
    }

    public Word toWord(WordAddRequest dto, UUID userID) {
        return Word.newBuilder(validator).
                setUserId(userID).
                setValue(dto.getValue()).
                setNote(dto.getNote()).
                setTranscriptions(dto.getTranscriptions().stream().
                        map(this::toWordTranscription).
                        toList()).
                setInterpretations(dto.getInterpretations().stream().
                        map(this::toWordInterpretation).
                        toList()).
                setTranslations(dto.getTranslates().stream().
                        map(this::toWordTranslation).
                        toList()).
                setExamples(dto.getExamples().stream().
                        map(this::toWordExample).
                        toList()).
                setRepeatData(wordService.initialRepeatData(userID)).
                build();
    }

    public Word toWord(WordUpdateRequest dto, UUID userID) {
        return wordService.tryFindById(userID, dto.getWordId()).builder().
                setValue(dto.getValue()).
                setNote(dto.getNote()).
                setTranscriptions(dto.getTranscriptions().stream().
                        map(this::toWordTranscription).
                        toList()).
                setInterpretations(dto.getInterpretations().stream().
                        map(this::toWordInterpretation).
                        toList()).
                setTranslations(dto.getTranslates().stream().
                        map(this::toWordTranslation).
                        toList()).
                setExamples(dto.getExamples().stream().
                        map(this::toWordExample).
                        toList()).
                build();
    }

    public Word toWord(WordRepeatRequest dto, UUID userID) {
        return wordService.tryFindById(userID, dto.getWordId());
    }

    public Pageable toPageableForDictionaryWords(int page, int size, String sort) {
        size = Math.min(configData.maxPageSize(), size);

        return PageRequest.of(
                page,
                size,
                sortRules.toWordsSort(sort)
        );
    }


    private ExampleRequestResponse toExampleRequestResponse(WordExample wordExample) {
        return new ExampleRequestResponse().
                setOrigin(wordExample.getOrigin()).
                setTranslate(wordExample.getTranslate()).
                setNote(wordExample.getNote());
    }

    private InterpretationRequestResponse toInterpretationRequestResponse(WordInterpretation wordInterpretation) {
        return new InterpretationRequestResponse().
                setValue(wordInterpretation.getValue());
    }

    private TranscriptionRequestResponse toTranscriptionRequestResponse(WordTranscription wordTranscription) {
        return new TranscriptionRequestResponse().
                setValue(wordTranscription.getValue()).
                setNote(wordTranscription.getNote());
    }

    private TranslateRequestResponse toTranslateRequestResponse(WordTranslation wordTranslation) {
        return new TranslateRequestResponse().
                setValue(wordTranslation.getValue()).
                setNote(wordTranslation.getNote());
    }


    private WordTranscription toWordTranscription(TranscriptionRequestResponse dto) {
        return new WordTranscription(dto.getValue(), dto.getNote());
    }

    private WordInterpretation toWordInterpretation(InterpretationRequestResponse dto) {
        return new WordInterpretation(dto.getValue());
    }

    private WordTranslation toWordTranslation(TranslateRequestResponse dto) {
        return new WordTranslation(dto.getValue(), dto.getNote());
    }

    private WordExample toWordExample(ExampleRequestResponse dto) {
        return new WordExample(dto.getOrigin(), dto.getTranslate(), dto.getNote());
    }

}
