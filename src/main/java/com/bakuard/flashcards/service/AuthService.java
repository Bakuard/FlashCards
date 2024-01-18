package com.bakuard.flashcards.service;

import com.bakuard.flashcards.config.configData.ConfigData;
import com.bakuard.flashcards.dal.IntervalRepository;
import com.bakuard.flashcards.dal.UserRepository;
import com.bakuard.flashcards.dal.fragment.UserSaver;
import com.bakuard.flashcards.model.auth.JwsWithUser;
import com.bakuard.flashcards.model.auth.credential.Credential;
import com.bakuard.flashcards.model.auth.credential.User;
import com.bakuard.flashcards.validation.ValidatorUtil;
import com.bakuard.flashcards.validation.exception.FailToSendMailException;
import com.bakuard.flashcards.validation.exception.IncorrectCredentials;
import com.bakuard.flashcards.validation.exception.NotUniqueEntityException;
import com.bakuard.flashcards.validation.exception.UnknownEntityException;
import jakarta.validation.ConstraintViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.UUID;

/**
 * Сервис отвечающий за процесс аутентификации и регистрации пользователей. Среди способов
 * регистрации и аутентификации поддерживаются: через почту.
 */
@Transactional
public class AuthService {

    private UserService userService;
    private JwsService jwsService;
    private EmailService emailService;
    private ConfigData conf;
    private ValidatorUtil validator;

    /**
     * Создает новый сервис аутентификации и регистрации пользователей.
     * @param userService сервис пользователей
     * @param jwsService сервис jws токенов
     * @param emailService сервис рассылки на почту писем подтверждения
     * @param conf общие данные конфигурации приложения
     * @param validator объект отвечающий за валидация входных данных пользователя
     */
    public AuthService(UserService userService,
                       JwsService jwsService,
                       EmailService emailService,
                       ConfigData conf,
                       ValidatorUtil validator) {
        this.userService = userService;
        this.jwsService = jwsService;
        this.emailService = emailService;
        this.conf = conf;
        this.validator = validator;
    }

    /**
     * Выполняет вход для указанных учетных данных и в случае успеха - возвращает подробную
     * информацию об учетных данных пользователя и JWS токен общего доступа. Если среди существующих
     * пользователей нет пользователя с такой почтой или указан неверный пароль - генерирует исключение.
     * @param credential учетные данные пользователя совершающего вход в приложение.
     * @return подробная информация об учетных данных пользователя и JWS токен общего доступ
     * @throws ConstraintViolationException если нарушен хотя бы один из инвариантов {@link Credential}
     * @throws UnknownEntityException если пользователя с указанной почтой не существует
     * @throws IncorrectCredentials см. {@link UserService#assertCurrentPassword(User, String)}
     * @see Credential
     * @see JwsWithUser
     */
    public JwsWithUser enter(Credential credential) {
        validator.assertValid(credential);
        User user = userService.tryFindByEmail(credential.email());
        userService.assertCurrentPassword(user, credential.password());
        String jws = jwsService.generateJws(user.getId(), "common", conf.jws().commonTokenLifeTime());
        return new JwsWithUser(user, jws);
    }

    /**
     * Первый из двух шагов регистрации. На этом шаге проверяется корректность создаваемых учетных
     * данных (требования к учетным данным см. {@link Credential}) и в случае успеха - на почту пользователя
     * отправляется письмо с подтверждением. В обратной ссылке указанной в письме будет содержаться JWS
     * токен для подтверждения регистрации, который требуется для выполнения второго шага регистрации.
     * @param credential учетные данные пользователя
     * @throws ConstraintViolationException если нарушен хотя бы один из инвариантов {@link Credential}
     * @throws FailToSendMailException см. {@link EmailService#confirmEmailForRegistration} вернет User.email.unique
     * @throws NotUniqueEntityException если уже есть пользователь с такой почтой.
     *                                  {@link NotUniqueEntityException#getMessageKey()} вернет User.email.unique
     * @see Credential
     */
    public void registerFirstStep(Credential credential) {
        validator.assertValid(credential);
        userService.assertUnique(credential.email());
        String jws = jwsService.generateJws(credential, "register", conf.jws().registrationTokenLifeTime());
        emailService.confirmEmailForRegistration(jws, credential.email());
    }

    /**
     * Последний из двух шагов регистрации. На этом шаге создается и сохраняется новый пользователь
     * с указанными на первом шаге (см {@link #registerFirstStep(Credential)}) учетными данными, а
     * затем возвращается вместе с JWS токеном общего доступа.
     * @param jwsBody учетные данные создаваемого пользователя
     * @return данные нового пользователя вместе с JWS токеном общего доступа.
     * @see JwsWithUser
     */
    public JwsWithUser registerFinalStep(Credential jwsBody) {
        User user = userService.createUserFromCredential(jwsBody);
        user = userService.save(user);

        String jws = jwsService.generateJws(user.getId(), "common", conf.jws().commonTokenLifeTime());
        return new JwsWithUser(user, jws);
    }

    /**
     * Первый из двух шагов восстановления пароля. На этом шаге проверяется корректность введенных учетных
     * данных (требования к учетным данным см. {@link Credential}) и в случае успеха - на почту пользователя
     * отправляется письмо с подтверждением. Новый пароль задается в указанных учетных данных. В обратной
     * ссылке указанной в письме будет содержаться JWS токен для подтверждения смены пароля, который требуется
     * для выполнения второго шага смены пароля.
     * @param credential учетные данные пользователя с новым паролем
     * @throws ConstraintViolationException если нарушен хотя бы один из инвариантов {@link Credential}
     * @throws FailToSendMailException см. {@link EmailService#confirmEmailForRestorePass}
     * @throws UnknownEntityException если пользователя с указанной почтой не существует.
     *                                {@link UnknownEntityException#getMessageKey()} вернет User.email.exists
     * @see Credential
     */
    public void restorePasswordFirstStep(Credential credential) {
        validator.assertValid(credential);
        userService.assertExists(credential.email());
        String jws = jwsService.generateJws(credential, "restorePassword", conf.jws().restorePassTokenLifeTime());
        emailService.confirmEmailForRestorePass(jws, credential.email());
    }

    /**
     * Последний из двух шагов восстановления пароля. На этом шаге пользователю с указанной на первом шаге почтой
     * задается указанный пароль. Затем метод возвращает обновленные данные пользователя вместе с JWS токеном
     * общего доступа.
     * @param jwsBody учетные данные пользователя, у которого обновили пароль
     * @return данные пользователя вместе с JWS токеном общего доступа.
     * @see JwsWithUser
     */
    public JwsWithUser restorePasswordFinalStep(Credential jwsBody) {
        User user = userService.tryFindByEmail(jwsBody.email());
        userService.changePasswordWithoutCheck(user, jwsBody.password());
        user = userService.save(user);
        String jws = jwsService.generateJws(user.getId(), "common", conf.jws().commonTokenLifeTime());
        return new JwsWithUser(user, jws);
    }

    /**
     * Первый из двух шагов удаления учетных данных пользователя и всех данных связанных с ними. На этом шаге
     * проверяется - существует ли пользователь с указанными данными и если да, то на почту пользователя
     * отправляется письмо с подтверждением удаления всех его данных. В обратной ссылке указанной в письме будет
     * содержаться JWS токен для подтверждения удаления пользователя, который требуется для выполнения второго
     * шага удаления пользователя.
     * @param userId идентификатор пользователя
     * @param email почта пользователя
     * @throws UnknownEntityException если пользователя с идентификатором userId и почтой email не существует.
     * @throws FailToSendMailException см. {@link EmailService#confirmEmailForDeletion}
     */
    public void deletionFirstStep(UUID userId, String email) {
        userService.assertExists(userId, email);
        String jws = jwsService.generateJws(userId, "delete", conf.jws().deleteUserTokenLifeTime());
        emailService.confirmEmailForDeletion(jws, email);
    }

    /**
     * Последний из двух шагов удаления учетных данных пользователя и всех данных связанных с ним. На этом шаге
     * происходит фактическое удаление всех данных пользователя с указанным идентификатор. Если пользователь
     * userId уже был удален - метод ничего не делает.
     * @param userId идентификатор удаляемого пользователя
     */
    public void deletionFinalStep(UUID userId) {
        userService.deleteById(userId);
    }
}
