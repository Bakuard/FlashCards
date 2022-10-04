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
import javax.validation.constraints.Email;
import javax.validation.constraints.NotNull;
import javax.validation.groups.Default;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.*;

@Table("users")
public class User implements Entity {

    public static BuilderCreator newBuilder(ValidatorUtil validator) {
        return new BuilderCreator(validator);
    }


    @Id
    @Column("user_id")
    private UUID id;
    @Column("email")
    @NotNull(message = "User.email.notNull", groups = Groups.A.class)
    @Email(message = "User.email.format", groups = Groups.B.class)
    private final String email;
    @Column("password_hash")
    private String passwordHash;
    @Column("salt")
    private final String salt;
    @MappedCollection(idColumn = "user_id", keyColumn = "index")
    @NotNull(message = "User.roles.notNull", groups = Groups.C.class)
    @NotContainsNull(message = "User.roles.notContainsNull", groups = Groups.D.class)
    @AllUnique(message = "User.roles.allUnique", nameOfGetterMethod = "name", groups = Groups.E.class)
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
                 String email,
                 String password,
                 String salt,
                 List<Role> roles,
                 ValidatorUtil validator) {
        this.id = id;
        this.email = email;
        this.salt = salt;
        this.roles = roles;
        this.validator = validator;

        validator.assertAllEmpty(this,
                validator.check(new RawPassword(password), Default.class),
                validator.check(this, Groups.A.class, Groups.B.class),
                validator.check(this, Groups.C.class, Groups.D.class, Groups.E.class, Default.class)
        );
        this.passwordHash = calculatePasswordHash(password, salt);
    }

    private User(UUID id,
                 String email,
                 String passwordHash,
                 PasswordChangeData passwordChangeData,
                 String salt,
                 List<Role> roles,
                 ValidatorUtil validator) {
        this.id = id;
        this.email = email;
        this.passwordHash = passwordHash;
        this.salt = salt;
        this.roles = roles;
        this.validator = validator;

        if(passwordChangeData != null) {
            validator.assertAllEmpty(this,
                    validator.check(passwordChangeData, Default.class),
                    validator.check(this, Groups.A.class, Groups.B.class),
                    validator.check(this, Groups.C.class, Groups.D.class, Groups.E.class, Default.class)
            );
            changePassword(passwordChangeData);
        } else {
            validator.assertAllEmpty(this,
                    validator.check(this, Groups.A.class, Groups.B.class),
                    validator.check(this, Groups.C.class, Groups.D.class, Groups.E.class, Default.class)
            );
        }
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

    public List<Role> getRoles() {
        return Collections.unmodifiableList(roles);
    }

    @Override
    public void generateIdIfAbsent() {
        if(id == null) id = UUID.randomUUID();
    }

    public void assertCurrentPassword(String currentPassword) {
        validator.assertValid(new RawPassword(currentPassword));

        if(!calculatePasswordHash(currentPassword, salt).equals(passwordHash)) {
            throw new IncorrectCredentials();
        }
    }

    public BuilderUpdater builder() {
        return new BuilderUpdater(id, passwordHash, salt, validator).
                setEmail(email).
                setRoles(roles);
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

    private void changePassword(PasswordChangeData data) {
        if(!calculatePasswordHash(data.currentPassword(), salt).equals(passwordHash)) {
            throw new IncorrectCredentials();
        }

        this.passwordHash = calculatePasswordHash(data.newPassword(), salt);
    }


    public static class BuilderCreator {

        private UUID userID;
        private String email;
        private String password;
        private String salt;
        private List<Role> roles;
        private final ValidatorUtil validator;

        private BuilderCreator(ValidatorUtil validator) {
            this.salt = generateSalt();
            this.roles = new ArrayList<>();
            this.validator = validator;
        }

        public BuilderCreator setOrGenerateID(UUID userID) {
            this.userID = userID == null ? UUID.randomUUID() : userID;
            return this;
        }

        public BuilderCreator setEmail(String email) {
            this.email = email;
            return this;
        }

        public BuilderCreator setPassword(String password) {
            this.password = password;
            return this;
        }

        public BuilderCreator setOrGenerateSalt(String salt) {
            this.salt = salt == null ? generateSalt() : salt;
            return this;
        }

        public BuilderCreator setRoles(List<Role> roles) {
            this.roles = roles == null ? null : new ArrayList<>(roles);
            return this;
        }

        public BuilderCreator addRole(Role role) {
            roles.add(role);
            return this;
        }

        public BuilderCreator addRole(String role) {
            roles.add(new Role(role));
            return this;
        }

        public User build() {
            return new User(userID, email, password, salt, roles, validator);
        }


        private String generateSalt() {
            return Base64.getEncoder().encodeToString(SecureRandom.getSeed(255));
        }

    }

    public static class BuilderUpdater {

        private final UUID userID;
        private String email;
        private final String passwordHash;
        private PasswordChangeData passwordChangeData;
        private final String salt;
        private List<Role> roles;
        private final ValidatorUtil validator;

        private BuilderUpdater(UUID userID,
                               String passwordHash,
                               String salt,
                               ValidatorUtil validator) {
            this.userID = userID;
            this.passwordHash = passwordHash;
            this.salt = salt;
            this.validator = validator;
        }

        public BuilderUpdater setEmail(String email) {
            this.email = email;
            return this;
        }

        public BuilderUpdater changePassword(PasswordChangeData passwordChangeData) {
            this.passwordChangeData = passwordChangeData;
            return this;
        }

        public BuilderUpdater setRoles(List<Role> roles) {
            this.roles = roles == null ? null : new ArrayList<>(roles);
            return this;
        }

        public BuilderUpdater addRole(Role role) {
            roles.add(role);
            return this;
        }

        public BuilderUpdater addRole(String role) {
            roles.add(new Role(role));
            return this;
        }

        public User build() {
            return new User(
                    userID,
                    email,
                    passwordHash,
                    passwordChangeData,
                    salt,
                    roles,
                    validator
            );
        }

    }

}
