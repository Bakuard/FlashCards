package com.bakuard.flashcards.dal;

import com.bakuard.flashcards.config.MutableClock;
import com.bakuard.flashcards.config.TestConfig;
import com.bakuard.flashcards.model.RepeatDataFromEnglish;
import com.bakuard.flashcards.model.RepeatDataFromNative;
import com.bakuard.flashcards.model.auth.credential.User;
import com.bakuard.flashcards.model.word.*;
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
@Import(TestConfig.class)
class WordRepositoryTest {

    @Autowired
    private WordRepository wordRepository;
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
                "repeat_expressions_from_native_statistic"
        ));
        clock.setDate(2022, 7, 7);
    }

    @Test
    @DisplayName("""
            save(word):
             there are not words in DB with such value
             => success save word
            """)
    public void save() {
        User user = user(1);
        commit(() -> userRepository.save(user));
        Word expected = word(user.getId(), "value 1", "note 1", 1);

        commit(() -> wordRepository.save(expected));

        Word actual = wordRepository.findById(expected.getId()).orElseThrow();
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
        commit(() -> userRepository.save(user));
        Word expected = word(user.getId(), "value 1", "note 1", 1);
        commit(() -> wordRepository.save(expected));

        Optional<Word> actual = wordRepository.findById(user.getId(), toUUID(1));

        Assertions.assertThat(actual).isEmpty();
    }

    @Test
    @DisplayName("""
            findById(userId, wordId):
             there is word with such id
             => return correct word
            """)
    public void findById2() {
        User user = user(1);
        commit(() -> userRepository.save(user));
        Word expected = word(user.getId(), "value 1", "note 1", 1);
        commit(() -> wordRepository.save(expected));

        Word actual = wordRepository.findById(user.getId(), expected.getId()).orElseThrow();

        org.assertj.core.api.Assertions.
                assertThat(expected).
                usingRecursiveComparison().
                isEqualTo(actual);
    }

    @Test
    @DisplayName("""
            countForValue(userId, value, maxDistance):
             user with userId hasn't any words
             => return 0
            """)
    public void countForValue1() {
        User user = commit(() -> userRepository.save(user(1)));

        long actual = wordRepository.countForValue(user.getId(), "value", 2);

        Assertions.assertThat(actual).isZero();
    }

    @Test
    @DisplayName("""
            countForValue(userId, value, maxDistance):
             user has some words,
             there are not words with maxDistance <= 2
             => return 0
            """)
    public void countForValue2() {
        User user = commit(() -> {
            User temp = userRepository.save(user(1));
            wordRepository.save(word(temp.getId(), "value", "note", 1));
            wordRepository.save(word(temp.getId(), "cock", "note", 3));
            wordRepository.save(word(temp.getId(), "rise", "note", 5));
            wordRepository.save(word(temp.getId(), "value1234", "note", 10));
            return temp;
        });

        long actual = wordRepository.countForValue(user.getId(), "cockroach", 2);

        Assertions.assertThat(actual).isZero();
    }

    @Test
    @DisplayName("""
            countForValue(userId, value, maxDistance):
             user has some words,
             there are words with maxDistance <= 2
             => return correct result
            """)
    public void countForValue3() {
        User user = commit(() -> {
            User temp = userRepository.save(user(1));
            wordRepository.save(word(temp.getId(), "frog", "note", 1));
            wordRepository.save(word(temp.getId(), "frog1", "note", 3));
            wordRepository.save(word(temp.getId(), "broom", "note", 5));
            wordRepository.save(word(temp.getId(), "distance", "note", 10));
            return temp;
        });

        long actual = wordRepository.countForValue(user.getId(), "frog", 2);

        Assertions.assertThat(actual).isEqualTo(2);
    }

    @Test
    @DisplayName("""
            findByValue(userId, value, maxDistance, limit, offset):
             user with userId hasn't any words
             return empty list
            """)
    public void findByValue1() {
        User user = commit(() -> userRepository.save(user(1)));

        List<Word> actual = wordRepository.findByValue(
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
             user has some words,
             there are not words with maxDistance <= 2
             return empty list
            """)
    public void findByValue2() {
        User user = commit(() -> {
            User temp = userRepository.save(user(1));
            wordRepository.save(word(temp.getId(), "value", "note", 1));
            wordRepository.save(word(temp.getId(), "cock", "note", 3));
            wordRepository.save(word(temp.getId(), "rise", "note", 5));
            wordRepository.save(word(temp.getId(), "value1234", "note", 10));
            return temp;
        });

        List<Word> actual = wordRepository.findByValue(
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
             user has some words,
             there are words with maxDistance <= 2
             => return correct result
            """)
    public void findByValue3() {
        User user = commit(() -> userRepository.save(user(1)));
        List<Word> words = List.of(
                word(user.getId(), "frog", "note", 1),
                word(user.getId(), "frog1", "note", 3),
                word(user.getId(), "broom", "note", 5),
                word(user.getId(), "distance", "note", 10)
        );
        commit(() -> words.forEach(word -> wordRepository.save(word)));

        List<Word> actual = wordRepository.findByValue(
                user.getId(),
                "frog",
                2,
                10,
                0
        );

        Assertions.assertThat(actual).
                usingRecursiveFieldByFieldElementComparator().
                containsExactly(words.get(0), words.get(1));
    }

    @Test
    @DisplayName("""
            deleteById(userId, wordId):
             there is not word with such wordId
             => do nothing
            """)
    public void deleteById1() {
        User user = user(1);
        commit(() -> userRepository.save(user));
        Word expected = word(user.getId(), "value 1", "note 1", 1);
        commit(() -> wordRepository.save(expected));

        commit(() -> wordRepository.deleteById(user.getId(), toUUID(1)));

        Assertions.assertThat(wordRepository.existsById(expected.getId())).isTrue();
    }

    @Test
    @DisplayName("""
            deleteById(userId, wordId):
             there is word with such wordId
             => delete this word
            """)
    public void deleteById2() {
        User user = user(1);
        commit(() -> userRepository.save(user));
        Word expected = word(user.getId(), "value 1", "note 1", 1);
        commit(() -> wordRepository.save(expected));

        commit(() -> wordRepository.deleteById(user.getId(), expected.getId()));

        Assertions.assertThat(wordRepository.existsById(expected.getId())).isFalse();
    }

    @Test
    @DisplayName("""
            existsById(userId, wordId):
             there is not word with such wordId
             => return false
            """)
    public void existsById1() {
        User user = user(1);
        commit(() -> userRepository.save(user));
        Word expected = word(user.getId(), "value 1", "note 1", 1);
        commit(() -> wordRepository.save(expected));

        Assertions.assertThat(wordRepository.existsById(user.getId(), toUUID(1))).isFalse();
    }

    @Test
    @DisplayName("""
            existsById(userId, wordId):
             there is word with such wordId
             => return true
            """)
    public void existsById2() {
        User user = user(1);
        commit(() -> userRepository.save(user));
        Word expected = word(user.getId(), "value 1", "note 1", 1);
        commit(() -> wordRepository.save(expected));

        Assertions.assertThat(wordRepository.existsById(user.getId(), expected.getId())).isTrue();
    }

    @Test
    @DisplayName("""
            count(user):
             user haven't any words
             => return 0
            """)
    public void count1() {
        User user1 = commit(() -> userRepository.save(user(1)));
        User user2 = commit(() -> userRepository.save(user(2)));
        User user3 = commit(() -> userRepository.save(user(3)));
        commit(() -> {
            wordRepository.save(word(user1.getId(), "value1", "note1", 1));
            wordRepository.save(word(user2.getId(), "value2", "note2", 1));
        });

        long actual = wordRepository.count(user3.getId());

        Assertions.assertThat(actual).isZero();
    }

    @Test
    @DisplayName("""
            count(user):
             user have some words
             => return correct result
            """)
    public void count2() {
        User user1 = commit(() -> userRepository.save(user(1)));
        User user2 = commit(() -> userRepository.save(user(2)));
        User user3 = commit(() -> userRepository.save(user(3)));
        commit(() -> {
            wordRepository.save(word(user1.getId(), "value1", "note1", 1));
            wordRepository.save(word(user2.getId(), "value2", "note2", 1));
            wordRepository.save(word(user3.getId(), "value3", "note3", 1));
            wordRepository.save(word(user3.getId(), "value4", "note4", 1));
        });

        long actual = wordRepository.count(user3.getId());

        Assertions.assertThat(actual).isEqualTo(2);
    }

    @Test
    @DisplayName("""
            countForRepeatFromEnglish(userId, date):
             user with such id have some words for repeat with date
             => return correct result
            """)
    public void countForRepeatFromEnglish1() {
        User user = commit(() -> userRepository.save(user(1)));
        commit(() -> {
            clock.setDate(2022, 7, 7);
            wordRepository.save(
                    word(user.getId(), "value1", "note1", 1)
            );
            clock.setDate(2022, 7, 7);
            wordRepository.save(
                    word(user.getId(), "value2", "note2", 3)
            );
            clock.setDate(2022, 7, 10);
            wordRepository.save(
                    word(user.getId(), "value3", "note3", 1)
            );
            clock.setDate(2022, 7, 7);
            wordRepository.save(
                    word(user.getId(), "value4", "note4", 10)
            );
        });

        long actual = wordRepository.countForRepeatFromEnglish(
                user.getId(), LocalDate.of(2022, 7, 10)
        );

        Assertions.assertThat(actual).isEqualTo(2);
    }

    @Test
    @DisplayName("""
            countForRepeatFromEnglish(userId, date):
             user haven't any words
             => return 0
            """)
    public void countForRepeatFromEnglish2() {
        User user = commit(() -> userRepository.save(user(1)));

        long actual = wordRepository.countForRepeatFromEnglish(
                user.getId(), LocalDate.of(2022, 7, 10)
        );

        Assertions.assertThat(actual).isZero();
    }

    @Test
    @DisplayName("""
            countForRepeatFromEnglish(userId, date):
             user have words, but haven't any words to repeat
             => return 0
            """)
    public void countForRepeatFromEnglish3() {
        User user = commit(() -> userRepository.save(user(1)));
        commit(() -> {
            clock.setDate(2022, 7, 7);
            wordRepository.save(
                    word(user.getId(), "value1", "note1", 1)
            );
            clock.setDate(2022, 7, 7);
            wordRepository.save(
                    word(user.getId(), "value2", "note2", 3)
            );
            clock.setDate(2022, 7, 10);
            wordRepository.save(
                    word(user.getId(), "value3", "note3", 1)
            );
            clock.setDate(2022, 7, 7);
            wordRepository.save(
                    word(user.getId(), "value4", "note4", 10)
            );
        });

        long actual = wordRepository.countForRepeatFromEnglish(
                user.getId(), LocalDate.of(2022, 7, 7)
        );

        Assertions.assertThat(actual).isZero();
    }

    @Test
    @DisplayName("""
            countForRepeatFromNative(userId, date):
             user with such id have some words for repeat with date
             => return correct result
            """)
    public void countForRepeatFromNative1() {
        User user = commit(() -> userRepository.save(user(1)));
        commit(() -> {
            clock.setDate(2022, 7, 7);
            wordRepository.save(
                    word(user.getId(), "value1", "note1", 1)
            );
            clock.setDate(2022, 7, 7);
            wordRepository.save(
                    word(user.getId(), "value2", "note2", 3)
            );
            clock.setDate(2022, 7, 10);
            wordRepository.save(
                    word(user.getId(), "value3", "note3", 1)
            );
            clock.setDate(2022, 7, 7);
            wordRepository.save(
                    word(user.getId(), "value4", "note4", 10)
            );
        });

        long actual = wordRepository.countForRepeatFromNative(
                user.getId(), LocalDate.of(2022, 7, 10)
        );

        Assertions.assertThat(actual).isEqualTo(2);
    }

    @Test
    @DisplayName("""
            countForRepeatFromNative(userId, date):
             user haven't any words
             => return 0
            """)
    public void countForRepeatFromNative2() {
        User user = commit(() -> userRepository.save(user(1)));

        long actual = wordRepository.countForRepeatFromNative(
                user.getId(), LocalDate.of(2022, 7, 10)
        );

        Assertions.assertThat(actual).isZero();
    }

    @Test
    @DisplayName("""
            countForRepeatFromNative(userId, date):
             user have words, but haven't any words to repeat
             => return 0
            """)
    public void countForRepeatFromNative3() {
        User user = commit(() -> userRepository.save(user(1)));
        commit(() -> {
            clock.setDate(2022, 7, 7);
            wordRepository.save(
                    word(user.getId(), "value1", "note1", 1)
            );
            clock.setDate(2022, 7, 7);
            wordRepository.save(
                    word(user.getId(), "value2", "note2", 3)
            );
            clock.setDate(2022, 7, 10);
            wordRepository.save(
                    word(user.getId(), "value3", "note3", 1)
            );
            clock.setDate(2022, 7, 7);
            wordRepository.save(
                    word(user.getId(), "value4", "note4", 10)
            );
        });

        long actual = wordRepository.countForRepeatFromNative(
                user.getId(), LocalDate.of(2022, 7, 7)
        );

        Assertions.assertThat(actual).isZero();
    }

    @Test
    @DisplayName("""
            findByUserId(userId, pageable):
             user haven't any words
             => return empty page
            """)
    public void findByUserId1() {
        User user = commit(() -> userRepository.save(user(1)));

        Page<Word> actual = wordRepository.findByUserId(user.getId(), PageRequest.of(0, 20));

        Assertions.assertThat(actual).isEmpty();
    }

    @Test
    @DisplayName("""
            findByUserId(userId, pageable):
             user have some words,
             sort words by value descending
             => return correct result
            """)
    public void findByUserId2() {
        User user = commit(() -> userRepository.save(user(1)));
        List<Word> words = words(user.getId());
        commit(() -> words.forEach(word -> wordRepository.save(word)));

        Page<Word> actual = wordRepository.findByUserId(user.getId(),
                PageRequest.of(0, 20, Sort.by("value").descending()));

        List<Word> expected = words.stream().
                sorted(Comparator.comparing(Word::getValue).reversed()).
                toList();
        Assertions.assertThat(actual.getContent()).
                usingRecursiveFieldByFieldElementComparator().
                isEqualTo(expected);
    }

    @Test
    @DisplayName("""
            findByUserId(userId, pageable):
             user have some words,
             sort words by interval asc and value asc
             => return correct result
            """)
    public void findByUserId3() {
        User user = commit(() -> userRepository.save(user(1)));
        List<Word> words = words(user.getId());
        commit(() -> words.forEach(word -> wordRepository.save(word)));

        Page<Word> actual = wordRepository.findByUserId(user.getId(),
                PageRequest.of(0, 20, Sort.by("repeat_interval_from_english", "value")));

        List<Word> expected = words.stream().
                sorted(Comparator.comparing((Word w) -> w.getRepeatDataFromEnglish().interval()).
                        thenComparing(Word::getValue)).
                toList();
        Assertions.assertThat(actual.getContent()).
                usingRecursiveFieldByFieldElementComparator().
                isEqualTo(expected);
    }

    @Test
    @DisplayName("""
            findAllForRepeatFromEnglish(userId, date):
             user haven't any words
             => return empty page
            """)
    public void findAllForRepeatFromEnglish1() {
        User user = commit(() -> userRepository.save(user(1)));

        List<Word> actual = wordRepository.findAllForRepeatFromEnglish(
                user.getId(),
                LocalDate.of(2022, 7, 10),
                20, 0
        );

        Assertions.assertThat(actual).isEmpty();
    }

    @Test
    @DisplayName("""
            findAllForRepeatFromEnglish(userId, date):
             user have some words,
             there are not words for repeat
             => return empty page
            """)
    public void findAllForRepeatFromEnglish2() {
        User user = commit(() -> userRepository.save(user(1)));
        List<Word> words = words(user.getId());
        commit(() -> words.forEach(word -> wordRepository.save(word)));

        List<Word> actual = wordRepository.findAllForRepeatFromEnglish(
                user.getId(),
                LocalDate.of(2022, 7, 1),
                20, 0
        );

        Assertions.assertThat(actual).isEmpty();
    }

    @Test
    @DisplayName("""
            findAllForRepeatFromEnglish(userId, date):
             user have some words,
             there are words for repeat
             => return correct result
            """)
    public void findAllForRepeatFromEnglish3() {
        User user = commit(() -> userRepository.save(user(1)));
        List<Word> words = words(user.getId());
        commit(() -> words.forEach(word -> wordRepository.save(word)));

        LocalDate repeatDate = LocalDate.of(2022, 7, 10);
        List<Word> actual = wordRepository.findAllForRepeatFromEnglish(
                user.getId(),
                repeatDate,
                2, 0
        );

        List<Word> expected = words.stream().
                sorted(Comparator.comparing((Word w) -> w.getRepeatDataFromEnglish().nextDateOfRepeat())).
                filter(w -> w.getRepeatDataFromEnglish().nextDateOfRepeat().compareTo(repeatDate) <= 0).
                limit(2).
                toList();
        Assertions.assertThat(actual).
                usingRecursiveFieldByFieldElementComparator().
                isEqualTo(expected);
    }

    @Test
    @DisplayName("""
            findAllForRepeatFromNative(userId, date):
             user haven't any words
             => return empty page
            """)
    public void findAllForRepeatFromNative1() {
        User user = commit(() -> userRepository.save(user(1)));

        List<Word> actual = wordRepository.findAllForRepeatFromNative(
                user.getId(),
                LocalDate.of(2022, 7, 10),
                20, 0
        );

        Assertions.assertThat(actual).isEmpty();
    }

    @Test
    @DisplayName("""
            findAllForRepeatFromNative(userId, date):
             user have some words,
             there are not words for repeat
             => return empty page
            """)
    public void findAllForRepeatFromNative2() {
        User user = commit(() -> userRepository.save(user(1)));
        List<Word> words = words(user.getId());
        commit(() -> words.forEach(word -> wordRepository.save(word)));

        List<Word> actual = wordRepository.findAllForRepeatFromNative(
                user.getId(),
                LocalDate.of(2022, 7, 1),
                20, 0
        );

        Assertions.assertThat(actual).isEmpty();
    }

    @Test
    @DisplayName("""
            findAllForRepeatFromNative(userId, date):
             user have some words,
             there are words for repeat
             => return correct result
            """)
    public void findAllForRepeatFromNative3() {
        User user = commit(() -> userRepository.save(user(1)));
        List<Word> words = words(user.getId());
        commit(() -> words.forEach(word -> wordRepository.save(word)));

        LocalDate repeatDate = LocalDate.of(2022, 7, 10);
        List<Word> actual = wordRepository.findAllForRepeatFromNative(
                user.getId(),
                repeatDate,
                2, 0
        );

        List<Word> expected = words.stream().
                sorted(Comparator.comparing((Word w) -> w.getRepeatDataFromEnglish().nextDateOfRepeat())).
                filter(w -> w.getRepeatDataFromEnglish().nextDateOfRepeat().compareTo(repeatDate) <= 0).
                limit(2).
                toList();
        Assertions.assertThat(actual).
                usingRecursiveFieldByFieldElementComparator().
                isEqualTo(expected);
    }

    @Test
    @DisplayName("""
            countForTranslate(userId, translate):
             user with userId hasn't any words
             => return 0
            """)
    public void countForTranslate1() {
        User user = commit(() -> userRepository.save(user(1)));

        long actual = wordRepository.countForTranslate(user.getId(), "unknown translate");

        Assertions.assertThat(actual).isZero();
    }

    @Test
    @DisplayName("""
            countForTranslate(userId, translate):
             user has some words,
             user hasn't words with this translate
             => return correct result
            """)
    public void countForTranslate2() {
        User user = commit(() -> userRepository.save(user(1)));
        commit(() -> {
            wordRepository.save(word(user.getId(), "wordA", "noteA", 10));
            wordRepository.save(word(user.getId(), "wordB", "noteB", 10));
            wordRepository.save(word(user.getId(), "wordC", "noteC", 10));
        });

        long actual = wordRepository.countForTranslate(user.getId(), "unknown translate");

        Assertions.assertThat(actual).isZero();
    }

    @Test
    @DisplayName("""
            countForTranslate(userId, translate):
             user has some words,
             user has words with this translate
             => return correct result
            """)
    public void countForTranslate3() {
        User user = commit(() -> userRepository.save(user(1)));
        commit(() -> {
            wordRepository.save(word(user.getId(), "wordA", "noteA", 10));
            wordRepository.save(word(user.getId(), "wordB", "noteB", 10));
            wordRepository.save(word(user.getId(), "wordC", "noteC", 10));
            wordRepository.save(
                    new Word(user.getId(), 1, clock).
                            setValue("wordD").
                            setNote("noteD").
                            addTranslation(new WordTranslation("translateX", "noteX"))
            );
            wordRepository.save(
                    new Word(user.getId(), 1, clock).
                            setValue("wordE").
                            setNote("noteE").
                            addTranslation(new WordTranslation("translateX", "noteX"))
            );
        });

        long actual = wordRepository.countForTranslate(user.getId(), "translateX");

        Assertions.assertThat(actual).isEqualTo(2);
    }

    @Test
    @DisplayName("""
            findByTranslate(userId, translate, limit, offset):
             user with userId hasn't any words
             => return empty list
            """)
    public void findByTranslate1() {
        User user = commit(() -> userRepository.save(user(1)));

        List<Word> actual = wordRepository.findByTranslate(user.getId(), "translateX", 10, 0);

        Assertions.assertThat(actual).isEmpty();
    }

    @Test
    @DisplayName("""
            findByTranslate(userId, translate, limit, offset):
             user with userId has some words,
             user hasn't any words with this translate
             => return empty list
            """)
    public void findByTranslate2() {
        User user = commit(() -> userRepository.save(user(1)));
        commit(() -> {
            wordRepository.save(word(user.getId(), "wordA", "noteA", 1));
            wordRepository.save(word(user.getId(), "wordB", "noteB", 1));
            wordRepository.save(word(user.getId(), "wordC", "noteC", 1));
        });

        List<Word> actual = wordRepository.findByTranslate(user.getId(), "translateX", 10, 0);

        Assertions.assertThat(actual).isEmpty();
    }

    @Test
    @DisplayName("""
            findByTranslate(userId, translate, limit, offset):
             user with userId has some words,
             user has words with this translate
             => return empty list
            """)
    public void findByTranslate3() {
        User user = commit(() -> userRepository.save(user(1)));
        Word wordD = new Word(user.getId(), 1, clock).
                setValue("wordD").
                setNote("noteD").
                addTranslation(new WordTranslation("translateX", "noteX"));
        Word wordE = new Word(user.getId(), 1, clock).
                setValue("wordE").
                setNote("noteE").
                addTranslation(new WordTranslation("translateX", "noteX"));
        Word wordF = new Word(user.getId(), 1, clock).
                setValue("wordF").
                setNote("noteF").
                addTranslation(new WordTranslation("translateX", "noteX"));
        commit(() -> {
            wordRepository.save(word(user.getId(), "wordA", "noteA", 10));
            wordRepository.save(word(user.getId(), "wordB", "noteB", 10));
            wordRepository.save(word(user.getId(), "wordC", "noteC", 10));
            wordRepository.save(wordF);
            wordRepository.save(wordE);
            wordRepository.save(wordD);
        });

        List<Word> actual = wordRepository.findByTranslate(user.getId(), "translateX", 10, 0);

        Assertions.assertThat(actual).containsExactly(wordD, wordE, wordF);
    }

    @Test
    @DisplayName("""
            getWordIndexByFirstCharacter(userId, firstCharacter):
             user with userId hasn't any words
             => return -1;
            """)
    public void getWordIndexByFirstCharacter1() {
        User user = commit(() -> userRepository.save(user(1)));

        long actual = wordRepository.getWordIndexByFirstCharacter(user.getId(), "A");

        Assertions.assertThat(actual).isEqualTo(-1);
    }

    @Test
    @DisplayName("""
            getWordIndexByFirstCharacter(userId, firstCharacter):
             user with userId has some words,
             user hasn't any words with first character = firstCharacter
             return -1
            """)
    public void getWordIndexByFirstCharacter2() {
        User user = commit(() -> userRepository.save(user(1)));
        commit(() -> {
            wordRepository.save(word(user.getId(), "B word", null, 1));
            wordRepository.save(word(user.getId(), "b word2", null, 1));
            wordRepository.save(word(user.getId(), "C word", null, 1));
            wordRepository.save(word(user.getId(), "c word2", null, 1));
            wordRepository.save(word(user.getId(), "F word", null, 1));
            wordRepository.save(word(user.getId(), "J word", null, 1));
            wordRepository.save(word(user.getId(), "k word", null, 1));
            wordRepository.save(word(user.getId(), "Z word", null, 1));
        });

        long actual = wordRepository.getWordIndexByFirstCharacter(user.getId(), "A");

        Assertions.assertThat(actual).isEqualTo(-1);
    }

    @Test
    @DisplayName("""
            getWordIndexByFirstCharacter(userId, firstCharacter):
             user with userId has some words,
             user has words with first character = firstCharacter
             => return index first word with first character = firstCharacter
            """)
    public void getWordIndexByFirstCharacter3() {
        User user = commit(() -> userRepository.save(user(1)));
        commit(() -> {
            wordRepository.save(word(user.getId(), "A word", null, 1));
            wordRepository.save(word(user.getId(), "B word", null, 1));
            wordRepository.save(word(user.getId(), "C word", null, 1));
            wordRepository.save(word(user.getId(), "D word", null, 1));
            wordRepository.save(word(user.getId(), "E word", null, 1));
            wordRepository.save(word(user.getId(), "F word", null, 1));
            wordRepository.save(word(user.getId(), "G word", null, 1));
            wordRepository.save(word(user.getId(), "H word", null, 1));
            wordRepository.save(word(user.getId(), "I word", null, 1));
            wordRepository.save(word(user.getId(), "J word", null, 1));
            wordRepository.save(word(user.getId(), "K word", null, 1));
            wordRepository.save(word(user.getId(), "L word", null, 1));
            wordRepository.save(word(user.getId(), "M word", null, 1));
            wordRepository.save(word(user.getId(), "m word2", null, 1));
            wordRepository.save(word(user.getId(), "M word3", null, 1));
            wordRepository.save(word(user.getId(), "m word4", null, 1));
            wordRepository.save(word(user.getId(), "N word", null, 1));
            wordRepository.save(word(user.getId(), "O word", null, 1));
            wordRepository.save(word(user.getId(), "P word", null, 1));
            wordRepository.save(word(user.getId(), "Q word", null, 1));
            wordRepository.save(word(user.getId(), "R word", null, 1));
            wordRepository.save(word(user.getId(), "S word", null, 1));
            wordRepository.save(word(user.getId(), "T word", null, 1));
            wordRepository.save(word(user.getId(), "U word", null, 1));
            wordRepository.save(word(user.getId(), "V word", null, 1));
            wordRepository.save(word(user.getId(), "W word", null, 1));
            wordRepository.save(word(user.getId(), "X word", null, 1));
            wordRepository.save(word(user.getId(), "Y word", null, 1));
            wordRepository.save(word(user.getId(), "Z word", null, 1));
        });

        long actual = wordRepository.getWordIndexByFirstCharacter(user.getId(), "m");

        Assertions.assertThat(actual).isEqualTo(12);
    }


    private UUID toUUID(int number) {
        return UUID.fromString("00000000-0000-0000-0000-" + String.format("%012d", number));
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
        return new Word(userId, interval, clock).
                setValue(value).
                setNote(note).
                addTranslation(new WordTranslation("translateA", "noteA")).
                addTranslation(new WordTranslation("translateB", "noteB")).
                addTranslation(new WordTranslation("translateC", "noteC")).
                addTranscription(new WordTranscription("transcriptionA", "noteA")).
                addTranscription(new WordTranscription("transcriptionB", "noteB")).
                addTranscription(new WordTranscription("transcriptionC", "noteC")).
                addInterpretation(new WordInterpretation("interpretationA")).
                addInterpretation(new WordInterpretation("interpretationB")).
                addInterpretation(new WordInterpretation("interpretationC")).
                addExample(new WordExample("exampleA", "exampleTranslate", "noteA")).
                addExample(new WordExample("exampleB", "exampleTranslate", "noteB")).
                addExample(new WordExample("exampleC", "exampleTranslate", "noteC"));
    }

    private List<Word> words(UUID userId) {
        ArrayList<Word> words = new ArrayList<>();

        clock.setDate(2022, 7, 1);
        words.add(
                word(userId, "wordA", "noteA", 1)
        );
        clock.setDate(2022, 7, 2);
        words.add(
                word(userId, "wordB", "noteB", 1)
        );
        clock.setDate(2022, 7, 6);
        words.add(
                word(userId, "wordC", "noteB", 3)
        );
        clock.setDate(2022, 7, 7);
        words.add(
                word(userId, "wordD", "noteD", 3)
        );
        clock.setDate(2022, 7, 8);
        words.add(
                word(userId, "wordE", "noteE", 3)
        );
        clock.setDate(2022, 7, 10);
        words.add(
                word(userId, "wordF", "noteF", 1)
        );

        return words;
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