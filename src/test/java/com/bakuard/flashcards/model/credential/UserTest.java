package com.bakuard.flashcards.model.credential;

import com.bakuard.flashcards.config.TestConfig;
import com.bakuard.flashcards.validation.IncorrectCredentials;
import com.bakuard.flashcards.validation.ValidatorUtil;
import org.assertj.core.api.Assertions;
import org.assertj.core.api.InstanceOfAssertFactories;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
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
             password is null
             => exception
            """)
    public void newUser1() {
        Assertions.
                assertThatExceptionOfType(ConstraintViolationException.class).
                isThrownBy(() -> User.newBuilder(validator).setEmail(null).setPassword(null).build()).
                extracting(ex -> ex.getConstraintViolations().stream().
                                map(ConstraintViolation::getMessage).
                                collect(Collectors.toList()),
                        InstanceOfAssertFactories.collection(String.class)).
                containsExactlyInAnyOrder("Password.format", "Email.notNull");
    }

    @Test
    @DisplayName("""
            new User:
             email string is not email format,
             password is blank
             => exception
            """)
    public void newUser2() {
        Assertions.
                assertThatExceptionOfType(ConstraintViolationException.class).
                isThrownBy(() -> User.newBuilder(validator).
                        setEmail("asdf").
                        setPassword("      ").
                        build()).
                extracting(ex -> ex.getConstraintViolations().stream().
                                map(ConstraintViolation::getMessage).
                                collect(Collectors.toList()),
                        InstanceOfAssertFactories.collection(String.class)).
                containsExactlyInAnyOrder("Password.format", "Email.format");
    }

    @Test
    @DisplayName("""
            new User:
             email is blank,
             password length < 8
             => exception
            """)
    public void newUser3() {
        Assertions.
                assertThatExceptionOfType(ConstraintViolationException.class).
                isThrownBy(() -> User.newBuilder(validator).
                        setEmail("       ").
                        setPassword("1234567").
                        build()).
                extracting(ex -> ex.getConstraintViolations().stream().
                                map(ConstraintViolation::getMessage).
                                collect(Collectors.toList()),
                        InstanceOfAssertFactories.collection(String.class)).
                containsExactlyInAnyOrder("Password.format", "Email.format");
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
                isThrownBy(() -> user.checkPassword(null)).
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
                isThrownBy(() -> user.checkPassword("       ")).
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
                isThrownBy(() -> user.checkPassword("1234567")).
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
                        user.checkPassword("012345678901234567890123456789012345678901234567891")).
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
                assertThatCode(() -> user.checkPassword("password")).
                doesNotThrowAnyException();
    }

    @Test
    @DisplayName("""
            setPassword(currentPassword, newPassword):
             newPassword is null
             => exception
            """)
    public void setPassword1() {
        User user = User.newBuilder(validator).
                setEmail("me@gmail.com").
                setPassword("password").
                build();

        Assertions.
                assertThatExceptionOfType(ConstraintViolationException.class).
                isThrownBy(() -> user.setPassword("password", null)).
                extracting(ex -> ex.getConstraintViolations().stream().
                                map(ConstraintViolation::getMessage).
                                collect(Collectors.toSet()),
                        InstanceOfAssertFactories.collection(String.class)).
                containsOnly("Password.format");
    }

    @Test
    @DisplayName("""
            setPassword(currentPassword, newPassword):
             newPassword is blank
             => exception
            """)
    public void setPassword2() {
        User user = User.newBuilder(validator).
                setEmail("me@gmail.com").
                setPassword("password").
                build();

        Assertions.
                assertThatExceptionOfType(ConstraintViolationException.class).
                isThrownBy(() -> user.setPassword("password", "     ")).
                extracting(ex -> ex.getConstraintViolations().stream().
                                map(ConstraintViolation::getMessage).
                                collect(Collectors.toSet()),
                        InstanceOfAssertFactories.collection(String.class)).
                containsOnly("Password.format");
    }

    @Test
    @DisplayName("""
            setPassword(currentPassword, newPassword):
             newPassword less than 8
             => exception
            """)
    public void setPassword3() {
        User user = User.newBuilder(validator).
                setEmail("me@gmail.com").
                setPassword("password").
                build();

        Assertions.
                assertThatExceptionOfType(ConstraintViolationException.class).
                isThrownBy(() -> user.setPassword("password", "1234567")).
                extracting(ex -> ex.getConstraintViolations().stream().
                                map(ConstraintViolation::getMessage).
                                collect(Collectors.toSet()),
                        InstanceOfAssertFactories.collection(String.class)).
                containsOnly("Password.format");
    }

    @Test
    @DisplayName("""
            setPassword(currentPassword, newPassword):
             newPassword greater than 50
             => exception
            """)
    public void setPassword4() {
        User user = User.newBuilder(validator).
                setEmail("me@gmail.com").
                setPassword("password").
                build();

        Assertions.
                assertThatExceptionOfType(ConstraintViolationException.class).
                isThrownBy(() ->
                        user.setPassword("password", "012345678901234567890123456789012345678901234567891")).
                extracting(ex -> ex.getConstraintViolations().stream().
                                map(ConstraintViolation::getMessage).
                                collect(Collectors.toSet()),
                        InstanceOfAssertFactories.collection(String.class)).
                containsOnly("Password.format");
    }

    @Test
    @DisplayName("""
            setPassword(currentPassword, newPassword):
             currentPassword is wrong, newPassword is correct
             => exception
            """)
    public void setPassword5() {
        User user = User.newBuilder(validator).
                setEmail("me@gmail.com").
                setPassword("password").
                build();

        Assertions.
                assertThatExceptionOfType(IncorrectCredentials.class).
                isThrownBy(() -> user.setPassword("unknown pass", "12345678"));
    }

    @Test
    @DisplayName("""
            setPassword(currentPassword, newPassword):
             currentPassword is correct, newPassword is correct
             => exception
            """)
    public void setPassword6() {
        User user = User.newBuilder(validator).
                setEmail("me@gmail.com").
                setPassword("password").
                build();

        user.setPassword("password", "12345678");

        Assertions.
                assertThatCode(() -> user.checkPassword("12345678")).
                doesNotThrowAnyException();
    }

}