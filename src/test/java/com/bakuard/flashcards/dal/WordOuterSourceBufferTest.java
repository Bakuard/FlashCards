package com.bakuard.flashcards.dal;

import com.bakuard.flashcards.config.MutableClock;
import com.bakuard.flashcards.config.SpringConfig;
import com.bakuard.flashcards.config.TestConfig;
import com.bakuard.flashcards.model.auth.credential.Credential;
import com.bakuard.flashcards.model.auth.credential.User;
import com.bakuard.flashcards.model.word.Word;
import com.bakuard.flashcards.model.word.WordExample;
import com.bakuard.flashcards.model.word.WordInterpretation;
import com.bakuard.flashcards.model.word.WordTranscription;
import com.bakuard.flashcards.model.word.WordTranslation;
import com.bakuard.flashcards.model.word.supplementation.SupplementedWord;
import com.bakuard.flashcards.model.word.supplementation.SupplementedWordExample;
import com.bakuard.flashcards.validation.exception.NotUniqueEntityException;
import com.bakuard.flashcards.validation.exception.UnknownEntityException;
import org.assertj.core.api.Assertions;
import org.assertj.core.api.InstanceOfAssertFactories;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.function.Executable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.jdbc.JdbcTestUtils;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Supplier;

@ExtendWith(SpringExtension.class)
@TestPropertySource(locations = "classpath:test.properties")
@Import({SpringConfig.class, TestConfig.class})
class WordOuterSourceBufferTest {

    @Autowired
    private WordRepository wordRepository;
    @Autowired
    private WordOuterSourceBuffer wordOuterSourceBuffer;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private JdbcTemplate jdbcTemplate;
    @Autowired
    private DataSourceTransactionManager transactionManager;
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
                "repeat_expressions_from_native_statistic",
                "word_outer_source",
                "words_examples_outer_source"
        ));
        clock.setDate(2022, 7, 7);
    }

    @Test
    @DisplayName("""
            save(word):
             word is null
             => exception
            """)
    public void save1() {
        Assertions.assertThatNullPointerException().
                isThrownBy(() -> commit(() -> wordOuterSourceBuffer.save(null)));
    }

    @Test
    @DisplayName("""
            save(word):
             word is new,
             word with this value and outerSource already exists in DB
             => exception
            """)
    public void save2() {
        User user = commit(() -> userRepository.save(user(1)));
        Word word = commit(() -> wordRepository.save(word(user.getId(), "wordA", "noteA", 1)));
        commit(() -> wordOuterSourceBuffer.save(supplementedWord("outerSource1", word)));

        Assertions.assertThatExceptionOfType(NotUniqueEntityException.class).
                isThrownBy(() -> commit(() ->
                    wordOuterSourceBuffer.save(supplementedWord("outerSource1", word))
                ));
    }

    @Test
    @DisplayName("""
            save(word):
             word is not new,
             word with this value and outerSource not exists in DB
             => exception
            """)
    public void save3() {
        User user = commit(() -> userRepository.save(user(1)));
        Word word = commit(() -> wordRepository.save(word(user.getId(), "wordA", "noteA", 1)));
        commit(() -> wordOuterSourceBuffer.save(supplementedWord("outerSource1", word)));
        SupplementedWord supplementedWord = supplementedWord("outerSource2", word);
        commit(() -> wordOuterSourceBuffer.save(supplementedWord));

        Assertions.assertThatExceptionOfType(UnknownEntityException.class).
                isThrownBy(() -> commit(() ->
                        wordOuterSourceBuffer.save(supplementedWord.setOuterSourceName("outerSource1"))
                ));
    }

    @Test
    @DisplayName("""
            deleteUnusedExamples():
             outer source buffer contains examples for deleted user
             => remove this examples
            """)
    public void deleteUnusedExamples1() {
        User user = commit(() -> userRepository.save(user(1)));
        commit(() -> {
            Word word = word(user.getId(), "wordA", "noteA", 1);
            wordOuterSourceBuffer.save(supplementedWord("outerSource1", word));
            wordOuterSourceBuffer.save(supplementedWord("outerSource2", word));
            wordOuterSourceBuffer.save(supplementedWord("outerSource3", word));
            wordRepository.save(word);
        });

        commit(() -> {
           userRepository.deleteById(user.getId());
           wordOuterSourceBuffer.deleteUnusedExamples();
        });

        SoftAssertions assertions = new SoftAssertions();
        assertions.assertThat(wordOuterSourceBuffer.findByWordValueAndOuterSource(
                "outerSource1", "wordA", user.getId())).
                isPresent().
                get().extracting(SupplementedWord::getExamples, InstanceOfAssertFactories.LIST).
                isEmpty();
        assertions.assertThat(wordOuterSourceBuffer.findByWordValueAndOuterSource(
                        "outerSource2", "wordA", user.getId())).
                isPresent().
                get().extracting(SupplementedWord::getExamples, InstanceOfAssertFactories.LIST).
                isEmpty();
        assertions.assertThat(wordOuterSourceBuffer.findByWordValueAndOuterSource(
                        "outerSource3", "wordA", user.getId())).
                isPresent().
                get().extracting(SupplementedWord::getExamples, InstanceOfAssertFactories.LIST).
                isEmpty();
        assertions.assertAll();
    }

    @Test
    @DisplayName("""
            deleteUnusedExamples():
             outer source buffer contains examples for deleted word
             => remove this examples
            """)
    public void deleteUnusedExamples2() {
        User user = commit(() -> userRepository.save(user(1)));
        commit(() -> {
            Word word = word(user.getId(), "wordA", "noteA", 1);
            wordOuterSourceBuffer.save(supplementedWord("outerSource1", word));
            wordOuterSourceBuffer.save(supplementedWord("outerSource2", word));
            wordOuterSourceBuffer.save(supplementedWord("outerSource3", word));
            wordRepository.save(word);
        });
        Word wordB = commit(() -> {
            Word word = word(user.getId(), "wordB", "noteA", 1);
            wordOuterSourceBuffer.save(supplementedWord("outerSource1", word));
            wordOuterSourceBuffer.save(supplementedWord("outerSource2", word));
            wordOuterSourceBuffer.save(supplementedWord("outerSource3", word));
            return wordRepository.save(word);
        });

        commit(() -> {
            wordRepository.deleteById(user.getId(), wordB.getId());
            wordOuterSourceBuffer.deleteUnusedExamples();
        });

        SoftAssertions assertions = new SoftAssertions();
        assertions.assertThat(wordOuterSourceBuffer.findByWordValueAndOuterSource(
                        "outerSource1", "wordB", user.getId())).
                isPresent().
                get().extracting(SupplementedWord::getExamples, InstanceOfAssertFactories.LIST).
                isEmpty();
        assertions.assertThat(wordOuterSourceBuffer.findByWordValueAndOuterSource(
                        "outerSource2", "wordB", user.getId())).
                isPresent().
                get().extracting(SupplementedWord::getExamples, InstanceOfAssertFactories.LIST).
                isEmpty();
        assertions.assertThat(wordOuterSourceBuffer.findByWordValueAndOuterSource(
                        "outerSource3", "wordB", user.getId())).
                isPresent().
                get().extracting(SupplementedWord::getExamples, InstanceOfAssertFactories.LIST).
                isEmpty();
        assertions.assertAll();
    }

    @Test
    @DisplayName("""
            deleteUnusedExamples():
             outer source buffer contains examples for deleted word examples
             => remove this examples
            """)
    public void deleteUnusedExamples3() {
        User user = commit(() -> userRepository.save(user(1)));
        Word wordA = commit(() -> {
            Word word = word(user.getId(), "wordA", "noteA", 1);
            wordOuterSourceBuffer.save(supplementedWord("outerSource1", word));
            wordOuterSourceBuffer.save(supplementedWord("outerSource2", word).
                    removeExampleBy("exampleB"));
            wordOuterSourceBuffer.save(supplementedWord("outerSource3", word));
            return wordRepository.save(word);
        });

        wordA.removeExampleBy("exampleA").
                removeExampleBy("exampleC");
        commit(() -> {
            wordRepository.save(wordA);
            wordOuterSourceBuffer.deleteUnusedExamples();
        });

        SoftAssertions assertions = new SoftAssertions();
        assertions.assertThat(wordOuterSourceBuffer.findByWordValueAndOuterSource(
                        "outerSource1", "wordA", user.getId())).
                isPresent().
                get().extracting(SupplementedWord::getExamples, InstanceOfAssertFactories.LIST).
                containsExactly(supplementedWordExample("exampleB", "translateB", "outerSource1"));
        assertions.assertThat(wordOuterSourceBuffer.findByWordValueAndOuterSource(
                        "outerSource2", "wordA", user.getId())).
                isPresent().
                get().extracting(SupplementedWord::getExamples, InstanceOfAssertFactories.LIST).
                isEmpty();
        assertions.assertThat(wordOuterSourceBuffer.findByWordValueAndOuterSource(
                        "outerSource3", "wordA", user.getId())).
                isPresent().
                get().extracting(SupplementedWord::getExamples, InstanceOfAssertFactories.LIST).
                containsExactly(supplementedWordExample("exampleB", "translateB", "outerSource3"));
        assertions.assertAll();
    }

    @Test
    @DisplayName("""
            deleteUnusedExamples():
             outer source buffer contains examples for deleted user
             => doesn't remove examples of existed users
            """)
    public void deleteUnusedExamples4() {
        User userA = commit(() -> userRepository.save(user(1)));
        User userB = commit(() -> userRepository.save(user(2)));
        commit(() -> {
            Word word = word(userA.getId(), "wordA", "noteA", 1);
            wordOuterSourceBuffer.save(supplementedWord("outerSource1", word));
            wordOuterSourceBuffer.save(supplementedWord("outerSource2", word));
            wordOuterSourceBuffer.save(supplementedWord("outerSource3", word));
            wordRepository.save(word);
        });
        commit(() -> {
            Word word = word(userB.getId(), "wordB", "noteA", 1);
            wordOuterSourceBuffer.save(supplementedWord("outerSource1", word));
            wordOuterSourceBuffer.save(supplementedWord("outerSource2", word));
            wordOuterSourceBuffer.save(supplementedWord("outerSource3", word));
            wordRepository.save(word);
        });

        commit(() -> {
            userRepository.deleteById(userA.getId());
            wordOuterSourceBuffer.deleteUnusedExamples();
        });

        SoftAssertions assertions = new SoftAssertions();
        assertions.assertThat(wordOuterSourceBuffer.findByWordValueAndOuterSource(
                        "outerSource1", "wordB", userB.getId())).
                isPresent().
                get().extracting(SupplementedWord::getExamples, InstanceOfAssertFactories.LIST).
                containsExactly(
                        supplementedWordExample("exampleA", "translateA", "outerSource1"),
                        supplementedWordExample("exampleB", "translateB", "outerSource1"),
                        supplementedWordExample("exampleC", "translateC", "outerSource1")
                );
        assertions.assertThat(wordOuterSourceBuffer.findByWordValueAndOuterSource(
                        "outerSource2", "wordB", userB.getId())).
                isPresent().
                get().extracting(SupplementedWord::getExamples, InstanceOfAssertFactories.LIST).
                containsExactly(
                        supplementedWordExample("exampleA", "translateA", "outerSource2"),
                        supplementedWordExample("exampleB", "translateB", "outerSource2"),
                        supplementedWordExample("exampleC", "translateC", "outerSource2")
                );
        assertions.assertThat(wordOuterSourceBuffer.findByWordValueAndOuterSource(
                        "outerSource3", "wordB", userB.getId())).
                isPresent().
                get().extracting(SupplementedWord::getExamples, InstanceOfAssertFactories.LIST).
                containsExactly(
                        supplementedWordExample("exampleA", "translateA", "outerSource3"),
                        supplementedWordExample("exampleB", "translateB", "outerSource3"),
                        supplementedWordExample("exampleC", "translateC", "outerSource3")
                );
        assertions.assertAll();
    }

    @Test
    @DisplayName("""
            deleteUnusedExamples():
             outer source buffer contains examples for deleted word
             => doesn't remove examples of existed words
            """)
    public void deleteUnusedExamples5() {
        User user = commit(() -> userRepository.save(user(1)));
        Word wordA = commit(() -> {
            Word word = word(user.getId(), "wordA", "noteA", 1);
            wordOuterSourceBuffer.save(supplementedWord("outerSource1", word));
            wordOuterSourceBuffer.save(supplementedWord("outerSource2", word));
            wordOuterSourceBuffer.save(supplementedWord("outerSource3", word));
            return wordRepository.save(word);
        });
        Word wordB = commit(() -> {
            Word word = word(user.getId(), "wordB", "noteA", 1);
            wordOuterSourceBuffer.save(supplementedWord("outerSource1", word));
            wordOuterSourceBuffer.save(supplementedWord("outerSource2", word));
            wordOuterSourceBuffer.save(supplementedWord("outerSource3", word));
            return wordRepository.save(word);
        });

        commit(() -> {
            wordRepository.deleteById(user.getId(), wordA.getId());
            wordOuterSourceBuffer.deleteUnusedExamples();
        });

        SoftAssertions assertions = new SoftAssertions();
        assertions.assertThat(wordOuterSourceBuffer.findByWordValueAndOuterSource(
                        "outerSource1", "wordB", user.getId())).
                isPresent().
                get().extracting(SupplementedWord::getExamples, InstanceOfAssertFactories.LIST).
                containsExactly(
                        supplementedWordExample("exampleA", "translateA", "outerSource1"),
                        supplementedWordExample("exampleB", "translateB", "outerSource1"),
                        supplementedWordExample("exampleC", "translateC", "outerSource1")
                );
        assertions.assertThat(wordOuterSourceBuffer.findByWordValueAndOuterSource(
                        "outerSource2", "wordB", user.getId())).
                isPresent().
                get().extracting(SupplementedWord::getExamples, InstanceOfAssertFactories.LIST).
                containsExactly(
                        supplementedWordExample("exampleA", "translateA", "outerSource2"),
                        supplementedWordExample("exampleB", "translateB", "outerSource2"),
                        supplementedWordExample("exampleC", "translateC", "outerSource2")
                );
        assertions.assertThat(wordOuterSourceBuffer.findByWordValueAndOuterSource(
                        "outerSource3", "wordB", user.getId())).
                isPresent().
                get().extracting(SupplementedWord::getExamples, InstanceOfAssertFactories.LIST).
                containsExactly(
                        supplementedWordExample("exampleA", "translateA", "outerSource3"),
                        supplementedWordExample("exampleB", "translateB", "outerSource3"),
                        supplementedWordExample("exampleC", "translateC", "outerSource3")
                );
        assertions.assertAll();
    }

    @Test
    @DisplayName("""
            deleteUnusedExamples():
             there are not examples for delete
             => return 0
            """)
    public void deleteUnusedExamples6() {
        User user = commit(() -> userRepository.save(user(1)));
        commit(() -> {
            Word word = word(user.getId(), "wordA", "noteA", 1);
            wordOuterSourceBuffer.save(supplementedWord("outerSource1", word));
            wordOuterSourceBuffer.save(supplementedWord("outerSource2", word));
            wordOuterSourceBuffer.save(supplementedWord("outerSource3", word));
            wordRepository.save(word);
        });

        int actual = commit(() -> wordOuterSourceBuffer.deleteUnusedExamples());

        Assertions.assertThat(actual).isZero();
    }

    @Test
    @DisplayName("""
            deleteUnusedExamples():
             there are examples for delete
             => return correct result
            """)
    public void deleteUnusedExamples7() {
        User user = commit(() -> userRepository.save(user(1)));
        Word wordA = commit(() -> {
            Word word = word(user.getId(), "wordA", "noteA", 1);
            wordOuterSourceBuffer.save(supplementedWord("outerSource1", word));
            wordOuterSourceBuffer.save(supplementedWord("outerSource2", word));
            wordOuterSourceBuffer.save(supplementedWord("outerSource3", word));
            return wordRepository.save(word);
        });
        Word wordB = commit(() -> {
            Word word = word(user.getId(), "wordB", "noteA", 1);
            wordOuterSourceBuffer.save(supplementedWord("outerSource1", word));
            wordOuterSourceBuffer.save(supplementedWord("outerSource2", word));
            wordOuterSourceBuffer.save(supplementedWord("outerSource3", word));
            return wordRepository.save(word);
        });

        int actual = commit(() -> {
            wordRepository.deleteById(wordA.getId());
            wordRepository.deleteById(wordB.getId());
            return wordOuterSourceBuffer.deleteUnusedExamples();
        });

        Assertions.assertThat(actual).isEqualTo(18);
    }

    @Test
    @DisplayName("""
            findByWordValueAndOuterSource(outerSourceName, wordValue, examplesOwnerId):
             outerSourceName is null
             => exception
            """)
    public void findByWordValueAndOuterSource1() {
        User user = commit(() -> userRepository.save(user(1)));

        Assertions.assertThatNullPointerException().
                isThrownBy(() -> wordOuterSourceBuffer.findByWordValueAndOuterSource(
                        null, "wordA", user.getId()));
    }

    @Test
    @DisplayName("""
            findByWordValueAndOuterSource(outerSourceName, wordValue, examplesOwnerId):
             wordValue is null
             => exception
            """)
    public void findByWordValueAndOuterSource2() {
        User user = commit(() -> userRepository.save(user(1)));

        Assertions.assertThatNullPointerException().
                isThrownBy(() -> wordOuterSourceBuffer.findByWordValueAndOuterSource(
                        "outerSource", null, user.getId()));
    }

    @Test
    @DisplayName("""
            findByWordValueAndOuterSource(outerSourceName, wordValue, examplesOwnerId):
             examplesOwnerId is null
             => exception
            """)
    public void findByWordValueAndOuterSource3() {
        Assertions.assertThatNullPointerException().
                isThrownBy(() -> wordOuterSourceBuffer.findByWordValueAndOuterSource(
                        "outerSource", "word", null));
    }
    
    @Test
    @DisplayName("""
            findByWordValueAndOuterSource(outerSourceName, wordValue, examplesOwnerId):
             buffer not contains data from outer source with outerSourceName,
             buffer contains data for word with wordValue,
             buffer contains examples for user with examplesOwnerId
             => empty Optional
            """)
    public void findByWordValueAndOuterSource4() {
        User user = commit(() -> userRepository.save(user(1)));
        Word word = commit(() -> wordRepository.save(word(user.getId(), "wordA", "noteA", 1)));
        commit(() -> wordOuterSourceBuffer.save(supplementedWord("outerSource1", word)));

        Optional<SupplementedWord> actual = wordOuterSourceBuffer.findByWordValueAndOuterSource(
                "outerSource2", "wordA", user.getId());

        Assertions.assertThat(actual).isEmpty();
    }

    @Test
    @DisplayName("""
            findByWordValueAndOuterSource(outerSourceName, wordValue, examplesOwnerId):
             buffer contains data from outer source with outerSourceName,
             buffer not contains data for word with wordValue,
             buffer not contains examples for user with examplesOwnerId
             => empty Optional
            """)
    public void findByWordValueAndOuterSource5() {
        User user = userRepository.save(user(1));
        Word word = commit(() -> wordRepository.save(word(user.getId(), "wordA", "noteA", 1)));
        commit(() -> wordOuterSourceBuffer.save(supplementedWord("outerSource1", word)));

        Optional<SupplementedWord> actual = wordOuterSourceBuffer.findByWordValueAndOuterSource(
                "outerSource1", "wordB", user.getId());

        Assertions.assertThat(actual).isEmpty();
    }

    @Test
    @DisplayName("""
            findByWordValueAndOuterSource(outerSourceName, wordValue, examplesOwnerId):
             buffer contains data from outer source with outerSourceName,
             buffer contains data for word with wordValue,
             buffer not contains examples for user with examplesOwnerId
             => return SupplementedWord without examples
            """)
    public void findByWordValueAndOuterSource6() {
        User user = commit(() -> userRepository.save(user(1)));
        User userWithoutData = commit(() -> userRepository.save(user(2)));
        Word word = commit(() -> wordRepository.save(word(user.getId(), "wordA", "noteA", 1)));
        SupplementedWord supplementedWord = supplementedWord("outerSource1", word);
        commit(() -> wordOuterSourceBuffer.save(supplementedWord));

        Optional<SupplementedWord> actual = wordOuterSourceBuffer.findByWordValueAndOuterSource(
                "outerSource1", "wordA", userWithoutData.getId());

        Assertions.assertThat(actual).
                isPresent().
                get().usingRecursiveComparison().
                isEqualTo(
                        new SupplementedWord(
                                supplementedWord.getId(),
                                userWithoutData.getId(),
                                word.getValue(),
                                "outerSource1",
                                LocalDate.now(clock),
                                toUri("outerSource1", word.getValue())).
                            addInterpretations(word.getInterpretations()).
                            addTranscriptions(word.getTranscriptions().stream().
                                map(i -> new WordTranscription(i.getValue(), null)).
                                toList()).
                            addTranslations(word.getTranslations().stream().
                                map(i -> new WordTranslation(i.getValue(), null)).
                                toList())
                );
    }

    @Test
    @DisplayName("""
            findByWordValueAndOuterSource(outerSourceName, wordValue, examplesOwnerId):
             buffer contains data from outer source with outerSourceName,
             buffer contains data for word with wordValue,
             buffer contains examples for user with examplesOwnerId
             => return correct data
            """)
    public void findByWordValueAndOuterSource7() {
        User user = commit(() -> userRepository.save(user(1)));
        Word word = commit(() -> wordRepository.save(word(user.getId(), "wordA", "noteA", 1)));
        SupplementedWord expected = supplementedWord("outerSource1", word);
        commit(() -> wordOuterSourceBuffer.save(expected));

        Optional<SupplementedWord> actual = wordOuterSourceBuffer.findByWordValueAndOuterSource(
                "outerSource1", "wordA", user.getId());

        Assertions.assertThat(actual).
                isPresent().
                get().usingRecursiveComparison().
                isEqualTo(expected);
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
                      int interval) {
        return new Word(userId, interval, interval, clock).
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
                addExample(new WordExample("exampleA", "translateA", "noteA")).
                addExample(new WordExample("exampleB", "translateB", "noteB")).
                addExample(new WordExample("exampleC", "translateC", "noteC"));
    }

    private SupplementedWord supplementedWord(String outerSourceName,
                                              Word word) {
        return new SupplementedWord(
                    word.getUserId(),
                    word.getValue(),
                    outerSourceName,
                    LocalDate.now(clock),
                    toUri(outerSourceName, word.getValue())).
                addInterpretations(word.getInterpretations()).
                addTranscriptions(word.getTranscriptions().stream().
                        map(i -> new WordTranscription(i.getValue(), null)).
                        toList()).
                addTranslations(word.getTranslations().stream().
                        map(i -> new WordTranslation(i.getValue(), null)).
                        toList()).
                addExamples(word.getExamples().stream().
                        map(i -> supplementedWordExample(i.getOrigin(), i.getTranslate(), outerSourceName)).
                        toList());
    }

    private SupplementedWordExample supplementedWordExample(String example,
                                                            String translate,
                                                            String outerSourceName) {
        return new SupplementedWordExample(
                example,
                translate,
                null,
                toUri(outerSourceName, example)
        );
    }

    private URI toUri(String outerSourceName, String value) {
        try {
            return new URI("https://" + outerSourceName + '/' +
                    URLEncoder.encode(value, StandardCharsets.UTF_8));
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
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
        } catch(Throwable e) {
            transactionManager.rollback(status);
            throw new RuntimeException(e);
        }
    }
    
}