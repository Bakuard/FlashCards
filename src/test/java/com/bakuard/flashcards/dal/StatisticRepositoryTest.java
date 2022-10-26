package com.bakuard.flashcards.dal;

import com.bakuard.flashcards.config.MutableClock;
import com.bakuard.flashcards.config.TestConfig;
import com.bakuard.flashcards.model.RepeatDataFromEnglish;
import com.bakuard.flashcards.model.RepeatDataFromNative;
import com.bakuard.flashcards.model.auth.credential.User;
import com.bakuard.flashcards.model.expression.Expression;
import com.bakuard.flashcards.model.filter.SortRules;
import com.bakuard.flashcards.model.filter.SortedEntity;
import com.bakuard.flashcards.model.statistic.*;
import com.bakuard.flashcards.model.word.Word;
import com.bakuard.flashcards.validation.InvalidParameter;
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
import java.util.UUID;
import java.util.function.Supplier;

@ExtendWith(SpringExtension.class)
@TestPropertySource(locations = "classpath:test.properties")
@Import(TestConfig.class)
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
    private SortRules sortRules;

    @BeforeEach
    public void beforeEach() {
        commit(() -> JdbcTestUtils.deleteFromTables(jdbcTemplate,
                "expressions",
                "words",
                "intervals",
                "users"
        ));
        clock.setDate(2022, 7, 7);
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

        WordRepetitionByPeriodStatistic actual = statisticRepository.wordRepetitionByPeriod(
                user.getId(), word.getId(), periodStart(0), periodEnd(1000)
        );

        Assertions.assertThat(actual).
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

        WordRepetitionByPeriodStatistic actual = statisticRepository.wordRepetitionByPeriod(
                user.getId(), word.getId(), periodStart(0), periodEnd(5)
        );

        Assertions.assertThat(actual).
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

        WordRepetitionByPeriodStatistic actual = statisticRepository.wordRepetitionByPeriod(
                user.getId(), word.getId(), periodStart(3), periodEnd(11)
        );

        Assertions.assertThat(actual).
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

        WordRepetitionByPeriodStatistic actual = statisticRepository.wordRepetitionByPeriod(
                user.getId(), word.getId(), periodStart(0), periodEnd(5)
        );

        Assertions.assertThat(actual).
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

        WordRepetitionByPeriodStatistic actual = statisticRepository.wordRepetitionByPeriod(
                user.getId(), word.getId(), periodStart(100), periodEnd(500)
        );

        Assertions.assertThat(actual).
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
            expressionRepetitionByPeriod(userId, wordId, start, end):
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
            expressionRepetitionByPeriod(userId, wordId, start, end):
             user didn't repeat this expression from english and native
             => return result with zero value for all field
            """)
    public void expressionRepetitionByPeriod2() {
        User user = commit(() -> userRepository.save(user(1)));
        Expression expression = commit(() ->
                expressionRepository.save(expression(user.getId(), "valueA", "noteA", 1)));

        ExpressionRepetitionByPeriodStatistic actual = statisticRepository.expressionRepetitionByPeriod(
                user.getId(), expression.getId(), periodStart(0), periodEnd(1000)
        );

        Assertions.assertThat(actual).
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
            expressionRepetitionByPeriod(userId, wordId, start, end):
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

        ExpressionRepetitionByPeriodStatistic actual = statisticRepository.expressionRepetitionByPeriod(
                user.getId(), expression.getId(), periodStart(0), periodEnd(5)
        );

        Assertions.assertThat(actual).
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
            expressionRepetitionByPeriod(userId, wordId, start, end):
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

        ExpressionRepetitionByPeriodStatistic actual = statisticRepository.expressionRepetitionByPeriod(
                user.getId(), expression.getId(), periodStart(3), periodEnd(11)
        );

        Assertions.assertThat(actual).
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
            expressionRepetitionByPeriod(userId, wordId, start, end):
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

        ExpressionRepetitionByPeriodStatistic actual = statisticRepository.expressionRepetitionByPeriod(
                user.getId(), expression.getId(), periodStart(0), periodEnd(5)
        );

        Assertions.assertThat(actual).
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
            expressionRepetitionByPeriod(userId, wordId, start, end):
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

        ExpressionRepetitionByPeriodStatistic actual = statisticRepository.expressionRepetitionByPeriod(
                user.getId(), expression.getId(), periodStart(100), periodEnd(500)
        );

        Assertions.assertThat(actual).
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

        Pageable pageable = PageRequest.of(0, 100, sortRules.getDefaultSort(SortedEntity.WORD_STATISTIC));
        Assertions.assertThatExceptionOfType(InvalidParameter.class).
                isThrownBy(() -> statisticRepository.wordsRepetitionByPeriod(
                        user.getId(), periodStart(10), periodEnd(0), pageable)
                );
    }

    @Test
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

            System.out.println("wordA -> " + wordA.getId());
            System.out.println("wordB -> " + wordB.getId());
            System.out.println("wordC -> " + wordC.getId());
            System.out.println("wordD -> " + wordD.getId());
            System.out.println("wordE -> " + wordE.getId());
            System.out.println("wordF -> " + wordF.getId());

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

        Pageable pageable = PageRequest.of(0, 100,
                sortRules.toSort("remember_from_english.asc,not_remember_from_english.asc,remember_from_native.desc,not_remember_from_native.desc", SortedEntity.WORD_STATISTIC));
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

    @Test
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

        Pageable pageable = PageRequest.of(0, 100, sortRules.getDefaultSort(SortedEntity.WORD_STATISTIC));
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

        Pageable pageable = PageRequest.of(0, 100,
                sortRules.getDefaultSort(SortedEntity.EXPRESSION_STATISTIC));
        Assertions.assertThatExceptionOfType(InvalidParameter.class).
                isThrownBy(() -> statisticRepository.expressionsRepetitionByPeriod(
                        user.getId(), periodStart(10), periodEnd(0), pageable)
                );
    }

    @Test
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

        Pageable pageable = PageRequest.of(0, 100,
                sortRules.toSort("""
                        remember_from_english.asc,
                        not_remember_from_english.asc,
                        remember_from_native.desc,
                        not_remember_from_native.desc
                        """, SortedEntity.WORD_STATISTIC));
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

    @Test
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

        Pageable pageable = PageRequest.of(0, 100,
                sortRules.getDefaultSort(SortedEntity.EXPRESSION_STATISTIC));
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


    private LocalDate periodStart(int plusDays) {
        return LocalDate.now(Clock.offset(clock, Duration.ofDays(plusDays)));
    }

    private LocalDate periodEnd(int plusDays) {
        return LocalDate.now(Clock.offset(clock, Duration.ofDays(plusDays)));
    }

    private User user(int number) {
        return User.newBuilder(validator).
                setPassword("password" + number).
                setEmail("me" + number + "@mail.com").
                setOrGenerateSalt("salt" + number).
                build();
    }

    private Word word(UUID userId,
                      String value,
                      String note,
                      int interval) {
        return Word.newBuilder(validator).
                setUserId(userId).
                setValue(value).
                setNote(note).
                setRepeatData(new RepeatDataFromEnglish(interval, LocalDate.now(clock))).
                setRepeatData(new RepeatDataFromNative(interval, LocalDate.now(clock))).
                build();
    }


    private Expression expression(UUID userId,
                                  String value,
                                  String note,
                                  int interval) {
        return Expression.newBuilder(validator).
                setUserId(userId).
                setValue(value).
                setNote(note).
                setRepeatData(new RepeatDataFromEnglish(interval, LocalDate.now(clock))).
                setRepeatData(new RepeatDataFromNative(interval, LocalDate.now(clock))).
                build();
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
        } catch (Throwable e) {
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
        } catch (Throwable e) {
            transactionManager.rollback(status);
            throw new RuntimeException(e);
        }
    }

}