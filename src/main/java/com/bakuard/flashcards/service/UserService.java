package com.bakuard.flashcards.service;

import com.bakuard.flashcards.config.configData.ConfigData;
import com.bakuard.flashcards.dal.IntervalRepository;
import com.bakuard.flashcards.dal.UserRepository;
import com.bakuard.flashcards.dal.fragment.UserSaver;
import com.bakuard.flashcards.model.auth.credential.Credential;
import com.bakuard.flashcards.model.auth.credential.User;
import com.bakuard.flashcards.validation.ValidatorUtil;
import com.bakuard.flashcards.validation.exception.NotUniqueEntityException;
import com.bakuard.flashcards.validation.exception.UnknownEntityException;
import jakarta.validation.ConstraintViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

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
            //add default repeat intervals
            intervalRepository.addAll(user.getId(), 1, 3, 5, 11);
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

            transactionTemplate.execute(status -> save(
                    new User(validator.assertValid(credential)).
                            addRole(conf.superAdmin().roleName())
            ));
        } else if(conf.superAdmin().recreate()) {
            transactionTemplate.execute(status -> {
                User superAdmin = userRepository.findByRole(conf.superAdmin().roleName(), 1, 0).get(0);
                Credential credential = new Credential(conf.superAdmin().mail(), conf.superAdmin().password());
                superAdmin.setCredential(validator.assertValid(credential));
                save(superAdmin);
                return superAdmin;
            });
        }
    }
}
