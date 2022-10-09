package com.bakuard.flashcards.controller;

import com.bakuard.flashcards.config.security.RequestContext;
import com.bakuard.flashcards.dto.DtoMapper;
import com.bakuard.flashcards.dto.exceptions.ExceptionResponse;
import com.bakuard.flashcards.dto.word.WordForRepetitionResponse;
import com.bakuard.flashcards.dto.word.WordRepeatRequest;
import com.bakuard.flashcards.dto.word.WordResponse;
import com.bakuard.flashcards.model.word.Word;
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
    private DtoMapper mapper;
    private RequestContext requestContext;

    @Autowired
    public RepetitionOfWordsController(WordService wordService,
                                       DtoMapper mapper,
                                       RequestContext requestContext) {
        this.wordService = wordService;
        this.mapper = mapper;
        this.requestContext = requestContext;
    }

    @Operation(summary = "Возвращает часть выборки слов доступных для повторения в текущую дату",
            responses = {
                    @ApiResponse(responseCode = "200"),
                    @ApiResponse(responseCode = "400",
                            description = "Если нарушен хотя бы один из инвариантов связаный с параметрами запроса",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = ExceptionResponse.class))),
                    @ApiResponse(responseCode = "401",
                            description = "Если передан некорректный токен или токен не указан",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = ExceptionResponse.class)))
            }
    )
    @GetMapping
    public ResponseEntity<Page<WordForRepetitionResponse>> findAllBy(
            @RequestParam("page")
            @Parameter(description = "Номер страницы выборки. Нумерация начинается с нуля.", required = true)
            int page,
            @RequestParam(value = "size", required = false)
            @Parameter(description = "Размер страницы выборки. Диапозон значений - [1, 100].")
            int size) {
        UUID userId = requestContext.getCurrentJwsBodyAs(UUID.class);
        logger.info("user {} find all words for repeat by page={}, size={}", userId, page, size);

        Pageable pageable = mapper.toPageableForDictionaryWords(page, size, "value.asc");
        Page<Word> result = wordService.findAllForRepeat(userId, pageable);

        return ResponseEntity.ok(mapper.toWordsForRepetitionResponse(result));
    }

    @Operation(summary = "Отмечает - помнит ли пользователь слово или нет.",
            responses = {
                    @ApiResponse(responseCode = "200"),
                    @ApiResponse(responseCode = "400",
                            description = "Если нарушен хотя бы один из инвариантов связаный с телом запроса",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = ExceptionResponse.class))),
                    @ApiResponse(responseCode = "401",
                            description = "Если передан некорректный токен или токен не указан",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = ExceptionResponse.class)))
            }
    )
    @PutMapping
    public ResponseEntity<WordResponse> repeat(@RequestBody WordRepeatRequest dto) {
        UUID userId = requestContext.getCurrentJwsBodyAs(UUID.class);
        logger.info("user {} repeat word {} as user {}. remember is {}",
                userId, dto.getWordId(), dto.getUserId(), dto.isRemember());

        Word word = wordService.repeat(dto.getUserId(), dto.getWordId(), dto.isRemember());

        return ResponseEntity.ok(mapper.toWordResponse(word));
    }

}
