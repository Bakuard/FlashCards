package com.bakuard.flashcards.controller;

import com.bakuard.flashcards.config.security.RequestContext;
import com.bakuard.flashcards.controller.message.Messages;
import com.bakuard.flashcards.dto.DtoMapper;
import com.bakuard.flashcards.dto.credential.*;
import com.bakuard.flashcards.dto.exceptions.ExceptionResponse;
import com.bakuard.flashcards.service.AuthService;
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
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Tag(name = "Учетные данные пользователей")
@RestController
@RequestMapping("/users")
public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class.getName());


    private AuthService authService;
    private DtoMapper mapper;
    private RequestContext requestContext;
    private Messages messages;

    @Autowired
    public AuthController(AuthService authService,
                          DtoMapper mapper,
                          RequestContext requestContext,
                          Messages messages) {
        this.authService = authService;
        this.mapper = mapper;
        this.requestContext = requestContext;
        this.messages = messages;
    }

    @Operation(summary = "Выполняет вход для указанного пользователя: возвращает jws если учетные данные верны.",
            responses = {
                    @ApiResponse(responseCode = "200"),
                    @ApiResponse(responseCode = "403",
                            description = "Если в учетных данных допущена ошибка",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = ExceptionResponse.class)))
            }
    )
    @PostMapping("/enter")
    public ResponseEntity<JwsResponse> enter(@RequestBody UserEnterRequest dto) {
        return ResponseEntity.ok(null);
    }

    @Operation(
            summary = "Регистрация нового пользователя.",
            description = """
                    Первый из двух шагов регистрации нового пользователя:
                     принимает учетные данные нового пользователя, проверяет их корректность и запрашивает
                     письмо с подтверждением на указанную почту.
                    """,
            responses = {
                    @ApiResponse(responseCode = "200"),
                    @ApiResponse(responseCode = "400",
                            description = """
                                    Если не удалось отправить письмо на почту, или нарушен хотя бы один из инвариантов
                                     тела запроса.
                                    """,
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = ExceptionResponse.class)))
            }
    )
    @PostMapping("/registration/firstStep")
    public ResponseEntity<String> registerFirstStep(@RequestBody UserAddRequest dto) {
        return ResponseEntity.ok(null);
    }

    @Operation(
            summary = "Регистрация нового пользователя.",
            description = "Завершающий шаг регистрации нового пользователя - проверка почты.",
            responses = {
                    @ApiResponse(responseCode = "200"),
                    @ApiResponse(responseCode = "401",
                            description = "Если передан не корректный токен завершения регистрации или токен не указан.",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = ExceptionResponse.class)))
            }
    )
    @PostMapping("/registration/finalStep")
    public ResponseEntity<JwsResponse> registerFinalStep() {
        return ResponseEntity.ok(null);
    }

    @Operation(
            summary = "Востановление учетных данных пользователя.",
            description = """
                    Первый из двух шагов востановления учетных данных пользователя:
                     принимает учетные данные и новый пароль пользователя, проверяет их корректность и запрашивает
                     письмо с подтверждением на указанную почту.
                    """,
            responses = {
                    @ApiResponse(responseCode = "200"),
                    @ApiResponse(responseCode = "400",
                            description = """
                                    Если не удалось отправить письмо на почту, или нарушен хотя бы один из инвариантов
                                     тела запроса.
                                    """,
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = ExceptionResponse.class)))
            }
    )
    @PostMapping("/restorePassword/firstStep")
    public ResponseEntity<String> restorePasswordFirstStep(@RequestBody PasswordRestoreRequest dto) {
        return ResponseEntity.ok(null);
    }

    @Operation(
            summary = "Востановление учетных данных пользователя.",
            description = "Завершающий шаг востановления учетных данных пользователя - проверка почты.",
            responses = {
                    @ApiResponse(responseCode = "200"),
                    @ApiResponse(responseCode = "403",
                            description = "Если передан не корректный токен востановления учетных данных или токен не указан.",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = ExceptionResponse.class)))
            }
    )
    @PostMapping("/restorePassword/finalStep")
    public ResponseEntity<JwsResponse> restorePasswordFinalStep() {
        return ResponseEntity.ok(null);
    }

    @Operation(summary = "Изменяет учетные данные пользователя.",
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
                            description = "Если не удалось найти пользователя по указанному идентификатору.",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = ExceptionResponse.class)))
            }
    )
    @PutMapping
    public ResponseEntity<UserResponse> update(@RequestBody UserUpdateRequest dto) {
        return ResponseEntity.ok(null);
    }

    @Operation(
            summary = "Возвращает пользователя в соответствии с токеном доступа.",
            responses = {
                    @ApiResponse(responseCode = "200"),
                    @ApiResponse(responseCode = "401",
                            description = "Если передан некорректный токен или токен не указан.",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = ExceptionResponse.class)))
            }
    )
    @GetMapping("/jws")
    public ResponseEntity<UserResponse> getUserByJws() {
        return ResponseEntity.ok(null);
    }

    @Operation(
            summary = "Возвращает пользователя по его идентификатору.",
            responses = {
                    @ApiResponse(responseCode = "200"),
                    @ApiResponse(responseCode = "401",
                            description = "Если передан некорректный токен или токен не указан.",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = ExceptionResponse.class))),
                    @ApiResponse(responseCode = "404",
                            description = "Если не удалось найти пользователя с указанным идентификатором.",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = ExceptionResponse.class)))
            }
    )
    @GetMapping("/id")
    public ResponseEntity<UserResponse> getUserById(
            @RequestParam
            @Parameter(description = "Уникальный идентификатор искомого пользователя.", required = true)
            UUID userId) {
        return ResponseEntity.ok(null);
    }

    @Operation(summary = "Возвращает часть учетных данных пользователей.",
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
    public ResponseEntity<Page<UserResponse>> findAllBy(
            @RequestParam
            @Parameter(description = "Номер страницы выборки. Нумерация начинается с нуля.", required = true)
            int page,
            @RequestParam
            @Parameter(description = "Размер страницы выборки. Диапозон значений - [1, 100].", required = true)
            int size,
            @RequestParam
            @Parameter(description = "Порядок сортировки.",
                    schema = @Schema(
                            defaultValue = "user_id.asc (Сортировка по идентификатору в порядке возрастания).",
                            allowableValues = {
                                    "user_id - сортировка по идентификатору",
                                    "email - сортировка по почте"
                            }
                    ))
            String sort) {
        return ResponseEntity.ok(null);
    }

    @Operation(
            summary = "Безвозвратное удаление всех данных пользователя.",
            description = """
                    Первый из двух шагов безвозвратного удаления всех данных пользователя:
                     принимает идентификатор удаляемого пользвоателя и запрашивает
                     письмо с подтверждением на указанную почту.
                    """,
            responses = {
                    @ApiResponse(responseCode = "200"),
                    @ApiResponse(responseCode = "400",
                            description = "Если не удалось отправить письмо на почту.",
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
    @DeleteMapping("/deletion/firstStep")
    public ResponseEntity<ResponseMessage<JwsResponse>> deleteFirstStep(@RequestParam UUID userId) {
        return ResponseEntity.ok(null);
    }

    @Operation(
            summary = "Безвозвратное удаление всех данных пользователя.",
            description = """
                    Завершающий из двух шагов безвозвратного удаления всех данных пользователя:
                     безвозвратно удаляет все данные пользователя.
                    """,
            responses = {
                    @ApiResponse(responseCode = "200"),
                    @ApiResponse(responseCode = "401",
                            description = "Если передан некорректный токен удаления учетных данных или токен не указан",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = ExceptionResponse.class))),
            }
    )
    @DeleteMapping("/deletion/finalStep")
    public ResponseEntity<String> deleteFinalStep() {
        return ResponseEntity.ok(null);
    }

}
