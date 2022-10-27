package com.bakuard.flashcards.controller;

import com.bakuard.flashcards.config.security.RequestContext;
import com.bakuard.flashcards.controller.message.Messages;
import com.bakuard.flashcards.dto.DtoMapper;
import com.bakuard.flashcards.dto.exceptions.ExceptionResponse;
import com.bakuard.flashcards.dto.settings.IntervalAddRequest;
import com.bakuard.flashcards.dto.settings.IntervalReplaceRequest;
import com.bakuard.flashcards.dto.settings.IntervalsResponse;
import com.bakuard.flashcards.service.IntervalService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Tag(name = "Общие настройки словарей и режимов повторения отдельного пользователя.")
@RestController
@RequestMapping("/settings")
public class SettingsController {

    private static Logger logger = LoggerFactory.getLogger(SettingsController.class.getName());


    private IntervalService intervalService;
    private RequestContext requestContext;
    private Messages messages;
    private DtoMapper mapper;

    @Autowired
    public SettingsController(IntervalService intervalService,
                              RequestContext requestContext,
                              Messages messages,
                              DtoMapper mapper) {
        this.intervalService = intervalService;
        this.requestContext = requestContext;
        this.messages = messages;
        this.mapper = mapper;
    }

    @Operation(summary = """
            Возвращает все интервалы повторения кнкретного пользователя.
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
                    description = "Если не удалось найти пользователя с указанным идентификатором.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ExceptionResponse.class)))
    })
    @GetMapping("/intervals")
    public ResponseEntity<IntervalsResponse> findAllIntervals(
            @RequestParam
            @Parameter(description = "Идентификатор пользователя, из слов которого формируется выборка для повторения.", required = true)
            UUID userId) {
        UUID jwsUserId = requestContext.getCurrentJwsBodyAs(UUID.class);
        logger.info("user {} find all repeat intervals of user {}", jwsUserId, userId);

        List<Integer> intervals = intervalService.findAll(userId);

        return ResponseEntity.ok(mapper.toIntervalsResponse(userId, intervals));
    }

    @Operation(summary = """
            Добавляет новый интервал повторения для указанного пользователя.
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
            @ApiResponse(responseCode = "404",
                    description = "Если не удалось найти указанного пользователя.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ExceptionResponse.class)))
    })
    @PostMapping("/intervals")
    public ResponseEntity<String> addInterval(IntervalAddRequest dto) {
        UUID jwsUserId = requestContext.getCurrentJwsBodyAs(UUID.class);
        logger.info("user {} add new interval for user {}", jwsUserId, dto.getUserId());

        intervalService.add(dto.getUserId(), dto.getInterval());

        return ResponseEntity.ok(messages.getMessage("settings.addInterval"));
    }

    @Operation(summary = """
            Заменяет один из указанных интервалов повтоерния указанного пользователя на указанное значение.
             В качестве нового интервала повторения может быть использован один из уже существующих интервалов
             повторения пользователя.
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
            @ApiResponse(responseCode = "404",
                    description = "Если не удалось найти указанного пользователя.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ExceptionResponse.class)))
    })
    @PutMapping("/intervals")
    public ResponseEntity<String> replaceInterval(IntervalReplaceRequest dto) {
        UUID jwsUserId = requestContext.getCurrentJwsBodyAs(UUID.class);
        logger.info("user {} replace interval {} to {} for user {}",
                jwsUserId, dto.getOldInterval(), dto.getNewInterval(), dto.getUserId());

        intervalService.replace(dto.getUserId(), dto.getOldInterval(), dto.getNewInterval());

        return ResponseEntity.ok(messages.getMessage("settings.replaceInterval"));
    }

}
