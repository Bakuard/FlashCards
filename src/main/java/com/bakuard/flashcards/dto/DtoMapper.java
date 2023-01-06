package com.bakuard.flashcards.dto;

import com.bakuard.flashcards.config.configData.ConfigData;
import com.bakuard.flashcards.controller.message.Messages;
import com.bakuard.flashcards.dto.common.*;
import com.bakuard.flashcards.dto.credential.*;
import com.bakuard.flashcards.dto.exceptions.ExceptionReasonResponse;
import com.bakuard.flashcards.dto.exceptions.ExceptionResponse;
import com.bakuard.flashcards.dto.expression.*;
import com.bakuard.flashcards.dto.settings.IntervalsResponse;
import com.bakuard.flashcards.dto.statistic.ExpressionRepetitionByPeriodResponse;
import com.bakuard.flashcards.dto.statistic.WordRepetitionByPeriodResponse;
import com.bakuard.flashcards.dto.word.*;
import com.bakuard.flashcards.model.auth.JwsWithUser;
import com.bakuard.flashcards.model.auth.credential.Credential;
import com.bakuard.flashcards.model.auth.credential.Role;
import com.bakuard.flashcards.model.auth.credential.User;
import com.bakuard.flashcards.model.expression.Expression;
import com.bakuard.flashcards.model.expression.ExpressionExample;
import com.bakuard.flashcards.model.expression.ExpressionInterpretation;
import com.bakuard.flashcards.model.expression.ExpressionTranslation;
import com.bakuard.flashcards.model.filter.SortRules;
import com.bakuard.flashcards.model.filter.SortedEntity;
import com.bakuard.flashcards.model.statistic.ExpressionRepetitionByPeriodStatistic;
import com.bakuard.flashcards.model.statistic.WordRepetitionByPeriodStatistic;
import com.bakuard.flashcards.model.word.*;
import com.bakuard.flashcards.model.word.supplementation.AggregateSupplementedWord;
import com.bakuard.flashcards.model.word.supplementation.ExampleOuterSource;
import com.bakuard.flashcards.model.word.supplementation.OuterSource;
import com.bakuard.flashcards.service.AuthService;
import com.bakuard.flashcards.service.ExpressionService;
import com.bakuard.flashcards.service.IntervalService;
import com.bakuard.flashcards.service.WordService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;

import javax.validation.ConstraintViolationException;
import java.time.Clock;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

public class DtoMapper {

    private WordService wordService;
    private ExpressionService expressionService;
    private IntervalService intervalService;
    private AuthService authService;
    private ConfigData configData;
    private SortRules sortRules;
    private Clock clock;
    private Messages messages;

    public DtoMapper(WordService wordService,
                     ExpressionService expressionService,
                     IntervalService intervalService,
                     AuthService authService,
                     ConfigData configData,
                     SortRules sortRules,
                     Clock clock,
                     Messages messages) {
        this.wordService = wordService;
        this.authService = authService;
        this.intervalService = intervalService;
        this.expressionService = expressionService;
        this.configData = configData;
        this.sortRules = sortRules;
        this.clock = clock;
        this.messages = messages;
    }

    public WordResponse toWordResponse(Word word) {
        return new WordResponse().
                setWordId(word.getId()).
                setUserId(word.getUserId()).
                setValue(word.getValue()).
                setNote(word.getNote()).
                setTranscriptions(word.getTranscriptions().stream().
                        map(this::toTranscriptionResponse).
                        toList()).
                setInterpretations(word.getInterpretations().stream().
                        map(this::toInterpretationResponse).
                        toList()).
                setTranslates(word.getTranslations().stream().
                        map(this::toTranslateResponse).
                        toList()).
                setExamples(word.getExamples().stream().
                        map(this::toExampleResponse).
                        toList());
    }

    public SupplementedWordResponse toSupplementedWordResponse(AggregateSupplementedWord aggregateWord) {
        return new SupplementedWordResponse().
                setWordId(aggregateWord.getWord().getId()).
                setUserId(aggregateWord.getWord().getUserId()).
                setValue(aggregateWord.getWord().getValue()).
                setNote(aggregateWord.getWord().getNote()).
                setInterpretations(aggregateWord.getInterpretations().stream().
                        map(i -> toSupplementedInterpretationResponse(i, aggregateWord)).
                        toList()).
                setTranscriptions(aggregateWord.getTranscriptions().stream().
                        map(i -> toSupplementedTranscriptionResponse(i, aggregateWord)).
                        toList()).
                setTranslates(aggregateWord.getTranslations().stream().
                        map(i -> toSupplementedTranslateResponse(i, aggregateWord)).
                        toList()).
                setExamples(aggregateWord.getExamples().stream().
                        map(i -> toSupplementedExampleResponse(i, aggregateWord)).
                        toList());
    }

    public Page<WordForDictionaryListResponse> toWordsForDictionaryListResponse(Page<Word> words) {
        return words.map(
                word -> new WordForDictionaryListResponse().
                        setWordId(word.getId()).
                        setUserId(word.getUserId()).
                        setValue(word.getValue()).
                        setHotRepeatFromEnglish(wordService.isHotRepeatFromEnglish(word)).
                        setHotRepeatFromNative(wordService.isHotRepeatFromNative(word))
        );
    }

    public Page<WordForRepetitionFromEnglishResponse> toWordsForRepetitionFromEnglishResponse(Page<Word> words) {
        return words.map(
                word -> new WordForRepetitionFromEnglishResponse().
                        setWordId(word.getId()).
                        setUserId(word.getUserId()).
                        setValue(word.getValue()).
                        setExamples(word.getExamples().stream().
                                map(WordExample::getOrigin).
                                toList())
        );
    }

    public Page<WordForRepetitionFromNativeResponse> toWordsForRepetitionFromNativeResponse(Page<Word> words) {
        return words.map(
                word -> new WordForRepetitionFromNativeResponse().
                        setWordId(word.getId()).
                        setUserId(word.getUserId()).
                        setInterpretations(word.getInterpretations().stream().
                                map(this::toInterpretationResponse).
                                toList()).
                        setTranslations(word.getTranslations().stream().
                                map(this::toTranslateResponse).
                                toList())
        );
    }

    public Word toWord(WordAddRequest dto) {
        int lowestInterval = intervalService.getLowestInterval(dto.getUserId());
        return new Word(dto.getUserId(), lowestInterval, lowestInterval, clock).
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
                        toList());
    }

    public Word toWord(WordUpdateRequest dto) {
        return wordService.tryFindById(dto.getUserId(), dto.getWordId()).
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
                        toList());
    }

    public Sort toWordSort(String sortRule) {
        return sortRules.toSort(sortRule, SortedEntity.WORD);
    }


    public ExpressionResponse toExpressionResponse(Expression expression) {
        return new ExpressionResponse().
                setExpressionId(expression.getId()).
                setUserId(expression.getUserId()).
                setValue(expression.getValue()).
                setNote(expression.getNote()).
                setInterpretations(expression.getInterpretations().stream().
                        map(this::toInterpretationResponse).
                        toList()).
                setTranslates(expression.getTranslations().stream().
                        map(this::toTranslateResponse).
                        toList()).
                setExamples(expression.getExamples().stream().
                        map(this::toExampleResponse).
                        toList());
    }

    public Page<ExpressionForDictionaryListResponse> toExpressionsForDictionaryListResponse(Page<Expression> expressions) {
        return expressions.map(
                expression -> new ExpressionForDictionaryListResponse().
                        setUserId(expression.getUserId()).
                        setExpressionId(expression.getId()).
                        setValue(expression.getValue()).
                        setHotRepeatFromEnglish(expressionService.isHotRepeatFromEnglish(expression)).
                        setHotRepeatFromNative(expressionService.isHotRepeatFromNative(expression))
        );
    }

    public Page<ExpressionForRepetitionFromEnglishResponse> toExpressionsForRepetitionFromEnglishResponse(Page<Expression> expressions) {
        return expressions.map(
                expression -> new ExpressionForRepetitionFromEnglishResponse().
                        setExpressionId(expression.getId()).
                        setUserId(expression.getUserId()).
                        setValue(expression.getValue()).
                        setExamples(expression.getExamples().stream().
                                map(ExpressionExample::getOrigin).
                                toList())
        );
    }

    public Page<ExpressionForRepetitionFromNativeResponse> toExpressionForRepetitionFromNativeResponse(Page<Expression> expressions) {
        return expressions.map(
                expression -> new ExpressionForRepetitionFromNativeResponse().
                        setExpressionId(expression.getId()).
                        setUserId(expression.getUserId()).
                        setInterpretations(expression.getInterpretations().stream().
                                map(this::toInterpretationResponse).
                                toList()).
                        setTranslations(expression.getTranslations().stream().
                                map(this::toTranslateResponse).
                                toList())
        );
    }

    public Expression toExpression(ExpressionAddRequest dto) {
        int lowestInterval = intervalService.getLowestInterval(dto.getUserID());
        return new Expression(dto.getUserID(), lowestInterval, lowestInterval, clock).
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
                        toList());
    }

    public Expression toExpression(ExpressionUpdateRequest dto) {
        return expressionService.tryFindById(dto.getUserId(), dto.getExpressionId()).
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
                        toList());
    }

    public Sort toExpressionSort(String sortRule) {
        return sortRules.toSort(sortRule, SortedEntity.EXPRESSION);
    }


    public Credential toCredential(UserEnterRequest dto) {
        return new Credential(dto.getEmail(), dto.getPassword());
    }

    public Credential toCredential(UserAddRequest dto) {
        return new Credential(dto.getEmail(), dto.getPassword());
    }

    public Credential toCredential(PasswordRestoreRequest dto) {
        return new Credential(dto.getEmail(), dto.getNewPassword());
    }

    public JwsResponse toJwsResponse(JwsWithUser jws) {
        JwsResponse response = new JwsResponse();
        response.setJws(jws.jws());
        response.setUser(toUserResponse(jws.user()));
        return response;
    }

    public User toUser(UserUpdateRequest dto) {
        return authService.tryFindById(dto.getUserId()).
                setEmail(dto.getEmail()).
                setRoles(dto.getRoles().stream().map(this::toRole).toList()).
                changePassword(dto.getPasswordChangeRequest().getCurrentPassword(),
                        dto.getPasswordChangeRequest().getNewPassword());
    }

    public UserResponse toUserResponse(User user) {
        UserResponse response = new UserResponse();
        response.setUserId(user.getId());
        response.setEmail(user.getEmail());
        response.setRoles(user.getRoles().stream().map(this::toUserRoleRequestResponse).toList());
        return response;
    }

    public Page<UserResponse> toUsersResponse(Page<User> users) {
        return users.map(this::toUserResponse);
    }

    public Sort toUserSort(String sortRule) {
        return sortRules.toSort(sortRule, SortedEntity.USER);
    }


    public IntervalsResponse toIntervalsResponse(UUID userId, List<Integer> intervals) {
        return new IntervalsResponse().
                setUserId(userId).
                setIntervals(intervals);
    }


    public WordRepetitionByPeriodResponse toWordRepetitionByPeriodResponse(WordRepetitionByPeriodStatistic statistic) {
        return new WordRepetitionByPeriodResponse().
                setWordId(statistic.wordId()).
                setUserId(statistic.userId()).
                setValue(statistic.value()).
                setNotRememberFromEnglish(statistic.notRememberFromEnglish()).
                setNotRememberFromNative(statistic.notRememberFromNative()).
                setRememberFromEnglish(statistic.rememberFromEnglish()).
                setRememberFromNative(statistic.rememberFromNative()).
                setTotalRepetitionNumbersFromEnglish(statistic.totalRepetitionNumbersFromEnglish()).
                setTotalRepetitionNumbersFromNative(statistic.totalRepetitionNumbersFromNative());
    }

    public ExpressionRepetitionByPeriodResponse toExpressionRepetitionByPeriodResponse(ExpressionRepetitionByPeriodStatistic statistic) {
        return new ExpressionRepetitionByPeriodResponse().
                setUserId(statistic.userId()).
                setExpressionId(statistic.expressionId()).
                setValue(statistic.value()).
                setNotRememberFromEnglish(statistic.notRememberFromEnglish()).
                setNotRememberFromNative(statistic.notRememberFromNative()).
                setRememberFromEnglish(statistic.rememberFromEnglish()).
                setRememberFromNative(statistic.rememberFromNative()).
                setTotalRepetitionNumbersFromEnglish(statistic.totalRepetitionNumbersFromEnglish()).
                setTotalRepetitionNumbersFromNative(statistic.totalRepetitionNumbersFromNative());
    }

    public Page<WordRepetitionByPeriodResponse> toWordsRepetitionByPeriodResponse(Page<WordRepetitionByPeriodStatistic> statistic) {
        return statistic.map(this::toWordRepetitionByPeriodResponse);
    }

    public Page<ExpressionRepetitionByPeriodResponse> toExpressionsRepetitionByPeriodResponse(Page<ExpressionRepetitionByPeriodStatistic> statistic) {
        return statistic.map(this::toExpressionRepetitionByPeriodResponse);
    }

    public Sort toExpressionStatisticSort(String sortRule) {
        return sortRules.toSort(sortRule, SortedEntity.EXPRESSION_STATISTIC);
    }

    public Sort toWordStatisticSort(String sortRule) {
        return sortRules.toSort(sortRule, SortedEntity.WORD_STATISTIC);
    }


    public ExceptionResponse toExceptionResponse(HttpStatus httpStatus, String... messageKeys) {
        ExceptionResponse response = new ExceptionResponse(httpStatus, clock);
        Arrays.stream(messageKeys).
                forEach(messageKey -> {
                    String message = messages.getMessage(messageKey);
                    response.addReason(new ExceptionReasonResponse(message));
                });
        return response;
    }

    public ExceptionResponse toExceptionResponse(HttpStatus httpStatus, ConstraintViolationException exception) {
        ExceptionResponse response = new ExceptionResponse(httpStatus, clock);
        exception.getConstraintViolations().
                forEach(constraint -> {
                    String message = messages.getMessage(constraint.getMessage());
                    response.addReason(new ExceptionReasonResponse(message));
                });
        return response;
    }


    public <T> RepetitionResponse<T> toRepetitionResponse(boolean isRemember, T payload) {
        return new RepetitionResponse<T>().
                setRemember(isRemember).
                setPayload(payload);
    }

    public Pageable toPageable(int page, int size) {
        size = Math.min(size, configData.pagination().maxPageSize());
        if(size == 0) size = configData.pagination().defaultPageSize();
        size = Math.max(configData.pagination().minPageSize(), size);

        return PageRequest.of(page, size);
    }

    public Pageable toPageable(int page, int size, Sort sort) {
        size = Math.min(size, configData.pagination().maxPageSize());
        if(size == 0) size = configData.pagination().defaultPageSize();
        size = Math.max(configData.pagination().minPageSize(), size);

        return PageRequest.of(page, size, sort);
    }


    private ExampleResponse toExampleResponse(WordExample wordExample) {
        return new ExampleResponse().
                setOrigin(wordExample.getOrigin()).
                setTranslate(wordExample.getTranslate()).
                setNote(wordExample.getNote());
    }

    private InterpretationResponse toInterpretationResponse(WordInterpretation wordInterpretation) {
        return new InterpretationResponse().
                setValue(wordInterpretation.getValue());
    }

    private TranscriptionResponse toTranscriptionResponse(WordTranscription wordTranscription) {
        return new TranscriptionResponse().
                setValue(wordTranscription.getValue()).
                setNote(wordTranscription.getNote());
    }

    private TranslateResponse toTranslateResponse(WordTranslation wordTranslation) {
        return new TranslateResponse().
                setValue(wordTranslation.getValue()).
                setNote(wordTranslation.getNote());
    }


    private SupplementedExampleResponse toSupplementedExampleResponse(WordExample example,
                                                                      AggregateSupplementedWord word) {
        return new SupplementedExampleResponse().
                setOrigin(example.getOrigin()).
                setNote(example.getNote()).
                setTranslate(example.getTranslate()).
                setOuterSource(word.getOuterSource(example).stream().
                        map(this::toExampleOuterSourceResponse).
                        toList());
    }

    private SupplementedInterpretationResponse toSupplementedInterpretationResponse(WordInterpretation interpretation,
                                                                                    AggregateSupplementedWord word) {
        return new SupplementedInterpretationResponse().
                setValue(interpretation.getValue()).
                setOuterSource(word.getOuterSource(interpretation).stream().
                        map(this::toOuterSourceResponse).
                        toList());
    }

    private SupplementedTranscriptionResponse toSupplementedTranscriptionResponse(WordTranscription transcription,
                                                                                  AggregateSupplementedWord word) {
        return new SupplementedTranscriptionResponse().
                setValue(transcription.getValue()).
                setNote(transcription.getNote()).
                setOuterSource(word.getOuterSource(transcription).stream().
                        map(this::toOuterSourceResponse).
                        toList());
    }

    private SupplementedTranslateResponse toSupplementedTranslateResponse(WordTranslation translation,
                                                                          AggregateSupplementedWord word) {
        return new SupplementedTranslateResponse().
                setValue(translation.getValue()).
                setNote(translation.getNote()).
                setOuterSource(word.getOuterSource(translation).stream().
                        map(this::toOuterSourceResponse).
                        toList());
    }

    private OuterSourceResponse toOuterSourceResponse(OuterSource outerSource) {
        return new OuterSourceResponse().
                setOuterSourceName(outerSource.name()).
                setOuterSourceUrl(outerSource.uri().toString());
    }

    private ExampleOuterSourceResponse toExampleOuterSourceResponse(ExampleOuterSource exampleOuterSource) {
        return new ExampleOuterSourceResponse().
                setOuterSourceName(exampleOuterSource.sourceName()).
                setOuterSourceUrl(exampleOuterSource.uri().toString()).
                setExampleTranslate(exampleOuterSource.translate());
    }


    private ExampleResponse toExampleResponse(ExpressionExample example) {
        return new ExampleResponse().
                setOrigin(example.getOrigin()).
                setTranslate(example.getTranslate()).
                setNote(example.getNote());
    }

    private InterpretationResponse toInterpretationResponse(ExpressionInterpretation interpretation) {
        return new InterpretationResponse().
                setValue(interpretation.getValue());
    }

    private TranslateResponse toTranslateResponse(ExpressionTranslation translation) {
        return new TranslateResponse().
                setNote(translation.getNote()).
                setValue(translation.getValue());
    }


    private WordTranscription toWordTranscription(TranscriptionRequest dto) {
        return new WordTranscription(dto.getValue(), dto.getNote());
    }

    private WordInterpretation toWordInterpretation(InterpretationRequest dto) {
        return new WordInterpretation(dto.getValue());
    }

    private WordTranslation toWordTranslation(TranslateRequest dto) {
        return new WordTranslation(dto.getValue(), dto.getNote());
    }

    private WordExample toWordExample(ExampleRequest dto) {
        return new WordExample(dto.getOrigin(), dto.getTranslate(), dto.getNote());
    }


    private ExpressionInterpretation toExpressionInterpretation(InterpretationRequest dto) {
        return new ExpressionInterpretation(dto.getValue());
    }

    private ExpressionTranslation toExpressionTranslation(TranslateRequest dto) {
        return new ExpressionTranslation(dto.getValue(), dto.getNote());
    }

    private ExpressionExample toExpressionExample(ExampleRequest dto) {
        return new ExpressionExample(dto.getOrigin(), dto.getTranslate(), dto.getNote());
    }


    private UserRoleRequestResponse toUserRoleRequestResponse(Role role) {
        UserRoleRequestResponse response = new UserRoleRequestResponse();
        response.setName(role.name());
        return response;
    }

    private Role toRole(UserRoleRequestResponse dto) {
        return new Role(dto.getName());
    }


    private <T> Stream<T> toStream(Collection<T> collection) {
        return collection == null ? Stream.empty() : collection.stream();
    }

}
