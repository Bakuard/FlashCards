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
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.*;

@Table("users")
public class User implements Entity {

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
    @MappedCollection(idColumn = "user_id", keyColumn = "index")
    @NotContainsNull(message = "User.roles.notContainsNull")
    @AllUnique(message = "User.roles.allUnique", nameOfGetterMethod = "name")
    private final List<@Valid Role> roles;
    @Transient
    private ValidatorUtil validator;

    @PersistenceCreator
    public User(UUID id, String email, String passwordHash, String salt, List<Role> roles) {
        this.id = id;
        this.email = email;
        this.passwordHash = passwordHash;
        this.salt = salt;
        this.roles = roles;
    }

    private User(UUID id,
                 Credential credential,
                 String salt,
                 List<Role> roles,
                 ValidatorUtil validator) {
        this.id = id;
        this.email = credential.email();
        this.salt = salt;
        this.roles = roles;
        this.validator = validator;

        validator.assertValid(this, credential);
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

    public List<Role> getRoles() {
        return Collections.unmodifiableList(roles);
    }

    @Override
    public void generateIdIfAbsent() {
        if(id == null) id = UUID.randomUUID();
    }

    public void setEmail(String email) {
        validator.assertValid(new Email(email));

        this.email = email;
    }

    public void assertCurrentPassword(String currentPassword) {
        validator.assertValid(new RawPassword(currentPassword));

        if(!calculatePasswordHash(currentPassword, salt).equals(passwordHash)) {
            throw new IncorrectCredentials();
        }
    }

    public void changePassword(String currentPassword, String newPassword) {
        validator.assertValid(new PasswordChangeData(currentPassword, newPassword));

        if(!calculatePasswordHash(currentPassword, salt).equals(passwordHash)) {
            throw new IncorrectCredentials();
        }

        this.passwordHash = calculatePasswordHash(newPassword, salt);
    }

    public void setCredential(Credential credential) {
        validator.assertValid(credential);

        this.email = credential.email();
        this.passwordHash = calculatePasswordHash(credential.password(), salt);
    }

    public void setRoles(List<Role> roles) {
        if(roles != null) {
            this.roles.clear();
            this.roles.addAll(roles);
        }
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


    private String calculatePasswordHash(String newPassword, String salt) {
        return Hashing.sha256().hashBytes(newPassword.concat(salt).getBytes(StandardCharsets.UTF_8)).toString();
    }


    public static class Builder {

        private UUID userID;
        private Credential credential;
        private String salt;
        private final List<Role> roles;
        private final ValidatorUtil validator;

        private Builder(ValidatorUtil validator) {
            this.salt = generateSalt();
            this.credential = new Credential(null, null);
            this.roles = new ArrayList<>();
            this.validator = validator;
        }

        public Builder setOrGenerateID(UUID userID) {
            this.userID = userID == null ? UUID.randomUUID() : userID;
            return this;
        }

        public Builder setEmail(String email) {
            this.credential = new Credential(email, credential.password());
            return this;
        }

        public Builder setPassword(String password) {
            this.credential = new Credential(credential.email(), password);
            return this;
        }

        public Builder setCredential(Credential credential) {
            if(credential != null) this.credential = credential;
            return this;
        }

        public Builder setOrGenerateSalt(String salt) {
            this.salt = salt == null ? generateSalt() : salt;
            return this;
        }

        public Builder setRoles(List<Role> roles) {
            if(roles != null) {
                this.roles.clear();
                this.roles.addAll(roles);
            }
            return this;
        }

        public Builder addRole(Role role) {
            roles.add(role);
            return this;
        }

        public Builder addRole(String role) {
            roles.add(new Role(role));
            return this;
        }

        public User build() {
            return new User(userID, credential, salt, roles, validator);
        }


        private String generateSalt() {
            return Base64.getEncoder().encodeToString(SecureRandom.getSeed(255));
        }

    }

}
