package com.bakuard.flashcards.service;

import com.bakuard.flashcards.config.configData.ConfigData;
import com.bakuard.flashcards.dal.IntervalRepository;
import com.bakuard.flashcards.dal.UserRepository;
import com.bakuard.flashcards.dal.fragment.UserSaver;
import com.bakuard.flashcards.model.auth.credential.Credential;
import com.bakuard.flashcards.model.auth.credential.User;
import com.bakuard.flashcards.validation.ValidatorUtil;
import com.bakuard.flashcards.validation.annotation.PasswordConstraintValidator;
import com.bakuard.flashcards.validation.exception.IncorrectCredentials;
import com.bakuard.flashcards.validation.exception.NotUniqueEntityException;
import com.bakuard.flashcards.validation.exception.UnknownEntityException;
import com.google.common.hash.Hashing;
import jakarta.validation.ConstraintViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.UUID;

/**
 * Сервис отвечающий за управление учетными данными пользователей.
 */
@Transactional
public class UserService {

    private UserRepository userRepository;
    private IntervalRepository intervalRepository;
    private ConfigData conf;
    private ValidatorUtil validator;
    private TransactionTemplate transactionTemplate;

    /**
     * Создает новый сервис управления учетными данными.
     * @param userRepository репозиторий учетных данных пользователей
     * @param intervalRepository репозиторий интервалов повторений
     * @param conf общие данные конфигурации приложения
     * @param validator объект отвечающий за валидация входных данных пользователя
     */
    public UserService(UserRepository userRepository,
                       IntervalRepository intervalRepository,
                       ConfigData conf,
                       ValidatorUtil validator,
                       TransactionTemplate transactionTemplate) {
        this.userRepository = userRepository;
        this.intervalRepository = intervalRepository;
        this.conf = conf;
        this.validator = validator;
        this.transactionTemplate = transactionTemplate;
    }

    /**
     * Делегирует вызов методу {@link com.bakuard.flashcards.dal.fragment.UserSaver#save(Object)} добавляя
     * предварительную валидацию данных пользователя.
     * @throws NotUniqueEntityException см. {@link UserSaver#save(Object)}
     * @throws ConstraintViolationException если нарушен хотя бы один из инвариантов {@link User}
     * @see com.bakuard.flashcards.dal.fragment.UserSaver
     */
    public User save(User user) {
        validator.assertValid(user);
        boolean isUserNew = user.isNew();
        user = userRepository.save(user);
        if(isUserNew) {
            intervalRepository.addAll(user.getId(), conf.repetition().defaultIntervals());
        }
        return user;
    }

    /**
     * Удаляет пользователя по его идентификатору.
     */
    public void deleteById(UUID userId) {
        userRepository.deleteById(userId);
    }

    /**
     * Проверяет - существует ли пользователь с таким идентификатором. Если это не так - выбрасывает исключение,
     * иначе - ничего не делает.
     * @param userId идентификатор искомого пользователя
     * @throws UnknownEntityException если пользователя с указанным идентификатором не существует.
     *                                {@link UnknownEntityException#getMessageKey()} вернет User.unknownId
     */
    public void assertExists(UUID userId) {
        if(!userRepository.existsById(userId)) {
            throw new UnknownEntityException(
                    "Unknown user with id=" + userId,
                    "User.unknownId"
            );
        }
    }

    /**
     * Проверяет - существует ли пользователь с таким идентификатором и почтой. Если это не так - выбрасывает
     * исключение, иначе - ничего не делает.
     * @param userId идентификатор искомого пользователя
     * @param email почта искомого пользователя
     * @throws UnknownEntityException если пользователя с указанным идентификатором и почтой не существует.
     *                                {@link UnknownEntityException#getMessageKey()} вернет User.unknownIdAndEmail
     */
    public void assertExists(UUID userId, String email) {
        if(userRepository.findById(userId).
                map(user -> !user.getEmail().equals(email)).
                orElse(true)) {
            throw new UnknownEntityException(
                    "Unknown user with id=" + userId + " and email='" + email + '\'',
                    "User.unknownIdAndEmail"
            );
        }
    }

    /**
     * Проверяет - существует ли пользователь с такой почтой. Если это не так - выбрасывает исключение,
     * иначе - ничего не делает.
     * @throws UnknownEntityException если пользователя с указанной почтой не существует.
     *                                {@link UnknownEntityException#getMessageKey()} вернет User.email.exists
     */
    public void assertExists(String email) {
        if(!userRepository.existsByEmail(email)) {
            throw new UnknownEntityException(
                    "Unknown user with email ='" + email + '\'', "User.email.exists");
        }
    }

    /**
     * Проверяет - существует ли уже пользователь с такой почтой. Если это так - выбрасывает
     * исключение, иначе - ничего не делает.
     * @throws NotUniqueEntityException если уже существует пользователь с такой почтой.
     */
    public void assertUnique(String email) {
        if(userRepository.existsByEmail(email)) {
            throw new NotUniqueEntityException(
                    "Use with email '" + email + "' already exists",
                    "User.email.unique");
        }
    }

    /**
     * Возвращает пользователя по его идентификатору. Если такого пользователя нет - выбрасывает исключение.
     * @param userId идентификатор искомого пользователя
     * @return пользователя по его идентификатору
     * @throws UnknownEntityException если пользователя с таким идентификатором не существует.
     *                                {@link UnknownEntityException#getMessageKey()} вернет User.unknownId
     */
    public User tryFindById(UUID userId) {
        return userRepository.findById(userId).orElseThrow(
                () -> new UnknownEntityException(
                        "Unknown user with id=" + userId,
                        "User.unknownId"
                )
        );
    }

    /**
     * Возвращает пользователя по его почте. Если такого пользователя нет - выбрасывает исключение.
     * @param email почта искомого пользователя
     * @return пользователя по его почте
     * @throws UnknownEntityException если пользователя с такой почтой не существует.
     *                                {@link UnknownEntityException#getMessageKey()} вернет User.unknownEmail
     */
    public User tryFindByEmail(String email) {
        return userRepository.findByEmail(email).orElseThrow(
                () -> new UnknownEntityException(
                        "Unknown user with email=" + email,
                        "User.unknownEmail"
                )
        );
    }

    /**
     * Возвращает указанную часть всех пользователей.
     * @param pageable параметры пагинации и сортировки
     * @return указанную часть всех пользователей.
     */
    public Page<User> findAll(Pageable pageable) {
        return userRepository.findAll(pageable);
    }

    /**
     * Если в постоянном хранилище отсутствует учетная запись супер-администратора, то создает её. <br/>
     * Иначе, если в конфигурации приложения задано пересоздание этой учетной записи - пересоздает её. <br/>
     * Иначе, ничего не делает.
     */
    public void createOrReplaceSuperAdminIfNecessary() {
        long countUserWithRole = userRepository.countForRole(conf.superAdmin().roleName());
        if(countUserWithRole < 1) {
            Credential credential = new Credential(conf.superAdmin().mail(), conf.superAdmin().password());
            validator.assertValid(credential);

            transactionTemplate.execute(status -> save(
                    createUserFromCredential(credential).addRole(conf.superAdmin().roleName())
            ));
        } else if(conf.superAdmin().recreate()) {
            transactionTemplate.execute(status -> {
                User superAdmin = userRepository.findByRole(conf.superAdmin().roleName(), 1, 0).get(0);
                Credential credential = new Credential(conf.superAdmin().mail(), conf.superAdmin().password());
                validator.assertValid(credential);
                superAdmin.setEmail(credential.email());
                changePasswordWithoutCheck(superAdmin, credential.password());
                save(superAdmin);
                return superAdmin;
            });
        }
    }

    /**
     * Проверяет - является ли указанный пароль текущим паролем пользователя. Если это не так - выбрасывает
     * исключение.
     * @param user пользователь, для которого выполняется проверка на корректность текущего пароля.
     * @param currentPassword предполагаемый текущий пароль пользователя.
     * @throws IncorrectCredentials Выбрасывается, если выполняется хотя бы одно из следующих условий:<br/>
     *                              1. если указан неверный текущий пароль. <br/>
     *                              2. если текущий пароль равен null. <br/>
     *                              {@link IncorrectCredentials#getMessageKey()} вернет User.password.notNull
     *                              или User.password.incorrect
     */
    public void assertCurrentPassword(User user, String currentPassword) {
        if(currentPassword == null) {
            throw new IncorrectCredentials(
                    "currentPassword can't be null", "User.password.notNull");
        }

        if(!calculatePasswordHash(currentPassword, user.getSalt()).equals(user.getPasswordHash())) {
            throw new IncorrectCredentials(
                    "Incorrect current password", "User.password.incorrect");
        }
    }

    /**
     * Изменяет текущий пароль пользователя на указанный, при условии, что указан корректный текущий пароль
     * пользователя. Иначе выбрасывает исключение.
     * @param user пользователь, у которого меняется пароль.
     * @param currentPassword предполагаемый текущий пароль пользователя.
     * @param newPassword новый пароль пользователя.
     * @throws IncorrectCredentials Выбрасывается, если выполняется хотя бы одно из следующих условий:<br/>
     *                              1. если указан неверный текущий пароль. <br/>
     *                              2. если текущий пароль равен null. <br/>
     *                              3. если новый пароль равен null. <br/>
     *                              4. если новый пароль не содержит отображаемых символов. <br/>
     *                              5. если длина нового пароля не принадлежит промежутку [8; 50]. <br/>
     *                              {@link IncorrectCredentials#getMessageKey()} вернет User.password.notNull,
     *                              User.password.incorrect или User.newPassword.format
     */
    public void changePassword(User user, String currentPassword, String newPassword) {
        if(!new PasswordConstraintValidator().isValid(newPassword, null)) {
            throw new IncorrectCredentials(
                    "Incorrect password format", "User.newPassword.format");
        }

        if(currentPassword == null) {
            throw new IncorrectCredentials(
                    "currentPassword can't be null", "User.password.notNull");
        }

        if(!calculatePasswordHash(currentPassword, user.getSalt()).equals(user.getPasswordHash())) {
            throw new IncorrectCredentials(
                    "Incorrect current password", "User.password.incorrect");
        }

        user.setPasswordHash(calculatePasswordHash(newPassword, user.getSalt()));
    }

    /**
     * Изменяет текущий пароль пользователя на указанный, не проверяя на текущий.
     * @param user пользователь, у которого меняется пароль.
     * @param newPassword новый пароль.
     */
    public void changePasswordWithoutCheck(User user, String newPassword) {
        user.setPasswordHash(calculatePasswordHash(newPassword, user.getSalt()));
    }

    /**
     * Создает нового пользователя из указанных учетных данных.
     * @param credential учетные данные нового пользователя.
     * @return нового пользователя.
     */
    public User createUserFromCredential(Credential credential) {
        validator.assertValid(credential);
        String salt = generateSalt();
        return new User(
                null,
                credential.email(),
                calculatePasswordHash(credential.password(), salt),
                salt,
                new ArrayList<>()
        );
    }


    private String calculatePasswordHash(String newPassword, String salt) {
        return Hashing.sha256().hashBytes(newPassword.concat(salt).getBytes(StandardCharsets.UTF_8)).toString();
    }

    private String generateSalt() {
        return Base64.getEncoder().encodeToString(SecureRandom.getSeed(255));
    }
}
