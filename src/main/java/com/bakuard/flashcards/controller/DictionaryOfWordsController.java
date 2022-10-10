package com.bakuard.flashcards.controller;

import com.bakuard.flashcards.config.security.RequestContext;
import com.bakuard.flashcards.controller.message.Messages;
import com.bakuard.flashcards.dto.DtoMapper;
import com.bakuard.flashcards.dto.exceptions.ExceptionResponse;
import com.bakuard.flashcards.dto.word.WordAddRequest;
import com.bakuard.flashcards.dto.word.WordForDictionaryListResponse;
import com.bakuard.flashcards.dto.word.WordResponse;
import com.bakuard.flashcards.dto.word.WordUpdateRequest;
import com.bakuard.flashcards.model.word.Word;
import com.bakuard.flashcards.service.AuthService;
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

@Tag(name = "Словарь слов пользователя")
@RestController
@RequestMapping("/dictionary/words")
public class DictionaryOfWordsController {

    private static final Logger logger = LoggerFactory.getLogger(DictionaryOfWordsController.class.getName());


    private WordService wordService;
    private AuthService authService;
    private DtoMapper mapper;
    private RequestContext requestContext;
    private Messages messages;

    @Autowired
    public DictionaryOfWordsController(WordService wordService,
                                       AuthService authService,
                                       DtoMapper mapper,
                                       RequestContext requestContext,
                                       Messages messages) {
        this.wordService = wordService;
        this.authService = authService;
        this.mapper = mapper;
        this.requestContext = requestContext;
        this.messages = messages;
    }

    @Operation(summary = "Добавляет новое слово в словарь пользователя",
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
    @PostMapping
    public ResponseEntity<WordResponse> add(@RequestBody WordAddRequest dto) {
        UUID userId = requestContext.getCurrentJwsBodyAs(UUID.class);
        logger.info("user {} add word '{}' for user {}", userId, dto.getValue(), dto.getUserID());

        Word word = mapper.toWord(dto);
        word = wordService.save(word);
        return ResponseEntity.ok(mapper.toWordResponse(word));
    }

    @Operation(summary = "Обновляет слово в словаре пользователя",
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
    @PutMapping
    public ResponseEntity<WordResponse> update(@RequestBody WordUpdateRequest dto) {
        UUID userId = requestContext.getCurrentJwsBodyAs(UUID.class);
        logger.info("user {} update word {} for user {}", userId, dto.getWordId(), dto.getUserId());

        Word word = mapper.toWord(dto);
        word = wordService.save(word);
        return ResponseEntity.ok(mapper.toWordResponse(word));
    }

    @Operation(summary = "Возвращает часть выборки слов из словаря пользователя",
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
    @GetMapping
    public ResponseEntity<Page<WordForDictionaryListResponse>> findAllBy(
            @RequestParam
            @Parameter(description = "Идентификатор пользователя, из слов которого делается выборка", required = true)
            UUID userId,
            @RequestParam("page")
            @Parameter(description = "Номер страницы выборки. Нумерация начинается с нуля.", required = true)
            int page,
            @RequestParam(value = "size", required = false)
            @Parameter(description = "Размер страницы выборки. Диапозон значений - [1, 100].")
            int size,
            @RequestParam(value = "sort", required = false)
            @Parameter(description = "Порядок сортировки.",
                    schema = @Schema(
                            defaultValue = "value.asc (Сортировка по значению в порядке возрастания).",
                            allowableValues = {
                                    "value - сортировка по значению",
                                    "repeat_interval - сортировка по интервалу повторения",
                                    "last_date_of_repeat - сортировка по дате поседнего повторения"
                            }
                    ))
            String sort) {
        UUID jwsUserId = requestContext.getCurrentJwsBodyAs(UUID.class);
        logger.info("user {} get words of user {} by page={}, size={}, sort={}",
                jwsUserId, userId, page, size, sort);

        authService.assertExists(userId);
        Pageable pageable = mapper.toPageableForDictionaryWords(page, size, sort);
        Page<WordForDictionaryListResponse> result = mapper.toWordsForDictionaryListResponse(
                wordService.findByUserId(userId, pageable)
        );

        return ResponseEntity.ok(result);
    }

    @Operation(summary = "Возвращает слово из словаря пользователя по его идентификатору",
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
                            description = "Если не удалось найти слово по указанным id пользователя и самого слова.",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = ExceptionResponse.class)))
            }
    )
    @GetMapping("/id")
    public ResponseEntity<WordResponse> findById(
            @RequestParam
            @Parameter(description = "Идентификатор пользователя, слово которого запрашивается", required = true)
            UUID userId,
            @PathVariable
            @Parameter(description = "Уникальный идентификатор слова в формате UUID. Не может быть null.", required = true)
            UUID id) {
        UUID jwsUserId = requestContext.getCurrentJwsBodyAs(UUID.class);
        logger.info("user {} get word of user {} by id={}", jwsUserId, userId, id);

        Word word = wordService.tryFindById(userId, id);
        return ResponseEntity.ok(mapper.toWordResponse(word));
    }

    @Operation(summary = "Возвращает слово из словаря пользователя по его значению",
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
                            description = "Если не удалось найти слово по указанныму id пользователя и значению слова.",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = ExceptionResponse.class)))
            }
    )
    @GetMapping("/value")
    public ResponseEntity<WordResponse> findByValue(
            @RequestParam
            @Parameter(description = "Идентификатор пользователя, слово которого запрашивается", required = true)
            UUID userId,
            @PathVariable
            @Parameter(description = "Значение слова. Не может быть null.", required = true)
            String value) {
        UUID jwsUserId = requestContext.getCurrentJwsBodyAs(UUID.class);
        logger.info("user {} get word of user {} by value '{}'", jwsUserId, userId, value);

        Word word = wordService.tryFindByValue(userId, value);
        return ResponseEntity.ok(mapper.toWordResponse(word));
    }

    @Operation(summary = "Удаляет слово из словаря пользователя пользователя",
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
                            description = "Если не удалось найти слово по указанным id пользователя и самого слова.",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = ExceptionResponse.class)))
            }
    )
    @DeleteMapping("/id")
    public ResponseEntity<String> delete(
            @RequestParam
            @Parameter(description = "Идентификатор пользователя, слово которого удаляется", required = true)
            UUID userId,
            @PathVariable
            @Parameter(description = "Уникальный идентификатор слова в формате UUID. Не может быть null.", required = true)
            UUID id) {
        UUID jwsUserId = requestContext.getCurrentJwsBodyAs(UUID.class);
        logger.info("user {} delete word of user {} by id={}", jwsUserId, userId, id);

        wordService.tryDeleteById(userId, id);
        return ResponseEntity.ok(messages.getMessage("dictionary.words.delete"));
    }

}
