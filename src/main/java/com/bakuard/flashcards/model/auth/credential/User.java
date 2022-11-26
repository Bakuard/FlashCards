package com.bakuard.flashcards.model.auth.credential;

import com.bakuard.flashcards.model.Entity;
import com.bakuard.flashcards.validation.*;
import com.google.common.hash.Hashing;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.PersistenceCreator;
import org.springframework.data.annotation.Transient;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.MappedCollection;
import org.springframework.data.relational.core.mapping.Table;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.*;

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

    @PersistenceCreator
    public User(UUID id, String email, String passwordHash, String salt, List<Role> roles) {
        this.id = id;
        this.email = email;
        this.passwordHash = passwordHash;
        this.salt = salt;
        this.roles = roles;
    }

    public User(Credential credential) {
        this.email = credential.email();
        this.salt = generateSalt();
        this.roles = new ArrayList<>();
        this.passwordHash = calculatePasswordHash(credential.password(), salt);
    }

    @Override
    public UUID getId() {
        return id;
    }

    @Override
    public boolean isNew() {
        return id == null;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public String getSalt() {
        return salt;
    }

    public String getEmail() {
        return email;
    }

    public List<Role> getRoles() {
        return Collections.unmodifiableList(roles);
    }

    public boolean hasRole(String role) {
        return roles.stream().anyMatch(r -> r.name().equals(role));
    }

    @Override
    public void generateIdIfAbsent() {
        if(id == null) id = UUID.randomUUID();
    }

    public User setEmail(String email) {
        this.email = email;
        return this;
    }

    public void assertCurrentPassword(String currentPassword) {
        if(!new PasswordConstraintValidator().isValid(currentPassword, null)) {
            throw new IncorrectCredentials("User.password.format");
        }

        if(!calculatePasswordHash(currentPassword, salt).equals(passwordHash)) {
            throw new IncorrectCredentials("User.password.incorrect");
        }
    }

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

    public User setCredential(Credential credential) {
        this.email = credential.email();
        this.passwordHash = calculatePasswordHash(credential.password(), salt);
        return this;
    }

    public User setOrGenerateSalt(String salt) {
        this.salt = salt == null ? generateSalt() : salt;
        return this;
    }

    public User setRoles(List<Role> roles) {
        this.roles.clear();
        if(roles != null) this.roles.addAll(roles);
        return this;
    }

    public User addRole(Role role) {
        roles.add(role);
        return this;
    }

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
