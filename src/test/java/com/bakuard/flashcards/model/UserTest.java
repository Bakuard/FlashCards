package com.bakuard.flashcards.model;

import com.bakuard.flashcards.config.SpringConfig;
import com.bakuard.flashcards.model.credential.Credential;
import com.bakuard.flashcards.model.credential.IncorrectCredentials;
import com.bakuard.flashcards.model.credential.RawPassword;
import com.bakuard.flashcards.model.credential.User;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import java.util.Set;

@SpringBootTest(classes = SpringConfig.class)
@TestPropertySource(locations = "classpath:application.properties")
class UserTest {

    @Autowired
    private Validator validator;

    @Test
    @DisplayName("""
            User(credential):
             incorrect email
             => fail validate
            """)
    public void constructor1() {
        Credential credential = new Credential("12345678", "incorrect email");

        Set<ConstraintViolation<Credential>> constraints = validator.validate(credential);

        Assertions.assertThat(constraints).
                extracting(ConstraintViolation::getMessage).
                containsExactlyInAnyOrder("Email.format");
    }

    @Test
    @DisplayName("""
            User(credential):
             password is null
             => fail validate
            """)
    public void construct2() {
        Credential credential = new Credential(null, "user@gmail.com");

        Set<ConstraintViolation<Credential>> constraints = validator.validate(credential);

        Assertions.assertThat(constraints).
                extracting(ConstraintViolation::getMessage).
                containsExactlyInAnyOrder("Password.format");
    }

    @Test
    @DisplayName("""
            User(credential):
             password is blank
             => fail validate
            """)
    public void construct3() {
        Credential credential = new Credential("       ", "user@gmail.com");

        Set<ConstraintViolation<Credential>> constraints = validator.validate(credential);

        Assertions.assertThat(constraints).
                extracting(ConstraintViolation::getMessage).
                containsExactlyInAnyOrder("Password.format");
    }

    @Test
    @DisplayName("""
            User(credential):
             password is too short
             => fail validate
            """)
    public void construct4() {
        Credential credential = new Credential("1234567", "user@gmail.com");

        Set<ConstraintViolation<Credential>> constraints = validator.validate(credential);

        Assertions.assertThat(constraints).
                extracting(ConstraintViolation::getMessage).
                containsExactlyInAnyOrder("Password.format");
    }

    @Test
    @DisplayName("""
            User(credential):
             password is too long
             => fail validate
            """)
    public void construct5() {
        Credential credential = new Credential(
                "012345678901234567890123456789012345678901234567890",
                "user@gmail.com"
        );

        Set<ConstraintViolation<Credential>> constraints = validator.validate(credential);

        Assertions.assertThat(constraints).
                extracting(ConstraintViolation::getMessage).
                containsExactlyInAnyOrder("Password.format");
    }

    @Test
    @DisplayName("""
            User(credential):
             email is null
             => fail validate
            """)
    public void construct6() {
        Credential credential = new Credential("12345678", null);

        Set<ConstraintViolation<Credential>> constraints = validator.validate(credential);

        Assertions.assertThat(constraints).
                extracting(ConstraintViolation::getMessage).
                containsExactlyInAnyOrder("Email.notNull");
    }

    @Test
    @DisplayName("""
            setPassword(currentPassword, newPassword):
             incorrect currentPassword
             => exception
            """)
    public void setPassword1() {
        User user = new User(new Credential("12345678", "user@gmail.com"));

        Assertions.assertThatThrownBy(() ->
                user.setPassword(new RawPassword("87654321"), new RawPassword("new Password"))
        ).isInstanceOf(IncorrectCredentials.class);
    }

}