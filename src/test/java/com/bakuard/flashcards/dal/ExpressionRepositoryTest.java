package com.bakuard.flashcards.dal;

import com.bakuard.flashcards.config.SpringConfig;
import com.bakuard.flashcards.model.expression.Expression;
import com.bakuard.flashcards.model.RepeatData;
import com.bakuard.flashcards.model.credential.User;
import org.assertj.core.api.SoftAssertions;
import org.assertj.core.api.recursive.comparison.RecursiveComparisonConfiguration;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.jdbc.AutoConfigureDataJdbc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.jdbc.JdbcTestUtils;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import java.time.LocalDate;
import java.util.*;

@SpringBootTest(classes = SpringConfig.class)
@AutoConfigureDataJdbc
@TestPropertySource(locations = "classpath:application.properties")
class ExpressionRepositoryTest {

    @Autowired
    private ExpressionRepository expressionRepository;
    @Autowired
    private UserRepository userRepository;
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
            save(expression):
             there are not expressions in DB with such value
             => success save expression
            """)
    public void save() {
        User user = user(1);
        userRepository.save(user);
        Expression expected = expression(
                user.getId(), "value 1", "note 1", new RepeatData(1, LocalDate.now())
        );

        expressionRepository.save(expected);

        Expression actual = expressionRepository.findById(expected.getId()).orElseThrow();
        org.assertj.core.api.Assertions.
                assertThat(expected).
                usingRecursiveComparison().
                isEqualTo(actual);
    }

    @Test
    @DisplayName("""
            findById(userId, expressionId):
             there is not expression with such id
             => return empty Optional
            """)
    public void findById1() {
        User user = user(1);
        userRepository.save(user);
        Expression expected = expression(
                user.getId(), "value 1", "note 1", new RepeatData(1, LocalDate.now())
        );
        expressionRepository.save(expected);

        Optional<Expression> actual = expressionRepository.findById(user.getId(), toUUID(1));

        Assertions.assertTrue(actual.isEmpty());
    }

    @Test
    @DisplayName("""
            findById(userId, expressionId):
             there is word with such id
             => return correct expression
            """)
    public void findById2() {
        User user = user(1);
        userRepository.save(user);
        Expression expected = expression(
                user.getId(), "value 1", "note 1", new RepeatData(1, LocalDate.now())
        );
        expressionRepository.save(expected);

        Expression actual = expressionRepository.findById(user.getId(), expected.getId()).orElseThrow();

        org.assertj.core.api.Assertions.
                assertThat(expected).
                usingRecursiveComparison().
                isEqualTo(actual);
    }

    @Test
    @DisplayName("""
            findByValue(userId, value):
             there is not expression with such value
             => return empty optional
            """)
    public void findByValue1() {
        User user = user(1);
        userRepository.save(user);
        Expression expected = expression(
                user.getId(), "value 1", "note 1", new RepeatData(1, LocalDate.now())
        );
        expressionRepository.save(expected);

        Optional<Expression> actual = expressionRepository.findByValue(user.getId(), "Unknown value");

        Assertions.assertTrue(actual.isEmpty());
    }

    @Test
    @DisplayName("""
            findByValue(userId, value):
             there is expression with such value
             => return correct expression
            """)
    public void findByValue2() {
        User user = user(1);
        userRepository.save(user);
        Expression expected = expression(
                user.getId(), "value 1", "note 1", new RepeatData(1, LocalDate.now())
        );
        expressionRepository.save(expected);

        Expression actual = expressionRepository.findByValue(user.getId(), "value 1").orElseThrow();

        org.assertj.core.api.Assertions.
                assertThat(expected).
                usingRecursiveComparison().
                isEqualTo(actual);
    }

    @Test
    @DisplayName("""
            deleteById(userId, expressionId):
             there is not expression with such expressionId
             => do nothing
            """)
    public void deleteById1() {
        User user = user(1);
        userRepository.save(user);
        Expression expected = expression(
                user.getId(), "value 1", "note 1", new RepeatData(1, LocalDate.now())
        );
        expressionRepository.save(expected);

        expressionRepository.deleteById(user.getId(), toUUID(1));

        Assertions.assertTrue(expressionRepository.existsById(expected.getId()));
    }

    @Test
    @DisplayName("""
            deleteById(userId, expressionId):
             there is expression with such expressionId
             => delete this expression
            """)
    public void deleteById2() {
        User user = user(1);
        userRepository.save(user);
        Expression expected = expression(
                user.getId(), "value 1", "note 1", new RepeatData(1, LocalDate.now())
        );
        expressionRepository.save(expected);

        expressionRepository.deleteById(user.getId(), expected.getId());

        Assertions.assertFalse(expressionRepository.existsById(expected.getId()));
    }

    @Test
    @DisplayName("""
            existsById(userId, expressionId):
             there is not expression with such expressionId
             => return false
            """)
    public void existsById1() {
        User user = user(1);
        userRepository.save(user);
        Expression expected = expression(
                user.getId(), "value 1", "note 1", new RepeatData(1, LocalDate.now())
        );
        expressionRepository.save(expected);

        Assertions.assertFalse(expressionRepository.existsById(user.getId(), toUUID(1)));
    }

    @Test
    @DisplayName("""
            existsById(userId, expressionId):
             there is expression with such expressionId
             => return true
            """)
    public void existsById2() {
        User user = user(1);
        userRepository.save(user);
        Expression expected = expression(
                user.getId(), "value 1", "note 1", new RepeatData(1, LocalDate.now())
        );
        expressionRepository.save(expected);

        Assertions.assertTrue(expressionRepository.existsById(user.getId(), expected.getId()));
    }

    @Test
    @DisplayName("""
            count(user):
             user haven't any expressions
             => return 0
            """)
    public void count1() {
        User user1 = userRepository.save(user(1));
        User user2 = userRepository.save(user(2));
        User user3 = userRepository.save(user(3));
        expressionRepository.save(expression(user1.getId(), "value1", "note1", defaultRepeatData()));
        expressionRepository.save(expression(user2.getId(), "value2", "note2", defaultRepeatData()));

        long actual = expressionRepository.count(user3.getId());

        Assertions.assertEquals(0, actual);
    }

    @Test
    @DisplayName("""
            count(user):
             user have some expressions
             => return correct result
            """)
    public void count2() {
        User user1 = userRepository.save(user(1));
        User user2 = userRepository.save(user(2));
        User user3 = userRepository.save(user(3));
        expressionRepository.save(expression(user1.getId(), "value1", "note1", defaultRepeatData()));
        expressionRepository.save(expression(user2.getId(), "value2", "note2", defaultRepeatData()));
        expressionRepository.save(expression(user3.getId(), "value3", "note3", defaultRepeatData()));
        expressionRepository.save(expression(user3.getId(), "value4", "note4", defaultRepeatData()));

        long actual = expressionRepository.count(user3.getId());

        Assertions.assertEquals(2, actual);
    }

    @Test
    @DisplayName("""
            countForRepeat(userId, date):
             user with such id have some expressions for repeat with date
             => return correct result
            """)
    public void countForRepeat1() {
        User user1 = userRepository.save(user(1));
        expressionRepository.save(expression(user1.getId(), "value1", "note1",
                new RepeatData(1, LocalDate.of(2022, 7, 7))));
        expressionRepository.save(expression(user1.getId(), "value2", "note2",
                new RepeatData(3, LocalDate.of(2022, 7, 7))));
        expressionRepository.save(expression(user1.getId(), "value3", "note3",
                new RepeatData(1, LocalDate.of(2022, 7, 10))));
        expressionRepository.save(expression(user1.getId(), "value4", "note4",
                new RepeatData(10, LocalDate.of(2022, 7, 7))));

        long actual = expressionRepository.countForRepeat(
                user1.getId(), LocalDate.of(2022, 7, 10)
        );

        Assertions.assertEquals(2, actual);
    }

    @Test
    @DisplayName("""
            countForRepeat(userId, date):
             user haven't any expressions
             => return 0
            """)
    public void countForRepeat2() {
        User user1 = userRepository.save(user(1));

        long actual = expressionRepository.countForRepeat(
                user1.getId(), LocalDate.of(2022, 7, 10)
        );

        Assertions.assertEquals(0, actual);
    }

    @Test
    @DisplayName("""
            countForRepeat(userId, date):
             user have expressions, but haven't any expressions to repeat
             => return 0
            """)
    public void countForRepeat3() {
        User user1 = userRepository.save(user(1));
        expressionRepository.save(expression(user1.getId(), "value1", "note1",
                new RepeatData(1, LocalDate.of(2022, 7, 7))));
        expressionRepository.save(expression(user1.getId(), "value2", "note2",
                new RepeatData(3, LocalDate.of(2022, 7, 7))));
        expressionRepository.save(expression(user1.getId(), "value3", "note3",
                new RepeatData(1, LocalDate.of(2022, 7, 10))));
        expressionRepository.save(expression(user1.getId(), "value4", "note4",
                new RepeatData(10, LocalDate.of(2022, 7, 7))));

        long actual = expressionRepository.countForRepeat(
                user1.getId(), LocalDate.of(2022, 7, 7)
        );

        Assertions.assertEquals(0, actual);
    }

    @Test
    @DisplayName("""
            findByUserId(userId, pageable):
             user haven't any expressions
             => return empty page
            """)
    public void findByUserId1() {
        User user1 = userRepository.save(user(1));

        Page<Expression> actual = expressionRepository.findByUserId(user1.getId(), PageRequest.of(0, 20));

        Assertions.assertTrue(actual.isEmpty());
    }

    @Test
    @DisplayName("""
            findByUserId(userId, pageable):
             user have some expressions,
             sort expressions by value descending
             => return correct result
            """)
    public void findByUserId2() {
        User user = userRepository.save(user(1));
        List<Expression> expressions = expressions(user.getId());
        expressions.forEach(expression -> expressionRepository.save(expression));

        Page<Expression> actual = expressionRepository.findByUserId(user.getId(),
                PageRequest.of(0, 20, Sort.by("value").descending()));

        List<Expression> expected = expressions.stream().
                sorted(Comparator.comparing(Expression::getValue).reversed()).
                toList();
        Assertions.assertEquals(expected, actual.getContent());
    }

    @Test
    @DisplayName("""
            findByUserId(userId, pageable):
             user have some expressions,
             sort expressions by interval asc and value asc
             => return correct result
            """)
    public void findByUserId3() {
        User user = userRepository.save(user(1));
        List<Expression> expressions = expressions(user.getId());
        expressions.forEach(expression -> expressionRepository.save(expression));

        Page<Expression> actual = expressionRepository.findByUserId(user.getId(),
                PageRequest.of(0, 20, Sort.by("repeat_interval", "value")));

        List<Expression> expected = expressions.stream().
                sorted(Comparator.comparing((Expression e) -> e.getRepeatData().interval()).
                        thenComparing(Expression::getValue)).
                toList();
        Assertions.assertEquals(expected, actual.getContent());
    }

    @Test
    @DisplayName("""
            findAllForRepeat(userId, date):
             user haven't any expression
             => return empty page
            """)
    public void findAllForRepeat1() {
        User user = userRepository.save(user(1));

        List<Expression> actual = expressionRepository.findAllForRepeat(
                user.getId(),
                LocalDate.of(2022, 7, 10),
                20, 0
        );

        Assertions.assertTrue(actual.isEmpty());
    }

    @Test
    @DisplayName("""
            findAllForRepeat(userId, date):
             user have some expressions,
             there are not expressions for repeat
             => return empty page
            """)
    public void findAllForRepeat2() {
        User user = userRepository.save(user(1));
        List<Expression> expressions = expressions(user.getId());
        expressions.forEach(expression -> expressionRepository.save(expression));

        List<Expression> actual = expressionRepository.findAllForRepeat(
                user.getId(),
                LocalDate.of(2022, 7, 1),
                20, 0
        );

        Assertions.assertTrue(actual.isEmpty());
    }

    @Test
    @DisplayName("""
            findAllForRepeat(userId, date):
             user have some expressions,
             there are expressions for repeat
             => return correct result
            """)
    public void findAllForRepeat3() {
        User user = userRepository.save(user(1));
        List<Expression> words = expressions(user.getId());
        words.forEach(word -> expressionRepository.save(word));

        LocalDate repeatDate = LocalDate.of(2022, 7, 10);
        List<Expression> actual = expressionRepository.findAllForRepeat(
                user.getId(),
                repeatDate,
                2, 0
        );

        List<Expression> expected = words.stream().
                sorted(Comparator.comparing((Expression e) -> e.getRepeatData().nextDateOfRepeat())).
                filter(e -> e.getRepeatData().nextDateOfRepeat().compareTo(repeatDate) <= 0).
                limit(2).
                toList();
        Assertions.assertEquals(expected, actual);
    }

    @Test
    @DisplayName("""
            replaceRepeatInterval(userId, oldInterval, newInterval):
             there are not expressions with oldInterval
             => do nothing
            """)
    public void replaceRepeatInterval1() {
        User user = userRepository.save(user(1));
        List<Expression> expected = expressions(user.getId());
        expected.forEach(expression -> expressionRepository.save(expression));

        expressionRepository.replaceRepeatInterval(user.getId(), 5, 10);

        List<Expression> actual = expressionRepository.findByUserId(user.getId(),
                        PageRequest.of(0, 20, Sort.by("value").ascending())).
                getContent();
        org.assertj.core.api.Assertions.
                assertThat(actual).
                usingRecursiveFieldByFieldElementComparator(
                        RecursiveComparisonConfiguration.builder().
                                withIgnoreAllOverriddenEquals(true).
                                build()
                ).
                isEqualTo(expected);
    }

    @Test
    @DisplayName("""
            replaceRepeatInterval(userId, oldInterval, newInterval):
             there are expressions with oldInterval
             => replace them to newInterval
            """)
    public void replaceRepeatInterval2() {
        User user = userRepository.save(user(1));
        List<Expression> expected = expressions(user.getId());
        expected.forEach(word -> expressionRepository.save(word));

        expressionRepository.replaceRepeatInterval(user.getId(), 1, 10);

        List<Expression> actual = expressionRepository.findByUserId(user.getId(),
                        PageRequest.of(0, 20, Sort.by("value").ascending())).
                getContent();
        SoftAssertions assertions = new SoftAssertions();
        assertions.assertThat(actual).
                elements(2, 3, 4).allMatch(w -> w.getRepeatData().interval() == 3);
        assertions.assertThat(actual).
                elements(0, 1, 5).allMatch(w -> w.getRepeatData().interval() == 10);
        assertions.assertAll();
    }


    private UUID toUUID(int number) {
        return UUID.fromString("00000000-0000-0000-0000-" + String.format("%012d", number));
    }

    private User user(int number) {
        return new User(
                null,
                "password" + number,
                "salt" + number,
                "user" + number + "@gmail.com"
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

    private List<Expression> expressions(UUID userId) {
        ArrayList<Expression> expressions = new ArrayList<>();

        expressions.add(
                expression(userId, "expressionA", "noteA",
                        new RepeatData(1, LocalDate.of(2022, 7, 1))
                )
        );
        expressions.add(
                expression(userId, "expressionB", "noteB",
                        new RepeatData(1, LocalDate.of(2022, 7, 2))
                )
        );
        expressions.add(
                expression(userId, "expressionC", "noteB",
                        new RepeatData(3, LocalDate.of(2022, 7, 6))
                )
        );
        expressions.add(
                expression(userId, "expressionD", "noteD",
                        new RepeatData(3, LocalDate.of(2022, 7, 7))
                )
        );
        expressions.add(
                expression(userId, "expressionE", "noteE",
                        new RepeatData(3, LocalDate.of(2022, 7, 8))
                )
        );
        expressions.add(
                expression(userId, "expressionF", "noteF",
                        new RepeatData(1, LocalDate.of(2022, 7, 10))
                )
        );

        return expressions;
    }

    private RepeatData defaultRepeatData() {
        return new RepeatData(1, LocalDate.of(2022, 7, 7));
    }

}