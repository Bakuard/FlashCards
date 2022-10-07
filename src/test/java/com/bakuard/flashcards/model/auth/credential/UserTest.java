package com.bakuard.flashcards.model.auth.credential;

import com.bakuard.flashcards.config.TestConfig;
import com.bakuard.flashcards.model.auth.credential.PasswordChangeData;
import com.bakuard.flashcards.model.auth.credential.User;
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
@TestPropertySource(locations = "classpath:application.properties")
@Import(TestConfig.class)
class UserTest {

    @Autowired
    private ValidatorUtil validator;

    @Test
    @DisplayName("""
            new User:
             email is null,
             password is null,
             roles contians null
             => exception
            """)
    public void newUser1() {
        Assertions.
                assertThatExceptionOfType(ConstraintViolationException.class).
                isThrownBy(() -> User.newBuilder(validator).
                        setEmail(null).
                        setPassword(null).
                        addRole((Role) null).
                        addRole("role1").
                        build()).
                extracting(ex -> ex.getConstraintViolations().stream().
                                map(ConstraintViolation::getMessage).
                                collect(Collectors.toList()),
                        InstanceOfAssertFactories.collection(String.class)).
                containsExactlyInAnyOrder("Password.format",
                        "User.email.notNull",
                        "User.roles.notContainsNull");
    }

    @Test
    @DisplayName("""
            new User:
             email string is not email format,
             password is blank,
             roles contains duplicates
             => exception
            """)
    public void newUser2() {
        Assertions.
                assertThatExceptionOfType(ConstraintViolationException.class).
                isThrownBy(() -> User.newBuilder(validator).
                        setEmail("asdf").
                        setPassword("      ").
                        addRole("role1").
                        addRole("role1").
                        build()).
                extracting(ex -> ex.getConstraintViolations().stream().
                                map(ConstraintViolation::getMessage).
                                collect(Collectors.toList()),
                        InstanceOfAssertFactories.collection(String.class)).
                containsExactlyInAnyOrder("Password.format",
                        "User.email.format",
                        "User.roles.allUnique");
    }

    @Test
    @DisplayName("""
            new User:
             email is blank,
             password length < 8,
             some role is blank
             => exception
            """)
    public void newUser3() {
        Assertions.
                assertThatExceptionOfType(ConstraintViolationException.class).
                isThrownBy(() -> User.newBuilder(validator).
                        setEmail("       ").
                        setPassword("1234567").
                        addRole("role1").
                        addRole("      ").
                        build()).
                extracting(ex -> ex.getConstraintViolations().stream().
                                map(ConstraintViolation::getMessage).
                                collect(Collectors.toList()),
                        InstanceOfAssertFactories.collection(String.class)).
                containsExactlyInAnyOrder("Password.format",
                        "User.email.format",
                        "Role.name.notBlank");
    }

    @Test
    @DisplayName("""
            new User:
             email is correct,
             password length > 50
             => exception
            """)
    public void newUser4() {
        Assertions.
                assertThatExceptionOfType(ConstraintViolationException.class).
                isThrownBy(() -> User.newBuilder(validator).
                        setEmail("me@mail.com").
                        setPassword("012345678901234567890123456789012345678901234567891").
                        build()).
                extracting(ex -> ex.getConstraintViolations().stream().
                                map(ConstraintViolation::getMessage).
                                collect(Collectors.toList()),
                        InstanceOfAssertFactories.collection(String.class)).
                containsExactlyInAnyOrder("Password.format");
    }

    @Test
    @DisplayName("""
            checkPassword(currentPassword):
             currentPassword is null
             => exception
            """)
    public void checkPassword1() {
        User user = User.newBuilder(validator).
                setEmail("me@gmail.com").
                setPassword("password").
                build();

        Assertions.
                assertThatExceptionOfType(ConstraintViolationException.class).
                isThrownBy(() -> user.assertCurrentPassword(null)).
                extracting(ex -> ex.getConstraintViolations().stream().
                                map(ConstraintViolation::getMessage).
                                collect(Collectors.toSet()),
                        InstanceOfAssertFactories.collection(String.class)).
                containsOnly("Password.format");
    }

    @Test
    @DisplayName("""
            checkPassword(currentPassword):
             currentPassword is blank
             => exception
            """)
    public void checkPassword2() {
        User user = User.newBuilder(validator).
                setEmail("me@gmail.com").
                setPassword("password").
                build();

        Assertions.
                assertThatExceptionOfType(ConstraintViolationException.class).
                isThrownBy(() -> user.assertCurrentPassword("       ")).
                extracting(ex -> ex.getConstraintViolations().stream().
                                map(ConstraintViolation::getMessage).
                                collect(Collectors.toSet()),
                        InstanceOfAssertFactories.collection(String.class)).
                containsOnly("Password.format");
    }

    @Test
    @DisplayName("""
            checkPassword(currentPassword):
             currentPassword less than 8
             => exception
            """)
    public void checkPassword3() {
        User user = User.newBuilder(validator).
                setEmail("me@gmail.com").
                setPassword("password").
                build();

        Assertions.
                assertThatExceptionOfType(ConstraintViolationException.class).
                isThrownBy(() -> user.assertCurrentPassword("1234567")).
                extracting(ex -> ex.getConstraintViolations().stream().
                                map(ConstraintViolation::getMessage).
                                collect(Collectors.toSet()),
                        InstanceOfAssertFactories.collection(String.class)).
                containsOnly("Password.format");
    }

    @Test
    @DisplayName("""
            checkPassword(currentPassword):
             currentPassword greater than 50
             => exception
            """)
    public void checkPassword4() {
        User user = User.newBuilder(validator).
                setEmail("me@gmail.com").
                setPassword("password").
                build();

        Assertions.
                assertThatExceptionOfType(ConstraintViolationException.class).
                isThrownBy(() ->
                        user.assertCurrentPassword("012345678901234567890123456789012345678901234567891")).
                extracting(ex -> ex.getConstraintViolations().stream().
                                map(ConstraintViolation::getMessage).
                                collect(Collectors.toSet()),
                        InstanceOfAssertFactories.collection(String.class)).
                containsOnly("Password.format");
    }

    @Test
    @DisplayName("""
            checkPassword(currentPassword):
             currentPassword is correct
             => do nothing
            """)
    public void checkPassword5() {
        User user = User.newBuilder(validator).
                setEmail("me@gmail.com").
                setPassword("password").
                build();

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
        User user = User.newBuilder(validator).
                setEmail("me@gmail.com").
                setPassword("password").
                build();

        Assertions.
                assertThatExceptionOfType(ConstraintViolationException.class).
                isThrownBy(() -> user.builder().
                        changePassword(new PasswordChangeData("password", null)).
                        build()).
                extracting(ex -> ex.getConstraintViolations().stream().
                                map(ConstraintViolation::getMessage).
                                collect(Collectors.toSet()),
                        InstanceOfAssertFactories.collection(String.class)).
                containsOnly("NewPassword.format");
    }

    @Test
    @DisplayName("""
            changePassword:
             newPassword is blank
             => exception
            """)
    public void changePassword2() {
        User user = User.newBuilder(validator).
                setEmail("me@gmail.com").
                setPassword("password").
                build();

        Assertions.
                assertThatExceptionOfType(ConstraintViolationException.class).
                isThrownBy(() -> user.builder().
                        changePassword(new PasswordChangeData("password", "     ")).
                        build()).
                extracting(ex -> ex.getConstraintViolations().stream().
                                map(ConstraintViolation::getMessage).
                                collect(Collectors.toSet()),
                        InstanceOfAssertFactories.collection(String.class)).
                containsOnly("NewPassword.format");
    }

    @Test
    @DisplayName("""
            changePassword:
             newPassword less than 8
             => exception
            """)
    public void changePassword3() {
        User user = User.newBuilder(validator).
                setEmail("me@gmail.com").
                setPassword("password").
                build();

        Assertions.
                assertThatExceptionOfType(ConstraintViolationException.class).
                isThrownBy(() -> user.builder().
                        changePassword(new PasswordChangeData("password", "1234567")).
                        build()).
                extracting(ex -> ex.getConstraintViolations().stream().
                                map(ConstraintViolation::getMessage).
                                collect(Collectors.toSet()),
                        InstanceOfAssertFactories.collection(String.class)).
                containsOnly("NewPassword.format");
    }

    @Test
    @DisplayName("""
            changePassword:
             newPassword greater than 50
             => exception
            """)
    public void changePassword4() {
        User user = User.newBuilder(validator).
                setEmail("me@gmail.com").
                setPassword("password").
                build();

        Assertions.
                assertThatExceptionOfType(ConstraintViolationException.class).
                isThrownBy(() -> user.builder().
                                changePassword(
                                        new PasswordChangeData(
                                                "password",
                                                "01234567890123456789012345678901234567890123456789X")
                                ).
                                build()).
                extracting(ex -> ex.getConstraintViolations().stream().
                                map(ConstraintViolation::getMessage).
                                collect(Collectors.toSet()),
                        InstanceOfAssertFactories.collection(String.class)).
                containsOnly("NewPassword.format");
    }

    @Test
    @DisplayName("""
            changePassword:
             currentPassword is wrong, newPassword is correct
             => exception
            """)
    public void changePassword5() {
        User user = User.newBuilder(validator).
                setEmail("me@gmail.com").
                setPassword("password").
                build();

        Assertions.
                assertThatExceptionOfType(IncorrectCredentials.class).
                isThrownBy(() -> user.builder().
                        changePassword(new PasswordChangeData("unknown pass", "12345678")).
                        build());
    }

    @Test
    @DisplayName("""
            changePassword:
             currentPassword is correct, newPassword is correct
             => exception
            """)
    public void changePassword6() {
        User user = User.newBuilder(validator).
                setEmail("me@gmail.com").
                setPassword("password").
                build();

        User expected = user.builder().
                changePassword(new PasswordChangeData("password", "12345678")).
                build();

        Assertions.
                assertThatCode(() -> expected.assertCurrentPassword("12345678")).
                doesNotThrowAnyException();
    }

}