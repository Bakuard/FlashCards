package com.bakuard.flashcards.model.credential;

import com.bakuard.flashcards.model.Entity;
import com.google.common.hash.Hashing;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.PersistenceCreator;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.Objects;
import java.util.UUID;

@Table("users")
public class User implements Entity<User> {

    @Id
    @Column("user_id")
    private UUID id;
    @Column("password_hash")
    private String passwordHash;
    @Column("salt")
    private final String salt;
    @Column("email")
    private String email;

    @PersistenceCreator
    public User(UUID id, String passwordHash, String salt, String email) {
        this.id = id;
        this.passwordHash = passwordHash;
        this.salt = salt;
        this.email = email;
    }

    public User(Credential credential) {
        this.email = credential.getEmail().asString();
        salt = Base64.getEncoder().encodeToString(SecureRandom.getSeed(255));
        passwordHash = calculatePasswordHash(credential.getRawPassword().asString(), salt);
    }

    @Override
    public UUID getId() {
        return id;
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

    public void setPassword(RawPassword currentPassword, RawPassword newPassword) {
        if(calculatePasswordHash(currentPassword.asString(), salt).equals(passwordHash)) {
            this.passwordHash = calculatePasswordHash(newPassword.asString(), salt);
        } else {
            throw new IncorrectCredentials();
        }
    }

    public void setEmail(Email email) {
        this.email = email.asString();
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
                ", passwordHash='" + passwordHash + '\'' +
                ", salt='" + salt + '\'' +
                ", email='" + email + '\'' +
                '}';
    }


    private String calculatePasswordHash(String password, String salt) {
        return Hashing.sha256().hashBytes(password.concat(salt).getBytes(StandardCharsets.UTF_8)).toString();
    }

}
