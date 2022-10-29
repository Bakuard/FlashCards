package com.bakuard.flashcards.controller;

import com.bakuard.flashcards.config.security.RequestContext;
import com.bakuard.flashcards.controller.message.Messages;
import com.bakuard.flashcards.dto.DtoMapper;
import com.bakuard.flashcards.dto.exceptions.ExceptionResponse;
import com.bakuard.flashcards.dto.expression.ExpressionAddRequest;
import com.bakuard.flashcards.dto.expression.ExpressionForDictionaryListResponse;
import com.bakuard.flashcards.dto.expression.ExpressionResponse;
import com.bakuard.flashcards.dto.expression.ExpressionUpdateRequest;
import com.bakuard.flashcards.dto.word.WordForDictionaryListResponse;
import com.bakuard.flashcards.model.expression.Expression;
import com.bakuard.flashcards.service.AuthService;
import com.bakuard.flashcards.service.ExpressionService;
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

@Tag(name = "Словарь устойчевых выражений пользователя")
@SecurityRequirement(name = "JWTScheme")
@RestController
@RequestMapping("/dictionary/expressions")
public class DictionaryOfExpressionsController {

    private static final Logger logger = LoggerFactory.getLogger(DictionaryOfExpressionsController.class.getName());


    private ExpressionService expressionService;
    private AuthService authService;
    private DtoMapper mapper;
    private RequestContext requestContext;
    private Messages messages;

    @Autowired
    public DictionaryOfExpressionsController(ExpressionService expressionService,
                                             AuthService authService,
                                             DtoMapper mapper,
                                             RequestContext requestContext,
                                             Messages messages) {
        this.expressionService = expressionService;
        this.authService = authService;
        this.mapper = mapper;
        this.requestContext = requestContext;
        this.messages = messages;
    }

    @Operation(summary = "Добавляет новое устойчевое выражение в словарь пользователя")
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
            @ApiResponse(responseCode = "404",
                    description = "Если не удалось найти пользователя по указнному id.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ExceptionResponse.class)))
    })
    @PostMapping
    public ResponseEntity<ExpressionResponse> add(@RequestBody ExpressionAddRequest dto) {
        UUID userId = requestContext.getCurrentJwsBodyAs(UUID.class);
        logger.info("user {} add word '{}' fro user {}", userId, dto.getValue(), dto.getUserID());

        Expression expression = mapper.toExpression(dto);
        expression = expressionService.save(expression);
        return ResponseEntity.ok(mapper.toExpressionResponse(expression));
    }

    @Operation(summary = "Обновляет устойчевое выражение в словаре пользователя")
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
            @ApiResponse(responseCode = "404",
                    description = "Если не удалось найти выражение или пользователя по указнным id.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ExceptionResponse.class)))
    })
    @PutMapping
    public ResponseEntity<ExpressionResponse> update(@RequestBody ExpressionUpdateRequest dto) {
        UUID userId = requestContext.getCurrentJwsBodyAs(UUID.class);
        logger.info("user {} update word {} for user {}", userId, dto.getExpressionId(), dto.getUserId());

        Expression expression = mapper.toExpression(dto);
        expression = expressionService.save(expression);
        return ResponseEntity.ok(mapper.toExpressionResponse(expression));
    }

    @Operation(summary = "Возвращает часть выборки устойчевых выражений из словаря пользователя")
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
            @ApiResponse(responseCode = "404",
                    description = "Если не удалось найти пользователя с указанным идентификатором.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ExceptionResponse.class)))
    })
    @GetMapping
    public ResponseEntity<Page<ExpressionForDictionaryListResponse>> findAllBy(
            @RequestParam
            @Parameter(description = "Идентификатор пользователя, из выражений которого делается выборка", required = true)
            UUID userId,
            @RequestParam
            @Parameter(description = "Номер страницы выборки. Нумерация начинается с нуля.", required = true)
            int page,
            @RequestBody(required = false)
            @Parameter(description = "Размер страницы выборки. Диапозон значений - [1, 100].",
                    schema = @Schema(defaultValue = "20"))
            int size,
            @RequestParam(required = false)
            @Parameter(description = "Порядок сортировки.",
                    schema = @Schema(
                            defaultValue = "value.asc (Сортировка по значению в порядке возрастания).",
                            allowableValues = {
                                    "value - сортировка по значению",
                                    "repeat_interval_from_english - сортировка по интервалу повторения для перевода с английского",
                                    "repeat_interval_from_native - сортировка по дате поседнего повторения для переводоа с родного языка",
                                    "last_date_of_repeat_from_english - сортировка по последней дате повторения с английского",
                                    "last_date_of_repeat_from_native - сортировка по последней дате повторения с родного языка"
                            }
                    ))
            String sort) {
        UUID jwsUserId = requestContext.getCurrentJwsBodyAs(UUID.class);
        logger.info("user {} get expressions of user {} by page={}, size={}, sort={}",
                jwsUserId, userId, page, size, sort);

        authService.assertExists(userId);
        Pageable pageable = mapper.toPageable(page, size, mapper.toExpressionSort(sort));
        Page<ExpressionForDictionaryListResponse> result = mapper.toExpressionsForDictionaryListResponse(
                expressionService.findByUserId(userId, pageable)
        );

        return ResponseEntity.ok(result);
    }

    @Operation(summary = "Возвращает устойчевое выражение из словаря пользователя по его идентификатору")
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
            @ApiResponse(responseCode = "404",
                    description = "Если не удалось найти выражение или пользователя по указанным идентификаторам.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ExceptionResponse.class)))
    })
    @GetMapping("/id")
    public ResponseEntity<ExpressionResponse> findById(
            @RequestParam
            @Parameter(description = "Идентификатор пользователя, выражение которого запрашивается", required = true)
            UUID userId,
            @RequestParam
            @Parameter(description = "Уникальный идентификатор устойчевого выражения в формате UUID.", required = true)
            UUID expressionId) {
        UUID jwsUserId = requestContext.getCurrentJwsBodyAs(UUID.class);
        logger.info("user {} get expression of user {} by id={}", jwsUserId, userId, expressionId);

        Expression expression = expressionService.tryFindById(userId, expressionId);
        return ResponseEntity.ok(mapper.toExpressionResponse(expression));
    }

    @Operation(summary = """
            Возвращает устойчевое выражение и/или наиболее похожие по написанию к нему устойчевые выражения.
             Все выражения будут отсортирвоанны в порядке возрастания редакционного расстояние между искомым
             словом, а затем в лексеграфиечском порядке.
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
            @ApiResponse(responseCode = "404",
                    description = "Если пользователя с указанным id не существует.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ExceptionResponse.class)))
    })
    @GetMapping("/value")
    public ResponseEntity<Page<ExpressionForDictionaryListResponse>> findByValue(
            @RequestParam
            @Parameter(description = "Идентификатор пользователя, выражение которого запрашивается", required = true)
            UUID userId,
            @RequestParam
            @Parameter(description = "Значение устойчевого выражения. Не может быть null.", required = true)
            String value,
            @RequestParam
            @Parameter(description = """
                    Максимальное редакциооное растояние относительно искомого слова.
                     Диапозон допустимых значений [1, 20].
                    """, schema = @Schema(defaultValue = "1"))
            int maxDistance,
            @RequestParam("page")
            @Parameter(description = "Номер страницы выборки. Нумерация начинается с нуля.", required = true)
            int page,
            @RequestParam(value = "size", required = false)
            @Parameter(description = "Размер страницы выборки. Диапозон значений - [1, 100].",
                    schema = @Schema(defaultValue = "20"))
            int size) {
        UUID jwsUserId = requestContext.getCurrentJwsBodyAs(UUID.class);
        logger.info("user {} get expressions of user {} by value '{}', levenshtein_distance {}, page {}, size {}",
                jwsUserId, userId, value, maxDistance, page, size);

        Pageable pageable = mapper.toPageable(page, size);
        Page<Expression> expressions = expressionService.findByValue(userId, value, maxDistance, pageable);
        return ResponseEntity.ok(mapper.toExpressionsForDictionaryListResponse(expressions));
    }

    @Operation(summary = """
            Возвращает все выражения, один из переводов которых совпадает с указанным пользователем значением.
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
            @ApiResponse(responseCode = "404",
                    description = "Если пользователя с указанным id не существует.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ExceptionResponse.class)))
    })
    @GetMapping("/translate")
    public ResponseEntity<Page<ExpressionForDictionaryListResponse>> findByTranslate(
            @RequestParam
            @Parameter(description = "Идентификатор пользователя, из выражений которого делается выборка.", required = true)
            UUID userId,
            @RequestParam
            @Parameter(description = "Перевод выражения с английского на родной язык пользователя.", required = true)
            String translate,
            @RequestParam("page")
            @Parameter(description = "Номер страницы выборки. Нумерация начинается с нуля.", required = true)
            int page,
            @RequestParam(value = "size", required = false)
            @Parameter(description = "Размер страницы выборки. Диапозон значений - [1, 100].",
                    schema = @Schema(defaultValue = "20"))
            int size) {
        UUID jwsUserId = requestContext.getCurrentJwsBodyAs(UUID.class);
        logger.info("user {} get expressions of user {} by translate '{}', page {}, size {}",
                jwsUserId, userId, translate, page, size);

        Pageable pageable = mapper.toPageable(page, size);
        Page<Expression> expressions = expressionService.findByTranslate(userId, translate, pageable);
        return ResponseEntity.ok(mapper.toExpressionsForDictionaryListResponse(expressions));
    }

    @Operation(summary = "Удаляет устойчевое выражение из словаря пользователя")
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
            @ApiResponse(responseCode = "404",
                    description = "Если не удалось найти выражение или пользователя по указанным идентификаторам.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ExceptionResponse.class)))
    })
    @DeleteMapping("/id")
    public ResponseEntity<String> delete(
            @RequestParam
            @Parameter(description = "Идентификатор пользователя, выражение которого удаляется", required = true)
            UUID userId,
            @PathVariable
            @Parameter(description = "Уникальный идентификатор устойчевого выражения в формате UUID.", required = true)
            UUID expressionId) {
        UUID jwsUserId = requestContext.getCurrentJwsBodyAs(UUID.class);
        logger.info("user {} delete expression of user {} by id={}", jwsUserId, userId, expressionId);

        expressionService.tryDeleteById(userId, expressionId);
        return ResponseEntity.ok(messages.getMessage("dictionary.expressions.delete"));
    }

}
