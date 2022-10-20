package com.bakuard.flashcards.controller;

import com.bakuard.flashcards.config.security.RequestContext;
import com.bakuard.flashcards.controller.message.Messages;
import com.bakuard.flashcards.dto.DtoMapper;
import com.bakuard.flashcards.dto.exceptions.ExceptionResponse;
import com.bakuard.flashcards.dto.statistic.ExpressionRepetitionByPeriodResponse;
import com.bakuard.flashcards.dto.statistic.WordRepetitionByPeriodResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/statistic")
public class StatisticController {

    private static final Logger logger = LoggerFactory.getLogger(StatisticController.class.getName());


    private DtoMapper mapper;
    private RequestContext requestContext;
    private Messages messages;

    @Operation(summary = "Возвращает статистику о результатах повторения указанного слова за указанный период.",
            responses = {
                    @ApiResponse(responseCode = "200"),
                    @ApiResponse(responseCode = "400",
                            description = "Если нарушен хотя бы один из инвариантов связаный с параметрами запроса.",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = ExceptionResponse.class))),
                    @ApiResponse(responseCode = "401",
                            description = "Если передан некорректный токен или токен не указан.",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = ExceptionResponse.class))),
                    @ApiResponse(responseCode = "404",
                            description = "Если не удалось найти слово или пользователя по указанным идентификаторам.",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = ExceptionResponse.class)))
            }
    )
    @GetMapping("/words/id")
    public ResponseEntity<WordRepetitionByPeriodResponse> findStatisticForWordRepetition(
            @RequestParam
            @Parameter(description = "Идентификатор пользователя", required = true)
            UUID userId,
            @RequestParam
            @Parameter(description = "Идентификатор слова.", required = true)
            UUID wordId,
            @RequestParam
            @Parameter(description = """
                    Начало периода, за который собирается статистика. Значение не должно превышать
                     значения параметра endDate.
                    """)
            String startDate,
            @RequestParam
            @Parameter(description = "Конец периода, за который собирается статистика.")
            String endDate) {
        return null;
    }

    @Operation(summary = "Возвращает статистику о результатах повторения всех слов за указанный период.",
            responses = {
                    @ApiResponse(responseCode = "200"),
                    @ApiResponse(responseCode = "400",
                            description = "Если нарушен хотя бы один из инвариантов связаный с параметрами запроса.",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = ExceptionResponse.class))),
                    @ApiResponse(responseCode = "401",
                            description = "Если передан некорректный токен или токен не указан.",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = ExceptionResponse.class))),
                    @ApiResponse(responseCode = "404",
                            description = "Если не удалось найти пользователя по указанному идентификаторам.",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = ExceptionResponse.class)))
            }
    )
    @GetMapping("/words")
    public ResponseEntity<Page<WordRepetitionByPeriodResponse>> findStatisticForWordsRepetition(
            @RequestParam
            @Parameter(description = "Идентификатор пользователя", required = true)
            UUID userId,
            @RequestParam
            @Parameter(description = """
                    Начало периода, за который собирается статистика. Значение не должно превышать
                     значения параметра endDate.
                    """)
            String startDate,
            @RequestParam
            @Parameter(description = "Конец периода, за который собирается статистика.")
            String endDate,
            @RequestParam("page")
            @Parameter(description = "Номер страницы выборки. Нумерация начинается с нуля.", required = true)
            int page,
            @RequestParam(value = "size", required = false)
            @Parameter(description = "Размер страницы выборки. Диапозон значений - [1, 100].",
                    schema = @Schema(defaultValue = "20"))
            int size,
            @RequestParam(value = "sort", required = false)
            @Parameter(description = "Порядок сортировки.",
                    schema = @Schema(
                            defaultValue = "rememberFromEnglish.asc (Сортировка по кол-ву успешных повторений с английского).",
                            allowableValues = {
                                    "rememberFromEnglish - сортировка по кол-ву успешных повторений с английского",
                                    "rememberFromNative - сортировка по кол-ву успешных повторений с родного языка",
                                    "notRememberFromEnglish - сортировка по кол-ву не успешных повторений с английского",
                                    "notRememberFromNative - сортировка по кол-ву не успешных повторений с родного языка"
                            }
                    ))
            String sort) {
        return null;
    }

    @Operation(summary = "Возвращает статистику о результатах повторения указанного выражения за указанный период.",
            responses = {
                    @ApiResponse(responseCode = "200"),
                    @ApiResponse(responseCode = "400",
                            description = "Если нарушен хотя бы один из инвариантов связаный с параметрами запроса.",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = ExceptionResponse.class))),
                    @ApiResponse(responseCode = "401",
                            description = "Если передан некорректный токен или токен не указан.",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = ExceptionResponse.class))),
                    @ApiResponse(responseCode = "404",
                            description = "Если не удалось найти выражение или пользователя по указанным идентификаторам.",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = ExceptionResponse.class)))
            }
    )
    @GetMapping("/expressions/id")
    public ResponseEntity<ExpressionRepetitionByPeriodResponse> findStatisticForExpressionRepetition(
            @RequestParam
            @Parameter(description = "Идентификатор пользователя", required = true)
            UUID userId,
            @RequestParam
            @Parameter(description = "Идентификатор выражения.", required = true)
            UUID expressionId,
            @RequestParam
            @Parameter(description = """
                    Начало периода, за который собирается статистика. Значение не должно превышать
                     значения параметра endDate.
                    """)
            String startDate,
            @RequestParam
            @Parameter(description = "Конец периода, за который собирается статистика.")
            String endDate) {
        return null;
    }

    @Operation(summary = "Возвращает статистику о результатах повторения всех выражений за указанный период.",
            responses = {
                    @ApiResponse(responseCode = "200"),
                    @ApiResponse(responseCode = "400",
                            description = "Если нарушен хотя бы один из инвариантов связаный с параметрами запроса.",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = ExceptionResponse.class))),
                    @ApiResponse(responseCode = "401",
                            description = "Если передан некорректный токен или токен не указан.",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = ExceptionResponse.class))),
                    @ApiResponse(responseCode = "404",
                            description = "Если не удалось найти пользователя по указанному идентификаторам.",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = ExceptionResponse.class)))
            }
    )
    @GetMapping("/expressions")
    public ResponseEntity<Page<WordRepetitionByPeriodResponse>> findStatisticForExpressionsRepetition(
            @RequestParam
            @Parameter(description = "Идентификатор пользователя", required = true)
            UUID userId,
            @RequestParam
            @Parameter(description = """
                    Начало периода, за который собирается статистика. Значение не должно превышать
                     значения параметра endDate.
                    """)
            String startDate,
            @RequestParam
            @Parameter(description = "Конец периода, за который собирается статистика.")
            String endDate,
            @RequestParam("page")
            @Parameter(description = "Номер страницы выборки. Нумерация начинается с нуля.", required = true)
            int page,
            @RequestParam(value = "size", required = false)
            @Parameter(description = "Размер страницы выборки. Диапозон значений - [1, 100].",
                    schema = @Schema(defaultValue = "20"))
            int size,
            @RequestParam(value = "sort", required = false)
            @Parameter(description = "Порядок сортировки.",
                    schema = @Schema(
                            defaultValue = "rememberFromEnglish.asc (Сортировка по кол-ву успешных повторений с английского).",
                            allowableValues = {
                                    "rememberFromEnglish - сортировка по кол-ву успешных повторений с английского",
                                    "rememberFromNative - сортировка по кол-ву успешных повторений с родного языка",
                                    "notRememberFromEnglish - сортировка по кол-ву не успешных повторений с английского",
                                    "notRememberFromNative - сортировка по кол-ву не успешных повторений с родного языка"
                            }
                    ))
            String sort) {
        return null;
    }

}
