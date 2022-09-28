package com.bakuard.flashcards.model.credential;

import com.bakuard.flashcards.model.Entity;
import com.bakuard.flashcards.validation.IncorrectCredentials;
import com.bakuard.flashcards.validation.ValidatorUtil;
import com.google.common.hash.Hashing;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.PersistenceCreator;
import org.springframework.data.annotation.Transient;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.Objects;
import java.util.UUID;

@Table("users")
public class User implements Entity<User>{

    public static Builder newBuilder(ValidatorUtil validator) {
        return new Builder(validator);
    }


    @Id
    @Column("user_id")
    private UUID id;
    @Column("email")
    private String email;
    @Column("password_hash")
    private String passwordHash;
    @Column("salt")
    private final String salt;
    @Transient
    private ValidatorUtil validator;

    @PersistenceCreator
    public User(UUID id, String email, String passwordHash, String salt) {
        this.id = id;
        this.email = email;
        this.passwordHash = passwordHash;
        this.salt = salt;
    }

    private User(UUID id, String email, String password, String salt, ValidatorUtil validator) {
        validator.assertValid(
                new Email(email),
                new RawPassword(password)
        );

        this.id = id;
        this.email = email;
        this.passwordHash = calculatePasswordHash(password, salt);
        this.salt = salt;
        this.validator = validator;
    }

    @Override
    public UUID getId() {
        return id;
    }

    @Override
    public void setValidator(ValidatorUtil validator) {
        this.validator = validator;
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

    @Override
    public void generateIdIfAbsent() {
        if(id == null) id = UUID.randomUUID();
    }

    public void setPassword(String currentPassword, String newPassword) {
        validator.assertValid(new RawPassword(currentPassword), new RawPassword(newPassword));

        if(calculatePasswordHash(currentPassword, salt).equals(passwordHash)) {
            this.passwordHash = calculatePasswordHash(newPassword, salt);
        } else {
            throw new IncorrectCredentials();
        }
    }

    public void checkPassword(String currentPassword) {
        validator.assertValid(new RawPassword(currentPassword));

        if(!calculatePasswordHash(currentPassword, salt).equals(passwordHash)) {
            throw new IncorrectCredentials();
        }
    }

    public void setEmail(String email) {
        this.email = validator.assertValid(new Email(email)).asString();
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
                ", validator=" + validator +
                '}';
    }


    private String calculatePasswordHash(String password, String salt) {
        return Hashing.sha256().hashBytes(password.concat(salt).getBytes(StandardCharsets.UTF_8)).toString();
    }


    public static class Builder {

        private UUID userID;
        private String email;
        private String password;
        private String salt;
        private final ValidatorUtil validator;

        private Builder(ValidatorUtil validator) {
            this.salt = generateSalt();
            this.validator = validator;
        }

        public Builder setOrGenerateID(UUID userID) {
            this.userID = userID == null ? UUID.randomUUID() : userID;
            return this;
        }

        public Builder setEmail(String email) {
            this.email = email;
            return this;
        }

        public Builder setPassword(String password) {
            this.password = password;
            return this;
        }

        public Builder setOrGenerateSalt(String salt) {
            this.salt = salt == null ? generateSalt() : salt;
            return this;
        }

        public User build() {
            return new User(userID, email, password, salt, validator);
        }

        private String generateSalt() {
            return Base64.getEncoder().encodeToString(SecureRandom.getSeed(255));
        }

    }

}
