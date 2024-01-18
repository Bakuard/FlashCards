package com.bakuard.flashcards.service;

import com.bakuard.flashcards.config.SpringConfig;
import com.bakuard.flashcards.config.TestConfig;
import com.bakuard.flashcards.config.configData.ConfigData;
import com.bakuard.flashcards.dal.IntervalRepository;
import com.bakuard.flashcards.dal.UserRepository;
import com.bakuard.flashcards.model.auth.credential.Credential;
import com.bakuard.flashcards.model.auth.credential.Role;
import com.bakuard.flashcards.model.auth.credential.User;
import com.bakuard.flashcards.validation.ValidatorUtil;
import com.bakuard.flashcards.validation.exception.IncorrectCredentials;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.assertj.core.api.Assertions;
import org.assertj.core.api.InstanceOfAssertFactories;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.stream.Collectors;

@ExtendWith({SpringExtension.class, MockitoExtension.class})
@TestPropertySource(locations = "classpath:test.properties")
@Import({SpringConfig.class, TestConfig.class})
class UserServiceTest {

    @Autowired
    private ConfigData configData;
    @Autowired
    private ValidatorUtil validator;
    @Autowired
    private TransactionTemplate transactionTemplate;

    @Test
    @DisplayName("""
            assertCurrentPassword(user, currentPassword):
             currentPassword is null
             => exception
            """)
    public void assertCurrentPassword1() {
        UserService service = new UserService(
                Mockito.mock(UserRepository.class),
                Mockito.mock(IntervalRepository.class),
                configData,
                validator,
                transactionTemplate
        );
        User user = service.createUserFromCredential(
                new Credential("me@gmail.com", "password")
        );

        Assertions.
                assertThatExceptionOfType(IncorrectCredentials.class).
                isThrownBy(() -> service.assertCurrentPassword(user, null)).
                extracting(IncorrectCredentials::getMessageKey, InstanceOfAssertFactories.type(String.class)).
                isEqualTo("User.password.notNull");
    }

    @Test
    @DisplayName("""
            assertCurrentPassword(user, currentPassword):
             currentPassword is blank
             => exception
            """)
    public void assertCurrentPassword2() {
        UserService service = new UserService(
                Mockito.mock(UserRepository.class),
                Mockito.mock(IntervalRepository.class),
                configData,
                validator,
                transactionTemplate
        );
        User user = service.createUserFromCredential(
                new Credential("me@gmail.com", "password")
        );

        Assertions.
                assertThatExceptionOfType(IncorrectCredentials.class).
                isThrownBy(() -> service.assertCurrentPassword(user, "       ")).
                extracting(IncorrectCredentials::getMessageKey, InstanceOfAssertFactories.type(String.class)).
                isEqualTo("User.password.incorrect");
    }

    @Test
    @DisplayName("""
            assertCurrentPassword(user, currentPassword):
             currentPassword less than 8
             => exception
            """)
    public void assertCurrentPassword3() {
        UserService service = new UserService(
                Mockito.mock(UserRepository.class),
                Mockito.mock(IntervalRepository.class),
                configData,
                validator,
                transactionTemplate
        );
        User user = service.createUserFromCredential(
                new Credential("me@gmail.com", "password")
        );

        Assertions.
                assertThatExceptionOfType(IncorrectCredentials.class).
                isThrownBy(() -> service.assertCurrentPassword(user, "1234567")).
                extracting(IncorrectCredentials::getMessageKey, InstanceOfAssertFactories.type(String.class)).
                isEqualTo("User.password.incorrect");
    }

    @Test
    @DisplayName("""
            assertCurrentPassword(user, currentPassword):
             currentPassword greater than 50
             => exception
            """)
    public void assertCurrentPassword4() {
        UserService service = new UserService(
                Mockito.mock(UserRepository.class),
                Mockito.mock(IntervalRepository.class),
                configData,
                validator,
                transactionTemplate
        );
        User user = service.createUserFromCredential(
                new Credential("me@gmail.com", "password")
        );

        Assertions.
                assertThatExceptionOfType(IncorrectCredentials.class).
                isThrownBy(() ->
                        service.assertCurrentPassword(user, "012345678901234567890123456789012345678901234567891")).
                extracting(IncorrectCredentials::getMessageKey, InstanceOfAssertFactories.type(String.class)).
                isEqualTo("User.password.incorrect");
    }

    @Test
    @DisplayName("""
            assertCurrentPassword(user, currentPassword):
             currentPassword is correct
             => do nothing
            """)
    public void assertCurrentPassword5() {
        UserService service = new UserService(
                Mockito.mock(UserRepository.class),
                Mockito.mock(IntervalRepository.class),
                configData,
                validator,
                transactionTemplate
        );
        User user = service.createUserFromCredential(
                new Credential("me@gmail.com", "password")
        );

        Assertions.
                assertThatCode(() -> service.assertCurrentPassword(user, "password")).
                doesNotThrowAnyException();
    }

    @Test
    @DisplayName("""
            changePassword(user, currentPassword, newPassword):
             newPassword is null
             => exception
            """)
    public void changePassword1() {
        UserService service = new UserService(
                Mockito.mock(UserRepository.class),
                Mockito.mock(IntervalRepository.class),
                configData,
                validator,
                transactionTemplate
        );
        User user = service.createUserFromCredential(
                new Credential("me@gmail.com", "password")
        );

        Assertions.
                assertThatExceptionOfType(IncorrectCredentials.class).
                isThrownBy(() -> service.changePassword(user, "password", null)).
                extracting(IncorrectCredentials::getMessageKey, InstanceOfAssertFactories.type(String.class)).
                isEqualTo("User.newPassword.format");
    }

    @Test
    @DisplayName("""
            changePassword(user, currentPassword, newPassword):
             newPassword is blank
             => exception
            """)
    public void changePassword2() {
        UserService service = new UserService(
                Mockito.mock(UserRepository.class),
                Mockito.mock(IntervalRepository.class),
                configData,
                validator,
                transactionTemplate
        );
        User user = service.createUserFromCredential(
                new Credential("me@gmail.com", "password")
        );

        Assertions.
                assertThatExceptionOfType(IncorrectCredentials.class).
                isThrownBy(() -> service.changePassword(user, "password", "     ")).
                extracting(IncorrectCredentials::getMessageKey, InstanceOfAssertFactories.type(String.class)).
                isEqualTo("User.newPassword.format");
    }

    @Test
    @DisplayName("""
            changePassword(user, currentPassword, newPassword):
             newPassword less than 8
             => exception
            """)
    public void changePassword3() {
        UserService service = new UserService(
                Mockito.mock(UserRepository.class),
                Mockito.mock(IntervalRepository.class),
                configData,
                validator,
                transactionTemplate
        );
        User user = service.createUserFromCredential(
                new Credential("me@gmail.com", "password")
        );

        Assertions.
                assertThatExceptionOfType(IncorrectCredentials.class).
                isThrownBy(() -> service.changePassword(user, "password", "1234567")).
                extracting(IncorrectCredentials::getMessageKey, InstanceOfAssertFactories.type(String.class)).
                isEqualTo("User.newPassword.format");
    }

    @Test
    @DisplayName("""
            changePassword(user, currentPassword, newPassword):
             newPassword greater than 50
             => exception
            """)
    public void changePassword4() {
        UserService service = new UserService(
                Mockito.mock(UserRepository.class),
                Mockito.mock(IntervalRepository.class),
                configData,
                validator,
                transactionTemplate
        );
        User user = service.createUserFromCredential(
                new Credential("me@gmail.com", "password")
        );

        Assertions.
                assertThatExceptionOfType(IncorrectCredentials.class).
                isThrownBy(() -> service.changePassword(user,
                        "password",
                        "01234567890123456789012345678901234567890123456789X")).
                extracting(IncorrectCredentials::getMessageKey, InstanceOfAssertFactories.type(String.class)).
                isEqualTo("User.newPassword.format");
    }

    @Test
    @DisplayName("""
            changePassword(user, currentPassword, newPassword):
             currentPassword is wrong, newPassword is correct
             => exception
            """)
    public void changePassword5() {
        UserService service = new UserService(
                Mockito.mock(UserRepository.class),
                Mockito.mock(IntervalRepository.class),
                configData,
                validator,
                transactionTemplate
        );
        User user = service.createUserFromCredential(
                new Credential("me@gmail.com", "password")
        );

        Assertions.
                assertThatExceptionOfType(IncorrectCredentials.class).
                isThrownBy(() -> service.changePassword(user, "unknown pass", "12345678")).
                extracting(IncorrectCredentials::getMessageKey, InstanceOfAssertFactories.type(String.class)).
                isEqualTo("User.password.incorrect");
    }

    @Test
    @DisplayName("""
            changePassword(user, currentPassword, newPassword):
             currentPassword is correct, newPassword is correct
             => doesn't throw any exception
            """)
    public void changePassword6() {
        UserService service = new UserService(
                Mockito.mock(UserRepository.class),
                Mockito.mock(IntervalRepository.class),
                configData,
                validator,
                transactionTemplate
        );
        User user = service.createUserFromCredential(
                new Credential("me@gmail.com", "password")
        );

        service.changePassword(user, "password", "12345678");

        Assertions.
                assertThatCode(() -> service.assertCurrentPassword(user, "12345678")).
                doesNotThrowAnyException();
    }
}