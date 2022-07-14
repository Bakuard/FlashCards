package com.bakuard.flashcards.dal;

import com.bakuard.flashcards.config.SpringConfig;
import com.bakuard.flashcards.model.User;
import com.google.common.collect.ImmutableList;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.jdbc.AutoConfigureDataJdbc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.jdbc.JdbcTestUtils;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import java.util.List;

@SpringBootTest(classes = SpringConfig.class)
@AutoConfigureDataJdbc
@TestPropertySource(locations = "classpath:application.properties")
class IntervalsRepositoryTest {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private IntervalsRepository intervalsRepository;
    @Autowired
    private JdbcTemplate jdbcTemplate;
    @Autowired
    private DataSourceTransactionManager transactionManager;

    @BeforeEach
    public void beforeEach() {
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        TransactionStatus status = transactionManager.getTransaction(def);
        try {
            JdbcTestUtils.deleteFromTables(jdbcTemplate, "expressions", "words", "intervals", "users");
            transactionManager.commit(status);
        } catch(RuntimeException e) {
            transactionManager.rollback(status);
            throw e;
        }
    }

    @Test
    @DisplayName("""
            add(userId, interval):
             there is not duplicate for interval
             => add interval
            """)
    public void add1() {
        User user = userRepository.save(user(1));
        commit(() -> intervalsRepository.add(user.getId(), 10));

        ImmutableList<Integer> intervals = intervalsRepository.findAll(user.getId());

        Assertions.assertTrue(intervals.contains(10));
    }

    @Test
    @DisplayName("""
            add(userId, interval):
             there is duplicate for interval
             => exception
            """)
    public void add2() {
        User user = userRepository.save(user(1));
        commit(() -> intervalsRepository.add(user.getId(), 10));
        Assertions.assertThrows(
                DuplicateKeyException.class,
                () -> commit(() -> intervalsRepository.add(user.getId(), 10))
        );
    }

    @Test
    @DisplayName("""
            remove(userId, interval):
             there is not such interval
             => do nothing
            """)
    public void remove1() {
        User user = userRepository.save(user(1));
        commit(() -> {
            intervalsRepository.add(user.getId(), 1);
            intervalsRepository.add(user.getId(), 3);
            intervalsRepository.add(user.getId(), 5);
            intervalsRepository.add(user.getId(), 11);
        });

        commit(() -> intervalsRepository.remove(user.getId(), 100));

        ImmutableList<Integer> intervals = intervalsRepository.findAll(user.getId());
        Assertions.assertEquals(intervals, List.of(1, 3, 5, 11));
    }

    @Test
    @DisplayName("""
            remove(userId, interval):
             there is such interval
             => remove this interval
            """)
    public void remove2() {
        User user = userRepository.save(user(1));
        commit(() -> {
            intervalsRepository.add(user.getId(), 1);
            intervalsRepository.add(user.getId(), 3);
            intervalsRepository.add(user.getId(), 5);
            intervalsRepository.add(user.getId(), 11);
        });

        commit(() -> intervalsRepository.remove(user.getId(), 5));

        ImmutableList<Integer> intervals = intervalsRepository.findAll(user.getId());
        Assertions.assertEquals(intervals, List.of(1, 3, 11));
    }


    private User user(int number) {
        return new User(
                null,
                "password" + number,
                "salt" + number,
                "user" + number + "@gmail.com"
        );
    }

    private void commit(Runnable command) {
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        TransactionStatus status = transactionManager.getTransaction(def);
        try {
            command.run();
            transactionManager.commit(status);
        } catch(RuntimeException e) {
            transactionManager.rollback(status);
            throw e;
        }
    }

}