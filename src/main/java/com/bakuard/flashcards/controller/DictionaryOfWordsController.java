package com.bakuard.flashcards.controller;

import com.bakuard.flashcards.config.security.RequestContext;
import com.bakuard.flashcards.controller.message.Messages;
import com.bakuard.flashcards.dto.DtoMapper;
import com.bakuard.flashcards.dto.exceptions.ExceptionResponse;
import com.bakuard.flashcards.dto.word.*;
import com.bakuard.flashcards.model.word.Word;
import com.bakuard.flashcards.service.AuthService;
import com.bakuard.flashcards.service.WordService;
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

@Tag(name = "Словарь слов пользователя")
@SecurityRequirement(name = "JWTScheme")
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

    @Operation(summary = "Добавляет новое слово в словарь пользователя")
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
    public ResponseEntity<WordResponse> add(@RequestBody WordAddRequest dto) {
        UUID userId = requestContext.getCurrentJwsBodyAs(UUID.class);
        logger.info("user {} add word '{}' for user {}", userId, dto.getValue(), dto.getUserID());

        Word word = mapper.toWord(dto);
        word = wordService.save(word);
        return ResponseEntity.ok(mapper.toWordResponse(word));
    }

    @Operation(summary = "Обновляет слово в словаре пользователя")
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
                    description = "Если не удалось найти слово или пользователя по указнным id.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ExceptionResponse.class)))
    })
    @PutMapping
    public ResponseEntity<WordResponse> update(@RequestBody WordUpdateRequest dto) {
        UUID userId = requestContext.getCurrentJwsBodyAs(UUID.class);
        logger.info("user {} update word {} for user {}", userId, dto.getWordId(), dto.getUserId());

        Word word = mapper.toWord(dto);
        word = wordService.save(word);
        return ResponseEntity.ok(mapper.toWordResponse(word));
    }

    @Operation(summary = """
            Дополняет переданное слово из внешних источников переводами, транскрипциями, толкованиями, примерами.
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
                            schema = @Schema(implementation = ExceptionResponse.class)))
    })
    @PutMapping
    public ResponseEntity<WordResponse> supplement(@RequestBody WordSupplementRequest dto) {
        return null;
    }

    @Operation(summary = "Возвращает часть выборки слов из словаря пользователя")
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
    public ResponseEntity<Page<WordForDictionaryListResponse>> findAllBy(
            @RequestParam
            @Parameter(description = "Идентификатор пользователя, из слов которого делается выборка", required = true)
            UUID userId,
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
        logger.info("user {} get words of user {} by page={}, size={}, sort={}",
                jwsUserId, userId, page, size, sort);

        authService.assertExists(userId);
        Pageable pageable = mapper.toPageable(page, size, mapper.toWordSort(sort));
        Page<WordForDictionaryListResponse> result = mapper.toWordsForDictionaryListResponse(
                wordService.findByUserId(userId, pageable)
        );

        return ResponseEntity.ok(result);
    }

    @Operation(summary = "Возвращает слово из словаря пользователя по его идентификатору")
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
                    description = "Если не удалось найти слово или пользователя по указанным идентификаторам.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ExceptionResponse.class)))
    })
    @GetMapping("/id")
    public ResponseEntity<WordResponse> findById(
            @RequestParam
            @Parameter(description = "Идентификатор пользователя, слово которого запрашивается", required = true)
            UUID userId,
            @PathVariable
            @Parameter(description = "Уникальный идентификатор слова в формате UUID.", required = true)
            UUID wordId) {
        UUID jwsUserId = requestContext.getCurrentJwsBodyAs(UUID.class);
        logger.info("user {} get word of user {} by id={}", jwsUserId, userId, wordId);

        Word word = wordService.tryFindById(userId, wordId);
        return ResponseEntity.ok(mapper.toWordResponse(word));
    }

    @Operation(summary = """
            Возвращает слово и/или наиболее похожие по написанию к нему слова. Все слова будут отсортирвоанны
             в порядке возрастания редакционного расстояние между искомым словом, а затем в лексеграфиечском
             порядке.
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
    public ResponseEntity<Page<WordForDictionaryListResponse>> findByValue(
            @RequestParam
            @Parameter(description = "Идентификатор пользователя, из слов которого делается выборка.", required = true)
            UUID userId,
            @PathVariable
            @Parameter(description = "Значение искомого слова. Не может быть null.", required = true)
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
        logger.info("user {} get words of user {} by value '{}', levenshtein_distance {}, page {}, size {}",
                jwsUserId, userId, value, maxDistance, page, size);

        Pageable pageable = mapper.toPageable(page, size);
        Page<Word> words = wordService.findByValue(userId, value, maxDistance, pageable);
        return ResponseEntity.ok(mapper.toWordsForDictionaryListResponse(words));
    }

    @Operation(summary = """
            Возвращает все слова, один из переводов которых совпадает с указанным пользователем значением.
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
    public ResponseEntity<Page<WordForDictionaryListResponse>> findByTranslate(
            @RequestParam
            @Parameter(description = "Идентификатор пользователя, из слов которого делается выборка.", required = true)
            UUID userId,
            @RequestParam
            @Parameter(description = "Перевод слова с английского на родной язык пользователя.", required = true)
            String translate,
            @RequestParam("page")
            @Parameter(description = "Номер страницы выборки. Нумерация начинается с нуля.", required = true)
            int page,
            @RequestParam(value = "size", required = false)
            @Parameter(description = "Размер страницы выборки. Диапозон значений - [1, 100].",
                    schema = @Schema(defaultValue = "20"))
            int size) {
        UUID jwsUserId = requestContext.getCurrentJwsBodyAs(UUID.class);
        logger.info("user {} get words of user {} by translate '{}', page {}, size {}",
                jwsUserId, userId, translate, page, size);

        Pageable pageable = mapper.toPageable(page, size);
        Page<Word> words = wordService.findByTranslate(userId, translate, pageable);
        return ResponseEntity.ok(mapper.toWordsForDictionaryListResponse(words));
    }

    @Operation(summary = """
            Возвращает часть выборки слов из словаря пользователя. Номер страницы используемый при пагинации
             автоматчиески подбирается таким образом, чтобы первое слово из всех слов пользователя, которое
             начинается на указанную букву, было на этой странице. Используется для быстрого перехода к букве
             в словаре.
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
    @GetMapping("/firstCharacter")
    public ResponseEntity<Page<WordForDictionaryListResponse>> jumpToCharacter(
            @RequestParam
            @Parameter(description = "Идентификатор пользователя, из слов которого делается выборка.", required = true)
            UUID userId,
            @RequestParam
            @Parameter(description = "Первый символ значения искомых слов на английском", required = true)
            String wordFirstCharacter,
            @RequestParam(value = "size", required = false)
            @Parameter(description = "Размер страницы выборки. Диапозон значений - [1, 100].",
                    schema = @Schema(defaultValue = "20"))
            int size) {
        UUID jwsUserId = requestContext.getCurrentJwsBodyAs(UUID.class);
        logger.info("user {} get words of user {} by wordFirstCharacter '{}', size {}",
                jwsUserId, userId, wordFirstCharacter, size);

        Page<Word> words = wordService.jumpToCharacter(userId, wordFirstCharacter, size);
        return ResponseEntity.ok(mapper.toWordsForDictionaryListResponse(words));
    }

    @Operation(summary = "Удаляет слово из словаря пользователя пользователя")
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
                    description = "Если не удалось найти слово или пользователя по указанным идентификаторам.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ExceptionResponse.class)))
    })
    @DeleteMapping("/id")
    public ResponseEntity<String> delete(
            @RequestParam
            @Parameter(description = "Идентификатор пользователя, слово которого удаляется.", required = true)
            UUID userId,
            @PathVariable
            @Parameter(description = "Уникальный идентификатор слова в формате UUID.", required = true)
            UUID wordId) {
        UUID jwsUserId = requestContext.getCurrentJwsBodyAs(UUID.class);
        logger.info("user {} delete word of user {} by id={}", jwsUserId, userId, wordId);

        wordService.tryDeleteById(userId, wordId);
        return ResponseEntity.ok(messages.getMessage("dictionary.words.delete"));
    }

}
