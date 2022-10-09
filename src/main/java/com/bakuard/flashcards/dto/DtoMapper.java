package com.bakuard.flashcards.dto;

import com.bakuard.flashcards.config.ConfigData;
import com.bakuard.flashcards.dto.common.ExampleRequestResponse;
import com.bakuard.flashcards.dto.common.InterpretationRequestResponse;
import com.bakuard.flashcards.dto.common.TranscriptionRequestResponse;
import com.bakuard.flashcards.dto.common.TranslateRequestResponse;
import com.bakuard.flashcards.dto.exceptions.ExceptionReasonResponse;
import com.bakuard.flashcards.dto.exceptions.ExceptionResponse;
import com.bakuard.flashcards.dto.expression.*;
import com.bakuard.flashcards.dto.word.*;
import com.bakuard.flashcards.model.expression.Expression;
import com.bakuard.flashcards.model.expression.ExpressionExample;
import com.bakuard.flashcards.model.expression.ExpressionInterpretation;
import com.bakuard.flashcards.model.expression.ExpressionTranslation;
import com.bakuard.flashcards.model.filter.SortRules;
import com.bakuard.flashcards.model.filter.SortedEntity;
import com.bakuard.flashcards.model.word.*;
import com.bakuard.flashcards.service.ExpressionService;
import com.bakuard.flashcards.service.WordService;
import com.bakuard.flashcards.validation.ValidatorUtil;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;

import javax.validation.ConstraintViolationException;
import java.time.Clock;
import java.util.Arrays;
import java.util.Collection;
import java.util.UUID;
import java.util.stream.Stream;

public class DtoMapper {

    private WordService wordService;
    private ExpressionService expressionService;
    private ConfigData configData;
    private SortRules sortRules;
    private ValidatorUtil validator;
    private Clock clock;

    public DtoMapper(WordService wordService,
                     ExpressionService expressionService,
                     ConfigData configData,
                     SortRules sortRules,
                     ValidatorUtil validator,
                     Clock clock) {
        this.wordService = wordService;
        this.expressionService = expressionService;
        this.configData = configData;
        this.sortRules = sortRules;
        this.validator = validator;
        this.clock = clock;
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

    public Word toWord(WordAddRequest dto) {
        return Word.newBuilder(validator).
                setUserId(dto.getUserID()).
                setValue(dto.getValue()).
                setNote(dto.getNote()).
                setTranscriptions(toStream(dto.getTranscriptions()).
                        map(this::toWordTranscription).
                        toList()).
                setInterpretations(toStream(dto.getInterpretations()).
                        map(this::toWordInterpretation).
                        toList()).
                setTranslations(toStream(dto.getTranslates()).
                        map(this::toWordTranslation).
                        toList()).
                setExamples(toStream(dto.getExamples()).
                        map(this::toWordExample).
                        toList()).
                setRepeatData(wordService.initialRepeatData(dto.getUserID())).
                build();
    }

    public Word toWord(WordUpdateRequest dto) {
        return wordService.tryFindById(dto.getUserId(), dto.getWordId()).builder().
                setValue(dto.getValue()).
                setNote(dto.getNote()).
                setTranscriptions(toStream(dto.getTranscriptions()).
                        map(this::toWordTranscription).
                        toList()).
                setInterpretations(toStream(dto.getInterpretations()).
                        map(this::toWordInterpretation).
                        toList()).
                setTranslations(toStream(dto.getTranslates()).
                        map(this::toWordTranslation).
                        toList()).
                setExamples(toStream(dto.getExamples()).
                        map(this::toWordExample).
                        toList()).
                build();
    }

    public Pageable toPageableForDictionaryWords(int page, int size, String sort) {
        size = Math.min(configData.maxPageSize(), size);
        size = Math.max(configData.minPageSize(), size);

        return PageRequest.of(
                page,
                size,
                sortRules.toSort(sort, SortedEntity.WORD)
        );
    }


    public ExpressionResponse toExpressionResponse(Expression expression) {
        return new ExpressionResponse().
                setExpressionId(expression.getId()).
                setUserId(expression.getUserId()).
                setValue(expression.getValue()).
                setNote(expression.getNote()).
                setInterpretations(expression.getInterpretations().stream().
                        map(this::toInterpretationRequestResponse).
                        toList()).
                setTranslates(expression.getTranslations().stream().
                        map(this::toTranslateRequestResponse).
                        toList()).
                setExamples(expression.getExamples().stream().
                        map(this::toExampleRequestResponse).
                        toList());
    }

    public Page<ExpressionForDictionaryListResponse> toExpressionForDictionaryListResponse(Page<Expression> expressions) {
        return expressions.map(
                expression -> new ExpressionForDictionaryListResponse().
                        setUserId(expression.getUserId()).
                        setExpressionId(expression.getId()).
                        setValue(expression.getValue()).
                        setHotRepeat(expressionService.isHotRepeat(expression))
        );
    }

    public Page<ExpressionForRepetitionResponse> toExpressionForRepetitionResponse(Page<Expression> expressions) {
        return expressions.map(
                expression -> new ExpressionForRepetitionResponse().
                        setExpressionId(expression.getId()).
                        setUserId(expression.getUserId()).
                        setValue(expression.getValue()).
                        setExamples(expression.getExamples().stream().
                                map(ExpressionExample::getOrigin).
                                toList())
        );
    }

    public Expression toExpression(ExpressionAddRequest dto) {
        return Expression.newBuilder(validator).
                setUserId(dto.getUserID()).
                setValue(dto.getValue()).
                setNote(dto.getNote()).
                setInterpretations(toStream(dto.getInterpretations()).
                        map(this::toExpressionInterpretation).
                        toList()).
                setTranslations(toStream(dto.getTranslates()).
                        map(this::toExpressionTranslation).
                        toList()).
                setExamples(toStream(dto.getExamples()).
                        map(this::toExpressionExample).
                        toList()).
                setRepeatData(expressionService.initialRepeatData(dto.getUserID())).
                build();
    }

    public Expression toExpression(ExpressionUpdateRequest dto) {
        return expressionService.tryFindById(dto.getUserId(), dto.getExpressionId()).builder().
                setValue(dto.getValue()).
                setNote(dto.getNote()).
                setInterpretations(toStream(dto.getInterpretations()).
                        map(this::toExpressionInterpretation).
                        toList()).
                setTranslations(toStream(dto.getTranslates()).
                        map(this::toExpressionTranslation).
                        toList()).
                setExamples(toStream(dto.getExamples()).
                        map(this::toExpressionExample).
                        toList()).
                build();
    }

    public Pageable toPageableForDictionaryExpressions(int page, int size, String sort) {
        size = Math.min(configData.maxPageSize(), size);
        size = Math.max(configData.minPageSize(), size);

        return PageRequest.of(
                page,
                size,
                sortRules.toSort(sort, SortedEntity.EXPRESSION)
        );
    }


    public ExceptionResponse toExceptionResponse(HttpStatus httpStatus, String... messageKeys) {
        ExceptionResponse response = new ExceptionResponse(httpStatus, clock);
        Arrays.stream(messageKeys).forEach(message -> response.addReason(new ExceptionReasonResponse(message)));
        return response;
    }

    public ExceptionResponse toExceptionResponse(HttpStatus httpStatus, ConstraintViolationException exception) {
        ExceptionResponse response = new ExceptionResponse(httpStatus, clock);
        exception.getConstraintViolations().
                forEach(constraint -> response.addReason(new ExceptionReasonResponse(constraint.getMessage())));
        return response;
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


    private ExampleRequestResponse toExampleRequestResponse(ExpressionExample example) {
        return new ExampleRequestResponse().
                setOrigin(example.getOrigin()).
                setTranslate(example.getTranslate()).
                setNote(example.getNote());
    }

    private InterpretationRequestResponse toInterpretationRequestResponse(ExpressionInterpretation interpretation) {
        return new InterpretationRequestResponse().
                setValue(interpretation.getValue());
    }

    private TranslateRequestResponse toTranslateRequestResponse(ExpressionTranslation translation) {
        return new TranslateRequestResponse().
                setNote(translation.getNote()).
                setValue(translation.getValue());
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


    private ExpressionInterpretation toExpressionInterpretation(InterpretationRequestResponse dto) {
        return new ExpressionInterpretation(dto.getValue());
    }

    private ExpressionTranslation toExpressionTranslation(TranslateRequestResponse dto) {
        return new ExpressionTranslation(dto.getValue(), dto.getNote());
    }

    private ExpressionExample toExpressionExample(ExampleRequestResponse dto) {
        return new ExpressionExample(dto.getOrigin(), dto.getTranslate(), dto.getNote());
    }


    private <T> Stream<T> toStream(Collection<T> collection) {
        return collection == null ? Stream.empty() : collection.stream();
    }

}
