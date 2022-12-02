package com.bakuard.flashcards.controller;

import com.bakuard.flashcards.config.security.RequestContext;
import com.bakuard.flashcards.dto.DtoMapper;
import com.bakuard.flashcards.dto.common.RepetitionResponse;
import com.bakuard.flashcards.dto.exceptions.ExceptionResponse;
import com.bakuard.flashcards.dto.expression.*;
import com.bakuard.flashcards.model.RepetitionResult;
import com.bakuard.flashcards.model.auth.policy.Authorizer;
import com.bakuard.flashcards.model.expression.Expression;
import com.bakuard.flashcards.service.ExpressionService;
import com.bakuard.flashcards.service.StatisticService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
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
@SecurityRequirement(name = "JWTScheme")
@RestController
@RequestMapping("/repetition/expressions")
public class RepetitionOfExpressionsController {

    private static final Logger logger = LoggerFactory.getLogger(RepetitionOfExpressionsController.class.getName());


    private ExpressionService expressionService;
    private StatisticService statisticService;
    private DtoMapper mapper;
    private RequestContext requestContext;
    private Authorizer authorizer;

    @Autowired
    public RepetitionOfExpressionsController(ExpressionService expressionService,
                                             StatisticService statisticService,
                                             DtoMapper mapper,
                                             RequestContext requestContext,
                                             Authorizer authorizer) {
        this.expressionService = expressionService;
        this.statisticService = statisticService;
        this.mapper = mapper;
        this.requestContext = requestContext;
        this.authorizer = authorizer;
    }

    @Operation(summary = """
            Возвращает часть выборки устойчевых выражений доступных для повторения в текущую дату.
             Используется для повторения слов с английского на родной язык пользователя.
            """)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200"),
            @ApiResponse(responseCode = "400",
                    description = "Если нарушен хотя бы один из инвариантов связаный с параметрами запроса",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ExceptionResponse.class))),
            @ApiResponse(responseCode = "401",
                    description = "Если передан некорректный токен или токен не указан",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ExceptionResponse.class))),
            @ApiResponse(responseCode = "403",
                    description = """
                            Если недостаточно прав для выполнения этой операции. Для выполнения этой
                             операции необходимо одно из следующих прав и привелегий:<br/>
                            <ol>
                                <li>Иметь роль супер администратора.</li>
                                <li>Вы должны быть пользователем, над данными которого выполняется эта операция.</li>
                            </ol>
                            """,
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ExceptionResponse.class))),
            @ApiResponse(responseCode = "404",
                    description = "Если не удалось найти пользователя с указанным идентификатором.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ExceptionResponse.class)))
    })
    @GetMapping("/english")
    public ResponseEntity<Page<ExpressionForRepetitionFromEnglishResponse>> findAllFromEnglishBy(
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
        authorizer.assertToHasAccess(jwsUserId, "repetition", userId, "findAllFromEnglishBy");

        Pageable pageable = mapper.toPageable(page, size, mapper.toExpressionSort(null));
        Page<Expression> result = expressionService.findAllForRepeatFromEnglish(userId, pageable);

        return ResponseEntity.ok(mapper.toExpressionsForRepetitionFromEnglishResponse(result));
    }

    @Operation(summary = """
            Отмечает - помнит ли пользователь устойчевое выражение или нет. Используется при
             повторении слов с английского на родной язык пользователя.
            """)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200"),
            @ApiResponse(responseCode = "400",
                    description = "Если нарушен хотя бы один из инвариантов связаный с телом запроса",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ExceptionResponse.class))),
            @ApiResponse(responseCode = "401",
                    description = "Если передан некорректный токен или токен не указан",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ExceptionResponse.class))),
            @ApiResponse(responseCode = "403",
                    description = """
                            Если недостаточно прав для выполнения этой операции. Для выполнения этой
                             операции необходимо одно из следующих прав и привелегий:<br/>
                            <ol>
                                <li>Иметь роль супер администратора.</li>
                                <li>Вы должны быть пользователем, над данными которого выполняется эта операция.</li>
                            </ol>
                            """,
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ExceptionResponse.class))),
            @ApiResponse(responseCode = "404",
                    description = "Если не удалось найти выражение или пользователя по указанным идентификаторам.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ExceptionResponse.class)))
    })
    @PutMapping("/english")
    public ResponseEntity<ExpressionResponse> repeatFromEnglish(
            @RequestBody ExpressionRepeatFromEnglishRequest dto) {
        UUID userId = requestContext.getCurrentJwsBodyAs(UUID.class);
        logger.info("user {} repeat expression from english to native {} as user {}. remember is {}",
                userId, dto.getExpressionId(), dto.getUserId(), dto.isRemember());
        authorizer.assertToHasAccess(userId, "repetition", dto.getUserId(), "repeatFromEnglish");

        Expression expression = expressionService.repeatFromEnglish(dto.getUserId(), dto.getExpressionId(), dto.isRemember());
        statisticService.appendExpressionFromEnglish(dto.getUserId(), dto.getExpressionId(), dto.isRemember());

        return ResponseEntity.ok(mapper.toExpressionResponse(expression));
    }

    @Operation(summary = """
            Возвращает часть выборки выражений доступных для повторения в текущую дату. Используется для
             повторения выражений с родного языка пользователя на английский.
            """)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200"),
            @ApiResponse(responseCode = "400",
                    description = "Если нарушен хотя бы один из инвариантов связаный с параметрами запроса",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ExceptionResponse.class))),
            @ApiResponse(responseCode = "401",
                    description = "Если передан некорректный токен или токен не указан",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ExceptionResponse.class))),
            @ApiResponse(responseCode = "403",
                    description = """
                            Если недостаточно прав для выполнения этой операции. Для выполнения этой
                             операции необходимо одно из следующих прав и привелегий:<br/>
                            <ol>
                                <li>Иметь роль супер администратора.</li>
                                <li>Вы должны быть пользователем, над данными которого выполняется эта операция.</li>
                            </ol>
                            """,
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ExceptionResponse.class))),
            @ApiResponse(responseCode = "404",
                    description = "Если не удалось найти пользователя с указанным идентификатором.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ExceptionResponse.class)))
    })
    @GetMapping("/native")
    public ResponseEntity<Page<ExpressionForRepetitionFromNativeResponse>> findAllFromNativeBy(
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
        authorizer.assertToHasAccess(jwsUserId, "repetition", userId, "findAllFromNativeBy");

        Pageable pageable = mapper.toPageable(page, size, mapper.toExpressionSort(null));
        Page<Expression> result = expressionService.findAllForRepeatFromNative(userId, pageable);

        return ResponseEntity.ok(mapper.toExpressionForRepetitionFromNativeResponse(result));
    }

    @Operation(summary = """
            Отмечает - помнит ли пользователь выражение или нет. Используется при повторении выражений
             с родного языка пользователя на английский.
            """)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200"),
            @ApiResponse(responseCode = "400",
                    description = "Если нарушен хотя бы один из инвариантов связаный с телом запроса",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ExceptionResponse.class))),
            @ApiResponse(responseCode = "401",
                    description = "Если передан некорректный токен или токен не указан",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ExceptionResponse.class))),
            @ApiResponse(responseCode = "403",
                    description = """
                            Если недостаточно прав для выполнения этой операции. Для выполнения этой
                             операции необходимо одно из следующих прав и привелегий:<br/>
                            <ol>
                                <li>Иметь роль супер администратора.</li>
                                <li>Вы должны быть пользователем, над данными которого выполняется эта операция.</li>
                            </ol>
                            """,
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ExceptionResponse.class))),
            @ApiResponse(responseCode = "404",
                    description = "Если не удалось найти выражение или пользователя по указанным идентификаторам.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ExceptionResponse.class)))
    })
    @PutMapping("/native")
    public ResponseEntity<RepetitionResponse<ExpressionResponse>> repeatFromNative(
            @RequestBody ExpressionRepeatFromNativeRequest dto) {
        UUID userId = requestContext.getCurrentJwsBodyAs(UUID.class);
        logger.info("user {} repeat expression from english to native {} as user {}. inputValue is {}",
                userId, dto.getExpressionId(), dto.getUserId(), dto.getInputValue());
        authorizer.assertToHasAccess(userId, "repetition", dto.getUserId(), "repeatFromNative");

        RepetitionResult<Expression> repetitionResult =
                expressionService.repeatFromNative(dto.getUserId(), dto.getExpressionId(), dto.getInputValue());
        statisticService.appendExpressionFromNative(dto.getUserId(), dto.getExpressionId(), repetitionResult.isRemember());

        RepetitionResponse<ExpressionResponse> response = mapper.toRepetitionResponse(
                repetitionResult.isRemember(),
                mapper.toExpressionResponse(repetitionResult.payload())
        );
        return ResponseEntity.ok(response);
    }

    @Operation(summary = """
            Сбрасывает интервал повторения с английского языка для указанного выражения на наименьший
             и устанавливает в качестве даты повторения текущую дату. Этот запрос удобен в тех случаях,
             когда пользователь обнаружил, что забыл выражение, и хочет немедленно отметить его для скорейшего
             повторения, но ближайшая дата повторения этого выражения не совпадает с текущей.
            """)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200"),
            @ApiResponse(responseCode = "400",
                    description = "Если нарушен хотя бы один из инвариантов связаный с телом запроса",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ExceptionResponse.class))),
            @ApiResponse(responseCode = "401",
                    description = "Если передан некорректный токен или токен не указан",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ExceptionResponse.class))),
            @ApiResponse(responseCode = "403",
                    description = """
                            Если недостаточно прав для выполнения этой операции. Для выполнения этой
                             операции необходимо одно из следующих прав и привелегий:<br/>
                            <ol>
                                <li>Иметь роль супер администратора.</li>
                                <li>Вы должны быть пользователем, над данными которого выполняется эта операция.</li>
                            </ol>
                            """,
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ExceptionResponse.class))),
            @ApiResponse(responseCode = "404",
                    description = "Если не удалось найти выражение или пользователя по указанным идентификаторам.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ExceptionResponse.class)))
    })
    @PutMapping("/english/markForRepetition")
    public ResponseEntity<ExpressionResponse> markForRepetitionFromEnglish(ExpressionMarkForRepetitionRequest dto) {
        UUID userId = requestContext.getCurrentJwsBodyAs(UUID.class);
        logger.info("user {} mark expression {} of user {} for repetition from english.",
                userId, dto.getExpressionId(), dto.getUserId());
        authorizer.assertToHasAccess(userId, "repetition", dto.getUserId(), "markForRepetitionFromEnglish");

        Expression expression = expressionService.markForRepetitionFromEnglish(dto.getUserId(), dto.getExpressionId());

        return ResponseEntity.ok(mapper.toExpressionResponse(expression));
    }

    @Operation(summary = """
            Сбрасывает интервал повторения с родного языка пользователя для указанного выражения на наименьший
             и устанавливает в качестве даты повторения текущую дату. Этот запрос удобен в тех случаях,
             когда пользователь обнаружил, что забыл выражение, и хочет немедленно отметить его для скорейшего
             повторения, но ближайшая дата повторения этого выражения не совпадает с текущей.
            """)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200"),
            @ApiResponse(responseCode = "400",
                    description = "Если нарушен хотя бы один из инвариантов связаный с телом запроса",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ExceptionResponse.class))),
            @ApiResponse(responseCode = "401",
                    description = "Если передан некорректный токен или токен не указан",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ExceptionResponse.class))),
            @ApiResponse(responseCode = "403",
                    description = """
                            Если недостаточно прав для выполнения этой операции. Для выполнения этой
                             операции необходимо одно из следующих прав и привелегий:<br/>
                            <ol>
                                <li>Иметь роль супер администратора.</li>
                                <li>Вы должны быть пользователем, над данными которого выполняется эта операция.</li>
                            </ol>
                            """,
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ExceptionResponse.class))),
            @ApiResponse(responseCode = "404",
                    description = "Если не удалось найти выражение или пользователя по указанным идентификаторам.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ExceptionResponse.class)))
    })
    @PutMapping("/native/markForRepetition")
    public ResponseEntity<ExpressionResponse> markForRepetitionFromNative(ExpressionMarkForRepetitionRequest dto) {
        UUID userId = requestContext.getCurrentJwsBodyAs(UUID.class);
        logger.info("user {} mark expression {} of user {} for repetition from native.",
                userId, dto.getExpressionId(), dto.getUserId());
        authorizer.assertToHasAccess(userId, "repetition", dto.getUserId(), "markForRepetitionFromNative");

        Expression expression = expressionService.markForRepetitionFromNative(dto.getUserId(), dto.getExpressionId());

        return ResponseEntity.ok(mapper.toExpressionResponse(expression));
    }

}
