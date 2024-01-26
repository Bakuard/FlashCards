package com.bakuard.flashcards.dal;

import com.bakuard.flashcards.config.MutableClock;
import com.bakuard.flashcards.config.SpringConfig;
import com.bakuard.flashcards.config.TestConfig;
import com.bakuard.flashcards.config.configData.ConfigData;
import com.bakuard.flashcards.model.auth.credential.User;
import com.bakuard.flashcards.model.expression.Expression;
import com.bakuard.flashcards.model.statistic.ExpressionRepetitionByPeriodStatistic;
import com.bakuard.flashcards.model.statistic.RepeatExpressionFromEnglishStatistic;
import com.bakuard.flashcards.model.statistic.RepeatExpressionFromNativeStatistic;
import com.bakuard.flashcards.model.statistic.RepeatWordFromEnglishStatistic;
import com.bakuard.flashcards.model.statistic.RepeatWordFromNativeStatistic;
import com.bakuard.flashcards.model.statistic.WordRepetitionByPeriodStatistic;
import com.bakuard.flashcards.model.word.Word;
import com.bakuard.flashcards.validation.ValidatorUtil;
import com.bakuard.flashcards.validation.exception.InvalidParameter;
import com.bakuard.flashcards.validation.exception.NotUniqueEntityException;
import com.bakuard.flashcards.validation.exception.UnknownEntityException;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.function.Executable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.jdbc.JdbcTestUtils;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import java.time.Clock;
import java.time.Duration;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Supplier;

@ExtendWith(SpringExtension.class)
@TestPropertySource(locations = "classpath:test.properties")
@Import({SpringConfig.class, TestConfig.class})
class StatisticRepositoryTest {

    @Autowired
    private StatisticRepository statisticRepository;
    @Autowired
    private UserRepository userRepository;
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
    private MutableClock clock;
    @Autowired
    private ConfigData config;

    @BeforeEach
    public void beforeEach() {
        commit(() -> JdbcTestUtils.deleteFromTables(jdbcTemplate,
                "expressions",
                "words",
                "intervals",
                "users",
                "word_outer_source",
                "words_examples_outer_source"
        ));
        clock.setDate(2022, 7, 7);
    }

    @Test
    @DisplayName("""
            append(RepeatWordFromEnglishStatistic statistic):
             statistic is null
             => exception
            """)
    public void appendWordFromEnglish1() {
        RepeatWordFromEnglishStatistic statistic = null;

        Assertions.assertThatNullPointerException().
                isThrownBy(() -> commit(() -> statisticRepository.append(statistic)));
    }

    @Test
    @DisplayName("""
            append(RepeatWordFromEnglishStatistic statistic):
             user with id = statistic.userId() not exists
             => exception
            """)
    public void appendWordFromEnglish2() {
        User user = commit(() -> userRepository.save(user(1)));
        Word word = commit(() -> wordRepository.save(word(user.getId(), "valueA", "noteA", 1)));
        RepeatWordFromEnglishStatistic statistic = wordFromEnglish(toUUID(2), word.getId(), 1, true);

        Assertions.assertThatExceptionOfType(UnknownEntityException.class).
                isThrownBy(() -> commit(() -> statisticRepository.append(statistic)));
    }

    @Test
    @DisplayName("""
            append(RepeatWordFromEnglishStatistic statistic):
             word with id = statistic.wordId() not exists
             => exception
            """)
    public void appendWordFromEnglish3() {
        User user = commit(() -> userRepository.save(user(1)));
        RepeatWordFromEnglishStatistic statistic = wordFromEnglish(user.getId(), toUUID(2), 1, true);

        Assertions.assertThatExceptionOfType(UnknownEntityException.class).
                isThrownBy(() -> commit(() -> statisticRepository.append(statistic)));
    }

    @Test
    @DisplayName("""
            append(RepeatWordFromEnglishStatistic statistic):
             this statistic already exists in DB
             => exception
            """)
    public void appendWordFromEnglish4() {
        User user = commit(() -> userRepository.save(user(1)));
        Word word = commit(() -> wordRepository.save(word(user.getId(), "valueA", "noteA", 1)));
        RepeatWordFromEnglishStatistic statistic = wordFromEnglish(user.getId(), word.getId(), 1, true);
        commit(() -> statisticRepository.append(statistic));

        Assertions.assertThatExceptionOfType(NotUniqueEntityException.class).
                isThrownBy(() -> commit(() -> statisticRepository.append(statistic)));
    }

    @Test
    @DisplayName("""
            append(RepeatWordFromNativeStatistic statistic):
             statistic is null
             => exception
            """)
    public void appendWordFromNative1() {
        RepeatWordFromNativeStatistic statistic = null;

        Assertions.assertThatNullPointerException().
                isThrownBy(() -> commit(() -> statisticRepository.append(statistic)));
    }

    @Test
    @DisplayName("""
            append(RepeatWordFromNativeStatistic statistic):
             user with id = statistic.userId() not exists
             => exception
            """)
    public void appendWordFromNative2() {
        User user = commit(() -> userRepository.save(user(1)));
        Word word = commit(() -> wordRepository.save(word(user.getId(), "valueA", "noteA", 1)));
        RepeatWordFromNativeStatistic statistic = wordFromNative(toUUID(2), word.getId(), 1, true);

        Assertions.assertThatExceptionOfType(UnknownEntityException.class).
                isThrownBy(() -> commit(() -> statisticRepository.append(statistic)));
    }

    @Test
    @DisplayName("""
            append(RepeatWordFromNativeStatistic statistic):
             word with id = statistic.wordId() not exists
             => exception
            """)
    public void appendWordFromNative3() {
        User user = commit(() -> userRepository.save(user(1)));
        RepeatWordFromNativeStatistic statistic = wordFromNative(user.getId(), toUUID(2), 1, true);

        Assertions.assertThatExceptionOfType(UnknownEntityException.class).
                isThrownBy(() -> commit(() -> statisticRepository.append(statistic)));
    }

    @Test
    @DisplayName("""
            append(RepeatWordFromNativeStatistic statistic):
             this statistic already exists in DB
             => exception
            """)
    public void appendWordFromNative4() {
        User user = commit(() -> userRepository.save(user(1)));
        Word word = commit(() -> wordRepository.save(word(user.getId(), "valueA", "noteA", 1)));
        RepeatWordFromNativeStatistic statistic = wordFromNative(user.getId(), word.getId(), 1, true);
        commit(() -> statisticRepository.append(statistic));

        Assertions.assertThatExceptionOfType(NotUniqueEntityException.class).
                isThrownBy(() -> commit(() -> statisticRepository.append(statistic)));
    }

    @Test
    @DisplayName("""
            append(RepeatExpressionFromEnglishStatistic statistic):
             statistic is null
             => exception
            """)
    public void appendExpressionFromEnglish1() {
        RepeatExpressionFromEnglishStatistic statistic = null;

        Assertions.assertThatNullPointerException().
                isThrownBy(() -> commit(() -> statisticRepository.append(statistic)));
    }

    @Test
    @DisplayName("""
            append(RepeatExpressionFromEnglishStatistic statistic):
             user with id = statistic.userId() not exists
             => exception
            """)
    public void appendExpressionFromEnglish2() {
        User user = commit(() -> userRepository.save(user(1)));
        Expression expression = commit(() -> expressionRepository.save(expression(user.getId(), "valueA", "noteA", 1)));
        RepeatExpressionFromEnglishStatistic statistic = expressionFromEnglish(toUUID(2), expression.getId(), 1, true);

        Assertions.assertThatExceptionOfType(UnknownEntityException.class).
                isThrownBy(() -> commit(() -> statisticRepository.append(statistic)));
    }

    @Test
    @DisplayName("""
            append(RepeatExpressionFromEnglishStatistic statistic):
             expression with id = statistic.expressionId() not exists
             => exception
            """)
    public void appendExpressionFromEnglish3() {
        User user = commit(() -> userRepository.save(user(1)));
        RepeatExpressionFromEnglishStatistic statistic = expressionFromEnglish(user.getId(), toUUID(2), 1, true);

        Assertions.assertThatExceptionOfType(UnknownEntityException.class).
                isThrownBy(() -> commit(() -> statisticRepository.append(statistic)));
    }

    @Test
    @DisplayName("""
            append(RepeatExpressionFromEnglishStatistic statistic):
             this statistic already exists in DB
             => exception
            """)
    public void appendExpressionFromEnglish4() {
        User user = commit(() -> userRepository.save(user(1)));
        Expression expression = commit(() -> expressionRepository.save(expression(user.getId(), "valueA", "noteA", 1)));
        RepeatExpressionFromEnglishStatistic statistic = expressionFromEnglish(user.getId(), expression.getId(), 1, true);
        commit(() -> statisticRepository.append(statistic));

        Assertions.assertThatExceptionOfType(NotUniqueEntityException.class).
                isThrownBy(() -> commit(() -> statisticRepository.append(statistic)));
    }

    @Test
    @DisplayName("""
            append(RepeatExpressionFromNativeStatistic statistic):
             statistic is null
             => exception
            """)
    public void appendExpressionFromNative1() {
        RepeatExpressionFromNativeStatistic statistic = null;

        Assertions.assertThatNullPointerException().
                isThrownBy(() -> commit(() -> statisticRepository.append(statistic)));
    }

    @Test
    @DisplayName("""
            append(RepeatExpressionFromNativeStatistic statistic):
             user with id = statistic.userId() not exists
             => exception
            """)
    public void appendExpressionFromNative2() {
        User user = commit(() -> userRepository.save(user(1)));
        Expression expression = commit(() -> expressionRepository.save(expression(user.getId(), "valueA", "noteA", 1)));
        RepeatExpressionFromNativeStatistic statistic = expressionFromNative(toUUID(2), expression.getId(), 1, true);

        Assertions.assertThatExceptionOfType(UnknownEntityException.class).
                isThrownBy(() -> commit(() -> statisticRepository.append(statistic)));
    }

    @Test
    @DisplayName("""
            append(RepeatExpressionFromNativeStatistic statistic):
             expression with id = statistic.expressionId() not exists
             => exception
            """)
    public void appendExpressionFromNative3() {
        User user = commit(() -> userRepository.save(user(1)));
        RepeatExpressionFromNativeStatistic statistic = expressionFromNative(user.getId(), toUUID(2), 1, true);

        Assertions.assertThatExceptionOfType(UnknownEntityException.class).
                isThrownBy(() -> commit(() -> statisticRepository.append(statistic)));
    }

    @Test
    @DisplayName("""
            append(RepeatExpressionFromNativeStatistic statistic):
             this statistic already exists in DB
             => exception
            """)
    public void appendExpressionFromNative4() {
        User user = commit(() -> userRepository.save(user(1)));
        Expression expression = commit(() -> expressionRepository.save(expression(user.getId(), "valueA", "noteA", 1)));
        RepeatExpressionFromNativeStatistic statistic = expressionFromNative(user.getId(), expression.getId(), 1, true);
        commit(() -> statisticRepository.append(statistic));

        Assertions.assertThatExceptionOfType(NotUniqueEntityException.class).
                isThrownBy(() -> commit(() -> statisticRepository.append(statistic)));
    }

    @Test
    @DisplayName("""
            wordRepetitionByPeriod(userId, wordId, start, end):
             start > end
             => exception
            """)
    public void wordRepetitionByPeriod1() {
        User user = commit(() -> userRepository.save(user(1)));
        Word word = commit(() -> wordRepository.save(word(user.getId(), "valueA", "noteA", 1)));
        commit(() -> {
            statisticRepository.append(
                    wordFromEnglish(user.getId(), word.getId(), 0, true));
            statisticRepository.append(
                    wordFromEnglish(user.getId(), word.getId(), 3, true));
            statisticRepository.append(
                    wordFromEnglish(user.getId(), word.getId(), 5, true));
            statisticRepository.append(
                    wordFromEnglish(user.getId(), word.getId(), 10, true));
            statisticRepository.append(
                    wordFromEnglish(user.getId(), word.getId(), 11, false));
            statisticRepository.append(
                    wordFromNative(user.getId(), word.getId(), 0, true));
            statisticRepository.append(
                    wordFromNative(user.getId(), word.getId(), 3, true));
            statisticRepository.append(
                    wordFromNative(user.getId(), word.getId(), 4, false));
            statisticRepository.append(
                    wordFromNative(user.getId(), word.getId(), 5, false));
            statisticRepository.append(
                    wordFromNative(user.getId(), word.getId(), 6, false));
        });

        Assertions.assertThatExceptionOfType(InvalidParameter.class).
                isThrownBy(() -> statisticRepository.wordRepetitionByPeriod(
                        user.getId(), word.getId(), periodStart(10), periodEnd(0)));
    }

    @Test
    @DisplayName("""
            wordRepetitionByPeriod(userId, wordId, start, end):
             user didn't repeat this word from english and native
             => return result with zero value for all field
            """)
    public void wordRepetitionByPeriod2() {
        User user = commit(() -> userRepository.save(user(1)));
        Word word = commit(() -> wordRepository.save(word(user.getId(), "valueA", "noteA", 1)));

        Optional<WordRepetitionByPeriodStatistic> actual = statisticRepository.wordRepetitionByPeriod(
                user.getId(), word.getId(), periodStart(0), periodEnd(1000)
        );

        Assertions.assertThat(actual).
                isPresent().
                get().
                usingRecursiveComparison().
                isEqualTo(new WordRepetitionByPeriodStatistic(
                        user.getId(),
                        word.getId(),
                        "valueA",
                        0,
                        0,
                        0,
                        0
                ));
    }

    @Test
    @DisplayName("""
            wordRepetitionByPeriod(userId, wordId, start, end):
             user didn't repeat this word from english,
             user repeated this word from native
             => return result with zero value for repeat from english
            """)
    public void wordRepetitionByPeriod3() {
        User user = commit(() -> userRepository.save(user(1)));
        Word word = commit(() -> wordRepository.save(word(user.getId(), "valueA", "noteA", 1)));
        commit(() -> {
            statisticRepository.append(
                    wordFromNative(user.getId(), word.getId(), 0, true));
            statisticRepository.append(
                    wordFromNative(user.getId(), word.getId(), 3, true));
            statisticRepository.append(
                    wordFromNative(user.getId(), word.getId(), 4, false));
            statisticRepository.append(
                    wordFromNative(user.getId(), word.getId(), 5, false));
            statisticRepository.append(
                    wordFromNative(user.getId(), word.getId(), 6, false));
        });

        Optional<WordRepetitionByPeriodStatistic> actual = statisticRepository.wordRepetitionByPeriod(
                user.getId(), word.getId(), periodStart(0), periodEnd(5)
        );

        Assertions.assertThat(actual).
                isPresent().
                get().
                usingRecursiveComparison().
                isEqualTo(new WordRepetitionByPeriodStatistic(
                        user.getId(),
                        word.getId(),
                        "valueA",
                        0,
                        0,
                        2,
                        2
                ));
    }

    @Test
    @DisplayName("""
            wordRepetitionByPeriod(userId, wordId, start, end):
             user didn't repeat this word from native,
             user repeated this word from english
             => return result with zero value for repeat from native
            """)
    public void wordRepetitionByPeriod4() {
        User user = commit(() -> userRepository.save(user(1)));
        Word word = commit(() -> wordRepository.save(word(user.getId(), "valueA", "noteA", 1)));
        commit(() -> {
            statisticRepository.append(
                    wordFromEnglish(user.getId(), word.getId(), 0, true));
            statisticRepository.append(
                    wordFromEnglish(user.getId(), word.getId(), 3, true));
            statisticRepository.append(
                    wordFromEnglish(user.getId(), word.getId(), 5, true));
            statisticRepository.append(
                    wordFromEnglish(user.getId(), word.getId(), 10, true));
            statisticRepository.append(
                    wordFromEnglish(user.getId(), word.getId(), 11, false));
        });

        Optional<WordRepetitionByPeriodStatistic> actual = statisticRepository.wordRepetitionByPeriod(
                user.getId(), word.getId(), periodStart(3), periodEnd(11)
        );

        Assertions.assertThat(actual).
                isPresent().
                get().
                usingRecursiveComparison().
                isEqualTo(new WordRepetitionByPeriodStatistic(
                        user.getId(),
                        word.getId(),
                        "valueA",
                        3,
                        1,
                        0,
                        0
                ));
    }

    @Test
    @DisplayName("""
            wordRepetitionByPeriod(userId, wordId, start, end):
             user repeated this word from native,
             user repeated this word from english
             => return number repetition from english and native
            """)
    public void wordRepetitionByPeriod5() {
        User user = commit(() -> userRepository.save(user(1)));
        Word word = commit(() -> wordRepository.save(word(user.getId(), "valueA", "noteA", 1)));
        commit(() -> {
            statisticRepository.append(
                    wordFromEnglish(user.getId(), word.getId(), 0, true));
            statisticRepository.append(
                    wordFromEnglish(user.getId(), word.getId(), 3, true));
            statisticRepository.append(
                    wordFromEnglish(user.getId(), word.getId(), 5, true));
            statisticRepository.append(
                    wordFromEnglish(user.getId(), word.getId(), 10, true));
            statisticRepository.append(
                    wordFromEnglish(user.getId(), word.getId(), 11, false));
            statisticRepository.append(
                    wordFromNative(user.getId(), word.getId(), 0, true));
            statisticRepository.append(
                    wordFromNative(user.getId(), word.getId(), 3, true));
            statisticRepository.append(
                    wordFromNative(user.getId(), word.getId(), 4, false));
            statisticRepository.append(
                    wordFromNative(user.getId(), word.getId(), 5, false));
            statisticRepository.append(
                    wordFromNative(user.getId(), word.getId(), 6, false));
        });

        Optional<WordRepetitionByPeriodStatistic> actual = statisticRepository.wordRepetitionByPeriod(
                user.getId(), word.getId(), periodStart(0), periodEnd(5)
        );

        Assertions.assertThat(actual).
                isPresent().
                get().
                usingRecursiveComparison().
                isEqualTo(new WordRepetitionByPeriodStatistic(
                        user.getId(),
                        word.getId(),
                        "valueA",
                        3,
                        0,
                        2,
                        2
                ));
    }

    @Test
    @DisplayName("""
            wordRepetitionByPeriod(userId, wordId, start, end):
             user repeated this word from native,
             user repeated this word from english,
             there are not data for repetition period
             => return result with zero value for all field
            """)
    public void wordRepetitionByPeriod6() {
        User user = commit(() -> userRepository.save(user(1)));
        Word word = commit(() -> wordRepository.save(word(user.getId(), "valueA", "noteA", 1)));
        commit(() -> {
            statisticRepository.append(
                    wordFromEnglish(user.getId(), word.getId(), 0, true));
            statisticRepository.append(
                    wordFromEnglish(user.getId(), word.getId(), 3, true));
            statisticRepository.append(
                    wordFromEnglish(user.getId(), word.getId(), 5, true));
            statisticRepository.append(
                    wordFromEnglish(user.getId(), word.getId(), 10, true));
            statisticRepository.append(
                    wordFromEnglish(user.getId(), word.getId(), 11, false));
            statisticRepository.append(
                    wordFromNative(user.getId(), word.getId(), 0, true));
            statisticRepository.append(
                    wordFromNative(user.getId(), word.getId(), 3, true));
            statisticRepository.append(
                    wordFromNative(user.getId(), word.getId(), 4, false));
            statisticRepository.append(
                    wordFromNative(user.getId(), word.getId(), 5, false));
            statisticRepository.append(
                    wordFromNative(user.getId(), word.getId(), 6, false));
        });

        Optional<WordRepetitionByPeriodStatistic> actual = statisticRepository.wordRepetitionByPeriod(
                user.getId(), word.getId(), periodStart(100), periodEnd(500)
        );

        Assertions.assertThat(actual).
                isPresent().
                get().
                usingRecursiveComparison().
                isEqualTo(new WordRepetitionByPeriodStatistic(
                        user.getId(),
                        word.getId(),
                        "valueA",
                        0,
                        0,
                        0,
                        0
                ));
    }

    @Test
    @DisplayName("""
            wordRepetitionByPeriod(userId, wordId, start, end):
             userId is null
             => exception
            """)
    public void wordRepetitionByPeriod7() {
        User user = commit(() -> userRepository.save(user(1)));
        Word word = commit(() -> wordRepository.save(word(user.getId(), "valueA", "noteA", 1)));

        Assertions.assertThatNullPointerException().
                isThrownBy(() ->
                        commit(() -> statisticRepository.wordRepetitionByPeriod(
                                null, word.getId(), periodStart(1), periodEnd(5))
                        )
                );
    }

    @Test
    @DisplayName("""
            wordRepetitionByPeriod(userId, wordId, start, end):
             wordId is null
             => exception
            """)
    public void wordRepetitionByPeriod8() {
        User user = commit(() -> userRepository.save(user(1)));

        Assertions.assertThatNullPointerException().
                isThrownBy(() ->
                        commit(() -> statisticRepository.wordRepetitionByPeriod(
                                user.getId(), null, periodStart(1), periodEnd(5))
                        )
                );
    }

    @Test
    @DisplayName("""
            wordRepetitionByPeriod(userId, wordId, start, end):
             start is null
             => exception
            """)
    public void wordRepetitionByPeriod9() {
        User user = commit(() -> userRepository.save(user(1)));
        Word word = commit(() -> wordRepository.save(word(user.getId(), "valueA", "noteA", 1)));

        Assertions.assertThatNullPointerException().
                isThrownBy(() ->
                        commit(() -> statisticRepository.wordRepetitionByPeriod(
                                user.getId(), word.getId(), null, periodEnd(5))
                        )
                );
    }

    @Test
    @DisplayName("""
            wordRepetitionByPeriod(userId, wordId, start, end):
             end is null
             => exception
            """)
    public void wordRepetitionByPeriod10() {
        User user = commit(() -> userRepository.save(user(1)));
        Word word = commit(() -> wordRepository.save(word(user.getId(), "valueA", "noteA", 1)));

        Assertions.assertThatNullPointerException().
                isThrownBy(() ->
                        commit(() -> statisticRepository.wordRepetitionByPeriod(
                                user.getId(), word.getId(), periodStart(1), null)
                        )
                );
    }

    @Test
    @DisplayName("""
            wordRepetitionByPeriod(userId, wordId, start, end):
             user with userId not exists
             => return empty Optional
            """)
    public void wordRepetitionByPeriod11() {
        User user = commit(() -> userRepository.save(user(1)));
        Word word = commit(() -> wordRepository.save(word(user.getId(), "valueA", "noteA", 1)));
        commit(() -> {
            statisticRepository.append(
                    wordFromEnglish(user.getId(), word.getId(), 0, true));
            statisticRepository.append(
                    wordFromEnglish(user.getId(), word.getId(), 3, true));
            statisticRepository.append(
                    wordFromEnglish(user.getId(), word.getId(), 5, true));
            statisticRepository.append(
                    wordFromEnglish(user.getId(), word.getId(), 10, true));
            statisticRepository.append(
                    wordFromEnglish(user.getId(), word.getId(), 11, false));
            statisticRepository.append(
                    wordFromNative(user.getId(), word.getId(), 0, true));
            statisticRepository.append(
                    wordFromNative(user.getId(), word.getId(), 3, true));
            statisticRepository.append(
                    wordFromNative(user.getId(), word.getId(), 4, false));
            statisticRepository.append(
                    wordFromNative(user.getId(), word.getId(), 5, false));
            statisticRepository.append(
                    wordFromNative(user.getId(), word.getId(), 6, false));
        });

        Optional<WordRepetitionByPeriodStatistic> actual = statisticRepository.wordRepetitionByPeriod(
                toUUID(1), word.getId(), periodStart(0), periodEnd(5)
        );

        Assertions.assertThat(actual).isEmpty();
    }

    @Test
    @DisplayName("""
            wordRepetitionByPeriod(userId, wordId, start, end):
             user with userId hasn't word with wordId
             => return empty Optional
            """)
    public void wordRepetitionByPeriod12() {
        User user = commit(() -> userRepository.save(user(1)));
        User user2 = commit(() -> userRepository.save(user(2)));
        Word word = commit(() -> wordRepository.save(word(user.getId(), "valueA", "noteA", 1)));
        commit(() -> {
            statisticRepository.append(
                    wordFromEnglish(user.getId(), word.getId(), 0, true));
            statisticRepository.append(
                    wordFromEnglish(user.getId(), word.getId(), 3, true));
            statisticRepository.append(
                    wordFromEnglish(user.getId(), word.getId(), 5, true));
            statisticRepository.append(
                    wordFromEnglish(user.getId(), word.getId(), 10, true));
            statisticRepository.append(
                    wordFromEnglish(user.getId(), word.getId(), 11, false));
            statisticRepository.append(
                    wordFromNative(user.getId(), word.getId(), 0, true));
            statisticRepository.append(
                    wordFromNative(user.getId(), word.getId(), 3, true));
            statisticRepository.append(
                    wordFromNative(user.getId(), word.getId(), 4, false));
            statisticRepository.append(
                    wordFromNative(user.getId(), word.getId(), 5, false));
            statisticRepository.append(
                    wordFromNative(user.getId(), word.getId(), 6, false));
        });

        Optional<WordRepetitionByPeriodStatistic> actual = statisticRepository.wordRepetitionByPeriod(
                user2.getId(), word.getId(), periodStart(0), periodEnd(5)
        );

        Assertions.assertThat(actual).isEmpty();
    }

    @Test
    @DisplayName("""
            expressionRepetitionByPeriod(userId, expressionId, start, end):
             start > end
             => exception
            """)
    public void expressionRepetitionByPeriod1() {
        User user = commit(() -> userRepository.save(user(1)));
        Expression expression = commit(() ->
                expressionRepository.save(expression(user.getId(), "valueA", "noteA", 1)));
        commit(() -> {
            statisticRepository.append(
                    expressionFromEnglish(user.getId(), expression.getId(), 0, true));
            statisticRepository.append(
                    expressionFromEnglish(user.getId(), expression.getId(), 3, true));
            statisticRepository.append(
                    expressionFromEnglish(user.getId(), expression.getId(), 5, true));
            statisticRepository.append(
                    expressionFromEnglish(user.getId(), expression.getId(), 10, true));
            statisticRepository.append(
                    expressionFromEnglish(user.getId(), expression.getId(), 11, false));
            statisticRepository.append(
                    expressionFromNative(user.getId(), expression.getId(), 0, true));
            statisticRepository.append(
                    expressionFromNative(user.getId(), expression.getId(), 3, true));
            statisticRepository.append(
                    expressionFromNative(user.getId(), expression.getId(), 4, false));
            statisticRepository.append(
                    expressionFromNative(user.getId(), expression.getId(), 5, false));
            statisticRepository.append(
                    expressionFromNative(user.getId(), expression.getId(), 6, false));
        });

        Assertions.assertThatExceptionOfType(InvalidParameter.class).
                isThrownBy(() -> statisticRepository.expressionRepetitionByPeriod(
                        user.getId(), expression.getId(), periodStart(10), periodEnd(0)));
    }

    @Test
    @DisplayName("""
            expressionRepetitionByPeriod(userId, expressionId, start, end):
             user didn't repeat this expression from english and native
             => return result with zero value for all field
            """)
    public void expressionRepetitionByPeriod2() {
        User user = commit(() -> userRepository.save(user(1)));
        Expression expression = commit(() ->
                expressionRepository.save(expression(user.getId(), "valueA", "noteA", 1)));

        Optional<ExpressionRepetitionByPeriodStatistic> actual = statisticRepository.expressionRepetitionByPeriod(
                user.getId(), expression.getId(), periodStart(0), periodEnd(1000)
        );

        Assertions.assertThat(actual).
                isPresent().
                get().
                usingRecursiveComparison().
                isEqualTo(new ExpressionRepetitionByPeriodStatistic(
                        user.getId(),
                        expression.getId(),
                        "valueA",
                        0,
                        0,
                        0,
                        0
                ));
    }

    @Test
    @DisplayName("""
            expressionRepetitionByPeriod(userId, expressionId, start, end):
             user didn't repeat this expression from english,
             user repeated this expression from native
             => return result with zero value for repeat from english
            """)
    public void expressionRepetitionByPeriod3() {
        User user = commit(() -> userRepository.save(user(1)));
        Expression expression = commit(() ->
                expressionRepository.save(expression(user.getId(), "valueA", "noteA", 1)));
        commit(() -> {
            statisticRepository.append(
                    expressionFromNative(user.getId(), expression.getId(), 0, true));
            statisticRepository.append(
                    expressionFromNative(user.getId(), expression.getId(), 3, true));
            statisticRepository.append(
                    expressionFromNative(user.getId(), expression.getId(), 4, false));
            statisticRepository.append(
                    expressionFromNative(user.getId(), expression.getId(), 5, false));
            statisticRepository.append(
                    expressionFromNative(user.getId(), expression.getId(), 6, false));
        });

        Optional<ExpressionRepetitionByPeriodStatistic> actual = statisticRepository.expressionRepetitionByPeriod(
                user.getId(), expression.getId(), periodStart(0), periodEnd(5)
        );

        Assertions.assertThat(actual).
                isPresent().
                get().
                usingRecursiveComparison().
                isEqualTo(new ExpressionRepetitionByPeriodStatistic(
                        user.getId(),
                        expression.getId(),
                        "valueA",
                        0,
                        0,
                        2,
                        2
                ));
    }

    @Test
    @DisplayName("""
            expressionRepetitionByPeriod(userId, expressionId, start, end):
             user didn't repeat this expression from native,
             user repeated this expression from english
             => return result with zero value for repeat from native
            """)
    public void expressionRepetitionByPeriod4() {
        User user = commit(() -> userRepository.save(user(1)));
        Expression expression = commit(() ->
                expressionRepository.save(expression(user.getId(), "valueA", "noteA", 1)));
        commit(() -> {
            statisticRepository.append(
                    expressionFromEnglish(user.getId(), expression.getId(), 0, true));
            statisticRepository.append(
                    expressionFromEnglish(user.getId(), expression.getId(), 3, true));
            statisticRepository.append(
                    expressionFromEnglish(user.getId(), expression.getId(), 5, true));
            statisticRepository.append(
                    expressionFromEnglish(user.getId(), expression.getId(), 10, true));
            statisticRepository.append(
                    expressionFromEnglish(user.getId(), expression.getId(), 11, false));
        });

        Optional<ExpressionRepetitionByPeriodStatistic> actual = statisticRepository.expressionRepetitionByPeriod(
                user.getId(), expression.getId(), periodStart(3), periodEnd(11)
        );

        Assertions.assertThat(actual).
                isPresent().
                get().
                usingRecursiveComparison().
                isEqualTo(new ExpressionRepetitionByPeriodStatistic(
                        user.getId(),
                        expression.getId(),
                        "valueA",
                        3,
                        1,
                        0,
                        0
                ));
    }

    @Test
    @DisplayName("""
            expressionRepetitionByPeriod(userId, expressionId, start, end):
             user repeated this expression from native,
             user repeated this expression from english
             => return number repetition from english and native
            """)
    public void expressionRepetitionByPeriod5() {
        User user = commit(() -> userRepository.save(user(1)));
        Expression expression = commit(() ->
                expressionRepository.save(expression(user.getId(), "valueA", "noteA", 1)));
        commit(() -> {
            statisticRepository.append(
                    expressionFromEnglish(user.getId(), expression.getId(), 0, true));
            statisticRepository.append(
                    expressionFromEnglish(user.getId(), expression.getId(), 3, true));
            statisticRepository.append(
                    expressionFromEnglish(user.getId(), expression.getId(), 5, true));
            statisticRepository.append(
                    expressionFromEnglish(user.getId(), expression.getId(), 10, true));
            statisticRepository.append(
                    expressionFromEnglish(user.getId(), expression.getId(), 11, false));
            statisticRepository.append(
                    expressionFromNative(user.getId(), expression.getId(), 0, true));
            statisticRepository.append(
                    expressionFromNative(user.getId(), expression.getId(), 3, true));
            statisticRepository.append(
                    expressionFromNative(user.getId(), expression.getId(), 4, false));
            statisticRepository.append(
                    expressionFromNative(user.getId(), expression.getId(), 5, false));
            statisticRepository.append(
                    expressionFromNative(user.getId(), expression.getId(), 6, false));
        });

        Optional<ExpressionRepetitionByPeriodStatistic> actual = statisticRepository.expressionRepetitionByPeriod(
                user.getId(), expression.getId(), periodStart(0), periodEnd(5)
        );

        Assertions.assertThat(actual).
                isPresent().
                get().
                usingRecursiveComparison().
                isEqualTo(new ExpressionRepetitionByPeriodStatistic(
                        user.getId(),
                        expression.getId(),
                        "valueA",
                        3,
                        0,
                        2,
                        2
                ));
    }

    @Test
    @DisplayName("""
            expressionRepetitionByPeriod(userId, expressionId, start, end):
             user repeated this expression from native,
             user repeated this expression from english,
             there are not data for repetition period
             => return result with zero value for all field
            """)
    public void expressionRepetitionByPeriod6() {
        User user = commit(() -> userRepository.save(user(1)));
        Expression expression = commit(() ->
                expressionRepository.save(expression(user.getId(), "valueA", "noteA", 1)));
        commit(() -> {
            statisticRepository.append(
                    expressionFromEnglish(user.getId(), expression.getId(), 0, true));
            statisticRepository.append(
                    expressionFromEnglish(user.getId(), expression.getId(), 3, true));
            statisticRepository.append(
                    expressionFromEnglish(user.getId(), expression.getId(), 5, true));
            statisticRepository.append(
                    expressionFromEnglish(user.getId(), expression.getId(), 10, true));
            statisticRepository.append(
                    expressionFromEnglish(user.getId(), expression.getId(), 11, false));
            statisticRepository.append(
                    expressionFromNative(user.getId(), expression.getId(), 0, true));
            statisticRepository.append(
                    expressionFromNative(user.getId(), expression.getId(), 3, true));
            statisticRepository.append(
                    expressionFromNative(user.getId(), expression.getId(), 4, false));
            statisticRepository.append(
                    expressionFromNative(user.getId(), expression.getId(), 5, false));
            statisticRepository.append(
                    expressionFromNative(user.getId(), expression.getId(), 6, false));
        });

        Optional<ExpressionRepetitionByPeriodStatistic> actual = statisticRepository.expressionRepetitionByPeriod(
                user.getId(), expression.getId(), periodStart(100), periodEnd(500)
        );

        Assertions.assertThat(actual).
                isPresent().
                get().
                usingRecursiveComparison().
                isEqualTo(new ExpressionRepetitionByPeriodStatistic(
                        user.getId(),
                        expression.getId(),
                        "valueA",
                        0,
                        0,
                        0,
                        0
                ));
    }

    @Test
    @DisplayName("""
            expressionRepetitionByPeriod(userId, expressionId, start, end):
             userId is null
             => exception
            """)
    public void expressionRepetitionByPeriod7() {
        User user = commit(() -> userRepository.save(user(1)));
        Expression expression = commit(() -> expressionRepository.save(expression(user.getId(), "valueA", "noteA", 1)));

        Assertions.assertThatNullPointerException().
                isThrownBy(() ->
                        commit(() -> statisticRepository.expressionRepetitionByPeriod(
                                null, expression.getId(), periodStart(1), periodEnd(5))
                        )
                );
    }

    @Test
    @DisplayName("""
            expressionRepetitionByPeriod(userId, expressionId, start, end):
             expressionId is null
             => exception
            """)
    public void expressionRepetitionByPeriod8() {
        User user = commit(() -> userRepository.save(user(1)));
        Expression expression = commit(() -> expressionRepository.save(expression(user.getId(), "valueA", "noteA", 1)));

        Assertions.assertThatNullPointerException().
                isThrownBy(() ->
                        commit(() -> statisticRepository.expressionRepetitionByPeriod(
                                user.getId(), null, periodStart(1), periodEnd(5))
                        )
                );
    }

    @Test
    @DisplayName("""
            expressionRepetitionByPeriod(userId, expressionId, start, end):
             start is null
             => exception
            """)
    public void expressionRepetitionByPeriod9() {
        User user = commit(() -> userRepository.save(user(1)));
        Expression expression = commit(() -> expressionRepository.save(expression(user.getId(), "valueA", "noteA", 1)));

        Assertions.assertThatNullPointerException().
                isThrownBy(() ->
                        commit(() -> statisticRepository.expressionRepetitionByPeriod(
                                user.getId(), expression.getId(), null, periodEnd(5))
                        )
                );
    }

    @Test
    @DisplayName("""
            expressionRepetitionByPeriod(userId, expressionId, start, end):
             end is null
             => exception
            """)
    public void expressionRepetitionByPeriod10() {
        User user = commit(() -> userRepository.save(user(1)));
        Expression expression = commit(() -> expressionRepository.save(expression(user.getId(), "valueA", "noteA", 1)));

        Assertions.assertThatNullPointerException().
                isThrownBy(() ->
                        commit(() -> statisticRepository.expressionRepetitionByPeriod(
                                user.getId(), expression.getId(), periodStart(1), null)
                        )
                );
    }

    @Test
    @DisplayName("""
            expressionRepetitionByPeriod(userId, expressionId, start, end):
             user with userId not exists
             => return empty Optional
            """)
    public void expressionRepetitionByPeriod11() {
        User user = commit(() -> userRepository.save(user(1)));
        Expression expression = commit(() ->
                expressionRepository.save(expression(user.getId(), "valueA", "noteA", 1)));
        commit(() -> {
            statisticRepository.append(
                    expressionFromEnglish(user.getId(), expression.getId(), 0, true));
            statisticRepository.append(
                    expressionFromEnglish(user.getId(), expression.getId(), 3, true));
            statisticRepository.append(
                    expressionFromEnglish(user.getId(), expression.getId(), 5, true));
            statisticRepository.append(
                    expressionFromEnglish(user.getId(), expression.getId(), 10, true));
            statisticRepository.append(
                    expressionFromEnglish(user.getId(), expression.getId(), 11, false));
            statisticRepository.append(
                    expressionFromNative(user.getId(), expression.getId(), 0, true));
            statisticRepository.append(
                    expressionFromNative(user.getId(), expression.getId(), 3, true));
            statisticRepository.append(
                    expressionFromNative(user.getId(), expression.getId(), 4, false));
            statisticRepository.append(
                    expressionFromNative(user.getId(), expression.getId(), 5, false));
            statisticRepository.append(
                    expressionFromNative(user.getId(), expression.getId(), 6, false));
        });

        Optional<ExpressionRepetitionByPeriodStatistic> actual = statisticRepository.expressionRepetitionByPeriod(
                toUUID(2), expression.getId(), periodStart(0), periodEnd(5)
        );

        Assertions.assertThat(actual).isEmpty();
    }

    @Test
    @DisplayName("""
            expressionRepetitionByPeriod(userId, expressionId, start, end):
             user with userId hasn't expression with expressionId
             => return empty Optional
            """)
    public void expressionRepetitionByPeriod12() {
        User user = commit(() -> userRepository.save(user(1)));
        User user2 = commit(() -> userRepository.save(user(2)));
        Expression expression = commit(() ->
                expressionRepository.save(expression(user.getId(), "valueA", "noteA", 1)));
        commit(() -> {
            statisticRepository.append(
                    expressionFromEnglish(user.getId(), expression.getId(), 0, true));
            statisticRepository.append(
                    expressionFromEnglish(user.getId(), expression.getId(), 3, true));
            statisticRepository.append(
                    expressionFromEnglish(user.getId(), expression.getId(), 5, true));
            statisticRepository.append(
                    expressionFromEnglish(user.getId(), expression.getId(), 10, true));
            statisticRepository.append(
                    expressionFromEnglish(user.getId(), expression.getId(), 11, false));
            statisticRepository.append(
                    expressionFromNative(user.getId(), expression.getId(), 0, true));
            statisticRepository.append(
                    expressionFromNative(user.getId(), expression.getId(), 3, true));
            statisticRepository.append(
                    expressionFromNative(user.getId(), expression.getId(), 4, false));
            statisticRepository.append(
                    expressionFromNative(user.getId(), expression.getId(), 5, false));
            statisticRepository.append(
                    expressionFromNative(user.getId(), expression.getId(), 6, false));
        });

        Optional<ExpressionRepetitionByPeriodStatistic> actual = statisticRepository.expressionRepetitionByPeriod(
                user2.getId(), expression.getId(), periodStart(0), periodEnd(5)
        );

        Assertions.assertThat(actual).isEmpty();
    }

    @Test
    @DisplayName("""
            wordsRepetitionByPeriod(userId, start, end, pageable):
             start > end
             => exception
            """)
    public void wordsRepetitionByPeriod1() {
        User user = commit(() -> userRepository.save(user(1)));
        Word wordA = word(user.getId(), "wordA", "noteA", 1);
        Word wordB = word(user.getId(), "wordB", "noteB", 1);
        Word wordC = word(user.getId(), "wordC", "noteC", 1);
        Word wordD = word(user.getId(), "wordD", "noteD", 1);
        Word wordE = word(user.getId(), "wordE", "noteE", 1);
        Word wordF = word(user.getId(), "wordF", "noteF", 1);
        commit(() -> {
            wordRepository.save(wordA);
            wordRepository.save(wordB);
            wordRepository.save(wordC);
            wordRepository.save(wordD);
            wordRepository.save(wordE);
            wordRepository.save(wordF);

            statisticRepository.append(wordFromEnglish(user.getId(), wordA.getId(), 0, true));
            statisticRepository.append(wordFromEnglish(user.getId(), wordA.getId(), 1, false));
            statisticRepository.append(wordFromNative(user.getId(), wordA.getId(), 0, true));
            statisticRepository.append(wordFromNative(user.getId(), wordA.getId(), 1, false));

            statisticRepository.append(wordFromEnglish(user.getId(), wordB.getId(), 0, false));
            statisticRepository.append(wordFromEnglish(user.getId(), wordB.getId(), 1, false));
            statisticRepository.append(wordFromEnglish(user.getId(), wordB.getId(), 2, true));
            statisticRepository.append(wordFromNative(user.getId(), wordB.getId(), 0, false));
            statisticRepository.append(wordFromNative(user.getId(), wordB.getId(), 1, true));

            statisticRepository.append(wordFromEnglish(user.getId(), wordC.getId(), 0, false));
            statisticRepository.append(wordFromEnglish(user.getId(), wordC.getId(), 1, false));
            statisticRepository.append(wordFromEnglish(user.getId(), wordC.getId(), 2, true));
            statisticRepository.append(wordFromNative(user.getId(), wordC.getId(), 0, false));
            statisticRepository.append(wordFromNative(user.getId(), wordC.getId(), 1, false));
            statisticRepository.append(wordFromNative(user.getId(), wordC.getId(), 2, true));

            statisticRepository.append(wordFromEnglish(user.getId(), wordD.getId(), 0, true));
            statisticRepository.append(wordFromEnglish(user.getId(), wordD.getId(), 1, false));
            statisticRepository.append(wordFromEnglish(user.getId(), wordD.getId(), 2, false));
            statisticRepository.append(wordFromNative(user.getId(), wordD.getId(), 0, false));
            statisticRepository.append(wordFromNative(user.getId(), wordD.getId(), 1, true));

            statisticRepository.append(wordFromEnglish(user.getId(), wordE.getId(), 0, false));
            statisticRepository.append(wordFromEnglish(user.getId(), wordE.getId(), 1, true));
            statisticRepository.append(wordFromEnglish(user.getId(), wordE.getId(), 2, false));
            statisticRepository.append(wordFromNative(user.getId(), wordE.getId(), 0, true));
            statisticRepository.append(wordFromNative(user.getId(), wordE.getId(), 1, true));

            statisticRepository.append(wordFromEnglish(user.getId(), wordF.getId(), 0, false));
            statisticRepository.append(wordFromEnglish(user.getId(), wordF.getId(), 1, false));
            statisticRepository.append(wordFromEnglish(user.getId(), wordF.getId(), 2, false));
            statisticRepository.append(wordFromNative(user.getId(), wordF.getId(), 0, true));
            statisticRepository.append(wordFromNative(user.getId(), wordF.getId(), 1, false));
        });

        Pageable pageable = PaginationRequest.toWordStatisticsPageRequest(
                0,
                100,
                "",
                config
        );
        Assertions.assertThatExceptionOfType(InvalidParameter.class).
                isThrownBy(() -> statisticRepository.wordsRepetitionByPeriod(
                        user.getId(), periodStart(10), periodEnd(0), pageable)
                );
    }

    @RepeatedTest(5)
    @DisplayName("""
            wordsRepetitionByPeriod(userId, start, end, pageable):
             user repeated words from native,
             user repeated words from english,
             sort by: remember_from_english.asc,
                      not_remember_from_english.asc,
                      remember_from_native.desc,
                      not_remember_from_native.desc
             => return number repetition from english and native
            """)
    public void wordsRepetitionByPeriod2() {
        User user = commit(() -> userRepository.save(user(1)));
        Word wordA = word(user.getId(), "wordA", "noteA", 1);
        Word wordB = word(user.getId(), "wordB", "noteB", 1);
        Word wordC = word(user.getId(), "wordC", "noteC", 1);
        Word wordD = word(user.getId(), "wordD", "noteD", 1);
        Word wordE = word(user.getId(), "wordE", "noteE", 1);
        Word wordF = word(user.getId(), "wordF", "noteF", 1);
        commit(() -> {
            wordRepository.save(wordA);
            wordRepository.save(wordB);
            wordRepository.save(wordC);
            wordRepository.save(wordD);
            wordRepository.save(wordE);
            wordRepository.save(wordF);

            statisticRepository.append(wordFromEnglish(user.getId(), wordA.getId(), 0, true));
            statisticRepository.append(wordFromEnglish(user.getId(), wordA.getId(), 1, false));
            statisticRepository.append(wordFromNative(user.getId(), wordA.getId(), 0, true));
            statisticRepository.append(wordFromNative(user.getId(), wordA.getId(), 1, false));

            statisticRepository.append(wordFromEnglish(user.getId(), wordB.getId(), 0, false));
            statisticRepository.append(wordFromEnglish(user.getId(), wordB.getId(), 1, false));
            statisticRepository.append(wordFromEnglish(user.getId(), wordB.getId(), 2, true));
            statisticRepository.append(wordFromNative(user.getId(), wordB.getId(), 0, false));
            statisticRepository.append(wordFromNative(user.getId(), wordB.getId(), 1, true));

            statisticRepository.append(wordFromEnglish(user.getId(), wordC.getId(), 0, false));
            statisticRepository.append(wordFromEnglish(user.getId(), wordC.getId(), 1, false));
            statisticRepository.append(wordFromEnglish(user.getId(), wordC.getId(), 2, true));
            statisticRepository.append(wordFromNative(user.getId(), wordC.getId(), 0, false));
            statisticRepository.append(wordFromNative(user.getId(), wordC.getId(), 1, false));
            statisticRepository.append(wordFromNative(user.getId(), wordC.getId(), 2, true));

            statisticRepository.append(wordFromEnglish(user.getId(), wordD.getId(), 0, true));
            statisticRepository.append(wordFromEnglish(user.getId(), wordD.getId(), 1, false));
            statisticRepository.append(wordFromEnglish(user.getId(), wordD.getId(), 2, false));
            statisticRepository.append(wordFromNative(user.getId(), wordD.getId(), 0, false));
            statisticRepository.append(wordFromNative(user.getId(), wordD.getId(), 1, true));

            statisticRepository.append(wordFromEnglish(user.getId(), wordE.getId(), 0, false));
            statisticRepository.append(wordFromEnglish(user.getId(), wordE.getId(), 1, true));
            statisticRepository.append(wordFromEnglish(user.getId(), wordE.getId(), 2, false));
            statisticRepository.append(wordFromNative(user.getId(), wordE.getId(), 0, true));
            statisticRepository.append(wordFromNative(user.getId(), wordE.getId(), 1, true));

            statisticRepository.append(wordFromEnglish(user.getId(), wordF.getId(), 0, false));
            statisticRepository.append(wordFromEnglish(user.getId(), wordF.getId(), 1, false));
            statisticRepository.append(wordFromEnglish(user.getId(), wordF.getId(), 2, false));
            statisticRepository.append(wordFromNative(user.getId(), wordF.getId(), 0, true));
            statisticRepository.append(wordFromNative(user.getId(), wordF.getId(), 1, false));
        });

        Pageable pageable = PaginationRequest.toWordStatisticsPageRequest(
                0,
                100,
                """
                remember_from_english.asc,
                not_remember_from_english.asc,
                remember_from_native.desc,
                not_remember_from_native.desc
                """,
                config
        );
        Page<WordRepetitionByPeriodStatistic> actual = statisticRepository.wordsRepetitionByPeriod(
                user.getId(), periodStart(0), periodEnd(100), pageable
        );

        Assertions.assertThat(actual.getContent()).
                containsExactly(
                        new WordRepetitionByPeriodStatistic(
                                user.getId(),
                                wordF.getId(),
                                "wordF",
                                0,
                                3,
                                1,
                                1),
                        new WordRepetitionByPeriodStatistic(
                                user.getId(),
                                wordA.getId(),
                                "wordA",
                                1,
                                1,
                                1,
                                1),
                        new WordRepetitionByPeriodStatistic(
                                user.getId(),
                                wordE.getId(),
                                "wordE",
                                1,
                                2,
                                2,
                                0),
                        new WordRepetitionByPeriodStatistic(
                                user.getId(),
                                wordC.getId(),
                                "wordC",
                                1,
                                2,
                                1,
                                2),
                        new WordRepetitionByPeriodStatistic(
                                user.getId(),
                                wordB.getId(),
                                "wordB",
                                1,
                                2,
                                1,
                                1),
                        new WordRepetitionByPeriodStatistic(
                                user.getId(),
                                wordD.getId(),
                                "wordD",
                                1,
                                2,
                                1,
                                1)
                );
    }

    @RepeatedTest(5)
    @DisplayName("""
            wordsRepetitionByPeriod(userId, start, end, pageable):
             user repeated words from native,
             user repeated words from english,
             period not include all words
             => return number repetition from english and native
            """)
    public void wordsRepetitionByPeriod3() {
        User user = commit(() -> userRepository.save(user(1)));
        Word wordA = word(user.getId(), "wordA", "noteA", 1);
        Word wordB = word(user.getId(), "wordB", "noteB", 1);
        Word wordC = word(user.getId(), "wordC", "noteC", 1);
        commit(() -> {
            wordRepository.save(wordA);
            wordRepository.save(wordB);
            wordRepository.save(wordC);

            statisticRepository.append(wordFromEnglish(user.getId(), wordA.getId(), 0, true));
            statisticRepository.append(wordFromEnglish(user.getId(), wordA.getId(), 3, true));
            statisticRepository.append(wordFromEnglish(user.getId(), wordA.getId(), 10, true));
            statisticRepository.append(wordFromNative(user.getId(), wordA.getId(), 0, true));
            statisticRepository.append(wordFromNative(user.getId(), wordA.getId(), 5, true));
            statisticRepository.append(wordFromNative(user.getId(), wordA.getId(), 10, true));

            statisticRepository.append(wordFromEnglish(user.getId(), wordB.getId(), 0, true));
            statisticRepository.append(wordFromEnglish(user.getId(), wordB.getId(), 3, true));
            statisticRepository.append(wordFromEnglish(user.getId(), wordB.getId(), 5, false));
            statisticRepository.append(wordFromNative(user.getId(), wordB.getId(), 0, true));
            statisticRepository.append(wordFromNative(user.getId(), wordB.getId(), 3, false));
            statisticRepository.append(wordFromNative(user.getId(), wordB.getId(), 4, true));

            statisticRepository.append(wordFromEnglish(user.getId(), wordC.getId(), 0, false));
            statisticRepository.append(wordFromEnglish(user.getId(), wordC.getId(), 1, false));
            statisticRepository.append(wordFromEnglish(user.getId(), wordC.getId(), 2, true));
            statisticRepository.append(wordFromNative(user.getId(), wordC.getId(), 0, false));
            statisticRepository.append(wordFromNative(user.getId(), wordC.getId(), 1, false));
            statisticRepository.append(wordFromNative(user.getId(), wordC.getId(), 2, true));
        });

        Pageable pageable = PaginationRequest.toWordStatisticsPageRequest(
                0,
                100,
                "",
                config
        );
        Page<WordRepetitionByPeriodStatistic> actual = statisticRepository.wordsRepetitionByPeriod(
                user.getId(), periodStart(0), periodEnd(4), pageable
        );

        Assertions.assertThat(actual.getContent()).
                containsExactly(
                        new WordRepetitionByPeriodStatistic(
                                user.getId(),
                                wordC.getId(),
                                "wordC",
                                1,
                                2,
                                1,
                                2),
                        new WordRepetitionByPeriodStatistic(
                                user.getId(),
                                wordA.getId(),
                                "wordA",
                                2,
                                0,
                                1,
                                0),
                        new WordRepetitionByPeriodStatistic(
                                user.getId(),
                                wordB.getId(),
                                "wordB",
                                2,
                                0,
                                2,
                                1)
                );
    }

    @Test
    @DisplayName("""
            wordsRepetitionByPeriod(userId, start, end, pageable):
             userId is null
             => exception
            """)
    public void wordsRepetitionByPeriod4() {
        Pageable pageable = PaginationRequest.toWordStatisticsPageRequest(
                0,
                100,
                "",
                config
        );

        Assertions.assertThatNullPointerException().
                isThrownBy(() ->
                        commit(() -> statisticRepository.wordsRepetitionByPeriod(
                                null, periodStart(1), periodEnd(5), pageable)
                        )
                );
    }

    @Test
    @DisplayName("""
            wordsRepetitionByPeriod(userId, start, end, pageable):
             start is null
             => exception
            """)
    public void wordsRepetitionByPeriod5() {
        User user = commit(() -> userRepository.save(user(1)));
        Pageable pageable = PaginationRequest.toWordStatisticsPageRequest(
                0,
                100,
                "",
                config
        );

        Assertions.assertThatNullPointerException().
                isThrownBy(() ->
                        commit(() -> statisticRepository.wordsRepetitionByPeriod(
                                user.getId(), null, periodEnd(5), pageable)
                        )
                );
    }

    @Test
    @DisplayName("""
            wordsRepetitionByPeriod(userId, start, end, pageable):
             end is null
             => exception
            """)
    public void wordsRepetitionByPeriod6() {
        User user = commit(() -> userRepository.save(user(1)));
        Pageable pageable = PaginationRequest.toWordStatisticsPageRequest(
                0,
                100,
                "",
                config
        );

        Assertions.assertThatNullPointerException().
                isThrownBy(() ->
                        commit(() -> statisticRepository.wordsRepetitionByPeriod(
                                user.getId(), periodStart(1), null, pageable)
                        )
                );
    }

    @Test
    @DisplayName("""
            wordsRepetitionByPeriod(userId, start, end, pageable):
             pageable is null
             => exception
            """)
    public void wordsRepetitionByPeriod7() {
        User user = commit(() -> userRepository.save(user(1)));

        Assertions.assertThatNullPointerException().
                isThrownBy(() ->
                        commit(() -> statisticRepository.wordsRepetitionByPeriod(
                                user.getId(), periodStart(1), periodEnd(5), null)
                        )
                );
    }

    @Test
    @DisplayName("""
            expressionsRepetitionByPeriod(userId, start, end, pageable):
             start > end
             => exception
            """)
    public void expressionsRepetitionByPeriod1() {
        User user = commit(() -> userRepository.save(user(1)));
        Expression expressionA = expression(user.getId(), "expressionA", "noteA", 1);
        Expression expressionB = expression(user.getId(), "expressionB", "noteB", 1);
        Expression expressionC = expression(user.getId(), "expressionC", "noteC", 1);
        Expression expressionD = expression(user.getId(), "expressionD", "noteD", 1);
        Expression expressionE = expression(user.getId(), "expressionE", "noteE", 1);
        Expression expressionF = expression(user.getId(), "expressionF", "noteF", 1);
        commit(() -> {
            expressionRepository.save(expressionA);
            expressionRepository.save(expressionB);
            expressionRepository.save(expressionC);
            expressionRepository.save(expressionD);
            expressionRepository.save(expressionE);
            expressionRepository.save(expressionF);

            statisticRepository.append(expressionFromEnglish(user.getId(), expressionA.getId(), 0, true));
            statisticRepository.append(expressionFromEnglish(user.getId(), expressionA.getId(), 1, false));
            statisticRepository.append(expressionFromNative(user.getId(), expressionA.getId(), 0, true));
            statisticRepository.append(expressionFromNative(user.getId(), expressionA.getId(), 1, false));

            statisticRepository.append(expressionFromEnglish(user.getId(), expressionB.getId(), 0, false));
            statisticRepository.append(expressionFromEnglish(user.getId(), expressionB.getId(), 1, false));
            statisticRepository.append(expressionFromEnglish(user.getId(), expressionB.getId(), 2, true));
            statisticRepository.append(expressionFromNative(user.getId(), expressionB.getId(), 0, false));
            statisticRepository.append(expressionFromNative(user.getId(), expressionB.getId(), 1, true));

            statisticRepository.append(expressionFromEnglish(user.getId(), expressionC.getId(), 0, false));
            statisticRepository.append(expressionFromEnglish(user.getId(), expressionC.getId(), 1, false));
            statisticRepository.append(expressionFromEnglish(user.getId(), expressionC.getId(), 2, true));
            statisticRepository.append(expressionFromNative(user.getId(), expressionC.getId(), 0, false));
            statisticRepository.append(expressionFromNative(user.getId(), expressionC.getId(), 1, false));
            statisticRepository.append(expressionFromNative(user.getId(), expressionC.getId(), 2, true));

            statisticRepository.append(expressionFromEnglish(user.getId(), expressionD.getId(), 0, true));
            statisticRepository.append(expressionFromEnglish(user.getId(), expressionD.getId(), 1, false));
            statisticRepository.append(expressionFromEnglish(user.getId(), expressionD.getId(), 2, false));
            statisticRepository.append(expressionFromNative(user.getId(), expressionD.getId(), 0, false));
            statisticRepository.append(expressionFromNative(user.getId(), expressionD.getId(), 1, true));

            statisticRepository.append(expressionFromEnglish(user.getId(), expressionE.getId(), 0, false));
            statisticRepository.append(expressionFromEnglish(user.getId(), expressionE.getId(), 1, true));
            statisticRepository.append(expressionFromEnglish(user.getId(), expressionE.getId(), 2, false));
            statisticRepository.append(expressionFromNative(user.getId(), expressionE.getId(), 0, true));
            statisticRepository.append(expressionFromNative(user.getId(), expressionE.getId(), 1, true));

            statisticRepository.append(expressionFromEnglish(user.getId(), expressionF.getId(), 0, false));
            statisticRepository.append(expressionFromEnglish(user.getId(), expressionF.getId(), 1, false));
            statisticRepository.append(expressionFromEnglish(user.getId(), expressionF.getId(), 2, false));
            statisticRepository.append(expressionFromNative(user.getId(), expressionF.getId(), 0, true));
            statisticRepository.append(expressionFromNative(user.getId(), expressionF.getId(), 1, false));
        });

        Pageable pageable = PaginationRequest.toExpressionStatisticsPageRequest(
                0,
                100,
                "",
                config
        );
        Assertions.assertThatExceptionOfType(InvalidParameter.class).
                isThrownBy(() -> statisticRepository.expressionsRepetitionByPeriod(
                        user.getId(), periodStart(10), periodEnd(0), pageable)
                );
    }

    @RepeatedTest(5)
    @DisplayName("""
            expressionsRepetitionByPeriod(userId, start, end, pageable):
             user repeated expressions from native,
             user repeated expressions from english,
             sort by: remember_from_english.asc,
                      not_remember_from_english.asc,
                      remember_from_native.desc,
                      not_remember_from_native.desc
             => return number repetition from english and native
            """)
    public void expressionsRepetitionByPeriod2() {
        User user = commit(() -> userRepository.save(user(1)));
        Expression expressionA = expression(user.getId(), "expressionA", "noteA", 1);
        Expression expressionB = expression(user.getId(), "expressionB", "noteB", 1);
        Expression expressionC = expression(user.getId(), "expressionC", "noteC", 1);
        Expression expressionD = expression(user.getId(), "expressionD", "noteD", 1);
        Expression expressionE = expression(user.getId(), "expressionE", "noteE", 1);
        Expression expressionF = expression(user.getId(), "expressionF", "noteF", 1);
        commit(() -> {
            expressionRepository.save(expressionA);
            expressionRepository.save(expressionB);
            expressionRepository.save(expressionC);
            expressionRepository.save(expressionD);
            expressionRepository.save(expressionE);
            expressionRepository.save(expressionF);

            statisticRepository.append(expressionFromEnglish(user.getId(), expressionA.getId(), 0, true));
            statisticRepository.append(expressionFromEnglish(user.getId(), expressionA.getId(), 1, false));
            statisticRepository.append(expressionFromNative(user.getId(), expressionA.getId(), 0, true));
            statisticRepository.append(expressionFromNative(user.getId(), expressionA.getId(), 1, false));

            statisticRepository.append(expressionFromEnglish(user.getId(), expressionB.getId(), 0, false));
            statisticRepository.append(expressionFromEnglish(user.getId(), expressionB.getId(), 1, false));
            statisticRepository.append(expressionFromEnglish(user.getId(), expressionB.getId(), 2, true));
            statisticRepository.append(expressionFromNative(user.getId(), expressionB.getId(), 0, false));
            statisticRepository.append(expressionFromNative(user.getId(), expressionB.getId(), 1, true));

            statisticRepository.append(expressionFromEnglish(user.getId(), expressionC.getId(), 0, false));
            statisticRepository.append(expressionFromEnglish(user.getId(), expressionC.getId(), 1, false));
            statisticRepository.append(expressionFromEnglish(user.getId(), expressionC.getId(), 2, true));
            statisticRepository.append(expressionFromNative(user.getId(), expressionC.getId(), 0, false));
            statisticRepository.append(expressionFromNative(user.getId(), expressionC.getId(), 1, false));
            statisticRepository.append(expressionFromNative(user.getId(), expressionC.getId(), 2, true));

            statisticRepository.append(expressionFromEnglish(user.getId(), expressionD.getId(), 0, true));
            statisticRepository.append(expressionFromEnglish(user.getId(), expressionD.getId(), 1, false));
            statisticRepository.append(expressionFromEnglish(user.getId(), expressionD.getId(), 2, false));
            statisticRepository.append(expressionFromNative(user.getId(), expressionD.getId(), 0, false));
            statisticRepository.append(expressionFromNative(user.getId(), expressionD.getId(), 1, true));

            statisticRepository.append(expressionFromEnglish(user.getId(), expressionE.getId(), 0, false));
            statisticRepository.append(expressionFromEnglish(user.getId(), expressionE.getId(), 1, true));
            statisticRepository.append(expressionFromEnglish(user.getId(), expressionE.getId(), 2, false));
            statisticRepository.append(expressionFromNative(user.getId(), expressionE.getId(), 0, true));
            statisticRepository.append(expressionFromNative(user.getId(), expressionE.getId(), 1, true));

            statisticRepository.append(expressionFromEnglish(user.getId(), expressionF.getId(), 0, false));
            statisticRepository.append(expressionFromEnglish(user.getId(), expressionF.getId(), 1, false));
            statisticRepository.append(expressionFromEnglish(user.getId(), expressionF.getId(), 2, false));
            statisticRepository.append(expressionFromNative(user.getId(), expressionF.getId(), 0, true));
            statisticRepository.append(expressionFromNative(user.getId(), expressionF.getId(), 1, false));
        });

        Pageable pageable = PaginationRequest.toExpressionStatisticsPageRequest(
                0,
                100,
                """
                        remember_from_english.asc,
                        not_remember_from_english.asc,
                        remember_from_native.desc,
                        not_remember_from_native.desc
                        """,
                config
        );
        Page<ExpressionRepetitionByPeriodStatistic> actual = statisticRepository.expressionsRepetitionByPeriod(
                user.getId(), periodStart(0), periodEnd(100), pageable
        );

        Assertions.assertThat(actual.getContent()).
                containsExactly(
                        new ExpressionRepetitionByPeriodStatistic(
                                user.getId(),
                                expressionF.getId(),
                                "expressionF",
                                0,
                                3,
                                1,
                                1),
                        new ExpressionRepetitionByPeriodStatistic(
                                user.getId(),
                                expressionA.getId(),
                                "expressionA",
                                1,
                                1,
                                1,
                                1),
                        new ExpressionRepetitionByPeriodStatistic(
                                user.getId(),
                                expressionE.getId(),
                                "expressionE",
                                1,
                                2,
                                2,
                                0),
                        new ExpressionRepetitionByPeriodStatistic(
                                user.getId(),
                                expressionC.getId(),
                                "expressionC",
                                1,
                                2,
                                1,
                                2),
                        new ExpressionRepetitionByPeriodStatistic(
                                user.getId(),
                                expressionB.getId(),
                                "expressionB",
                                1,
                                2,
                                1,
                                1),
                        new ExpressionRepetitionByPeriodStatistic(
                                user.getId(),
                                expressionD.getId(),
                                "expressionD",
                                1,
                                2,
                                1,
                                1)
                );
    }

    @RepeatedTest(5)
    @DisplayName("""
            expressionsRepetitionByPeriod(userId, start, end, pageable):
             user repeated expressions from native,
             user repeated expressions from english,
             period not include all expressions
             => return number repetition from english and native
            """)
    public void expressionsRepetitionByPeriod3() {
        User user = commit(() -> userRepository.save(user(1)));
        Expression expressionA = expression(user.getId(), "expressionA", "noteA", 1);
        Expression expressionB = expression(user.getId(), "expressionB", "noteB", 1);
        Expression expressionC = expression(user.getId(), "expressionC", "noteC", 1);
        commit(() -> {
            expressionRepository.save(expressionA);
            expressionRepository.save(expressionB);
            expressionRepository.save(expressionC);

            statisticRepository.append(expressionFromEnglish(user.getId(), expressionA.getId(), 0, true));
            statisticRepository.append(expressionFromEnglish(user.getId(), expressionA.getId(), 3, true));
            statisticRepository.append(expressionFromEnglish(user.getId(), expressionA.getId(), 10, true));
            statisticRepository.append(expressionFromNative(user.getId(), expressionA.getId(), 0, true));
            statisticRepository.append(expressionFromNative(user.getId(), expressionA.getId(), 5, true));
            statisticRepository.append(expressionFromNative(user.getId(), expressionA.getId(), 10, true));

            statisticRepository.append(expressionFromEnglish(user.getId(), expressionB.getId(), 0, true));
            statisticRepository.append(expressionFromEnglish(user.getId(), expressionB.getId(), 3, true));
            statisticRepository.append(expressionFromEnglish(user.getId(), expressionB.getId(), 5, false));
            statisticRepository.append(expressionFromNative(user.getId(), expressionB.getId(), 0, true));
            statisticRepository.append(expressionFromNative(user.getId(), expressionB.getId(), 3, false));
            statisticRepository.append(expressionFromNative(user.getId(), expressionB.getId(), 4, true));

            statisticRepository.append(expressionFromEnglish(user.getId(), expressionC.getId(), 0, false));
            statisticRepository.append(expressionFromEnglish(user.getId(), expressionC.getId(), 1, false));
            statisticRepository.append(expressionFromEnglish(user.getId(), expressionC.getId(), 2, true));
            statisticRepository.append(expressionFromNative(user.getId(), expressionC.getId(), 0, false));
            statisticRepository.append(expressionFromNative(user.getId(), expressionC.getId(), 1, false));
            statisticRepository.append(expressionFromNative(user.getId(), expressionC.getId(), 2, true));
        });

        Pageable pageable = PaginationRequest.toExpressionStatisticsPageRequest(
                0,
                100,
                "",
                config
        );
        Page<ExpressionRepetitionByPeriodStatistic> actual = statisticRepository.expressionsRepetitionByPeriod(
                user.getId(), periodStart(0), periodEnd(4), pageable
        );

        Assertions.assertThat(actual.getContent()).
                containsExactly(
                        new ExpressionRepetitionByPeriodStatistic(
                                user.getId(),
                                expressionC.getId(),
                                "expressionC",
                                1,
                                2,
                                1,
                                2),
                        new ExpressionRepetitionByPeriodStatistic(
                                user.getId(),
                                expressionA.getId(),
                                "expressionA",
                                2,
                                0,
                                1,
                                0),
                        new ExpressionRepetitionByPeriodStatistic(
                                user.getId(),
                                expressionB.getId(),
                                "expressionB",
                                2,
                                0,
                                2,
                                1)
                );
    }

    @Test
    @DisplayName("""
            expressionsRepetitionByPeriod(userId, start, end, pageable):
             userId is null
             => exception
            """)
    public void expressionsRepetitionByPeriod4() {
        Pageable pageable = PaginationRequest.toExpressionStatisticsPageRequest(
                0,
                100,
                "",
                config
        );

        Assertions.assertThatNullPointerException().
                isThrownBy(() ->
                        commit(() -> statisticRepository.expressionsRepetitionByPeriod(
                                null, periodStart(1), periodEnd(5), pageable)
                        )
                );
    }

    @Test
    @DisplayName("""
            expressionsRepetitionByPeriod(userId, start, end, pageable):
             start is null
             => exception
            """)
    public void expressionsRepetitionByPeriod5() {
        User user = commit(() -> userRepository.save(user(1)));
        Pageable pageable = PaginationRequest.toExpressionStatisticsPageRequest(
                0,
                100,
                "",
                config
        );

        Assertions.assertThatNullPointerException().
                isThrownBy(() ->
                        commit(() -> statisticRepository.expressionsRepetitionByPeriod(
                                user.getId(), null, periodEnd(5), pageable)
                        )
                );
    }

    @Test
    @DisplayName("""
            expressionsRepetitionByPeriod(userId, start, end, pageable):
             end is null
             => exception
            """)
    public void expressionsRepetitionByPeriod6() {
        User user = commit(() -> userRepository.save(user(1)));
        Pageable pageable = PaginationRequest.toExpressionStatisticsPageRequest(
                0,
                100,
                "",
                config
        );

        Assertions.assertThatNullPointerException().
                isThrownBy(() ->
                        commit(() -> statisticRepository.expressionsRepetitionByPeriod(
                                user.getId(), periodStart(1), null, pageable)
                        )
                );
    }

    @Test
    @DisplayName("""
            expressionsRepetitionByPeriod(userId, start, end, pageable):
             pageable is null
             => exception
            """)
    public void expressionsRepetitionByPeriod7() {
        User user = commit(() -> userRepository.save(user(1)));

        Assertions.assertThatNullPointerException().
                isThrownBy(() ->
                        commit(() -> statisticRepository.expressionsRepetitionByPeriod(
                                user.getId(), periodStart(1), periodEnd(5), null)
                        )
                );
    }


    private LocalDate periodStart(int plusDays) {
        return LocalDate.now(Clock.offset(clock, Duration.ofDays(plusDays)));
    }

    private LocalDate periodEnd(int plusDays) {
        return LocalDate.now(Clock.offset(clock, Duration.ofDays(plusDays)));
    }

    private UUID toUUID(int number) {
        return UUID.fromString("00000000-0000-0000-0000-" + String.format("%012d", number));
    }


    private User user(int number) {
        return new User(
                null,
                "me" + number + "@mail.com",
                "password" + number,
                "salt" + number,
                new ArrayList<>());
    }

    private Word word(UUID userId,
                      String value,
                      String note,
                      int interval) {
        return new Word(userId, interval, interval, clock).
                setValue(value).
                setNote(note);
    }


    private Expression expression(UUID userId,
                                  String value,
                                  String note,
                                  int interval) {
        return new Expression(userId, interval, interval, clock).
                setValue(value).
                setNote(note);
    }

    private RepeatWordFromEnglishStatistic wordFromEnglish(UUID userId,
                                                           UUID wordId,
                                                           int plusDays,
                                                           boolean isRemember) {
        return new RepeatWordFromEnglishStatistic(
                userId,
                wordId,
                LocalDate.now(Clock.offset(clock, Duration.ofDays(plusDays))),
                isRemember
        );
    }

    private RepeatWordFromNativeStatistic wordFromNative(UUID userId,
                                                         UUID wordId,
                                                         int plusDays,
                                                         boolean isRemember) {
        return new RepeatWordFromNativeStatistic(
                userId,
                wordId,
                LocalDate.now(Clock.offset(clock, Duration.ofDays(plusDays))),
                isRemember
        );
    }

    private RepeatExpressionFromEnglishStatistic expressionFromEnglish(UUID userId,
                                                                       UUID wordId,
                                                                       int plusDays,
                                                                       boolean isRemember) {
        return new RepeatExpressionFromEnglishStatistic(
                userId,
                wordId,
                LocalDate.now(Clock.offset(clock, Duration.ofDays(plusDays))),
                isRemember
        );
    }

    private RepeatExpressionFromNativeStatistic expressionFromNative(UUID userId,
                                                                     UUID wordId,
                                                                     int plusDays,
                                                                     boolean isRemember) {
        return new RepeatExpressionFromNativeStatistic(
                userId,
                wordId,
                LocalDate.now(Clock.offset(clock, Duration.ofDays(plusDays))),
                isRemember
        );
    }

    private void commit(Executable executable) {
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        TransactionStatus status = transactionManager.getTransaction(def);
        try {
            executable.execute();
            transactionManager.commit(status);
        } catch(RuntimeException e) {
            transactionManager.rollback(status);
            throw e;
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
        } catch(RuntimeException e) {
            transactionManager.rollback(status);
            throw e;
        }
    }

}