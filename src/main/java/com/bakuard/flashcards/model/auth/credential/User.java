package com.bakuard.flashcards.model.auth.credential;

import com.bakuard.flashcards.model.Entity;
import com.bakuard.flashcards.validation.AllUnique;
import com.bakuard.flashcards.validation.IncorrectCredentials;
import com.bakuard.flashcards.validation.NotContainsNull;
import com.bakuard.flashcards.validation.PasswordConstraintValidator;
import com.google.common.hash.Hashing;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.PersistenceCreator;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.MappedCollection;
import org.springframework.data.relational.core.mapping.Table;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.*;

/**
 * Представление пользователя с точки зрения бизнес логики.
 */
@Table("users")
public class User implements Entity {

    @Id
    @Column("user_id")
    private UUID id;
    @Column("email")
    @NotNull(message = "User.email.notNull")
    @javax.validation.constraints.Email(message = "User.email.format")
    private String email;
    @Column("password_hash")
    private String passwordHash;
    @Column("salt")
    private String salt;
    @MappedCollection(idColumn = "user_id", keyColumn = "index")
    @NotContainsNull(message = "User.roles.notContainsNull")
    @AllUnique(message = "User.roles.allUnique", nameOfGetterMethod = "name")
    private final List<@Valid Role> roles;

    /**
     * Данный конструктор используется слоем доступа к данным для загрузки пользователя.
     * @param id уникальный идентификатор пользователя.
     * @param email почта пользователя.
     * @param passwordHash хеш пароля пользователя.
     * @param salt соль используемая при хешировании пароля.
     * @param roles роли пользователя.
     */
    @PersistenceCreator
    public User(UUID id, String email, String passwordHash, String salt, List<Role> roles) {
        this.id = id;
        this.email = email;
        this.passwordHash = passwordHash;
        this.salt = salt;
        this.roles = roles;
    }

    /**
     * Создает нового пользователя на основе указанных учетных данных.
     * @param credential учетные данные пользователя.
     */
    public User(Credential credential) {
        this.email = credential.email();
        this.salt = generateSalt();
        this.roles = new ArrayList<>();
        this.passwordHash = calculatePasswordHash(credential.password(), salt);
    }

    /**
     * см. {@link  Entity#getId()}
     */
    @Override
    public UUID getId() {
        return id;
    }

    /**
     * см. {@link  Entity#isNew()} ()}
     */
    @Override
    public boolean isNew() {
        return id == null;
    }

    /**
     * Возвращает хеш пароля пользователя.
     * @return хеш пароля пользователя.
     */
    public String getPasswordHash() {
        return passwordHash;
    }

    /**
     * Возвращает соль для хеширования пароля пользователя.
     * @return соль для хеширования пароля пользователя.
     */
    public String getSalt() {
        return salt;
    }

    /**
     * Возвращает почту пользователя.
     * @return почта пользователя.
     */
    public String getEmail() {
        return email;
    }

    /**
     * Возвращает неизменяемый список всех ролей пользователя.
     * @return список всех ролей пользователя.
     */
    public List<Role> getRoles() {
        return Collections.unmodifiableList(roles);
    }

    /**
     * Проверяет - имеет ли пользователь указанную роль.
     * @param role роль пользователя.
     * @return true - если условие выше выполняется, иначе - false.
     */
    public boolean hasRole(String role) {
        return roles.stream().anyMatch(r -> r.name().equals(role));
    }

    /**
     * см. {@link  Entity#generateIdIfAbsent()} ()}
     */
    @Override
    public void generateIdIfAbsent() {
        if(id == null) id = UUID.randomUUID();
    }

    /**
     * Устанавливает почту пользователя.
     * @param email почта пользователя.
     * @return ссылка на этот же объект.
     */
    public User setEmail(String email) {
        this.email = email;
        return this;
    }

    /**
     * Проверяет - является ли указанный пароль текущим паролем пользователя. Если это не так - выбрасывает
     * исключение.
     * @param currentPassword предполагаемый текущий пароль пользователя.
     * @throws IncorrectCredentials Выбрасывается, если выполняется хотя бы одно из следующих условий:<br/>
     *                              1. если указан неверный текущий пароль. <br/>
     *                              2. если текущий пароль равен null. <br/>
     *                              {@link IncorrectCredentials#getMessageKey()} вернет User.password.notNull
     *                              или User.password.incorrect
     */
    public void assertCurrentPassword(String currentPassword) {
        if(currentPassword == null) {
            throw new IncorrectCredentials("User.password.notNull");
        }

        if(!calculatePasswordHash(currentPassword, salt).equals(passwordHash)) {
            throw new IncorrectCredentials("User.password.incorrect");
        }
    }

    /**
     * Изменяет текущий пароль пользователя на указанный, при условии, что указан корректный текущий пароль
     * пользователя. Иначе выбрасывает исключение.
     * @param currentPassword предполагаемый текущий пароль пользователя.
     * @param newPassword новый пароль пользователя.
     * @return ссылку на этот же объект.
     * @throws IncorrectCredentials Выбрасывается, если выполняется хотя бы одно из следующих условий:<br/>
     *                              1. если указан неверный текущий пароль. <br/>
     *                              2. если текущий пароль равен null. <br/>
     *                              3. если новый пароль равен null. <br/>
     *                              4. если новый пароль не содержит отображаемых символов. <br/>
     *                              5. если длина нового пароля не принадлежит промежутку [8; 50]. <br/>
     *                              {@link IncorrectCredentials#getMessageKey()} вернет User.password.notNull,
     *                              User.password.incorrect или User.newPassword.format
     */
    public User changePassword(String currentPassword, String newPassword) {
        if(!new PasswordConstraintValidator().isValid(newPassword, null)) {
            throw new IncorrectCredentials("User.newPassword.format");
        }

        if(currentPassword == null) {
            throw new IncorrectCredentials("User.password.notNull");
        }

        if(!calculatePasswordHash(currentPassword, salt).equals(passwordHash)) {
            throw new IncorrectCredentials("User.password.incorrect");
        }

        this.passwordHash = calculatePasswordHash(newPassword, salt);
        return this;
    }

    /**
     * Устанавливает новые учетные данные для пользователя (см. {@link Credential}).
     * @param credential новые учетные данные пользователя.
     * @return ссылку на этот же объект.
     */
    public User setCredential(Credential credential) {
        this.email = credential.email();
        this.passwordHash = calculatePasswordHash(credential.password(), salt);
        return this;
    }

    /**
     * Устанавливает новую соль для хеширования пароля. Используется при тестировании.
     * @param salt новая соль для хеширования паролей.
     * @return ссылку на этот же объект.
     */
    public User setOrGenerateSalt(String salt) {
        this.salt = salt == null ? generateSalt() : salt;
        return this;
    }

    /**
     * Устанавливает список ролей пользователя.
     * @param roles список ролей пользователя.
     * @return ссылку на этот же объект.
     */
    public User setRoles(List<Role> roles) {
        this.roles.clear();
        if(roles != null) this.roles.addAll(roles);
        return this;
    }

    /**
     * Добавляет новую роль пользователю.
     * @param role новая роль пользователя.
     * @return ссылку на этот же объект.
     */
    public User addRole(Role role) {
        roles.add(role);
        return this;
    }

    /**
     * Добавляет новую роль пользователю.
     * @param role наименование новой роли пользователя.
     * @return ссылку на этот же объект.
     */
    public User addRole(String role) {
        roles.add(new Role(role));
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return Objects.equals(id, user.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", email='" + email + '\'' +
                ", passwordHash='" + passwordHash + '\'' +
                ", salt='" + salt + '\'' +
                ", roles=" + roles +
                '}';
    }


    private String calculatePasswordHash(String newPassword, String salt) {
        return Hashing.sha256().hashBytes(newPassword.concat(salt).getBytes(StandardCharsets.UTF_8)).toString();
    }

    private String generateSalt() {
        return Base64.getEncoder().encodeToString(SecureRandom.getSeed(255));
    }

}
