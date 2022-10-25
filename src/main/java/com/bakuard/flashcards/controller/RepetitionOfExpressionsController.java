package com.bakuard.flashcards.controller;

import com.bakuard.flashcards.config.security.RequestContext;
import com.bakuard.flashcards.dto.DtoMapper;
import com.bakuard.flashcards.dto.common.RepetitionResponse;
import com.bakuard.flashcards.dto.exceptions.ExceptionResponse;
import com.bakuard.flashcards.dto.expression.*;
import com.bakuard.flashcards.model.RepetitionResult;
import com.bakuard.flashcards.model.expression.Expression;
import com.bakuard.flashcards.service.ExpressionService;
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

@Tag(name = "Повторение устойчевых выражений пользователя")
@RestController
@RequestMapping("/repetition/expressions")
public class RepetitionOfExpressionsController {

    private static final Logger logger = LoggerFactory.getLogger(RepetitionOfExpressionsController.class.getName());


    private ExpressionService expressionService;
    private DtoMapper mapper;
    private RequestContext requestContext;

    @Autowired
    public RepetitionOfExpressionsController(ExpressionService expressionService,
                                             DtoMapper mapper,
                                             RequestContext requestContext) {
        this.expressionService = expressionService;
        this.mapper = mapper;
        this.requestContext = requestContext;
    }

    @Operation(summary = """
            Возвращает часть выборки устойчевых выражений доступных для повторения в текущую дату.
             Используется для повторения слов с английского на родной язык пользователя.
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
    public ResponseEntity<Page<ExpressionForRepetitionEnglishToNativeResponse>> findAllEnglishToNativeBy(
            @RequestParam
            @Parameter(description = "Идентификатор пользователя, из выражений которого формируется выборка для повторения.", required = true)
            UUID userId,
            @RequestParam("page")
            @Parameter(description = "Номер страницы выборки. Нумерация начинается с нуля.", required = true)
            int page,
            @RequestParam(value = "size", required = false)
            @Parameter(description = "Размер страницы выборки. Диапозон значений - [1, 100].",
                    schema = @Schema(defaultValue = "20"))
            int size) {
        UUID jwsUserId = requestContext.getCurrentJwsBodyAs(UUID.class);
        logger.info("user {} find all expressions from english to native of user {} for repeat by page={}, size={}",
                jwsUserId, userId, page, size);

        Pageable pageable = mapper.toPageable(page, size, mapper.toExpressionSort(null));
        Page<Expression> result = expressionService.findAllForRepeatFromEnglish(userId, pageable);

        return ResponseEntity.ok(mapper.toExpressionsForRepetitionFromEnglishResponse(result));
    }

    @Operation(summary = """
            Отмечает - помнит ли пользователь устойчевое выражение или нет. Используется при
             повторении слов с английского на родной язык пользователя.
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
                            description = "Если не удалось найти выражение по указанным id пользователя и самого выражения.",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = ExceptionResponse.class)))
            }
    )
    @PutMapping("/english")
    public ResponseEntity<ExpressionResponse> repeatEnglishToNative(
            @RequestBody ExpressionRepeatEnglishToNativeRequest dto) {
        UUID userId = requestContext.getCurrentJwsBodyAs(UUID.class);
        logger.info("user {} repeat expression from english to native {} as user {}. remember is {}",
                userId, dto.getExpressionId(), dto.getUserId(), dto.isRemember());

        Expression expression = expressionService.repeatFromEnglish(dto.getUserId(), dto.getExpressionId(), dto.isRemember());
        expressionService.save(expression);

        return ResponseEntity.ok(mapper.toExpressionResponse(expression));
    }

    @Operation(summary = """
            Возвращает часть выборки выражений доступных для повторения в текущую дату. Используется для
             повторения выражений с родного языка пользователя на английский.
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
    public ResponseEntity<Page<ExpressionForRepetitionNativeToEnglishResponse>> findAllNativeToEnglishBy(
            @RequestParam
            @Parameter(description = "Идентификатор пользователя, из выражений которого формируется выборка для повторения.", required = true)
            UUID userId,
            @RequestParam("page")
            @Parameter(description = "Номер страницы выборки. Нумерация начинается с нуля.", required = true)
            int page,
            @RequestParam(value = "size", required = false)
            @Parameter(description = "Размер страницы выборки. Диапозон значений - [1, 100].",
                    schema = @Schema(defaultValue = "20"))
            int size) {
        UUID jwsUserId = requestContext.getCurrentJwsBodyAs(UUID.class);
        logger.info("user {} find all expressions from native to english of user {} for repeat by page={}, size={}",
                jwsUserId, userId, page, size);

        Pageable pageable = mapper.toPageable(page, size, mapper.toExpressionSort(null));
        Page<Expression> result = expressionService.findAllForRepeatFromNative(userId, pageable);

        return ResponseEntity.ok(mapper.toExpressionForRepetitionFromNativeResponse(result));
    }

    @Operation(summary = """
            Отмечает - помнит ли пользователь выражение или нет. Используется при повторении выражений
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
                            description = "Если не удалось найти слово по указанным id пользователя и самого слова.",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = ExceptionResponse.class)))
            }
    )
    @PutMapping("/native")
    public ResponseEntity<RepetitionResponse<ExpressionResponse>> repeatNativeToEnglish(
            @RequestBody ExpressionRepeatFromNativeToEnglishRequest dto) {
        UUID userId = requestContext.getCurrentJwsBodyAs(UUID.class);
        logger.info("user {} repeat expression from english to native {} as user {}. inputValue is {}",
                userId, dto.getExpressionId(), dto.getUserId(), dto.getInputValue());

        RepetitionResult<Expression> repetitionResult =
                expressionService.repeatFromNative(dto.getUserId(), dto.getExpressionId(), dto.getInputValue());
        expressionService.save(repetitionResult.payload());

        RepetitionResponse<ExpressionResponse> response = mapper.toRepetitionResponse(
                repetitionResult.isRemember(),
                mapper.toExpressionResponse(repetitionResult.payload())
        );
        return ResponseEntity.ok(response);
    }

}
