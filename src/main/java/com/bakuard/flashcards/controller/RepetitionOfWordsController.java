package com.bakuard.flashcards.controller;

import com.bakuard.flashcards.config.security.RequestContext;
import com.bakuard.flashcards.dto.DtoMapper;
import com.bakuard.flashcards.dto.common.RepetitionResponse;
import com.bakuard.flashcards.dto.exceptions.ExceptionResponse;
import com.bakuard.flashcards.dto.word.*;
import com.bakuard.flashcards.model.RepetitionResult;
import com.bakuard.flashcards.model.word.Word;
import com.bakuard.flashcards.service.StatisticService;
import com.bakuard.flashcards.service.WordService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Tag(name = "Повторение слов пользователя")
@RestController
@RequestMapping("/repetition/words")
public class RepetitionOfWordsController {

    private static final Logger logger = LoggerFactory.getLogger(RepetitionOfWordsController.class.getName());


    private WordService wordService;
    private StatisticService statisticService;
    private DtoMapper mapper;
    private RequestContext requestContext;

    @Autowired
    public RepetitionOfWordsController(WordService wordService,
                                       StatisticService statisticService,
                                       DtoMapper mapper,
                                       RequestContext requestContext) {
        this.wordService = wordService;
        this.statisticService = statisticService;
        this.mapper = mapper;
        this.requestContext = requestContext;
    }

    @Operation(summary = """
            Возвращает часть выборки слов доступных для повторения в текущую дату. Используется
             для повторения слов с английского на родной язык пользователя.
            """,
            responses = {
                    @ApiResponse(responseCode = "200"),
                    @ApiResponse(responseCode = "400",
                            description = "Если нарушен хотя бы один из инвариантов связаный с параметрами запроса",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = ExceptionResponse.class))),
                    @ApiResponse(responseCode = "401",
                            description = "Если передан некорректный токен или токен не указан",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = ExceptionResponse.class))),
                    @ApiResponse(responseCode = "404",
                            description = "Если не удалось найти пользователя с указанным идентификатором.",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = ExceptionResponse.class)))
            }
    )
    @GetMapping("/english")
    public ResponseEntity<Page<WordForRepetitionFromEnglishResponse>> findAllFromEnglishBy(
            @RequestParam
            @Parameter(description = "Идентификатор пользователя, из слов которого формируется выборка для повторения.", required = true)
            UUID userId,
            @RequestParam("page")
            @Parameter(description = "Номер страницы выборки. Нумерация начинается с нуля.", required = true)
            int page,
            @RequestParam(value = "size", required = false)
            @Parameter(description = "Размер страницы выборки. Диапозон значений - [1, 100].",
                    schema = @Schema(defaultValue = "20"))
            int size) {
        UUID jwsUserId = requestContext.getCurrentJwsBodyAs(UUID.class);
        logger.info("user {} find all words from english to native of user {} for repeat by page={}, size={}",
                jwsUserId, userId, page, size);

        Pageable pageable = mapper.toPageable(page, size, mapper.toWordSort(null));
        Page<Word> result = wordService.findAllForRepeatFromEnglish(userId, pageable);

        return ResponseEntity.ok(mapper.toWordsForRepetitionFromEnglishResponse(result));
    }

    @Operation(summary = """
            Отмечает - помнит ли пользователь слово или нет. Используется при повторении слов
             с английского на родной язык пользователя.
            """,
            responses = {
                    @ApiResponse(responseCode = "200"),
                    @ApiResponse(responseCode = "400",
                            description = "Если нарушен хотя бы один из инвариантов связаный с телом запроса",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = ExceptionResponse.class))),
                    @ApiResponse(responseCode = "401",
                            description = "Если передан некорректный токен или токен не указан",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = ExceptionResponse.class))),
                    @ApiResponse(responseCode = "404",
                            description = "Если не удалось найти слово или пользователя по указанным идентификаторам.",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = ExceptionResponse.class)))
            }
    )
    @PutMapping("/english")
    public ResponseEntity<WordResponse> repeatFromEnglish(@RequestBody WordRepeatFromEnglishRequest dto) {
        UUID userId = requestContext.getCurrentJwsBodyAs(UUID.class);
        logger.info("user {} repeat word from english to native {} as user {}. remember is {}",
                userId, dto.getWordId(), dto.getUserId(), dto.isRemember());

        Word word = wordService.repeatFromEnglish(dto.getUserId(), dto.getWordId(), dto.isRemember());
        statisticService.appendWordFromEnglish(dto.getUserId(), dto.getWordId(), dto.isRemember());

        return ResponseEntity.ok(mapper.toWordResponse(word));
    }

    @Operation(summary = """
            Возвращает часть выборки слов доступных для повторения в текущую дату. Используется для
             повторения слов с родного языка пользователя на английский.
            """,
            responses = {
                    @ApiResponse(responseCode = "200"),
                    @ApiResponse(responseCode = "400",
                            description = "Если нарушен хотя бы один из инвариантов связаный с параметрами запроса",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = ExceptionResponse.class))),
                    @ApiResponse(responseCode = "401",
                            description = "Если передан некорректный токен или токен не указан",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = ExceptionResponse.class))),
                    @ApiResponse(responseCode = "404",
                            description = "Если не удалось найти пользователя с указанным идентификатором.",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = ExceptionResponse.class)))
            }
    )
    @GetMapping("/native")
    public ResponseEntity<Page<WordForRepetitionFromNativeResponse>> findAllFromNativeBy(
            @RequestParam
            @Parameter(description = "Идентификатор пользователя, из слов которого формируется выборка для повторения.", required = true)
            UUID userId,
            @RequestParam("page")
            @Parameter(description = "Номер страницы выборки. Нумерация начинается с нуля.", required = true)
            int page,
            @RequestParam(value = "size", required = false)
            @Parameter(description = "Размер страницы выборки. Диапозон значений - [1, 100].",
                    schema = @Schema(defaultValue = "20"))
            int size) {
        UUID jwsUserId = requestContext.getCurrentJwsBodyAs(UUID.class);
        logger.info("user {} find all words from native to english of user {} for repeat by page={}, size={}",
                jwsUserId, userId, page, size);

        Pageable pageable = mapper.toPageable(page, size, mapper.toWordSort(null));
        Page<Word> result = wordService.findAllForRepeatFromNative(userId, pageable);

        return ResponseEntity.ok(mapper.toWordsForRepetitionFromNativeResponse(result));
    }

    @Operation(summary = """
            Отмечает - помнит ли пользователь слово или нет. Используется при повторении слов
             с родного языка пользователя на английский.
            """,
            responses = {
                    @ApiResponse(responseCode = "200"),
                    @ApiResponse(responseCode = "400",
                            description = "Если нарушен хотя бы один из инвариантов связаный с телом запроса",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = ExceptionResponse.class))),
                    @ApiResponse(responseCode = "401",
                            description = "Если передан некорректный токен или токен не указан",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = ExceptionResponse.class))),
                    @ApiResponse(responseCode = "404",
                            description = "Если не удалось найти слово или пользователя по указанным идентификаторам.",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = ExceptionResponse.class)))
            }
    )
    @PutMapping("/native")
    public ResponseEntity<RepetitionResponse<WordResponse>> repeatFromNative(@RequestBody WordRepeatFromNativeRequest dto) {
        UUID userId = requestContext.getCurrentJwsBodyAs(UUID.class);
        logger.info("user {} repeat word from native to english {} as user {}. inputTranslate is {}",
                userId, dto.getWordId(), dto.getUserId(), dto.getInputValue());

        RepetitionResult<Word> repetitionResult =
                wordService.repeatFromNative(dto.getUserId(), dto.getWordId(), dto.getInputValue());
        statisticService.appendWordFromNative(dto.getUserId(), dto.getWordId(), repetitionResult.isRemember());

        RepetitionResponse<WordResponse> response = mapper.toRepetitionResponse(
                repetitionResult.isRemember(),
                mapper.toWordResponse(repetitionResult.payload())
        );
        return ResponseEntity.ok(response);
    }

    @Operation(summary = """
            Сбрасывает интервал повторения с английского языка для указанного слова на наименьший
             и устанавливает в качестве даты повторения текущую дату. Этот запрос удобен в тех случаях,
             когда пользователь обнаружил, что забыл слово, и хочет немедленно отметить его для скорейшего
             повторения, но ближайшая дата повторения этого слова не совпадает с текущей.
            """,
            responses = {
                    @ApiResponse(responseCode = "200"),
                    @ApiResponse(responseCode = "400",
                            description = "Если нарушен хотя бы один из инвариантов связаный с телом запроса",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = ExceptionResponse.class))),
                    @ApiResponse(responseCode = "401",
                            description = "Если передан некорректный токен или токен не указан",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = ExceptionResponse.class))),
                    @ApiResponse(responseCode = "404",
                            description = "Если не удалось найти слово или пользователя по указанным идентификаторам.",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = ExceptionResponse.class)))
            }
    )
    @PutMapping("/english/markForRepetition")
    public ResponseEntity<WordResponse> markForRepetitionFromEnglish(WordMarkForRepetitionRequest dto) {
        UUID userId = requestContext.getCurrentJwsBodyAs(UUID.class);
        logger.info("user {} mark word {} of user {} for repetition from english.",
                userId, dto.getWordId(), dto.getUserId());

        Word word = wordService.markForRepetitionFromEnglish(dto.getUserId(), dto.getWordId());

        return ResponseEntity.ok(mapper.toWordResponse(word));
    }

    @Operation(summary = """
            Сбрасывает интервал повторения с родного языка пользователя для указанного слова на наименьший
             и устанавливает в качестве даты повторения текущую дату. Этот запрос удобен в тех случаях,
             когда пользователь обнаружил, что забыл слово, и хочет немедленно отметить его для скорейшего
             повторения, но ближайшая дата повторения этого слова не совпадает с текущей.
            """,
            responses = {
                    @ApiResponse(responseCode = "200"),
                    @ApiResponse(responseCode = "400",
                            description = "Если нарушен хотя бы один из инвариантов связаный с телом запроса",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = ExceptionResponse.class))),
                    @ApiResponse(responseCode = "401",
                            description = "Если передан некорректный токен или токен не указан",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = ExceptionResponse.class))),
                    @ApiResponse(responseCode = "404",
                            description = "Если не удалось найти слово или пользователя по указанным идентификаторам.",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = ExceptionResponse.class)))
            }
    )
    @PutMapping("/native/markForRepetition")
    public ResponseEntity<WordResponse> markForRepetitionFromNative(WordMarkForRepetitionRequest dto) {
        UUID userId = requestContext.getCurrentJwsBodyAs(UUID.class);
        logger.info("user {} mark word {} of user {} for repetition from native.",
                userId, dto.getWordId(), dto.getUserId());

        Word word = wordService.markForRepetitionFromNative(dto.getUserId(), dto.getWordId());

        return ResponseEntity.ok(mapper.toWordResponse(word));
    }

}
