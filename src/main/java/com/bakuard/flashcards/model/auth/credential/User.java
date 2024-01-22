package com.bakuard.flashcards.model.auth.credential;

import com.bakuard.flashcards.model.Entity;
import com.bakuard.flashcards.validation.annotation.AllUnique;
import com.bakuard.flashcards.validation.annotation.NotContainsNull;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.PersistenceCreator;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.MappedCollection;
import org.springframework.data.relational.core.mapping.Table;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

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
    @jakarta.validation.constraints.Email(message = "User.email.format")
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
     * Cм. {@link  Entity#getId()}
     */
    @Override
    public UUID getId() {
        return id;
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
     * {@inheritDoc}
     */
    @Override
    public void setId(UUID id) {
        this.id = id;
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
     * Устанавливает хеш-пароля.
     * @return ссылку на этот же объект.
     */
    public User setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
        return this;
    }

    /**
     * Устанавливает новую соль для хеширования пароля.
     * @param salt новая соль для хеширования паролей.
     * @return ссылку на этот же объект.
     */
    public User setSalt(String salt) {
        this.salt = salt;
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
}
