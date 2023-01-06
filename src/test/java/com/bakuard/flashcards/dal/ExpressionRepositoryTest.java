package com.bakuard.flashcards.dal;

import com.bakuard.flashcards.config.MutableClock;
import com.bakuard.flashcards.config.SpringConfig;
import com.bakuard.flashcards.config.TestConfig;
import com.bakuard.flashcards.model.auth.credential.Credential;
import com.bakuard.flashcards.model.auth.credential.User;
import com.bakuard.flashcards.model.expression.Expression;
import com.bakuard.flashcards.model.expression.ExpressionExample;
import com.bakuard.flashcards.model.expression.ExpressionInterpretation;
import com.bakuard.flashcards.model.expression.ExpressionTranslation;
import com.bakuard.flashcards.model.filter.SortRules;
import com.bakuard.flashcards.validation.ValidatorUtil;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.function.Executable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.jdbc.JdbcTestUtils;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import java.time.LocalDate;
import java.util.*;
import java.util.function.Supplier;

@ExtendWith(SpringExtension.class)
@TestPropertySource(locations = "classpath:test.properties")
@Import({SpringConfig.class, TestConfig.class})
class ExpressionRepositoryTest {

    @Autowired
    private ExpressionRepository expressionRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private JdbcTemplate jdbcTemplate;
    @Autowired
    private DataSourceTransactionManager transactionManager;
    @Autowired
    private ValidatorUtil validator;
    @Autowired
    private MutableClock clock;
    @Autowired
    private SortRules sortRules;

    @BeforeEach
    public void beforeEach() {
        commit(() -> JdbcTestUtils.deleteFromTables(jdbcTemplate,
                "expressions",
                "words",
                "intervals",
                "users",
                "repeat_words_from_english_statistic",
                "repeat_words_from_native_statistic",
                "repeat_expressions_from_english_statistic",
                "repeat_expressions_from_native_statistic",
                "word_outer_source",
                "words_examples_outer_source"
        ));
        clock.setDate(2022, 7, 7);
    }

    @Test
    @DisplayName("""
            save(expression):
             there are not expressions in DB with such value
             => success save expression
            """)
    public void save() {
        User user = user(1);
        commit(() -> userRepository.save(user));
        Expression expected = expression(user.getId(), "value 1", "note 1", 1);

        commit(() -> expressionRepository.save(expected));

        Expression actual = expressionRepository.findById(expected.getId()).orElseThrow();
        Assertions.
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
        commit(() -> userRepository.save(user));
        Expression expected = expression(user.getId(), "value 1", "note 1", 1);
        commit(() -> expressionRepository.save(expected));

        Optional<Expression> actual = expressionRepository.findById(user.getId(), toUUID(1));

        Assertions.assertThat(actual).isEmpty();
    }

    @Test
    @DisplayName("""
            findById(userId, expressionId):
             there is expression with such id
             => return correct expression
            """)
    public void findById2() {
        User user = user(1);
        commit(() -> userRepository.save(user));
        Expression expected = expression(user.getId(), "value 1", "note 1", 1);
        commit(() -> expressionRepository.save(expected));

        Expression actual = expressionRepository.findById(user.getId(), expected.getId()).orElseThrow();

        Assertions.
                assertThat(actual).
                usingRecursiveComparison().
                isEqualTo(expected);
    }

    @Test
    @DisplayName("""
            countForValue(userId, value, maxDistance):
             user with userId hasn't any expressions
             => return 0
            """)
    public void countForValue1() {
        User user = commit(() -> userRepository.save(user(1)));

        long actual = expressionRepository.countForValue(user.getId(), "value", 2);

        Assertions.assertThat(actual).isZero();
    }

    @Test
    @DisplayName("""
            countForValue(userId, value, maxDistance):
             user has some expressions,
             there are not expressions with maxDistance <= 2
             => return 0
            """)
    public void countForValue2() {
        User user = commit(() -> {
            User temp = userRepository.save(user(1));
            expressionRepository.save(expression(temp.getId(), "value", "note", 1));
            expressionRepository.save(expression(temp.getId(), "cock", "note", 3));
            expressionRepository.save(expression(temp.getId(), "rise", "note", 5));
            expressionRepository.save(expression(temp.getId(), "value1234", "note", 10));
            return temp;
        });

        long actual = expressionRepository.countForValue(user.getId(), "cockroach", 2);

        Assertions.assertThat(actual).isZero();
    }

    @Test
    @DisplayName("""
            countForValue(userId, value, maxDistance):
             user has some expressions,
             there are expressions with maxDistance <= 2
             => return correct result
            """)
    public void countForValue3() {
        User user = commit(() -> {
            User temp = userRepository.save(user(1));
            expressionRepository.save(expression(temp.getId(), "frog", "note", 1));
            expressionRepository.save(expression(temp.getId(), "frog1", "note", 3));
            expressionRepository.save(expression(temp.getId(), "broom", "note", 5));
            expressionRepository.save(expression(temp.getId(), "distance", "note", 10));
            return temp;
        });

        long actual = expressionRepository.countForValue(user.getId(), "frog", 2);

        Assertions.assertThat(actual).isEqualTo(2);
    }

    @Test
    @DisplayName("""
            findByValue(userId, value, maxDistance, limit, offset):
             user with userId hasn't any expressions
             return empty list
            """)
    public void findByValue1() {
        User user = commit(() -> userRepository.save(user(1)));

        List<Expression> actual = expressionRepository.findByValue(
                user.getId(),
                "cockroach",
                2,
                10,
                0
        );

        Assertions.assertThat(actual).isEmpty();
    }

    @Test
    @DisplayName("""
            findByValue(userId, value, maxDistance, limit, offset):
             user has some expressions,
             there are not expressions with maxDistance <= 2
             return empty list
            """)
    public void findByValue2() {
        User user = commit(() -> {
            User temp = userRepository.save(user(1));
            expressionRepository.save(expression(temp.getId(), "value", "note", 1));
            expressionRepository.save(expression(temp.getId(), "cock", "note", 3));
            expressionRepository.save(expression(temp.getId(), "rise", "note", 5));
            expressionRepository.save(expression(temp.getId(), "value1234", "note", 10));
            return temp;
        });

        List<Expression> actual = expressionRepository.findByValue(
                user.getId(),
                "cockroach",
                2,
                10,
                0
        );

        Assertions.assertThat(actual).isEmpty();
    }

    @Test
    @DisplayName("""
            findByValue(userId, value, maxDistance, limit, offset):
             user has some expressions,
             there are expressions with maxDistance <= 2
             => return correct result
            """)
    public void findByValue3() {
        User user = commit(() -> userRepository.save(user(1)));
        List<Expression> expressions = List.of(
                expression(user.getId(), "frog", "note", 1),
                expression(user.getId(), "frog1", "note", 3),
                expression(user.getId(), "broom", "note", 5),
                expression(user.getId(), "distance", "note", 10)
        );
        commit(() -> expressions.forEach(expression -> expressionRepository.save(expression)));

        List<Expression> actual = expressionRepository.findByValue(
                user.getId(),
                "frog",
                2,
                10,
                0
        );

        Assertions.assertThat(actual).
                usingRecursiveFieldByFieldElementComparator().
                containsExactly(expressions.get(0), expressions.get(1));
    }

    @Test
    @DisplayName("""
            deleteById(userId, expressionId):
             there is not expression with such expressionId
             => don't delete any expressions
            """)
    public void deleteById1() {
        User user = commit(() -> userRepository.save(user(1)));
        Expression expected = commit(() -> expressionRepository.save(
                expression(user.getId(), "value 1", "note 1", 1)
        ));

        commit(() -> expressionRepository.deleteById(user.getId(), toUUID(1)));

        Assertions.assertThat(expressionRepository.existsById(expected.getId())).isTrue();
    }

    @Test
    @DisplayName("""
            deleteById(userId, expressionId):
             there is not expression with such expressionId
             => return false
            """)
    public void deleteById2() {
        User user = commit(() -> userRepository.save(user(1)));
        Expression expected = commit(() -> expressionRepository.save(
                expression(user.getId(), "value 1", "note 1", 1)
        ));

        boolean actual = commit(() -> expressionRepository.deleteById(user.getId(), toUUID(1)));

        Assertions.assertThat(actual).isFalse();
    }

    @Test
    @DisplayName("""
            deleteById(userId, expressionId):
             there is expression with such expressionId
             => delete this expression
            """)
    public void deleteById3() {
        User user = commit(() -> userRepository.save(user(1)));
        Expression expected = commit(() -> expressionRepository.save(
                expression(user.getId(), "value 1", "note 1", 1)
        ));

        commit(() -> expressionRepository.deleteById(user.getId(), expected.getId()));

        Assertions.assertThat(expressionRepository.existsById(expected.getId())).isFalse();
    }

    @Test
    @DisplayName("""
            deleteById(userId, expressionId):
             there is expression with such expressionId
             => return true
            """)
    public void deleteById4() {
        User user = commit(() -> userRepository.save(user(1)));
        Expression expected = commit(() -> expressionRepository.save(
                expression(user.getId(), "value 1", "note 1", 1)
        ));

        boolean actual = commit(() -> expressionRepository.deleteById(user.getId(), expected.getId()));

        Assertions.assertThat(actual).isTrue();
    }

    @Test
    @DisplayName("""
            existsById(userId, expressionId):
             there is not expression with such expressionId
             => return false
            """)
    public void existsById1() {
        User user = commit(() -> userRepository.save(user(1)));
        commit(() -> expressionRepository.save(
                expression(user.getId(), "value 1", "note 1", 1)
        ));

        Assertions.assertThat(expressionRepository.existsById(user.getId(), toUUID(1))).isFalse();
    }

    @Test
    @DisplayName("""
            existsById(userId, expressionId):
             there is expression with such expressionId
             => return true
            """)
    public void existsById2() {
        User user = commit(() -> userRepository.save(user(1)));
        Expression expected = commit(() -> expressionRepository.save(
                expression(user.getId(), "value 1", "note 1", 1)
        ));

        Assertions.assertThat(expressionRepository.existsById(user.getId(), expected.getId())).isTrue();
    }

    @Test
    @DisplayName("""
            count(user):
             user haven't any expressions
             => return 0
            """)
    public void count1() {
        User user1 = commit(() -> userRepository.save(user(1)));
        User user2 = commit(() -> userRepository.save(user(2)));
        User user3 = commit(() -> userRepository.save(user(3)));
        commit(() -> {
            expressionRepository.save(expression(user1.getId(), "value1", "note1", 1));
            expressionRepository.save(expression(user2.getId(), "value2", "note2", 1));
        });

        long actual = expressionRepository.count(user3.getId());

        Assertions.assertThat(actual).isZero();
    }

    @Test
    @DisplayName("""
            count(user):
             user have some expressions
             => return correct result
            """)
    public void count2() {
        User user1 = commit(() -> userRepository.save(user(1)));
        User user2 = commit(() -> userRepository.save(user(2)));
        User user3 = commit(() -> userRepository.save(user(3)));
        commit(() -> {
            expressionRepository.save(expression(user1.getId(), "value1", "note1", 1));
            expressionRepository.save(expression(user2.getId(), "value2", "note2", 1));
            expressionRepository.save(expression(user3.getId(), "value3", "note3", 1));
            expressionRepository.save(expression(user3.getId(), "value4", "note4", 1));
        });

        long actual = expressionRepository.count(user3.getId());

        Assertions.assertThat(actual).isEqualTo(2);
    }

    @Test
    @DisplayName("""
            countForRepeatFromEnglish(userId, date):
             user with such id have some expressions for repeat with date
             => return correct result
            """)
    public void countForRepeatFromEnglish1() {
        User user = commit(() -> userRepository.save(user(1)));
        commit(() -> {
            clock.setDate(2022, 7, 7);
            expressionRepository.save(
                    expression(user.getId(), "value1", "note1", 1)
            );
            clock.setDate(2022, 7, 7);
            expressionRepository.save(
                    expression(user.getId(), "value2", "note2", 3)
            );
            clock.setDate(2022, 7, 10);
            expressionRepository.save(
                    expression(user.getId(), "value3", "note3", 1)
            );
            clock.setDate(2022, 7, 7);
            expressionRepository.save(
                    expression(user.getId(), "value4", "note4", 10)
            );
        });

        long actual = expressionRepository.countForRepeatFromEnglish(
                user.getId(), LocalDate.of(2022, 7, 10)
        );

        Assertions.assertThat(actual).isEqualTo(2);
    }

    @Test
    @DisplayName("""
            countForRepeatFromEnglish(userId, date):
             user haven't any expressions
             => return 0
            """)
    public void countForRepeatFromEnglish2() {
        User user = commit(() -> userRepository.save(user(1)));

        long actual = expressionRepository.countForRepeatFromEnglish(
                user.getId(), LocalDate.of(2022, 7, 10)
        );

        Assertions.assertThat(actual).isZero();
    }

    @Test
    @DisplayName("""
            countForRepeatFromEnglish(userId, date):
             user have expressions, but haven't any expressions to repeat
             => return 0
            """)
    public void countForRepeatFromEnglish3() {
        User user = commit(() -> userRepository.save(user(1)));
        commit(() -> {
            clock.setDate(2022, 7, 7);
            expressionRepository.save(
                    expression(user.getId(), "value1", "note1", 1)
            );
            clock.setDate(2022, 7, 7);
            expressionRepository.save(
                    expression(user.getId(), "value2", "note2", 3)
            );
            clock.setDate(2022, 7, 10);
            expressionRepository.save(
                    expression(user.getId(), "value3", "note3", 1)
            );
            clock.setDate(2022, 7, 7);
            expressionRepository.save(
                    expression(user.getId(), "value4", "note4", 10)
            );
        });

        long actual = expressionRepository.countForRepeatFromEnglish(
                user.getId(), LocalDate.of(2022, 7, 7)
        );

        Assertions.assertThat(actual).isZero();
    }

    @Test
    @DisplayName("""
            countForRepeatFromNative(userId, date):
             user with such id have some expressions for repeat with date
             => return correct result
            """)
    public void countForRepeatFromNative1() {
        User user = commit(() -> userRepository.save(user(1)));
        commit(() -> {
            clock.setDate(2022, 7, 7);
            expressionRepository.save(
                    expression(user.getId(), "value1", "note1", 1)
            );
            clock.setDate(2022, 7, 7);
            expressionRepository.save(
                    expression(user.getId(), "value2", "note2", 3)
            );
            clock.setDate(2022, 7, 10);
            expressionRepository.save(
                    expression(user.getId(), "value3", "note3", 1)
            );
            clock.setDate(2022, 7, 7);
            expressionRepository.save(
                    expression(user.getId(), "value4", "note4", 10)
            );
        });

        long actual = expressionRepository.countForRepeatFromNative(
                user.getId(), LocalDate.of(2022, 7, 10)
        );

        Assertions.assertThat(actual).isEqualTo(2);
    }

    @Test
    @DisplayName("""
            countForRepeatFromNative(userId, date):
             user haven't any expressions
             => return 0
            """)
    public void countForRepeatFromNative2() {
        User user = commit(() -> userRepository.save(user(1)));

        long actual = expressionRepository.countForRepeatFromNative(
                user.getId(), LocalDate.of(2022, 7, 10)
        );

        Assertions.assertThat(actual).isZero();
    }

    @Test
    @DisplayName("""
            countForRepeatFromNative(userId, date):
             user have expressions, but haven't any expressions to repeat
             => return 0
            """)
    public void countForRepeatFromNative3() {
        User user = commit(() -> userRepository.save(user(1)));
        commit(() -> {
            clock.setDate(2022, 7, 7);
            expressionRepository.save(
                    expression(user.getId(), "value1", "note1", 1)
            );
            clock.setDate(2022, 7, 7);
            expressionRepository.save(
                    expression(user.getId(), "value2", "note2", 3)
            );
            clock.setDate(2022, 7, 10);
            expressionRepository.save(
                    expression(user.getId(), "value3", "note3", 1)
            );
            clock.setDate(2022, 7, 7);
            expressionRepository.save(
                    expression(user.getId(), "value4", "note4", 10)
            );
        });

        long actual = expressionRepository.countForRepeatFromNative(
                user.getId(), LocalDate.of(2022, 7, 7)
        );

        Assertions.assertThat(actual).isZero();
    }

    @Test
    @DisplayName("""
            findByUserId(userId, pageable):
             user haven't any expressions
             => return empty page
            """)
    public void findByUserId1() {
        User user = commit(() -> userRepository.save(user(1)));

        Page<Expression> actual = expressionRepository.findByUserId(user.getId(), PageRequest.of(0, 20));

        Assertions.assertThat(actual).isEmpty();
    }

    @Test
    @DisplayName("""
            findByUserId(userId, pageable):
             user have some expressions,
             sort expressions by value descending
             => return correct result
            """)
    public void findByUserId2() {
        User user = commit(() -> userRepository.save(user(1)));
        List<Expression> expressions = expressions(user.getId());
        commit(() -> expressions.forEach(expression -> expressionRepository.save(expression)));

        Page<Expression> actual = expressionRepository.findByUserId(user.getId(),
                PageRequest.of(0, 20, Sort.by("value").descending()));

        List<Expression> expected = expressions.stream().
                sorted(Comparator.comparing(Expression::getValue).reversed()).
                toList();
        Assertions.assertThat(actual.getContent()).
                usingRecursiveFieldByFieldElementComparator().
                isEqualTo(expected);
    }

    @Test
    @DisplayName("""
            findByUserId(userId, pageable):
             user have some expressions,
             sort expressions by interval asc and value asc
             => return correct result
            """)
    public void findByUserId3() {
        User user = commit(() -> userRepository.save(user(1)));
        List<Expression> expressions = expressions(user.getId());
        commit(() -> expressions.forEach(expression -> expressionRepository.save(expression)));

        Page<Expression> actual = expressionRepository.findByUserId(user.getId(),
                PageRequest.of(0, 20, Sort.by("repeat_interval_from_english", "value")));

        List<Expression> expected = expressions.stream().
                sorted(Comparator.comparing((Expression e) -> e.getRepeatDataFromEnglish().interval()).
                        thenComparing(Expression::getValue)).
                toList();
        Assertions.assertThat(actual.getContent()).
                usingRecursiveFieldByFieldElementComparator().
                isEqualTo(expected);
    }

    @Test
    @DisplayName("""
            findAllForRepeatFromEnglish(userId, date):
             user haven't any expression
             => return empty page
            """)
    public void findAllForRepeatFromEnglish1() {
        User user = commit(() -> userRepository.save(user(1)));

        List<Expression> actual = expressionRepository.findAllForRepeatFromEnglish(
                user.getId(),
                LocalDate.of(2022, 7, 10),
                20, 0
        );

        Assertions.assertThat(actual).isEmpty();
    }

    @Test
    @DisplayName("""
            findAllForRepeatFromEnglish(userId, date):
             user have some expressions,
             there are not expressions for repeat
             => return empty page
            """)
    public void findAllForRepeatFromEnglish2() {
        User user = commit(() -> userRepository.save(user(1)));
        List<Expression> expressions = expressions(user.getId());
        expressions.forEach(expression -> expressionRepository.save(expression));

        List<Expression> actual = expressionRepository.findAllForRepeatFromEnglish(
                user.getId(),
                LocalDate.of(2022, 7, 1),
                20, 0
        );

        Assertions.assertThat(actual).isEmpty();
    }

    @Test
    @DisplayName("""
            findAllForRepeatFromEnglish(userId, date):
             user have some expressions,
             there are expressions for repeat
             => return correct result
            """)
    public void findAllForRepeatFromEnglish3() {
        User user = commit(() -> userRepository.save(user(1)));
        List<Expression> expressions = expressions(user.getId());
        commit(() -> expressions.forEach(expression -> expressionRepository.save(expression)));

        LocalDate repeatDate = LocalDate.of(2022, 7, 10);
        List<Expression> actual = expressionRepository.findAllForRepeatFromEnglish(
                user.getId(),
                repeatDate,
                2, 0
        );

        Assertions.assertThat(actual).
                usingRecursiveFieldByFieldElementComparator().
                isEqualTo(expressions.subList(0, 2));
    }

    @Test
    @DisplayName("""
            findAllForRepeatFromNative(userId, date):
             user haven't any expression
             => return empty page
            """)
    public void findAllForRepeatFromNative1() {
        User user = commit(() -> userRepository.save(user(1)));

        List<Expression> actual = expressionRepository.findAllForRepeatFromNative(
                user.getId(),
                LocalDate.of(2022, 7, 10),
                20, 0
        );

        Assertions.assertThat(actual).isEmpty();
    }

    @Test
    @DisplayName("""
            findAllForRepeatFromNative(userId, date):
             user have some expressions,
             there are not expressions for repeat
             => return empty page
            """)
    public void findAllForRepeatFromNative2() {
        User user = commit(() -> userRepository.save(user(1)));
        List<Expression> expressions = expressions(user.getId());
        expressions.forEach(expression -> expressionRepository.save(expression));

        List<Expression> actual = expressionRepository.findAllForRepeatFromNative(
                user.getId(),
                LocalDate.of(2022, 7, 1),
                20, 0
        );

        Assertions.assertThat(actual).isEmpty();
    }

    @Test
    @DisplayName("""
            findAllForRepeatFromNative(userId, date):
             user have some expressions,
             there are expressions for repeat
             => return correct result
            """)
    public void findAllForRepeatFromNative3() {
        User user = commit(() -> userRepository.save(user(1)));
        List<Expression> expressions = expressions(user.getId());
        commit(() -> expressions.forEach(expression -> expressionRepository.save(expression)));

        LocalDate repeatDate = LocalDate.of(2022, 7, 10);
        List<Expression> actual = expressionRepository.findAllForRepeatFromNative(
                user.getId(),
                repeatDate,
                2, 0
        );

        Assertions.assertThat(actual).
                usingRecursiveFieldByFieldElementComparator().
                isEqualTo(expressions.subList(0, 2));
    }

    @Test
    @DisplayName("""
            countForTranslate(userId, translate):
             user with userId hasn't any expressions
             => return 0
            """)
    public void countForTranslate1() {
        User user = commit(() -> userRepository.save(user(1)));

        long actual = expressionRepository.countForTranslate(user.getId(), "unknown translate");

        Assertions.assertThat(actual).isZero();
    }

    @Test
    @DisplayName("""
            countForTranslate(userId, translate):
             user has some expressions,
             user hasn't expressions with this translate
             => return correct result
            """)
    public void countForTranslate2() {
        User user = commit(() -> userRepository.save(user(1)));
        commit(() -> {
            expressionRepository.save(expression(user.getId(), "expressionA", "noteA", 10));
            expressionRepository.save(expression(user.getId(), "expressionB", "noteB", 10));
            expressionRepository.save(expression(user.getId(), "expressionC", "noteC", 10));
        });

        long actual = expressionRepository.countForTranslate(user.getId(), "unknown translate");

        Assertions.assertThat(actual).isZero();
    }

    @Test
    @DisplayName("""
            countForTranslate(userId, translate):
             user has some expressions,
             user has expressions with this translate
             => return correct result
            """)
    public void countForTranslate3() {
        User user = commit(() -> userRepository.save(user(1)));
        commit(() -> {
            expressionRepository.save(expression(user.getId(), "expressionA", "noteA", 10));
            expressionRepository.save(expression(user.getId(), "expressionB", "noteB", 10));
            expressionRepository.save(expression(user.getId(), "expressionC", "noteC", 10));
            expressionRepository.save(
                    new Expression(user.getId(), 1, 1, clock).
                            setValue("expressionD").
                            setNote("noteD").
                            addTranslation(new ExpressionTranslation("translateX", "noteX"))
            );
            expressionRepository.save(
                    new Expression(user.getId(), 1, 1, clock).
                            setValue("expressionE").
                            setNote("noteE").
                            addTranslation(new ExpressionTranslation("translateX", "noteX"))
            );
        });

        long actual = expressionRepository.countForTranslate(user.getId(), "translateX");

        Assertions.assertThat(actual).isEqualTo(2);
    }

    @Test
    @DisplayName("""
            findByTranslate(userId, translate, limit, offset):
             user with userId hasn't any expressions
             => return empty list
            """)
    public void findByTranslate1() {
        User user = commit(() -> userRepository.save(user(1)));

        List<Expression> actual = expressionRepository.findByTranslate(user.getId(), "translateX", 10, 0);

        Assertions.assertThat(actual).isEmpty();
    }

    @Test
    @DisplayName("""
            findByTranslate(userId, translate, limit, offset):
             user with userId has some expressions,
             user hasn't any expressions with this translate
             => return empty list
            """)
    public void findByTranslate2() {
        User user = commit(() -> userRepository.save(user(1)));
        commit(() -> {
            expressionRepository.save(expression(user.getId(), "expressionA", "noteA", 1));
            expressionRepository.save(expression(user.getId(), "expressionB", "noteB", 1));
            expressionRepository.save(expression(user.getId(), "expressionC", "noteC", 1));
        });

        List<Expression> actual = expressionRepository.findByTranslate(user.getId(), "translateX", 10, 0);

        Assertions.assertThat(actual).isEmpty();
    }

    @Test
    @DisplayName("""
            findByTranslate(userId, translate, limit, offset):
             user with userId has some expressions,
             user has expressions with this translate
             => return empty list
            """)
    public void findByTranslate3() {
        User user = commit(() -> userRepository.save(user(1)));
        Expression expressionD = new Expression(user.getId(), 1, 1, clock).
                setValue("expressionD").
                setNote("noteD").
                addTranslation(new ExpressionTranslation("translateX", "noteX"));
        Expression expressionE = new Expression(user.getId(), 1, 1, clock).
                setValue("expressionE").
                setNote("noteE").
                addTranslation(new ExpressionTranslation("translateX", "noteX"));
        Expression expressionF = new Expression(user.getId(), 1, 1, clock).
                setValue("expressionF").
                setNote("noteF").
                addTranslation(new ExpressionTranslation("translateX", "noteX"));
        commit(() -> {
            expressionRepository.save(expression(user.getId(), "expressionA", "noteA", 10));
            expressionRepository.save(expression(user.getId(), "expressionB", "noteB", 10));
            expressionRepository.save(expression(user.getId(), "expressionC", "noteC", 10));
            expressionRepository.save(expressionF);
            expressionRepository.save(expressionD);
            expressionRepository.save(expressionE);
        });

        List<Expression> actual = expressionRepository.findByTranslate(user.getId(), "translateX", 10, 0);

        Assertions.assertThat(actual).
                usingRecursiveFieldByFieldElementComparator().
                containsExactly(expressionD, expressionE, expressionF);
    }
    

    private UUID toUUID(int number) {
        return UUID.fromString("00000000-0000-0000-0000-" + String.format("%012d", number));
    }

    private User user(int number) {
        return new User(new Credential("me" + number + "@mail.com", "password" + number)).
                setOrGenerateSalt("salt" + number).
                addRole("role1").
                addRole("role2").
                addRole("role3");
    }

    private Expression expression(UUID userId,
                                  String value,
                                  String note,
                                  int interval) {
        return new Expression(userId, interval, interval, clock).
                setValue(value).
                setNote(note).
                addTranslation(new ExpressionTranslation("translateA", "noteA")).
                addTranslation(new ExpressionTranslation("translateB", "noteB")).
                addTranslation(new ExpressionTranslation("translateC", "noteC")).
                addInterpretation(new ExpressionInterpretation("interpretationA")).
                addInterpretation(new ExpressionInterpretation("interpretationB")).
                addInterpretation(new ExpressionInterpretation("interpretationC")).
                addExample(new ExpressionExample("exampleA", "exampleTranslate", "noteA")).
                addExample(new ExpressionExample("exampleB", "exampleTranslate", "noteA")).
                addExample(new ExpressionExample("exampleC", "exampleTranslate", "noteA"));
    }
    
    private List<Expression> expressions(UUID userId) {
        ArrayList<Expression> expressions = new ArrayList<>();

        clock.setDate(2022, 7, 1);
        expressions.add(
                expression(userId, "expressionA", "noteA", 1)
        );
        clock.setDate(2022, 7, 2);
        expressions.add(
                expression(userId, "expressionB", "noteB", 1)
        );
        clock.setDate(2022, 7, 6);
        expressions.add(
                expression(userId, "expressionC", "noteB", 3)
        );
        clock.setDate(2022, 7, 7);
        expressions.add(
                expression(userId, "expressionD", "noteD", 3)
        );
        clock.setDate(2022, 7, 8);
        expressions.add(
                expression(userId, "expressionE", "noteE", 3)
        );
        clock.setDate(2022, 7, 10);
        expressions.add(
                expression(userId, "expressionF", "noteF", 1)
        );

        return expressions;
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

    private <T> T commit(Supplier<T> supplier) {
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        TransactionStatus status = transactionManager.getTransaction(def);
        try {
            T result = supplier.get();
            transactionManager.commit(status);
            return result;
        } catch(Throwable e) {
            transactionManager.rollback(status);
            throw new RuntimeException(e);
        }
    }

}