package com.bakuard.flashcards.model;

import com.google.common.hash.Hashing;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.PersistenceCreator;

import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.Objects;
import java.util.UUID;

public class User {

    @Id
    private UUID id;
    private String passwordHash;
    private final String salt;
    private String email;

    @PersistenceCreator
    public User(UUID id, String passwordHash, String salt, String email) {
        this.id = id;
        this.passwordHash = passwordHash;
        this.salt = salt;
        this.email = email;
    }

    public User(String email, String password) {
        this.email = email;
        salt = Base64.getEncoder().encodeToString(SecureRandom.getSeed(255));
        passwordHash = calculatePasswordHash(password, salt);
    }

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

    public void generateIdIfAbsent() {
        if(id == null) id = UUID.randomUUID();
    }

    public void setPassword(String currentPassword, String newPassword) {
        if(passwordHash.equals(calculatePasswordHash(currentPassword, salt))) {
            this.passwordHash = calculatePasswordHash(newPassword, salt);
        }
    }

    public void setEmail(String email) {
        this.email = email;
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
