package com.bakuard.flashcards.controller;

import com.bakuard.flashcards.config.security.RequestContext;
import com.bakuard.flashcards.controller.message.Messages;
import com.bakuard.flashcards.dto.DtoMapper;
import com.bakuard.flashcards.dto.exceptions.ExceptionResponse;
import com.bakuard.flashcards.dto.word.*;
import com.bakuard.flashcards.model.auth.policy.Authorizer;
import com.bakuard.flashcards.model.word.supplementation.AggregateSupplementedWord;
import com.bakuard.flashcards.model.word.Word;
import com.bakuard.flashcards.service.AuthService;
import com.bakuard.flashcards.service.WordService;
import com.bakuard.flashcards.service.wordSupplementation.WordSupplementationService;
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
@SecurityRequirement(name = "commonToken")
@RestController
@RequestMapping("/dictionary/words")
public class DictionaryOfWordsController {

    private static final Logger logger = LoggerFactory.getLogger(DictionaryOfWordsController.class.getName());


    private WordService wordService;
    private AuthService authService;
    private WordSupplementationService wordSupplementationService;
    private DtoMapper mapper;
    private RequestContext requestContext;
    private Messages messages;
    private Authorizer authorizer;

    @Autowired
    public DictionaryOfWordsController(WordService wordService,
                                       AuthService authService,
                                       WordSupplementationService wordSupplementationService,
                                       DtoMapper mapper,
                                       RequestContext requestContext,
                                       Messages messages,
                                       Authorizer authorizer) {
        this.wordService = wordService;
        this.authService = authService;
        this.wordSupplementationService = wordSupplementationService;
        this.mapper = mapper;
        this.requestContext = requestContext;
        this.messages = messages;
        this.authorizer = authorizer;
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
        logger.info("user {} add word '{}' for user {}", userId, dto.getValue(), dto.getUserId());

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
                    description = "Если не удалось найти слово или пользователя по указнным id.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ExceptionResponse.class)))
    })
    @PutMapping
    public ResponseEntity<WordResponse> update(@RequestBody WordUpdateRequest dto) {
        UUID userId = requestContext.getCurrentJwsBodyAs(UUID.class);
        logger.info("user {} update word '{}' for user {}", userId, dto.getWordId(), dto.getUserId());
        authorizer.assertToHasAccess(userId, "dictionary", dto.getUserId(), "update");

        Word word = mapper.toWord(dto);
        word = wordService.save(word);
        return ResponseEntity.ok(mapper.toWordResponse(word));
    }

    @Operation(summary = """
            Заполняет новое слово из внешних источников переводами, транскрипциями, толкованиями и
             переводами примеров к этому слову.
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
                    description = "Если не удалось найти пользователя по указанному id.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ExceptionResponse.class)))
    })
    @PutMapping("/supplement/newWord")
    public ResponseEntity<SupplementedWordResponse> supplementNewWord(@RequestBody WordAddRequest dto) {
        UUID userId = requestContext.getCurrentJwsBodyAs(UUID.class);
        logger.info("user {} supplement word '{}' for user {}", userId, dto.getValue(), dto.getUserId());
        authorizer.assertToHasAccess(userId, "dictionary", dto.getUserId(), "supplementNewWord");

        AggregateSupplementedWord word = wordSupplementationService.supplement(mapper.toWord(dto));

        return ResponseEntity.ok(mapper.toSupplementedWordResponse(word));
    }

    @Operation(summary = """
            Дополняет переданное слово из внешних источников переводами, транскрипциями, толкованиями и
             переводами примеров к этому слову.
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
                    description = "Если не удалось найти пользователя или слово по соответствующему id.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ExceptionResponse.class)))
    })
    @PutMapping("/supplement/existedWord")
    public ResponseEntity<SupplementedWordResponse> supplementExistedWord(@RequestBody WordUpdateRequest dto) {
        UUID userId = requestContext.getCurrentJwsBodyAs(UUID.class);
        logger.info("user {} supplement word '{}' for user {}", userId, dto.getValue(), dto.getUserId());
        authorizer.assertToHasAccess(userId, "dictionary", dto.getUserId(), "supplementExistedWord");

        AggregateSupplementedWord word = wordSupplementationService.supplement(mapper.toWord(dto));

        return ResponseEntity.ok(mapper.toSupplementedWordResponse(word));
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
            @Parameter(description = """
                    Задает порядок сортировки.
                    <br/><br/>
                    Допустимые параметры (без учета регистра символов):
                    <ol>
                        <li>value - сортировка по значению </li>
                        <li>repeat_interval_from_english - сортировка по интервалу повторения для перевода с английского </li>
                        <li>repeat_interval_from_native - сортировка по дате поседнего повторения для переводоа с родного языка </li>
                        <li>last_date_of_repeat_from_english - сортировка по последней дате повторения с английского </li>
                        <li>last_date_of_repeat_from_native - сортировка по последней дате повторения с родного языка </li>
                    </ol>
                    Параметры сортировки можно комбинировать через запятую.
                    </br></br>
                    Направление сортировки для параметра задается в виде <i>параметр.направление</i>, где направление
                     задается одной из следующих констант (без учета регистра символов):
                    <ol>
                        <li>asc (по умолчанию)</li>
                        <li>ascending</li>
                        <li>desc</li>
                        <li>descending</li>
                    </ol>
                    """,
                    schema = @Schema(defaultValue = "value.asc"))
            String sort) {
        UUID jwsUserId = requestContext.getCurrentJwsBodyAs(UUID.class);
        logger.info("user {} get words of user {} by page={}, size={}, sort={}",
                jwsUserId, userId, page, size, sort);
        authorizer.assertToHasAccess(jwsUserId, "dictionary", userId, "findAllBy");

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
        authorizer.assertToHasAccess(jwsUserId, "dictionary", userId, "findById");

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
        authorizer.assertToHasAccess(jwsUserId, "dictionary", userId, "findByValue");

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
        authorizer.assertToHasAccess(jwsUserId, "dictionary", userId, "findByTranslate");

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
        authorizer.assertToHasAccess(jwsUserId, "dictionary", userId, "jumpToCharacter");

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
        authorizer.assertToHasAccess(jwsUserId, "dictionary", userId, "delete");

        wordService.tryDeleteById(userId, wordId);
        return ResponseEntity.ok(messages.getMessage("dictionary.words.delete"));
    }

}
