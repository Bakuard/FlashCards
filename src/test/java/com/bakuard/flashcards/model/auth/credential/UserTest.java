package com.bakuard.flashcards.model.auth.credential;

import com.bakuard.flashcards.config.SpringConfig;
import com.bakuard.flashcards.config.TestConfig;
import com.bakuard.flashcards.validation.IncorrectCredentials;
import com.bakuard.flashcards.validation.ValidatorUtil;
import org.assertj.core.api.Assertions;
import org.assertj.core.api.InstanceOfAssertFactories;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import java.util.stream.Collectors;

@ExtendWith(SpringExtension.class)
@TestPropertySource(locations = "classpath:test.properties")
@Import({SpringConfig.class, TestConfig.class})
class UserTest {

    @Autowired
    private ValidatorUtil validator;

    @Test
    @DisplayName("""
            new Credential:
             email is null,
             password is null
            => exception
            """)
    public void newCredential1() {
        Assertions.
                assertThatExceptionOfType(ConstraintViolationException.class).
                isThrownBy(() -> validator.assertValid(new Credential(null, null))).
                extracting(ex -> ex.getConstraintViolations().stream().
                                map(ConstraintViolation::getMessage).
                                collect(Collectors.toList()),
                        InstanceOfAssertFactories.collection(String.class)).
                containsExactlyInAnyOrder("Credential.password.format",
                        "Credential.email.notNull");
    }

    @Test
    @DisplayName("""
            new Credential:
             email string is not email format,
             password is blank
            => exception
            """)
    public void newCredential2() {
        Assertions.
                assertThatExceptionOfType(ConstraintViolationException.class).
                isThrownBy(() -> validator.assertValid(new Credential("asdf", "     "))).
                extracting(ex -> ex.getConstraintViolations().stream().
                                map(ConstraintViolation::getMessage).
                                collect(Collectors.toList()),
                        InstanceOfAssertFactories.collection(String.class)).
                containsExactlyInAnyOrder("Credential.password.format",
                        "Credential.email.format");
    }

    @Test
    @DisplayName("""
            new Credential:
             email is blank,
             password length < 8
            => exception
            """)
    public void newCredential3() {
        Assertions.
                assertThatExceptionOfType(ConstraintViolationException.class).
                isThrownBy(() -> validator.assertValid(new Credential("       ", "1234567"))).
                extracting(ex -> ex.getConstraintViolations().stream().
                                map(ConstraintViolation::getMessage).
                                collect(Collectors.toList()),
                        InstanceOfAssertFactories.collection(String.class)).
                containsExactlyInAnyOrder("Credential.password.format",
                        "Credential.email.format");
    }

    @Test
    @DisplayName("""
            new Credential:
             email is correct,
             password length > 50
            => exception
            """)
    public void newCredential4() {
        Assertions.
                assertThatExceptionOfType(ConstraintViolationException.class).
                isThrownBy(() -> validator.assertValid(new Credential("me@mail.com",
                        "012345678901234567890123456789012345678901234567891"))).
                extracting(ex -> ex.getConstraintViolations().stream().
                                map(ConstraintViolation::getMessage).
                                collect(Collectors.toList()),
                        InstanceOfAssertFactories.collection(String.class)).
                containsExactlyInAnyOrder("Credential.password.format");
    }

    @Test
    @DisplayName("""
            new User:
             roles contains null
             => exception
            """)
    public void newUser1() {
        Assertions.
                assertThatExceptionOfType(ConstraintViolationException.class).
                isThrownBy(() -> validator.assertValid(
                        new User(new Credential("me@mail.com", "password")).
                                addRole((Role) null).
                                addRole("role1")
                )).
                extracting(ex -> ex.getConstraintViolations().stream().
                                map(ConstraintViolation::getMessage).
                                collect(Collectors.toList()),
                        InstanceOfAssertFactories.collection(String.class)).
                containsExactlyInAnyOrder("User.roles.notContainsNull");
    }

    @Test
    @DisplayName("""
            new User:
             roles contains duplicates
             => exception
            """)
    public void newUser2() {
        Assertions.
                assertThatExceptionOfType(ConstraintViolationException.class).
                isThrownBy(() -> validator.assertValid(
                        new User(new Credential("me@mail.com", "password")).
                                addRole("role1").
                                addRole("role1")
                )).
                extracting(ex -> ex.getConstraintViolations().stream().
                                map(ConstraintViolation::getMessage).
                                collect(Collectors.toList()),
                        InstanceOfAssertFactories.collection(String.class)).
                containsExactlyInAnyOrder("User.roles.allUnique");
    }

    @Test
    @DisplayName("""
            new User:
             some role is blank
             => exception
            """)
    public void newUser3() {
        Assertions.
                assertThatExceptionOfType(ConstraintViolationException.class).
                isThrownBy(() -> validator.assertValid(
                        new User(new Credential("me@mail.com", "password")).
                                addRole("role1").
                                addRole("      ")
                )).
                extracting(ex -> ex.getConstraintViolations().stream().
                                map(ConstraintViolation::getMessage).
                                collect(Collectors.toList()),
                        InstanceOfAssertFactories.collection(String.class)).
                containsExactlyInAnyOrder("Role.name.notBlank");
    }

    @Test
    @DisplayName("""
            checkPassword(currentPassword):
             currentPassword is null
             => exception
            """)
    public void checkPassword1() {
        User user = new User(new Credential("me@gmail.com", "password"));

        Assertions.
                assertThatExceptionOfType(IncorrectCredentials.class).
                isThrownBy(() -> user.assertCurrentPassword(null)).
                extracting(IncorrectCredentials::getMessageKey, InstanceOfAssertFactories.type(String.class)).
                isEqualTo("User.password.format");
    }

    @Test
    @DisplayName("""
            checkPassword(currentPassword):
             currentPassword is blank
             => exception
            """)
    public void checkPassword2() {
        User user = new User(new Credential("me@gmail.com", "password"));

        Assertions.
                assertThatExceptionOfType(IncorrectCredentials.class).
                isThrownBy(() -> user.assertCurrentPassword("       ")).
                extracting(IncorrectCredentials::getMessageKey, InstanceOfAssertFactories.type(String.class)).
                isEqualTo("User.password.format");
    }

    @Test
    @DisplayName("""
            checkPassword(currentPassword):
             currentPassword less than 8
             => exception
            """)
    public void checkPassword3() {
        User user = new User(new Credential("me@gmail.com", "password"));

        Assertions.
                assertThatExceptionOfType(IncorrectCredentials.class).
                isThrownBy(() -> user.assertCurrentPassword("1234567")).
                extracting(IncorrectCredentials::getMessageKey, InstanceOfAssertFactories.type(String.class)).
                isEqualTo("User.password.format");
    }

    @Test
    @DisplayName("""
            checkPassword(currentPassword):
             currentPassword greater than 50
             => exception
            """)
    public void checkPassword4() {
        User user = new User(new Credential("me@gmail.com", "password"));

        Assertions.
                assertThatExceptionOfType(IncorrectCredentials.class).
                isThrownBy(() ->
                        user.assertCurrentPassword("012345678901234567890123456789012345678901234567891")).
                extracting(IncorrectCredentials::getMessageKey, InstanceOfAssertFactories.type(String.class)).
                isEqualTo("User.password.format");
    }

    @Test
    @DisplayName("""
            checkPassword(currentPassword):
             currentPassword is correct
             => do nothing
            """)
    public void checkPassword5() {
        User user = new User(new Credential("me@gmail.com", "password"));

        Assertions.
                assertThatCode(() -> user.assertCurrentPassword("password")).
                doesNotThrowAnyException();
    }

    @Test
    @DisplayName("""
            changePassword:
             newPassword is null
             => exception
            """)
    public void changePassword1() {
        User user = new User(new Credential("me@gmail.com", "password"));

        Assertions.
                assertThatExceptionOfType(IncorrectCredentials.class).
                isThrownBy(() -> user.changePassword("password", null)).
                extracting(IncorrectCredentials::getMessageKey, InstanceOfAssertFactories.type(String.class)).
                isEqualTo("User.newPassword.format");
    }

    @Test
    @DisplayName("""
            changePassword:
             newPassword is blank
             => exception
            """)
    public void changePassword2() {
        User user = new User(new Credential("me@gmail.com", "password"));

        Assertions.
                assertThatExceptionOfType(IncorrectCredentials.class).
                isThrownBy(() -> user.changePassword("password", "     ")).
                extracting(IncorrectCredentials::getMessageKey, InstanceOfAssertFactories.type(String.class)).
                isEqualTo("User.newPassword.format");
    }

    @Test
    @DisplayName("""
            changePassword:
             newPassword less than 8
             => exception
            """)
    public void changePassword3() {
        User user = new User(new Credential("me@gmail.com", "password"));

        Assertions.
                assertThatExceptionOfType(IncorrectCredentials.class).
                isThrownBy(() -> user.changePassword("password", "1234567")).
                extracting(IncorrectCredentials::getMessageKey, InstanceOfAssertFactories.type(String.class)).
                isEqualTo("User.newPassword.format");
    }

    @Test
    @DisplayName("""
            changePassword:
             newPassword greater than 50
             => exception
            """)
    public void changePassword4() {
        User user = new User(new Credential("me@gmail.com", "password"));

        Assertions.
                assertThatExceptionOfType(IncorrectCredentials.class).
                isThrownBy(() -> user.changePassword("password",
                                "01234567890123456789012345678901234567890123456789X")).
                extracting(IncorrectCredentials::getMessageKey, InstanceOfAssertFactories.type(String.class)).
                isEqualTo("User.newPassword.format");
    }

    @Test
    @DisplayName("""
            changePassword:
             currentPassword is wrong, newPassword is correct
             => exception
            """)
    public void changePassword5() {
        User user = new User(new Credential("me@gmail.com", "password"));

        Assertions.
                assertThatExceptionOfType(IncorrectCredentials.class).
                isThrownBy(() -> user.changePassword("unknown pass", "12345678")).
                extracting(IncorrectCredentials::getMessageKey, InstanceOfAssertFactories.type(String.class)).
                isEqualTo("User.password.incorrect");
    }

    @Test
    @DisplayName("""
            changePassword:
             currentPassword is correct, newPassword is correct
             => doesn't throw any exception
            """)
    public void changePassword6() {
        User user = new User(new Credential("me@gmail.com", "password"));

        user.changePassword("password", "12345678");

        Assertions.
                assertThatCode(() -> user.assertCurrentPassword("12345678")).
                doesNotThrowAnyException();
    }

}