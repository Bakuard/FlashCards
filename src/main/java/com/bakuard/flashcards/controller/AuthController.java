package com.bakuard.flashcards.controller;

import com.bakuard.flashcards.config.security.RequestContext;
import com.bakuard.flashcards.controller.message.Messages;
import com.bakuard.flashcards.dto.DtoMapper;
import com.bakuard.flashcards.dto.credential.*;
import com.bakuard.flashcards.dto.exceptions.ExceptionResponse;
import com.bakuard.flashcards.model.auth.JwsWithUser;
import com.bakuard.flashcards.model.auth.credential.Credential;
import com.bakuard.flashcards.model.auth.credential.Principal;
import com.bakuard.flashcards.model.auth.credential.User;
import com.bakuard.flashcards.model.auth.policy.Authorizer;
import com.bakuard.flashcards.model.auth.resource.Resource;
import com.bakuard.flashcards.service.AuthService;
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
    private Authorizer authorizer;

    @Autowired
    public AuthController(AuthService authService,
                          DtoMapper mapper,
                          RequestContext requestContext,
                          Messages messages,
                          Authorizer authorizer) {
        this.authService = authService;
        this.mapper = mapper;
        this.requestContext = requestContext;
        this.messages = messages;
        this.authorizer = authorizer;
    }

    @Operation(summary = "Выполняет вход для указанного пользователя: возвращает jws если учетные данные верны.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200"),
            @ApiResponse(responseCode = "403",
                    description = "Если в учетных данных допущена ошибка",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ExceptionResponse.class)))
    })
    @PostMapping("/enter")
    public ResponseEntity<JwsResponse> enter(@RequestBody UserEnterRequest dto) {
        logger.info("enter user with email '{}'", dto.getEmail());

        Credential credential = mapper.toCredential(dto);
        JwsWithUser jws = authService.enter(credential);

        return ResponseEntity.ok(mapper.toJwsResponse(jws));
    }

    @Operation(summary = "Регистрация нового пользователя.",
            description = """
                    Первый из двух шагов регистрации нового пользователя:
                     принимает учетные данные нового пользователя, проверяет их корректность и запрашивает
                     письмо с подтверждением на указанную почту.
                    """)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200"),
            @ApiResponse(responseCode = "400",
                    description = """
                                    Если не удалось отправить письмо на почту, или нарушен хотя бы один из инвариантов
                                     тела запроса.
                                    """,
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ExceptionResponse.class)))
    })
    @PostMapping("/registration/firstStep")
    public ResponseEntity<String> registerFirstStep(@RequestBody UserAddRequest dto) {
        logger.info("register new user with email '{}'. first step.", dto.getEmail());

        authService.registerFirstStep(mapper.toCredential(dto));

        return ResponseEntity.ok(messages.getMessage("auth.registration.firstStep"));
    }

    @Operation(
            summary = "Регистрация нового пользователя.",
            description = "Завершающий шаг регистрации нового пользователя - проверка почты.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200"),
            @ApiResponse(responseCode = "401",
                    description = "Если передан не корректный токен завершения регистрации или токен не указан.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ExceptionResponse.class)))
    })
    @SecurityRequirement(name = "JWTScheme")
    @PostMapping("/registration/finalStep")
    public ResponseEntity<JwsResponse> registerFinalStep() {
        Credential credential = requestContext.getCurrentJwsBodyAs(Credential.class);
        logger.info("register new user with email '{}'. final step.", credential.email());

        JwsWithUser jws = authService.registerFinalStep(credential);

        return ResponseEntity.ok(mapper.toJwsResponse(jws));
    }

    @Operation(summary = "Востановление учетных данных пользователя.",
            description = """
                    Первый из двух шагов востановления учетных данных пользователя:
                     принимает учетные данные и новый пароль пользователя, проверяет их корректность и запрашивает
                     письмо с подтверждением на указанную почту.
                    """)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200"),
            @ApiResponse(responseCode = "400",
                    description = """
                                    Если не удалось отправить письмо на почту, или нарушен хотя бы один из инвариантов
                                     тела запроса.
                                    """,
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ExceptionResponse.class)))
    })
    @PostMapping("/restorePassword/firstStep")
    public ResponseEntity<String> restorePasswordFirstStep(@RequestBody PasswordRestoreRequest dto) {
        logger.info("restore password for user with email '{}'. first step.", dto.getEmail());

        authService.restorePasswordFirstStep(mapper.toCredential(dto));

        return ResponseEntity.ok(messages.getMessage("auth.restorePassword.firstStep"));
    }

    @Operation(summary = "Востановление учетных данных пользователя.",
            description = "Завершающий шаг востановления учетных данных пользователя - проверка почты.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200"),
            @ApiResponse(responseCode = "403",
                    description = "Если передан не корректный токен востановления учетных данных или токен не указан.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ExceptionResponse.class)))
    })
    @SecurityRequirement(name = "JWTScheme")
    @PostMapping("/restorePassword/finalStep")
    public ResponseEntity<JwsResponse> restorePasswordFinalStep() {
        Credential credential = requestContext.getCurrentJwsBodyAs(Credential.class);
        logger.info("restore password for user with email '{}'. final step.", credential.email());

        JwsWithUser jws = authService.restorePasswordFinalStep(credential);

        return ResponseEntity.ok(mapper.toJwsResponse(jws));
    }

    @Operation(summary = "Изменяет учетные данные пользователя.")
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
                    description = "Если не удалось найти пользователя по указанному идентификатору.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ExceptionResponse.class)))
    })
    @SecurityRequirement(name = "JWTScheme")
    @PutMapping
    public ResponseEntity<UserResponse> update(@RequestBody UserUpdateRequest dto) {
        UUID userId = requestContext.getCurrentJwsBodyAs(UUID.class);
        logger.info("user {} update user with id={}", userId, dto.getUserId());
        authorizer.assertToHasAccess(userId, "user", userId, "update");

        User user = mapper.toUser(dto);
        authService.save(user);

        return ResponseEntity.ok(mapper.toUserResponse(user));
    }

    @Operation(summary = "Возвращает пользователя в соответствии с токеном доступа.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200"),
            @ApiResponse(responseCode = "401",
                    description = "Если передан некорректный токен или токен не указан.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ExceptionResponse.class)))
    })
    @SecurityRequirement(name = "JWTScheme")
    @GetMapping("/jws")
    public ResponseEntity<UserResponse> getUserByJws() {
        UUID userId = requestContext.getCurrentJwsBodyAs(UUID.class);
        logger.info("get user by jws where user id={}", userId);

        User user = authService.tryFindById(userId);

        return ResponseEntity.ok(mapper.toUserResponse(user));
    }

    @Operation(summary = "Возвращает пользователя по его идентификатору.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200"),
            @ApiResponse(responseCode = "401",
                    description = "Если передан некорректный токен или токен не указан.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ExceptionResponse.class))),
            @ApiResponse(responseCode = "404",
                    description = "Если не удалось найти пользователя с указанным идентификатором.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ExceptionResponse.class)))
    })
    @SecurityRequirement(name = "JWTScheme")
    @GetMapping("/id")
    public ResponseEntity<UserResponse> getUserById(
            @RequestParam
            @Parameter(description = "Уникальный идентификатор искомого пользователя.", required = true)
            UUID userId) {
        UUID jwsUserId = requestContext.getCurrentJwsBodyAs(UUID.class);
        logger.info("user {} get user with id={}", jwsUserId, userId);
        authorizer.assertToHasAccess(jwsUserId, "user", userId, "getUserById");

        User user = authService.tryFindById(userId);

        return ResponseEntity.ok(mapper.toUserResponse(user));
    }

    @Operation(summary = "Возвращает часть учетных данных пользователей.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200"),
            @ApiResponse(responseCode = "400",
                    description = "Если нарушен хотя бы один из инвариантов связаный с параметрами запроса",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ExceptionResponse.class))),
            @ApiResponse(responseCode = "401",
                    description = "Если передан некорректный токен или токен не указан",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ExceptionResponse.class)))
    })
    @SecurityRequirement(name = "JWTScheme")
    @GetMapping
    public ResponseEntity<Page<UserResponse>> findAllBy(
            @RequestParam
            @Parameter(description = "Номер страницы выборки. Нумерация начинается с нуля.", required = true)
            int page,
            @RequestParam(value = "size", required = false)
            @Parameter(description = "Размер страницы выборки. Диапозон значений - [1, 100].",
                    schema = @Schema(defaultValue = "20"))
            int size,
            @RequestParam(value = "sort", required = false)
            @Parameter(description = """
                    Задает порядок сортировки.
                    <br/></br>
                    Допустимые параметры (без учета регистра символов):
                    <ol>
                        <li>user_id - сортировка по идентификатору</li>
                        <li>email - сортировка по почте</li>
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
                    schema = @Schema(defaultValue = "user_id.asc"))
            String sort) {
        UUID userId = requestContext.getCurrentJwsBodyAs(UUID.class);
        logger.info("user {} find users by page={}, size={}, sort={}", userId, page, size, sort);
        authorizer.assertToHasAccess(Principal.of(userId), Resource.of("users"), "findAllBy");

        Page<User> users = authService.findAll(mapper.toPageable(page, size, mapper.toUserSort(sort)));

        return ResponseEntity.ok(mapper.toUsersResponse(users));
    }

    @Operation(summary = "Безвозвратное удаление всех данных пользователя.",
            description = """
                    Первый из двух шагов безвозвратного удаления всех данных пользователя:
                     принимает идентификатор удаляемого пользвоателя и запрашивает
                     письмо с подтверждением на указанную почту.
                    """)
    @ApiResponses(value = {
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
                    description = "Если не удалось найти пользователя с указанным идентификатором или почтой",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ExceptionResponse.class)))
    })
    @SecurityRequirement(name = "JWTScheme")
    @DeleteMapping("/deletion/firstStep")
    public ResponseEntity<String> deleteFirstStep(
            @RequestParam
            @Parameter(description = "Уникальный идентификатор удаляемого пользователя.", required = true)
            UUID userId,
            @RequestParam
            @Parameter(description = "Почта удаляемого пользователя.", required = true)
            String email) {
        UUID jwsUserId = requestContext.getCurrentJwsBodyAs(UUID.class);
        logger.info("user {} delete user with id={} and email={}. First step.", jwsUserId, userId, email);
        authorizer.assertToHasAccess(jwsUserId, "user", userId, "delete");

        authService.deletionFirstStep(userId, email);

        return ResponseEntity.ok(messages.getMessage("auth.deleteUser.firstStep"));
    }

    @Operation(summary = "Безвозвратное удаление всех данных пользователя.",
            description = """
                    Завершающий из двух шагов безвозвратного удаления всех данных пользователя:
                     безвозвратно удаляет все данные пользователя.
                    """)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200"),
            @ApiResponse(responseCode = "401",
                    description = "Если передан некорректный токен удаления учетных данных или токен не указан",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ExceptionResponse.class)))
    })
    @SecurityRequirement(name = "JWTScheme")
    @DeleteMapping("/deletion/finalStep")
    public ResponseEntity<String> deleteFinalStep() {
        UUID jwsUserId = requestContext.getCurrentJwsBodyAs(UUID.class);
        logger.info("delete user with id={}. Final step.", jwsUserId);

        authService.deletionFinalStep(jwsUserId);

        return ResponseEntity.ok(messages.getMessage("auth.deleteUser.finalStep"));
    }

}
