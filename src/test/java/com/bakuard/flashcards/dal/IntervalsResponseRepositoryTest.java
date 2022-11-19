package com.bakuard.flashcards.dal;

import com.bakuard.flashcards.config.SpringConfig;
import com.bakuard.flashcards.config.TestConfig;
import com.bakuard.flashcards.model.RepeatDataFromEnglish;
import com.bakuard.flashcards.model.RepeatDataFromNative;
import com.bakuard.flashcards.model.auth.credential.Credential;
import com.bakuard.flashcards.model.auth.credential.User;
import com.bakuard.flashcards.model.expression.Expression;
import com.bakuard.flashcards.model.word.Word;
import com.bakuard.flashcards.validation.InvalidParameter;
import com.bakuard.flashcards.validation.ValidatorUtil;
import com.google.common.collect.ImmutableList;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.jdbc.JdbcTestUtils;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import java.time.Clock;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;

@ExtendWith(SpringExtension.class)
@TestPropertySource(locations = "classpath:test.properties")
@Import({SpringConfig.class, TestConfig.class})
class IntervalsResponseRepositoryTest {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private IntervalRepository intervalRepository;
    @Autowired
    private WordRepository wordRepository;
    @Autowired
    private ExpressionRepository expressionRepository;
    @Autowired
    private JdbcTemplate jdbcTemplate;
    @Autowired
    private DataSourceTransactionManager transactionManager;
    @Autowired
    private ValidatorUtil validator;
    @Autowired
    private Clock clock;

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
                "words_interpretations_outer_source",
                "words_transcriptions_outer_source",
                "words_translations_outer_source"
        ));
    }

    @Test
    @DisplayName("""
            add(userId, interval):
             there is not duplicate for interval
             => add interval
            """)
    public void add1() {
        User user = userRepository.save(user(1));
        commit(() -> intervalRepository.add(user.getId(), 10));

        ImmutableList<Integer> intervals = intervalRepository.findAll(user.getId());

        Assertions.assertThat(intervals).contains(10);
    }

    @Test
    @DisplayName("""
            add(userId, interval):
             there is duplicate for interval
             => exception
            """)
    public void add2() {
        User user = userRepository.save(user(1));
        commit(() -> intervalRepository.add(user.getId(), 10));

        Assertions.assertThatExceptionOfType(DuplicateKeyException.class).
                isThrownBy(() -> commit(() -> intervalRepository.add(user.getId(), 10)));
    }

    @Test
    @DisplayName("""
            replace(userId, oldInterval, newInterval):
             user with userId hasn't oldInterval
             => exception
            """)
    public void replace1() {
        User user = commit(() -> userRepository.save(user(1)));
        commit(() -> {
            intervalRepository.add(user.getId(), 1);
            intervalRepository.add(user.getId(), 3);
            intervalRepository.add(user.getId(), 5);
            intervalRepository.add(user.getId(), 10);
        });

        Assertions.assertThatExceptionOfType(InvalidParameter.class).
                isThrownBy(() -> intervalRepository.replace(user.getId(), 20, 30));
    }

    @Test
    @DisplayName("""
            replace(userId, oldInterval, newInterval):
             user with userId has oldInterval,
             user with userId hasn't newInterval
             => replace oldInterval with newInterval
            """)
    public void replace2() {
        User user = commit(() -> userRepository.save(user(1)));
        commit(() -> {
            intervalRepository.add(user.getId(), 1);
            intervalRepository.add(user.getId(), 3);
            intervalRepository.add(user.getId(), 5);
            intervalRepository.add(user.getId(), 10);
        });

        commit(() -> intervalRepository.replace(user.getId(), 10, 30));

        List<Integer> actual = intervalRepository.findAll(user.getId());
        Assertions.assertThat(actual).containsExactly(1, 3, 5, 30);
    }

    @Test
    @DisplayName("""
            replace(userId, oldInterval, newInterval):
             user with userId has oldInterval,
             user with userId hasn't newInterval,
             user has words to repeat from english with oldInterval
             => replace word oldInterval with newInterval to repeat from english
            """)
    public void replace3() {
        User user = commit(() -> userRepository.save(user(1)));
        commit(() -> {
            intervalRepository.add(user.getId(), 1);
            intervalRepository.add(user.getId(), 3);
            intervalRepository.add(user.getId(), 5);
            intervalRepository.add(user.getId(), 10);
        });
        commit(() -> {
            wordRepository.save(word(user.getId(), "valueA", "noteA", 1, 1));
            wordRepository.save(word(user.getId(), "valueB", "noteB", 5, 5));
            wordRepository.save(word(user.getId(), "valueC", "noteC", 10, 10));
        });

        commit(() -> intervalRepository.replace(user.getId(), 10, 30));

        List<Integer> actual = findAllWords().stream().
                map(word -> word.getRepeatDataFromEnglish().interval()).
                toList();
        Assertions.assertThat(actual).containsExactly(1, 5, 30);
    }

    @Test
    @DisplayName("""
            replace(userId, oldInterval, newInterval):
             user with userId has oldInterval,
             user with userId hasn't newInterval,
             user has words to repeat from native with oldInterval
             => replace word oldInterval with newInterval to repeat from native
            """)
    public void replace4() {
        User user = commit(() -> userRepository.save(user(1)));
        commit(() -> {
            intervalRepository.add(user.getId(), 1);
            intervalRepository.add(user.getId(), 3);
            intervalRepository.add(user.getId(), 5);
            intervalRepository.add(user.getId(), 10);
        });
        commit(() -> {
            wordRepository.save(word(user.getId(), "valueA", "noteA", 1, 1));
            wordRepository.save(word(user.getId(), "valueB", "noteB", 5, 5));
            wordRepository.save(word(user.getId(), "valueC", "noteC", 10, 10));
        });

        commit(() -> intervalRepository.replace(user.getId(), 10, 30));

        List<Integer> actual = findAllWords().stream().
                map(word -> word.getRepeatDataFromNative().interval()).
                toList();
        Assertions.assertThat(actual).containsExactly(1, 5, 30);
    }

    @Test
    @DisplayName("""
            replace(userId, oldInterval, newInterval):
             user with userId has oldInterval,
             user with userId hasn't newInterval,
             user has expressions to repeat from english with oldInterval
             => replace expressions oldInterval with newInterval to repeat from english
            """)
    public void replace5() {
        User user = commit(() -> userRepository.save(user(1)));
        commit(() -> {
            intervalRepository.add(user.getId(), 1);
            intervalRepository.add(user.getId(), 3);
            intervalRepository.add(user.getId(), 5);
            intervalRepository.add(user.getId(), 10);
        });
        commit(() -> {
            expressionRepository.save(expression(user.getId(), "valueA", "noteA", 1, 1));
            expressionRepository.save(expression(user.getId(), "valueB", "noteB", 5, 5));
            expressionRepository.save(expression(user.getId(), "valueC", "noteC", 10, 10));
        });

        commit(() -> intervalRepository.replace(user.getId(), 10, 30));

        List<Integer> actual = findAllExpressions().stream().
                map(expression -> expression.getRepeatDataFromEnglish().interval()).
                toList();
        Assertions.assertThat(actual).containsExactly(1, 5, 30);
    }

    @Test
    @DisplayName("""
            replace(userId, oldInterval, newInterval):
             user with userId has oldInterval,
             user with userId hasn't newInterval,
             user has expressions to repeat from native with oldInterval
             => replace expressions oldInterval with newInterval to repeat from native
            """)
    public void replace6() {
        User user = commit(() -> userRepository.save(user(1)));
        commit(() -> {
            intervalRepository.add(user.getId(), 1);
            intervalRepository.add(user.getId(), 3);
            intervalRepository.add(user.getId(), 5);
            intervalRepository.add(user.getId(), 10);
        });
        commit(() -> {
            expressionRepository.save(expression(user.getId(), "valueA", "noteA", 1, 1));
            expressionRepository.save(expression(user.getId(), "valueB", "noteB", 5, 5));
            expressionRepository.save(expression(user.getId(), "valueC", "noteC", 10, 10));
        });

        commit(() -> intervalRepository.replace(user.getId(), 10, 30));

        List<Integer> actual = findAllExpressions().stream().
                map(expression -> expression.getRepeatDataFromNative().interval()).
                toList();
        Assertions.assertThat(actual).containsExactly(1, 5, 30);
    }

    @Test
    @DisplayName("""
            replace(userId, oldInterval, newInterval):
             user with userId has oldInterval,
             user with userId has newInterval
             => replace oldInterval with newInterval
            """)
    public void replace7() {
        User user = commit(() -> userRepository.save(user(1)));
        commit(() -> {
            intervalRepository.add(user.getId(), 1);
            intervalRepository.add(user.getId(), 3);
            intervalRepository.add(user.getId(), 5);
            intervalRepository.add(user.getId(), 10);
        });

        commit(() -> intervalRepository.replace(user.getId(), 10, 5));

        List<Integer> actual = intervalRepository.findAll(user.getId());
        Assertions.assertThat(actual).containsExactly(1, 3, 5);
    }

    @Test
    @DisplayName("""
            replace(userId, oldInterval, newInterval):
             user with userId has oldInterval,
             user with userId has newInterval,
             user has words to repeat from english with oldInterval
             => replace word oldInterval with newInterval to repeat from english
            """)
    public void replace8() {
        User user = commit(() -> userRepository.save(user(1)));
        commit(() -> {
            intervalRepository.add(user.getId(), 1);
            intervalRepository.add(user.getId(), 3);
            intervalRepository.add(user.getId(), 5);
            intervalRepository.add(user.getId(), 10);
        });
        commit(() -> {
            wordRepository.save(word(user.getId(), "valueA", "noteA", 1, 1));
            wordRepository.save(word(user.getId(), "valueB", "noteB", 5, 5));
            wordRepository.save(word(user.getId(), "valueC", "noteC", 10, 10));
        });

        commit(() -> intervalRepository.replace(user.getId(), 10, 5));

        List<Integer> actual = findAllWords().stream().
                map(word -> word.getRepeatDataFromEnglish().interval()).
                toList();
        Assertions.assertThat(actual).containsExactly(1, 5, 5);
    }

    @Test
    @DisplayName("""
            replace(userId, oldInterval, newInterval):
             user with userId has oldInterval,
             user with userId has newInterval,
             user has words to repeat from native with oldInterval
             => replace word oldInterval with newInterval to repeat from native
            """)
    public void replace9() {
        User user = commit(() -> userRepository.save(user(1)));
        commit(() -> {
            intervalRepository.add(user.getId(), 1);
            intervalRepository.add(user.getId(), 3);
            intervalRepository.add(user.getId(), 5);
            intervalRepository.add(user.getId(), 10);
        });
        commit(() -> {
            wordRepository.save(word(user.getId(), "valueA", "noteA", 1, 1));
            wordRepository.save(word(user.getId(), "valueB", "noteB", 5, 5));
            wordRepository.save(word(user.getId(), "valueC", "noteC", 10, 10));
        });

        commit(() -> intervalRepository.replace(user.getId(), 10, 5));

        List<Integer> actual = findAllWords().stream().
                map(word -> word.getRepeatDataFromNative().interval()).
                toList();
        Assertions.assertThat(actual).containsExactly(1, 5, 5);
    }

    @Test
    @DisplayName("""
            replace(userId, oldInterval, newInterval):
             user with userId has oldInterval,
             user with userId has newInterval,
             user has expressions to repeat from english with oldInterval
             => replace expressions oldInterval with newInterval to repeat from english
            """)
    public void replace10() {
        User user = commit(() -> userRepository.save(user(1)));
        commit(() -> {
            intervalRepository.add(user.getId(), 1);
            intervalRepository.add(user.getId(), 3);
            intervalRepository.add(user.getId(), 5);
            intervalRepository.add(user.getId(), 10);
        });
        commit(() -> {
            expressionRepository.save(expression(user.getId(), "valueA", "noteA", 1, 1));
            expressionRepository.save(expression(user.getId(), "valueB", "noteB", 5, 5));
            expressionRepository.save(expression(user.getId(), "valueC", "noteC", 10, 10));
        });

        commit(() -> intervalRepository.replace(user.getId(), 10, 5));

        List<Integer> actual = findAllExpressions().stream().
                map(expression -> expression.getRepeatDataFromEnglish().interval()).
                toList();
        Assertions.assertThat(actual).containsExactly(1, 5, 5);
    }

    @Test
    @DisplayName("""
            replace(userId, oldInterval, newInterval):
             user with userId has oldInterval,
             user with userId has newInterval,
             user has expressions to repeat from native with oldInterval
             => replace expressions oldInterval with newInterval to repeat from native
            """)
    public void replace11() {
        User user = commit(() -> userRepository.save(user(1)));
        commit(() -> {
            intervalRepository.add(user.getId(), 1);
            intervalRepository.add(user.getId(), 3);
            intervalRepository.add(user.getId(), 5);
            intervalRepository.add(user.getId(), 10);
        });
        commit(() -> {
            expressionRepository.save(expression(user.getId(), "valueA", "noteA", 1, 1));
            expressionRepository.save(expression(user.getId(), "valueB", "noteB", 5, 5));
            expressionRepository.save(expression(user.getId(), "valueC", "noteC", 10, 10));
        });

        commit(() -> intervalRepository.replace(user.getId(), 10, 5));

        List<Integer> actual = findAllExpressions().stream().
                map(expression -> expression.getRepeatDataFromNative().interval()).
                toList();
        Assertions.assertThat(actual).containsExactly(1, 5, 5);
    }

    @Test
    @DisplayName("""
            replace(userId, oldInterval, newInterval):
             user with userId has oldInterval,
             oldInterval = newInterval
             => don't change any intervals
            """)
    public void replace12() {
        User user = commit(() -> userRepository.save(user(1)));
        commit(() -> {
            intervalRepository.add(user.getId(), 1);
            intervalRepository.add(user.getId(), 3);
            intervalRepository.add(user.getId(), 5);
            intervalRepository.add(user.getId(), 10);
        });

        commit(() -> intervalRepository.replace(user.getId(), 5, 5));

        List<Integer> actual = intervalRepository.findAll(user.getId());
        Assertions.assertThat(actual).containsExactly(1, 3, 5, 10);
    }

    @Test
    @DisplayName("""
            replace(userId, oldInterval, newInterval):
             user with userId has oldInterval,
             oldInterval = newInterval,
             user has words to repeat from english with oldInterval
             => don't change any words intervals to repeat from english
            """)
    public void replace13() {
        User user = commit(() -> userRepository.save(user(1)));
        commit(() -> {
            intervalRepository.add(user.getId(), 1);
            intervalRepository.add(user.getId(), 3);
            intervalRepository.add(user.getId(), 5);
            intervalRepository.add(user.getId(), 10);
        });
        commit(() -> {
            wordRepository.save(word(user.getId(), "valueA", "noteA", 1, 1));
            wordRepository.save(word(user.getId(), "valueB", "noteB", 5, 5));
            wordRepository.save(word(user.getId(), "valueC", "noteC", 10, 10));
        });

        commit(() -> intervalRepository.replace(user.getId(), 5, 5));

        List<Integer> actual = findAllWords().stream().
                map(word -> word.getRepeatDataFromEnglish().interval()).
                toList();
        Assertions.assertThat(actual).containsExactly(1, 5, 10);
    }

    @Test
    @DisplayName("""
            replace(userId, oldInterval, newInterval):
             user with userId has oldInterval,
             oldInterval = newInterval,
             user has words to repeat from native with oldInterval
             => don't change any words intervals to repeat from native
            """)
    public void replace14() {
        User user = commit(() -> userRepository.save(user(1)));
        commit(() -> {
            intervalRepository.add(user.getId(), 1);
            intervalRepository.add(user.getId(), 3);
            intervalRepository.add(user.getId(), 5);
            intervalRepository.add(user.getId(), 10);
        });
        commit(() -> {
            wordRepository.save(word(user.getId(), "valueA", "noteA", 1, 1));
            wordRepository.save(word(user.getId(), "valueB", "noteB", 5, 5));
            wordRepository.save(word(user.getId(), "valueC", "noteC", 10, 10));
        });

        commit(() -> intervalRepository.replace(user.getId(), 5, 5));

        List<Integer> actual = findAllWords().stream().
                map(word -> word.getRepeatDataFromNative().interval()).
                toList();
        Assertions.assertThat(actual).containsExactly(1, 5, 10);
    }

    @Test
    @DisplayName("""
            replace(userId, oldInterval, newInterval):
             user with userId has oldInterval,
             oldInterval = newInterval,
             user has expressions to repeat from english with oldInterval
             => don't change any expressions intervals to repeat from english
            """)
    public void replace15() {
        User user = commit(() -> userRepository.save(user(1)));
        commit(() -> {
            intervalRepository.add(user.getId(), 1);
            intervalRepository.add(user.getId(), 3);
            intervalRepository.add(user.getId(), 5);
            intervalRepository.add(user.getId(), 10);
        });
        commit(() -> {
            expressionRepository.save(expression(user.getId(), "valueA", "noteA", 1, 1));
            expressionRepository.save(expression(user.getId(), "valueB", "noteB", 5, 5));
            expressionRepository.save(expression(user.getId(), "valueC", "noteC", 10, 10));
        });

        commit(() -> intervalRepository.replace(user.getId(), 5, 5));

        List<Integer> actual = findAllExpressions().stream().
                map(expression -> expression.getRepeatDataFromEnglish().interval()).
                toList();
        Assertions.assertThat(actual).containsExactly(1, 5, 10);
    }

    @Test
    @DisplayName("""
            replace(userId, oldInterval, newInterval):
             user with userId has oldInterval,
             oldInterval = newInterval,
             user has expressions to repeat from native with oldInterval
             => don't change any expressions intervals to repeat from native
            """)
    public void replace16() {
        User user = commit(() -> userRepository.save(user(1)));
        commit(() -> {
            intervalRepository.add(user.getId(), 1);
            intervalRepository.add(user.getId(), 3);
            intervalRepository.add(user.getId(), 5);
            intervalRepository.add(user.getId(), 10);
        });
        commit(() -> {
            expressionRepository.save(expression(user.getId(), "valueA", "noteA", 1, 1));
            expressionRepository.save(expression(user.getId(), "valueB", "noteB", 5, 5));
            expressionRepository.save(expression(user.getId(), "valueC", "noteC", 10, 10));
        });

        commit(() -> intervalRepository.replace(user.getId(), 5, 5));

        List<Integer> actual = findAllExpressions().stream().
                map(expression -> expression.getRepeatDataFromNative().interval()).
                toList();
        Assertions.assertThat(actual).containsExactly(1, 5, 10);
    }

    @Test
    @DisplayName("""
            replace(userId, oldInterval, newInterval):
             user with userId has oldInterval,
             user with userId has single interval
             => change interval
            """)
    public void replace17() {
        User user = commit(() -> userRepository.save(user(1)));
        commit(() -> intervalRepository.add(user.getId(), 1));

        commit(() -> intervalRepository.replace(user.getId(), 1, 10));

        List<Integer> actual = intervalRepository.findAll(user.getId());
        Assertions.assertThat(actual).containsExactly(10);
    }

    @Test
    @DisplayName("""
            replace(userId, oldInterval, newInterval):
             user with userId has oldInterval,
             user with userId has single interval
             user has words with oldInterval
             => change all words intervals to repeat from english
            """)
    public void replace18() {
        User user = commit(() -> userRepository.save(user(1)));
        commit(() -> intervalRepository.add(user.getId(), 1));
        commit(() -> {
            wordRepository.save(word(user.getId(), "valueA", "noteA", 1, 1));
            wordRepository.save(word(user.getId(), "valueB", "noteB", 1, 1));
            wordRepository.save(word(user.getId(), "valueC", "noteC", 1, 1));
        });

        commit(() -> intervalRepository.replace(user.getId(), 1, 10));

        List<Integer> actual = findAllWords().stream().
                map(word -> word.getRepeatDataFromEnglish().interval()).
                toList();
        Assertions.assertThat(actual).containsExactly(10, 10, 10);
    }

    @Test
    @DisplayName("""
            replace(userId, oldInterval, newInterval):
             user with userId has oldInterval,
             user with userId has single interval
             user has words with oldInterval
             => change all words intervals to repeat from native
            """)
    public void replace19() {
        User user = commit(() -> userRepository.save(user(1)));
        commit(() -> intervalRepository.add(user.getId(), 1));
        commit(() -> {
            wordRepository.save(word(user.getId(), "valueA", "noteA", 1, 1));
            wordRepository.save(word(user.getId(), "valueB", "noteB", 1, 1));
            wordRepository.save(word(user.getId(), "valueC", "noteC", 1, 1));
        });

        commit(() -> intervalRepository.replace(user.getId(), 1, 10));

        List<Integer> actual = findAllWords().stream().
                map(word -> word.getRepeatDataFromNative().interval()).
                toList();
        Assertions.assertThat(actual).containsExactly(10, 10, 10);
    }

    @Test
    @DisplayName("""
            replace(userId, oldInterval, newInterval):
             user with userId has oldInterval,
             user with userId has single interval
             user has expressions with oldInterval
             => change all expressions intervals to repeat from english
            """)
    public void replace20() {
        User user = commit(() -> userRepository.save(user(1)));
        commit(() -> intervalRepository.add(user.getId(), 1));
        commit(() -> {
            expressionRepository.save(expression(user.getId(), "valueA", "noteA", 1, 1));
            expressionRepository.save(expression(user.getId(), "valueB", "noteB", 1, 1));
            expressionRepository.save(expression(user.getId(), "valueC", "noteC", 1, 1));
        });

        commit(() -> intervalRepository.replace(user.getId(), 1, 10));

        List<Integer> actual = findAllExpressions().stream().
                map(expression -> expression.getRepeatDataFromEnglish().interval()).
                toList();
        Assertions.assertThat(actual).containsExactly(10, 10, 10);
    }

    @Test
    @DisplayName("""
            replace(userId, oldInterval, newInterval):
             user with userId has oldInterval,
             user with userId has single interval
             user has expressions with oldInterval
             => change all expressions intervals to repeat from native
            """)
    public void replace21() {
        User user = commit(() -> userRepository.save(user(1)));
        commit(() -> intervalRepository.add(user.getId(), 1));
        commit(() -> {
            expressionRepository.save(expression(user.getId(), "valueA", "noteA", 1, 1));
            expressionRepository.save(expression(user.getId(), "valueB", "noteB", 1, 1));
            expressionRepository.save(expression(user.getId(), "valueC", "noteC", 1, 1));
        });

        commit(() -> intervalRepository.replace(user.getId(), 1, 10));

        List<Integer> actual = findAllExpressions().stream().
                map(expression -> expression.getRepeatDataFromNative().interval()).
                toList();
        Assertions.assertThat(actual).containsExactly(10, 10, 10);
    }

    @Test
    @DisplayName("""
            replace(userId, oldInterval, newInterval):
             user with userId has oldInterval,
             user hasn't words with oldInterval
             => don't change any words
            """)
    public void replace22() {
        User user = commit(() -> userRepository.save(user(1)));
        commit(() -> {
            intervalRepository.add(user.getId(), 1);
            intervalRepository.add(user.getId(), 3);
            intervalRepository.add(user.getId(), 5);
            intervalRepository.add(user.getId(), 10);
        });
        List<Word> words = List.of(
                word(user.getId(), "valueA", "noteA", 1, 1),
                word(user.getId(), "valueB", "noteB", 5, 5),
                word(user.getId(), "valueC", "noteC", 10, 10)
        );
        commit(() -> words.forEach(word -> wordRepository.save(word)));

        commit(() -> intervalRepository.replace(user.getId(), 3, 30));

        List<Word> actual = findAllWords();
        Assertions.assertThat(actual).
                usingRecursiveFieldByFieldElementComparator().
                containsExactlyElementsOf(words);
    }

    @Test
    @DisplayName("""
            replace(userId, oldInterval, newInterval):
             user with userId has oldInterval,
             user hasn't expressions with oldInterval
             => don't change any expressions
            """)
    public void replace23() {
        User user = commit(() -> userRepository.save(user(1)));
        commit(() -> {
            intervalRepository.add(user.getId(), 1);
            intervalRepository.add(user.getId(), 3);
            intervalRepository.add(user.getId(), 5);
            intervalRepository.add(user.getId(), 10);
        });
        List<Expression> expressions = List.of(
                expression(user.getId(), "valueA", "noteA", 1, 1),
                expression(user.getId(), "valueB", "noteB", 5, 5),
                expression(user.getId(), "valueC", "noteC", 10, 10)
        );
        commit(() -> expressions.forEach(expression -> expressionRepository.save(expression)));

        commit(() -> intervalRepository.replace(user.getId(), 3, 30));

        List<Expression> actual = findAllExpressions();
        Assertions.assertThat(actual).
                usingRecursiveFieldByFieldElementComparator().
                containsExactlyInAnyOrderElementsOf(expressions);
    }


    private User user(int number) {
        return new User(new Credential("me" + number + "@mail.com", "password" + number)).
                setOrGenerateSalt("salt" + number).
                addRole("role1").
                addRole("role2").
                addRole("role3");
    }

    private Word word(UUID userId,
                      String value,
                      String note,
                      int intervalForEnglish,
                      int intervalForNative) {
        return new Word(userId, intervalForEnglish, intervalForNative, clock).
                setValue(value).
                setNote(note);
    }

    private Expression expression(UUID userId,
                                  String value,
                                  String note,
                                  int intervalForEnglish,
                                  int intervalForNative) {
        return new Expression(userId, intervalForEnglish, intervalForNative, clock).
                setValue(value).
                setNote(note);
    }

    private List<Word> findAllWords() {
        return wordRepository.findAll(PageRequest.of(0, 100, Sort.by("value"))).getContent();
    }

    private List<Expression> findAllExpressions() {
        return expressionRepository.findAll(PageRequest.of(0, 100, Sort.by("value"))).getContent();
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