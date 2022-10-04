package com.bakuard.flashcards.dal;

import com.bakuard.flashcards.config.TestConfig;
import com.bakuard.flashcards.dal.auth.UserRepository;
import com.bakuard.flashcards.model.RepeatData;
import com.bakuard.flashcards.model.auth.credential.User;
import com.bakuard.flashcards.model.expression.Expression;
import com.bakuard.flashcards.model.word.Word;
import com.bakuard.flashcards.validation.ValidatorUtil;
import com.google.common.collect.ImmutableList;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.jdbc.JdbcTestUtils;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@ExtendWith(SpringExtension.class)
@TestPropertySource(locations = "classpath:application.properties")
@Import(TestConfig.class)
class IntervalsRepositoryTest {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private IntervalsRepository intervalsRepository;
    @Autowired
    private WordsRepository wordsRepository;
    @Autowired
    private ExpressionRepository expressionRepository;
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
            removeUnused(userId):
             there are not unused intervals
             => do nothing
            """)
    public void removeUnused1() {
        User user = userRepository.save(user(1));
        commit(() -> {
            intervalsRepository.add(user.getId(), 1);
            intervalsRepository.add(user.getId(), 3);
            intervalsRepository.add(user.getId(), 5);
            intervalsRepository.add(user.getId(), 11);
        });
        wordsRepository.save(word(user.getId(), "v1", "n1", repeatData(1)));
        wordsRepository.save(word(user.getId(), "v2", "n2", repeatData(3)));
        expressionRepository.save(expression(user.getId(), "v1", "n1", repeatData(5)));
        expressionRepository.save(expression(user.getId(), "v2", "n2", repeatData(11)));

        commit(() -> intervalsRepository.removeUnused(user.getId()));

        ImmutableList<Integer> actual = intervalsRepository.findAll(user.getId());
        Assertions.assertEquals(actual, List.of(1, 3, 5, 11));
    }

    @Test
    @DisplayName("""
            removeUnused(userId):
             there are unused intervals
             => remove them
            """)
    public void removeUnused2() {
        User user = userRepository.save(user(1));
        commit(() -> {
            intervalsRepository.add(user.getId(), 1);
            intervalsRepository.add(user.getId(), 3);
            intervalsRepository.add(user.getId(), 5);
            intervalsRepository.add(user.getId(), 11);
        });
        wordsRepository.save(word(user.getId(), "v2", "n2", repeatData(3)));
        expressionRepository.save(expression(user.getId(), "v2", "n2", repeatData(11)));

        commit(() -> intervalsRepository.removeUnused(user.getId()));

        ImmutableList<Integer> actual = intervalsRepository.findAll(user.getId());
        Assertions.assertEquals(actual, List.of(3, 11));
    }

    @Test
    @DisplayName("""
            removeUnused(userId):
             there are unused intervals
             => don't remove intervals other users
            """)
    public void removeUnused3() {
        User user = userRepository.save(user(1));
        User otherUser = userRepository.save(user(2));
        commit(() -> {
            intervalsRepository.add(user.getId(), 1);
            intervalsRepository.add(user.getId(), 3);
            intervalsRepository.add(user.getId(), 5);
            intervalsRepository.add(user.getId(), 11);

            intervalsRepository.add(otherUser.getId(), 1);
            intervalsRepository.add(otherUser.getId(), 3);
            intervalsRepository.add(otherUser.getId(), 5);
            intervalsRepository.add(otherUser.getId(), 11);
        });
        wordsRepository.save(word(user.getId(), "v2", "n2", repeatData(3)));
        expressionRepository.save(expression(user.getId(), "v2", "n2", repeatData(11)));

        commit(() -> intervalsRepository.removeUnused(user.getId()));

        ImmutableList<Integer> actual = intervalsRepository.findAll(otherUser.getId());
        Assertions.assertEquals(actual, List.of(1, 3, 5, 11));
    }


    private User user(int number) {
        return User.newBuilder(validator).
                setPassword("password" + number).
                setOrGenerateSalt("salt" + number).
                setEmail("me" + number + "@mail.com").
                addRole("role1").
                addRole("role2").
                addRole("role3").
                build();
    }

    private Word word(UUID userId,
                      String value,
                      String note,
                      RepeatData repeatData) {
        return new Word(
                null,
                userId,
                value,
                note,
                new ArrayList<>(),
                new ArrayList<>(),
                new ArrayList<>(),
                new ArrayList<>(),
                repeatData
        );
    }

    private Expression expression(UUID userId,
                                  String value,
                                  String note,
                                  RepeatData repeatData) {
        return new Expression(
                null,
                userId,
                value,
                note,
                new ArrayList<>(),
                new ArrayList<>(),
                new ArrayList<>(),
                repeatData
        );
    }

    private RepeatData repeatData(int interval) {
        return new RepeatData(interval, LocalDate.of(2022, 7, 7));
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