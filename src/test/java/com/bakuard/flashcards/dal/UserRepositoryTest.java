package com.bakuard.flashcards.dal;

import com.bakuard.flashcards.config.TestConfig;
import com.bakuard.flashcards.model.credential.User;
import com.bakuard.flashcards.validation.ValidatorUtil;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.function.Executable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.jdbc.AutoConfigureDataJdbc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.jdbc.JdbcTestUtils;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import java.util.Optional;
import java.util.UUID;

@ExtendWith(SpringExtension.class)
@TestPropertySource(locations = "classpath:application.properties")
@Import(TestConfig.class)
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private JdbcTemplate jdbcTemplate;
    @Autowired
    private DataSourceTransactionManager transactionManager;
    @Autowired
    private ValidatorUtil validator;

    @BeforeEach
    public void beforeEach() {
        commit(() -> JdbcTestUtils.deleteFromTables(jdbcTemplate,
                "expressions", "words", "intervals", "users"));
    }

    @Test
    @DisplayName("""
            findByEmail(email):
             there is user with such email
             => return correct user
            """)
    public void findByEmail1() {
        User expected = user(1);
        commit(() -> {
            userRepository.save(expected);
            userRepository.save(user(2));
            userRepository.save(user(3));
        });

        User actual = userRepository.findByEmail(toEmail(1)).orElseThrow();

        org.assertj.core.api.Assertions.
                assertThat(expected).
                usingRecursiveComparison().
                isEqualTo(actual);
    }

    @Test
    @DisplayName("""
            findByEmail(email):
             there is not user with such email
             => return empty Optional
            """)
    public void findByEmail2() {
        User expected = user(1);
        commit(() -> {
            userRepository.save(expected);
            userRepository.save(user(2));
            userRepository.save(user(3));
        });

        Optional<User> actual = userRepository.findByEmail(toEmail(1000));

        Assertions.assertTrue(actual.isEmpty());
    }


    private UUID toUUID(int number) {
        return UUID.fromString("00000000-0000-0000-0000-" + String.format("%012d", number));
    }

    private String toEmail(int number) {
        return "user" + number + "@gmail.com";
    }

    private User user(int number) {
        return User.newBuilder(validator).
                setPassword("password" + number).
                setEmail(toEmail(number)).
                setOrGenerateSalt("salt" + number).
                build();
    }

    private void commit(Executable executable) {
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        TransactionStatus status = transactionManager.getTransaction(def);
        try {
            executable.execute();
            transactionManager.commit(status);
        } catch(Throwable e) {
            transactionManager.rollback(status);
            throw new RuntimeException(e);
        }
    }

}