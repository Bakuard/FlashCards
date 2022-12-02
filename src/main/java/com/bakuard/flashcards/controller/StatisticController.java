package com.bakuard.flashcards.controller;

import com.bakuard.flashcards.config.security.RequestContext;
import com.bakuard.flashcards.controller.message.Messages;
import com.bakuard.flashcards.dto.DtoMapper;
import com.bakuard.flashcards.dto.exceptions.ExceptionResponse;
import com.bakuard.flashcards.dto.statistic.ExpressionRepetitionByPeriodResponse;
import com.bakuard.flashcards.dto.statistic.WordRepetitionByPeriodResponse;
import com.bakuard.flashcards.model.auth.policy.Authorizer;
import com.bakuard.flashcards.model.statistic.ExpressionRepetitionByPeriodStatistic;
import com.bakuard.flashcards.model.statistic.WordRepetitionByPeriodStatistic;
import com.bakuard.flashcards.service.StatisticService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@Tag(name = "Контроллер статистики")
@SecurityRequirement(name = "JWTScheme")
@RestController
@RequestMapping("/statistic")
public class StatisticController {

    private static final Logger logger = LoggerFactory.getLogger(StatisticController.class.getName());


    private StatisticService statisticService;
    private DtoMapper mapper;
    private RequestContext requestContext;
    private Authorizer authorizer;

    @Autowired
    public StatisticController(StatisticService statisticService,
                               DtoMapper mapper,
                               RequestContext requestContext,
                               Authorizer authorizer) {
        this.statisticService = statisticService;
        this.mapper = mapper;
        this.requestContext = requestContext;
        this.authorizer = authorizer;
    }

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
                     значения параметра endDate. <br/>
                     Ограничения: дата задается в формате yyyy.mm.dd
                    """)
            String startDate,
            @RequestParam
            @Parameter(description = """
                    Конец периода, за который собирается статистика. <br/>
                    Ограничения: дата задается в формате yyyy.mm.dd
                    """)
            String endDate) {
        UUID jwsUserId = requestContext.getCurrentJwsBodyAs(UUID.class);
        logger.info("user {} find statistic of user {} for wordId={}, startDate={}, endDate={}",
                jwsUserId, userId, wordId, startDate, endDate);
        authorizer.assertToHasAccess(jwsUserId, "statistic", userId, "findStatisticForWordRepetition");

        WordRepetitionByPeriodStatistic statistic = statisticService.wordRepetitionByPeriod(
                userId, wordId, startDate, endDate
        );

        return ResponseEntity.ok(mapper.toWordRepetitionByPeriodResponse(statistic));
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
                     значения параметра endDate. <br/>
                     Ограничения: дата задается в формате yyyy.mm.dd
                    """)
            String startDate,
            @RequestParam
            @Parameter(description = """
                    Конец периода, за который собирается статистика. <br/>
                    Ограничения: дата задается в формате yyyy.mm.dd
                    """)
            String endDate,
            @RequestParam("page")
            @Parameter(description = "Номер страницы выборки. Нумерация начинается с нуля.", required = true)
            int page,
            @RequestParam(value = "size", required = false)
            @Parameter(description = "Размер страницы выборки. Диапозон значений - [1, 100].",
                    schema = @Schema(defaultValue = "20"))
            int size,
            @RequestParam(value = "sort", required = false)
            @Parameter(description = """
                    Задает порядок сортировки.
                    <br/><br/>
                    Допустимые параметры (без учета регистра символов):
                    <ol>
                        <li>remember_from_english - сортировка по кол-ву успешных повторений с английского.</li>
                        <li>remember_from_native - сортировка по кол-ву успешных повторений с родного языка.</li>
                        <li>not_remember_from_english - сортировка по кол-ву не успешных повторений с английского.</li>
                        <li>not_remember_from_native - сортировка по кол-ву не успешных повторений с родного языка.</li>
                    </ol>
                    Параметры сортировки можно комбинировать через запятую.
                    </br></br>
                    Направление сортировки для параметра задается в виде <i>параметр.направление</i>, где направление
                     задается одной из следующих констант (без учета регистра символов):
                    <ol>
                        <li>asc (по умолчанию)</li>
                        <li>ascending</li>
                        <li>dec</li>
                        <li>descending</li>
                    </ol>
                    """,
                    schema = @Schema(defaultValue = "remember_from_english.asc"))
            String sort) {
        UUID jwsUserId = requestContext.getCurrentJwsBodyAs(UUID.class);
        logger.info("user {} find statistic of user {} for startDate={}, endDate={}, page={}, size={}, sort={}",
                jwsUserId, userId, startDate, endDate, page, size, sort);
        authorizer.assertToHasAccess(jwsUserId, "statistic", userId, "findStatisticForWordsRepetition");

        Page<WordRepetitionByPeriodStatistic> statistic = statisticService.wordsRepetitionByPeriod(
                userId, startDate, endDate, mapper.toPageable(page, size, mapper.toWordStatisticSort(sort))
        );

        return ResponseEntity.ok(mapper.toWordsRepetitionByPeriodResponse(statistic));
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
                     значения параметра endDate. <br/>
                     Ограничения: дата задается в формате yyyy.mm.dd
                    """)
            String startDate,
            @RequestParam
            @Parameter(description = """
                    Конец периода, за который собирается статистика. <br/>
                    Ограничения: дата задается в формате yyyy.mm.dd
                    """)
            String endDate) {
        UUID jwsUserId = requestContext.getCurrentJwsBodyAs(UUID.class);
        logger.info("user {} find statistic of user {} for expressionId={}, startDate={}, endDate={}",
                jwsUserId, userId, expressionId, startDate, endDate);
        authorizer.assertToHasAccess(jwsUserId, "statistic", userId, "findStatisticForExpressionRepetition");

        ExpressionRepetitionByPeriodStatistic statistic = statisticService.expressionRepetitionByPeriod(
                userId, expressionId, startDate, endDate
        );

        return ResponseEntity.ok(mapper.toExpressionRepetitionByPeriodResponse(statistic));
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
                            description = "Если не удалось найти пользователя по указанному идентификаторам.",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = ExceptionResponse.class)))
            }
    )
    @GetMapping("/expressions")
    public ResponseEntity<Page<ExpressionRepetitionByPeriodResponse>> findStatisticForExpressionsRepetition(
            @RequestParam
            @Parameter(description = "Идентификатор пользователя", required = true)
            UUID userId,
            @RequestParam
            @Parameter(description = """
                    Начало периода, за который собирается статистика. Значение не должно превышать
                     значения параметра endDate. <br/>
                     Ограничения: дата задается в формате yyyy.mm.dd
                    """)
            String startDate,
            @RequestParam
            @Parameter(description = """
                    Конец периода, за который собирается статистика. <br/>
                    Ограничения: дата задается в формате yyyy.mm.dd
                    """)
            String endDate,
            @RequestParam("page")
            @Parameter(description = "Номер страницы выборки. Нумерация начинается с нуля.", required = true)
            int page,
            @RequestParam(value = "size", required = false)
            @Parameter(description = "Размер страницы выборки. Диапозон значений - [1, 100].",
                    schema = @Schema(defaultValue = "20"))
            int size,
            @RequestParam(value = "sort", required = false)
            @Parameter(description = """
                    Задает порядок сортировки.
                    <br/><br/>
                    Допустимые параметры (без учета регистра символов):
                    <ol>
                        <li>remember_from_english - сортировка по кол-ву успешных повторений с английского.</li>
                        <li>remember_from_native - сортировка по кол-ву успешных повторений с родного языка.</li>
                        <li>not_remember_from_english - сортировка по кол-ву не успешных повторений с английского.</li>
                        <li>not_remember_from_native - сортировка по кол-ву не успешных повторений с родного языка.</li>
                    </ol>
                    Параметры сортировки можно комбинировать через запятую.
                    </br></br>
                    Направление сортировки для параметра задается в виде <i>параметр.направление</i>, где направление
                     задается одной из следующих констант (без учета регистра символов):
                    <ol>
                        <li>asc (по умолчанию)</li>
                        <li>ascending</li>
                        <li>dec</li>
                        <li>descending</li>
                    </ol>
                    """,
                    schema = @Schema(defaultValue = "remember_from_english.asc"))
            String sort) {
        UUID jwsUserId = requestContext.getCurrentJwsBodyAs(UUID.class);
        logger.info("user {} find statistic of user {} for startDate={}, endDate={}, page={}, size={}, sort={}",
                jwsUserId, userId, startDate, endDate, page, size, sort);
        authorizer.assertToHasAccess(jwsUserId, "statistic", userId, "findStatisticForExpressionsRepetition");

        Page<ExpressionRepetitionByPeriodStatistic> statistic = statisticService.expressionsRepetitionByPeriod(
                userId, startDate, endDate, mapper.toPageable(page, size, mapper.toExpressionStatisticSort(sort))
        );

        return ResponseEntity.ok(mapper.toExpressionsRepetitionByPeriodResponse(statistic));
    }

}
