package com.bakuard.flashcards.dto;

import com.bakuard.flashcards.config.ConfigData;
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
import com.bakuard.flashcards.service.AuthService;
import com.bakuard.flashcards.service.ExpressionService;
import com.bakuard.flashcards.service.WordService;
import com.bakuard.flashcards.validation.ValidatorUtil;
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
    private AuthService authService;
    private ConfigData configData;
    private SortRules sortRules;
    private ValidatorUtil validator;
    private Clock clock;

    public DtoMapper(WordService wordService,
                     ExpressionService expressionService,
                     AuthService authService,
                     ConfigData configData,
                     SortRules sortRules,
                     ValidatorUtil validator,
                     Clock clock) {
        this.wordService = wordService;
        this.authService = authService;
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
                                map(this::toInterpretationRequestResponse).
                                toList()).
                        setTranslations(word.getTranslations().stream().
                                map(this::toTranslateRequestResponse).
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
                setInitialRepeatData(wordService.getLowestRepeatInterval(dto.getUserID()), clock).
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
                        map(this::toInterpretationRequestResponse).
                        toList()).
                setTranslates(expression.getTranslations().stream().
                        map(this::toTranslateRequestResponse).
                        toList()).
                setExamples(expression.getExamples().stream().
                        map(this::toExampleRequestResponse).
                        toList());
    }

    public Page<ExpressionForDictionaryListResponse> toExpressionsForDictionaryListResponse(Page<Expression> expressions) {
        return expressions.map(
                expression -> new ExpressionForDictionaryListResponse().
                        setUserId(expression.getUserId()).
                        setExpressionId(expression.getId()).
                        setValue(expression.getValue()).
                        setHotRepeat(expressionService.isHotRepeatFromEnglish(expression))
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
                                map(this::toInterpretationRequestResponse).
                                toList()).
                        setTranslations(expression.getTranslations().stream().
                                map(this::toTranslateRequestResponse).
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
                setInitialRepeatData(expressionService.getLowestRepeatInterval(dto.getUserID()), clock).
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
        User user = authService.tryFindById(dto.getUserId());
        user.setEmail(dto.getEmail());
        user.changePassword(dto.getPasswordChangeRequest().getCurrentPassword(),
                dto.getPasswordChangeRequest().getNewPassword());
        user.setRoles(dto.getRoles().stream().map(this::toRole).toList());
        return user;
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
        Arrays.stream(messageKeys).forEach(message -> response.addReason(new ExceptionReasonResponse(message)));
        return response;
    }

    public ExceptionResponse toExceptionResponse(HttpStatus httpStatus, ConstraintViolationException exception) {
        ExceptionResponse response = new ExceptionResponse(httpStatus, clock);
        exception.getConstraintViolations().
                forEach(constraint -> response.addReason(new ExceptionReasonResponse(constraint.getMessage())));
        return response;
    }


    public <T> ResponseMessage<T> toResponseMessage(String message, T body) {
        ResponseMessage<T> response = new ResponseMessage<>();
        response.setMessage(message);
        response.setPayload(body);
        return response;
    }

    public <T> RepetitionResponse<T> toRepetitionResponse(boolean isRemember, T payload) {
        return new RepetitionResponse<T>().
                setRemember(isRemember).
                setPayload(payload);
    }

    public Pageable toPageable(int page, int size) {
        size = Math.min(size, configData.maxPageSize());
        if(size == 0) size = configData.defaultPageSize();
        size = Math.max(configData.minPageSize(), size);

        return PageRequest.of(page, size);
    }

    public Pageable toPageable(int page, int size, Sort sort) {
        size = Math.min(size, configData.maxPageSize());
        if(size == 0) size = configData.defaultPageSize();
        size = Math.max(configData.minPageSize(), size);

        return PageRequest.of(page, size, sort);
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
