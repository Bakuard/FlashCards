package com.bakuard.flashcards.dal;

import com.bakuard.flashcards.config.SpringConfig;
import com.bakuard.flashcards.model.RepeatData;
import com.bakuard.flashcards.model.credential.User;
import com.bakuard.flashcards.model.word.Word;
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
class WordsRepositoryTest {

    @Autowired
    private WordsRepository wordsRepository;
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
            save(word):
             there are not words in DB with such value
             => success save word
            """)
    public void save() {
        User user = user(1);
        userRepository.save(user);
        Word expected = word(
                user.getId(), "value 1", "note 1", new RepeatData(1, LocalDate.now())
        );

        wordsRepository.save(expected);

        Word actual = wordsRepository.findById(expected.getId()).orElseThrow();
        org.assertj.core.api.Assertions.
                assertThat(expected).
                usingRecursiveComparison().
                isEqualTo(actual);
    }

    @Test
    @DisplayName("""
            findById(userId, wordId):
             there is not word with such id
             => return empty Optional
            """)
    public void findById1() {
        User user = user(1);
        userRepository.save(user);
        Word expected = word(
                user.getId(), "value 1", "note 1", new RepeatData(1, LocalDate.now())
        );
        wordsRepository.save(expected);

        Optional<Word> actual = wordsRepository.findById(user.getId(), toUUID(1));

        Assertions.assertTrue(actual.isEmpty());
    }

    @Test
    @DisplayName("""
            findById(userId, wordId):
             there is word with such id
             => return correct word
            """)
    public void findById2() {
        User user = user(1);
        userRepository.save(user);
        Word expected = word(
                user.getId(), "value 1", "note 1", new RepeatData(1, LocalDate.now())
        );
        wordsRepository.save(expected);

        Word actual = wordsRepository.findById(user.getId(), expected.getId()).orElseThrow();

        org.assertj.core.api.Assertions.
                assertThat(expected).
                usingRecursiveComparison().
                isEqualTo(actual);
    }

    @Test
    @DisplayName("""
            findByValue(userId, value):
             there is not word with such value
             => return empty optional
            """)
    public void findByValue1() {
        User user = user(1);
        userRepository.save(user);
        Word expected = word(
                user.getId(), "value 1", "note 1", new RepeatData(1, LocalDate.now())
        );
        wordsRepository.save(expected);

        Optional<Word> actual = wordsRepository.findByValue(user.getId(), "Unknown value");

        Assertions.assertTrue(actual.isEmpty());
    }

    @Test
    @DisplayName("""
            findByValue(userId, value):
             there is word with such value
             => return correct word
            """)
    public void findByValue2() {
        User user = user(1);
        userRepository.save(user);
        Word expected = word(
                user.getId(), "value 1", "note 1", new RepeatData(1, LocalDate.now())
        );
        wordsRepository.save(expected);

        Word actual = wordsRepository.findByValue(user.getId(), "value 1").orElseThrow();

        org.assertj.core.api.Assertions.
                assertThat(expected).
                usingRecursiveComparison().
                isEqualTo(actual);
    }

    @Test
    @DisplayName("""
            deleteById(userId, wordId):
             there is not word with such wordId
             => do nothing
            """)
    public void deleteById1() {
        User user = user(1);
        userRepository.save(user);
        Word expected = word(
                user.getId(), "value 1", "note 1", new RepeatData(1, LocalDate.now())
        );
        wordsRepository.save(expected);

        wordsRepository.deleteById(user.getId(), toUUID(1));

        Assertions.assertTrue(wordsRepository.existsById(expected.getId()));
    }

    @Test
    @DisplayName("""
            deleteById(userId, wordId):
             there is word with such wordId
             => delete this word
            """)
    public void deleteById2() {
        User user = user(1);
        userRepository.save(user);
        Word expected = word(
                user.getId(), "value 1", "note 1", new RepeatData(1, LocalDate.now())
        );
        wordsRepository.save(expected);

        wordsRepository.deleteById(user.getId(), expected.getId());

        Assertions.assertFalse(wordsRepository.existsById(expected.getId()));
    }

    @Test
    @DisplayName("""
            existsById(userId, wordId):
             there is not word with such wordId
             => return false
            """)
    public void existsById1() {
        User user = user(1);
        userRepository.save(user);
        Word expected = word(
                user.getId(), "value 1", "note 1", new RepeatData(1, LocalDate.now())
        );
        wordsRepository.save(expected);

        Assertions.assertFalse(wordsRepository.existsById(user.getId(), toUUID(1)));
    }

    @Test
    @DisplayName("""
            existsById(userId, wordId):
             there is word with such wordId
             => return true
            """)
    public void existsById2() {
        User user = user(1);
        userRepository.save(user);
        Word expected = word(
                user.getId(), "value 1", "note 1", new RepeatData(1, LocalDate.now())
        );
        wordsRepository.save(expected);

        Assertions.assertTrue(wordsRepository.existsById(user.getId(), expected.getId()));
    }

    @Test
    @DisplayName("""
            count(user):
             user haven't any words
             => return 0
            """)
    public void count1() {
        User user1 = userRepository.save(user(1));
        User user2 = userRepository.save(user(2));
        User user3 = userRepository.save(user(3));
        wordsRepository.save(word(user1.getId(), "value1", "note1", defaultRepeatData()));
        wordsRepository.save(word(user2.getId(), "value2", "note2", defaultRepeatData()));

        long actual = wordsRepository.count(user3.getId());

        Assertions.assertEquals(0, actual);
    }

    @Test
    @DisplayName("""
            count(user):
             user have some words
             => return correct result
            """)
    public void count2() {
        User user1 = userRepository.save(user(1));
        User user2 = userRepository.save(user(2));
        User user3 = userRepository.save(user(3));
        wordsRepository.save(word(user1.getId(), "value1", "note1", defaultRepeatData()));
        wordsRepository.save(word(user2.getId(), "value2", "note2", defaultRepeatData()));
        wordsRepository.save(word(user3.getId(), "value3", "note3", defaultRepeatData()));
        wordsRepository.save(word(user3.getId(), "value4", "note4", defaultRepeatData()));

        long actual = wordsRepository.count(user3.getId());

        Assertions.assertEquals(2, actual);
    }

    @Test
    @DisplayName("""
            countForRepeat(userId, date):
             user with such id have some words for repeat with date
             => return correct result
            """)
    public void countForRepeat1() {
        User user1 = userRepository.save(user(1));
        wordsRepository.save(word(user1.getId(), "value1", "note1",
                new RepeatData(1, LocalDate.of(2022, 7, 7))));
        wordsRepository.save(word(user1.getId(), "value2", "note2",
                new RepeatData(3, LocalDate.of(2022, 7, 7))));
        wordsRepository.save(word(user1.getId(), "value3", "note3",
                new RepeatData(1, LocalDate.of(2022, 7, 10))));
        wordsRepository.save(word(user1.getId(), "value4", "note4",
                new RepeatData(10, LocalDate.of(2022, 7, 7))));

        long actual = wordsRepository.countForRepeat(
                user1.getId(), LocalDate.of(2022, 7, 10)
        );

        Assertions.assertEquals(2, actual);
    }

    @Test
    @DisplayName("""
            countForRepeat(userId, date):
             user haven't any words
             => return 0
            """)
    public void countForRepeat2() {
        User user1 = userRepository.save(user(1));

        long actual = wordsRepository.countForRepeat(
                user1.getId(), LocalDate.of(2022, 7, 10)
        );

        Assertions.assertEquals(0, actual);
    }

    @Test
    @DisplayName("""
            countForRepeat(userId, date):
             user have words, but haven't any words to repeat
             => return 0
            """)
    public void countForRepeat3() {
        User user1 = userRepository.save(user(1));
        wordsRepository.save(word(user1.getId(), "value1", "note1",
                new RepeatData(1, LocalDate.of(2022, 7, 7))));
        wordsRepository.save(word(user1.getId(), "value2", "note2",
                new RepeatData(3, LocalDate.of(2022, 7, 7))));
        wordsRepository.save(word(user1.getId(), "value3", "note3",
                new RepeatData(1, LocalDate.of(2022, 7, 10))));
        wordsRepository.save(word(user1.getId(), "value4", "note4",
                new RepeatData(10, LocalDate.of(2022, 7, 7))));

        long actual = wordsRepository.countForRepeat(
                user1.getId(), LocalDate.of(2022, 7, 7)
        );

        Assertions.assertEquals(0, actual);
    }

    @Test
    @DisplayName("""
            findByUserId(userId, pageable):
             user haven't any words
             => return empty page
            """)
    public void findByUserId1() {
        User user1 = userRepository.save(user(1));

        Page<Word> actual = wordsRepository.findByUserId(user1.getId(), PageRequest.of(0, 20));

        Assertions.assertTrue(actual.isEmpty());
    }

    @Test
    @DisplayName("""
            findByUserId(userId, pageable):
             user have some words,
             sort words by value descending
             => return correct result
            """)
    public void findByUserId2() {
        User user = userRepository.save(user(1));
        List<Word> words = words(user.getId());
        words.forEach(word -> wordsRepository.save(word));

        Page<Word> actual = wordsRepository.findByUserId(user.getId(),
                PageRequest.of(0, 20, Sort.by("value").descending()));

        List<Word> expected = words.stream().
                sorted(Comparator.comparing(Word::getValue).reversed()).
                toList();
        Assertions.assertEquals(expected, actual.getContent());
    }

    @Test
    @DisplayName("""
            findByUserId(userId, pageable):
             user have some words,
             sort words by interval asc and value asc
             => return correct result
            """)
    public void findByUserId3() {
        User user = userRepository.save(user(1));
        List<Word> words = words(user.getId());
        words.forEach(word -> wordsRepository.save(word));

        Page<Word> actual = wordsRepository.findByUserId(user.getId(),
                PageRequest.of(0, 20, Sort.by("repeat_interval", "value")));

        List<Word> expected = words.stream().
                sorted(Comparator.comparing((Word w) -> w.getRepeatData().interval()).
                        thenComparing(Word::getValue)).
                toList();
        Assertions.assertEquals(expected, actual.getContent());
    }

    @Test
    @DisplayName("""
            findAllForRepeat(userId, date):
             user haven't any words
             => return empty page
            """)
    public void findAllForRepeat1() {
        User user = userRepository.save(user(1));

        List<Word> actual = wordsRepository.findAllForRepeat(
                user.getId(),
                LocalDate.of(2022, 7, 10),
                20, 0
        );

        Assertions.assertTrue(actual.isEmpty());
    }

    @Test
    @DisplayName("""
            findAllForRepeat(userId, date):
             user have some words,
             there are not words for repeat
             => return empty page
            """)
    public void findAllForRepeat2() {
        User user = userRepository.save(user(1));
        List<Word> words = words(user.getId());
        words.forEach(word -> wordsRepository.save(word));

        List<Word> actual = wordsRepository.findAllForRepeat(
                user.getId(),
                LocalDate.of(2022, 7, 1),
                20, 0
        );

        Assertions.assertTrue(actual.isEmpty());
    }

    @Test
    @DisplayName("""
            findAllForRepeat(userId, date):
             user have some words,
             there are words for repeat
             => return correct result
            """)
    public void findAllForRepeat3() {
        User user = userRepository.save(user(1));
        List<Word> words = words(user.getId());
        words.forEach(word -> wordsRepository.save(word));

        LocalDate repeatDate = LocalDate.of(2022, 7, 10);
        List<Word> actual = wordsRepository.findAllForRepeat(
                user.getId(),
                repeatDate,
                2, 0
        );

        List<Word> expected = words.stream().
                sorted(Comparator.comparing((Word w) -> w.getRepeatData().nextDateOfRepeat())).
                filter(w -> w.getRepeatData().nextDateOfRepeat().compareTo(repeatDate) <= 0).
                limit(2).
                toList();
        Assertions.assertEquals(expected, actual);
    }

    @Test
    @DisplayName("""
            replaceRepeatInterval(userId, oldInterval, newInterval):
             there are not words with oldInterval
             => do nothing
            """)
    public void replaceRepeatInterval1() {
        User user = userRepository.save(user(1));
        List<Word> expected = words(user.getId());
        expected.forEach(word -> wordsRepository.save(word));

        wordsRepository.replaceRepeatInterval(user.getId(), 5, 10);

        List<Word> actual = wordsRepository.findByUserId(user.getId(),
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
             there are words with oldInterval
             => replace them to newInterval
            """)
    public void replaceRepeatInterval2() {
        User user = userRepository.save(user(1));
        List<Word> expected = words(user.getId());
        expected.forEach(word -> wordsRepository.save(word));

        wordsRepository.replaceRepeatInterval(user.getId(), 1, 10);

        List<Word> actual = wordsRepository.findByUserId(user.getId(),
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

    private List<Word> words(UUID userId) {
        ArrayList<Word> words = new ArrayList<>();

        words.add(
                word(userId, "wordA", "noteA",
                        new RepeatData(1, LocalDate.of(2022, 7, 1))
                )
        );
        words.add(
                word(userId, "wordB", "noteB",
                        new RepeatData(1, LocalDate.of(2022, 7, 2))
                )
        );
        words.add(
                word(userId, "wordC", "noteB",
                        new RepeatData(3, LocalDate.of(2022, 7, 6))
                )
        );
        words.add(
                word(userId, "wordD", "noteD",
                        new RepeatData(3, LocalDate.of(2022, 7, 7))
                )
        );
        words.add(
                word(userId, "wordE", "noteE",
                        new RepeatData(3, LocalDate.of(2022, 7, 8))
                )
        );
        words.add(
                word(userId, "wordF", "noteF",
                        new RepeatData(1, LocalDate.of(2022, 7, 10))
                )
        );

        return words;
    }

    private RepeatData defaultRepeatData() {
        return new RepeatData(1, LocalDate.of(2022, 7, 7));
    }

}