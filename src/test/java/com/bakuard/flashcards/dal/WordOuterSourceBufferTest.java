package com.bakuard.flashcards.dal;

import com.bakuard.flashcards.config.MutableClock;
import com.bakuard.flashcards.config.SpringConfig;
import com.bakuard.flashcards.config.TestConfig;
import com.bakuard.flashcards.model.RepeatDataFromEnglish;
import com.bakuard.flashcards.model.RepeatDataFromNative;
import com.bakuard.flashcards.model.auth.credential.Credential;
import com.bakuard.flashcards.model.auth.credential.User;
import com.bakuard.flashcards.model.word.*;
import org.assertj.core.api.Assertions;
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

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
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
                "words_interpretations_outer_source",
                "words_transcriptions_outer_source",
                "words_translations_outer_source",
                "words_examples_outer_source"
        ));
        clock.setDate(2022, 7, 7);
    }

    @Test
    @DisplayName("""
            save(word):
             there are not words in DB with such value
             => success save word
            """)
    public void save1() {
        User user = user(1);
        commit(() -> userRepository.save(user));
        Word expected = word(user.getId(), "value 1", "note 1", 1);

        commit(() -> wordRepository.save(expected));

        Word actual = wordRepository.findById(expected.getId()).orElseThrow();
        Assertions.
                assertThat(expected).
                usingRecursiveComparison().
                isEqualTo(actual);
    }

    @Test
    @DisplayName("""
            deleteUnusedOuterSourceExamples():
             outer source buffer contains examples for deleted user
             => remove this examples
            """)
    public void deleteUnusedOuterSourceExamples1() {
        User user = commit(() -> userRepository.save(user(1)));
        Word wordA = commit(() -> {
            Word word = emptyWord(user.getId(), "wordA", "noteA", 1).
                    addExample(new WordExample("exampleA", "translateA", null).
                            addSourceInfo(exampleOuterSource("source1", "translate1"))).
                    addExample(new WordExample("exampleB", "translateB", null).
                            addSourceInfo(exampleOuterSource("source1", "translate1")).
                            addSourceInfo(exampleOuterSource("source2", "translate2")).
                            addSourceInfo(exampleOuterSource("source3", "translate3"))).
                    addExample(new WordExample("exampleC", "translateC", null).
                            addSourceInfo(exampleOuterSource("source5", "translate5")).
                            addSourceInfo(exampleOuterSource("source6", "translate6")));
            wordOuterSourceBuffer.saveDataFromOuterSource(word);
            wordRepository.save(word);
            return word;
        });
        Word wordB = commit(() -> {
            Word word = emptyWord(user.getId(), "wordB", "noteB", 1).
                    addExample(new WordExample("exampleA", "translateA", null).
                            addSourceInfo(exampleOuterSource("source1", "translate1"))).
                    addExample(new WordExample("exampleD", "translateD", null).
                            addSourceInfo(exampleOuterSource("source10", "translate10")).
                            addSourceInfo(exampleOuterSource("source20", "translate20")).
                            addSourceInfo(exampleOuterSource("source3", "translate3"))).
                    addExample(new WordExample("exampleE", "translateE", null).
                            addSourceInfo(exampleOuterSource("source5", "translate5")).
                            addSourceInfo(exampleOuterSource("source7", "translate7")));
            wordOuterSourceBuffer.saveDataFromOuterSource(word);
            wordRepository.save(word);
            return word;
        });

        commit(() -> {
            userRepository.deleteById(user.getId());
            wordOuterSourceBuffer.deleteUnusedOuterSourceExamples();
        });

        Word actualA = emptyWord(wordA.getId(), user.getId(), "wordA", "noteA", 1).
                addExample(new WordExample("exampleA", "translateA", "noteA")).
                addExample(new WordExample("exampleB", "translateB", "noteB")).
                addExample(new WordExample("exampleC", "translateC", "noteC"));
        Word actualB = emptyWord(wordB.getId(), user.getId(), "wordA", "noteA", 1).
                addExample(new WordExample("exampleA", "translateA", "noteA")).
                addExample(new WordExample("exampleD", "translateD", "noteD")).
                addExample(new WordExample("exampleE", "translateE", "noteE"));
        wordOuterSourceBuffer.mergeFromOuterSource(actualA);
        wordOuterSourceBuffer.mergeFromOuterSource(actualB);
        SoftAssertions softAssertions = new SoftAssertions();
        softAssertions.assertThat(actualA.getExamples()).
                extracting(example -> example.getSourceInfo().size()).
                containsExactly(0, 0, 0);
        softAssertions.assertThat(actualB.getExamples()).
                extracting(example -> example.getSourceInfo().size()).
                containsExactly(0, 0, 0);
        softAssertions.assertAll();
    }

    @Test
    @DisplayName("""
            deleteUnusedOuterSourceExamples():
             outer source buffer contains examples for deleted word
             => remove this examples
            """)
    public void deleteUnusedOuterSourceExamples2() {
        User user = commit(() -> userRepository.save(user(1)));
        Word word = emptyWord(user.getId(), "wordA", "noteA", 1).
                addExample(new WordExample("exampleA", "translateA", null).
                        addSourceInfo(exampleOuterSource("source1", "translate1"))).
                addExample(new WordExample("exampleB", "translateB", null).
                        addSourceInfo(exampleOuterSource("source1", "translate1")).
                        addSourceInfo(exampleOuterSource("source2", "translate2")).
                        addSourceInfo(exampleOuterSource("source3", "translate3"))).
                addExample(new WordExample("exampleC", "translateC", null).
                        addSourceInfo(exampleOuterSource("source5", "translate5")).
                        addSourceInfo(exampleOuterSource("source6", "translate6")));
        commit(() -> {
            wordOuterSourceBuffer.saveDataFromOuterSource(word);
            wordRepository.save(word);
        });

        commit(() -> {
            wordRepository.deleteById(user.getId(), word.getId());
            wordOuterSourceBuffer.deleteUnusedOuterSourceExamples();
        });

        Word actual = emptyWord(word.getId(), user.getId(), "wordA", "noteA", 1).
                addExample(new WordExample("exampleA", "translateA", "noteA")).
                addExample(new WordExample("exampleB", "translateB", "noteB")).
                addExample(new WordExample("exampleC", "translateC", "noteC"));
        wordOuterSourceBuffer.mergeFromOuterSource(actual);
        Assertions.assertThat(actual.getExamples()).
                containsExactly(
                        new WordExample("exampleA", "translateA", "noteA"),
                        new WordExample("exampleB", "translateB", "noteB"),
                        new WordExample("exampleC", "translateC", "noteC")
                );
    }

    @Test
    @DisplayName("""
            deleteUnusedOuterSourceExamples():
             outer source buffer contains examples for deleted word example
             => remove this examples
            """)
    public void deleteUnusedOuterSourceExamples3() {
        User user = commit(() -> userRepository.save(user(1)));
        Word word = emptyWord(user.getId(), "wordA", "noteA", 1).
                addExample(new WordExample("exampleA", "translateA", null).
                        addSourceInfo(exampleOuterSource("source1", "translate1"))).
                addExample(new WordExample("exampleB", "translateB", null).
                        addSourceInfo(exampleOuterSource("source1", "translate1")).
                        addSourceInfo(exampleOuterSource("source2", "translate2")).
                        addSourceInfo(exampleOuterSource("source3", "translate3"))).
                addExample(new WordExample("exampleC", "translateC", null).
                        addSourceInfo(exampleOuterSource("source5", "translate5")).
                        addSourceInfo(exampleOuterSource("source6", "translate6")));
        commit(() -> {
            wordOuterSourceBuffer.saveDataFromOuterSource(word);
            wordRepository.save(word);
        });

        commit(() -> {
            wordRepository.save(
                    word.setExamples(List.of(
                            new WordExample("exampleA", "translateA", null),
                            new WordExample("exampleC", "translateC", null)
                    ))
            );
            wordOuterSourceBuffer.deleteUnusedOuterSourceExamples();
        });

        Word actual = emptyWord(word.getId(), user.getId(), "wordA", "noteA", 1).
                addExample(new WordExample("exampleA", "translateA", "noteA")).
                addExample(new WordExample("exampleB", "translateB", "noteB")).
                addExample(new WordExample("exampleC", "translateC", "noteC"));
        wordOuterSourceBuffer.mergeFromOuterSource(actual);
        Assertions.assertThat(actual.getExamples()).
                containsExactly(
                        new WordExample("exampleA", "translateA", "noteA").
                                addSourceInfo(exampleOuterSource("source1", "translate1")),
                        new WordExample("exampleB", "translateB", "noteB"),
                        new WordExample("exampleC", "translateC", "noteC").
                                addSourceInfo(exampleOuterSource("source5", "translate5")).
                                addSourceInfo(exampleOuterSource("source6", "translate6"))
                );
    }

    @Test
    @DisplayName("""
            deleteUnusedOuterSourceExamples():
             outer source buffer contains examples for deleted user
             => doesn't remove examples of existed users
            """)
    public void deleteUnusedOuterSourceExamples4() {
        User deletedUser = commit(() -> userRepository.save(user(1)));
        User existedUser = commit(() -> userRepository.save(user(2)));
        commit(() -> {
            Word word = emptyWord(deletedUser.getId(), "wordA", "noteA", 1).
                    addExample(new WordExample("exampleA", "translateA", null).
                            addSourceInfo(exampleOuterSource("source1", "translate1"))).
                    addExample(new WordExample("exampleB", "translateB", null).
                            addSourceInfo(exampleOuterSource("source1", "translate1")).
                            addSourceInfo(exampleOuterSource("source2", "translate2")).
                            addSourceInfo(exampleOuterSource("source3", "translate3"))).
                    addExample(new WordExample("exampleC", "translateC", null).
                            addSourceInfo(exampleOuterSource("source5", "translate5")).
                            addSourceInfo(exampleOuterSource("source6", "translate6")));
            wordOuterSourceBuffer.saveDataFromOuterSource(word);
            wordRepository.save(word);
        });
        Word wordB = commit(() -> {
            Word word = emptyWord(existedUser.getId(), "wordB", "noteB", 1).
                    addExample(new WordExample("exampleA", "translateA", null).
                            addSourceInfo(exampleOuterSource("source1", "translate1"))).
                    addExample(new WordExample("exampleD", "translateD", null).
                            addSourceInfo(exampleOuterSource("source10", "translate10")).
                            addSourceInfo(exampleOuterSource("source20", "translate20")).
                            addSourceInfo(exampleOuterSource("source3", "translate3"))).
                    addExample(new WordExample("exampleE", "translateE", null).
                            addSourceInfo(exampleOuterSource("source5", "translate5")).
                            addSourceInfo(exampleOuterSource("source7", "translate7")));
            wordOuterSourceBuffer.saveDataFromOuterSource(word);
            wordRepository.save(word);
            return word;
        });

        commit(() -> {
            userRepository.deleteById(deletedUser.getId());
            wordOuterSourceBuffer.deleteUnusedOuterSourceExamples();
        });

        Word actual = emptyWord(wordB.getId(), existedUser.getId(), "wordA", "noteA", 1).
                addExample(new WordExample("exampleA", "translateA", "noteA")).
                addExample(new WordExample("exampleD", "translateD", "noteD")).
                addExample(new WordExample("exampleE", "translateE", "noteE"));
        wordOuterSourceBuffer.mergeFromOuterSource(actual);
        Assertions.assertThat(actual.getExamples()).
                containsExactly(
                        new WordExample("exampleA", "translateA", "noteA").
                                addSourceInfo(exampleOuterSource("source1", "translate1")),
                        new WordExample("exampleD", "translateD", "noteD").
                                addSourceInfo(exampleOuterSource("source10", "translate10")).
                                addSourceInfo(exampleOuterSource("source20", "translate20")).
                                addSourceInfo(exampleOuterSource("source3", "translate3")),
                        new WordExample("exampleE", "translateE", "noteE").
                                addSourceInfo(exampleOuterSource("source5", "translate5")).
                                addSourceInfo(exampleOuterSource("source7", "translate7"))
                );
    }

    @Test
    @DisplayName("""
            deleteUnusedOuterSourceExamples():
             outer source buffer contains examples for deleted word
             => doesn't remove examples of existed words
            """)
    public void deleteUnusedOuterSourceExamples5() {
        User user = commit(() -> userRepository.save(user(1)));
        Word deletedWord = commit(() -> {
            Word word = emptyWord(user.getId(), "deletedWord", "note", 1).
                    addExample(new WordExample("exampleA", "translateA", null).
                            addSourceInfo(exampleOuterSource("source1", "translate1"))).
                    addExample(new WordExample("exampleB", "translateB", null).
                            addSourceInfo(exampleOuterSource("source1", "translate1")).
                            addSourceInfo(exampleOuterSource("source2", "translate2")).
                            addSourceInfo(exampleOuterSource("source3", "translate3"))).
                    addExample(new WordExample("exampleC", "translateC", null).
                            addSourceInfo(exampleOuterSource("source5", "translate5")).
                            addSourceInfo(exampleOuterSource("source6", "translate6")));
            wordOuterSourceBuffer.saveDataFromOuterSource(word);
            wordRepository.save(word);
            return word;
        });
        Word existedWord = commit(() -> {
            Word word = emptyWord(user.getId(), "existedWord", "note", 1).
                    addExample(new WordExample("exampleA", "translateA", null).
                            addSourceInfo(exampleOuterSource("source1", "translate1"))).
                    addExample(new WordExample("exampleD", "translateD", null).
                            addSourceInfo(exampleOuterSource("source10", "translate10")).
                            addSourceInfo(exampleOuterSource("source20", "translate20")).
                            addSourceInfo(exampleOuterSource("source3", "translate3"))).
                    addExample(new WordExample("exampleE", "translateE", null).
                            addSourceInfo(exampleOuterSource("source5", "translate5")).
                            addSourceInfo(exampleOuterSource("source7", "translate7")));
            wordOuterSourceBuffer.saveDataFromOuterSource(word);
            wordRepository.save(word);
            return word;
        });

        commit(() -> {
            wordRepository.deleteById(user.getId(), deletedWord.getId());
            wordOuterSourceBuffer.deleteUnusedOuterSourceExamples();
        });

        Word actual = emptyWord(existedWord.getId(), user.getId(), "existedWord", "note", 1).
                addExample(new WordExample("exampleA", "translateA", "noteA")).
                addExample(new WordExample("exampleD", "translateD", "noteD")).
                addExample(new WordExample("exampleE", "translateE", "noteE"));
        wordOuterSourceBuffer.mergeFromOuterSource(actual);
        Assertions.assertThat(actual.getExamples()).
                containsExactly(
                        new WordExample("exampleA", "translateA", "noteA").
                                addSourceInfo(exampleOuterSource("source1", "translate1")),
                        new WordExample("exampleD", "translateD", "noteD").
                                addSourceInfo(exampleOuterSource("source10", "translate10")).
                                addSourceInfo(exampleOuterSource("source20", "translate20")).
                                addSourceInfo(exampleOuterSource("source3", "translate3")),
                        new WordExample("exampleE", "translateE", "noteE").
                                addSourceInfo(exampleOuterSource("source5", "translate5")).
                                addSourceInfo(exampleOuterSource("source7", "translate7"))
                );
    }

    @Test
    @DisplayName("""
            mergeFromOuterSource(word):
             outer source buffer doesn't contains transcriptions,
             outer source buffer doesn't contains interpretations,
             outer source buffer doesn't contains translates
             => don't load transcriptions, interpretations or translates from buffer
            """)
    public void mergeFromOuterSource1() {
        User user = commit(() -> userRepository.save(user(1)));
        commit(() -> wordOuterSourceBuffer.saveDataFromOuterSource(
                word(user.getId(), "wordA", "noteA", 1)
        ));

        Word actual = emptyWord(user.getId(), "wordA", "noteA", 1);
        wordOuterSourceBuffer.mergeFromOuterSource(actual);

        SoftAssertions softAssertions = new SoftAssertions();
        softAssertions.assertThat(actual.getTranslations()).isEmpty();
        softAssertions.assertThat(actual.getTranscriptions()).isEmpty();
        softAssertions.assertThat(actual.getInterpretations()).isEmpty();
        softAssertions.assertAll();
    }

    @Test
    @DisplayName("""
            mergeFromOuterSource(word):
             outer source buffer contains transcriptions,
             filled word doesn't contain any transcriptions
             => load transcriptions from buffer
            """)
    public void mergeFromOuterSource2() {
        User user = commit(() -> userRepository.save(user(1)));
        commit(() -> wordOuterSourceBuffer.saveDataFromOuterSource(
                emptyWord(user.getId(), "wordA", "noteA", 1).
                        addTranscription(new WordTranscription("transcriptionA", "noteA").
                                addSourceInfo(outerSource("source5"))).
                        addTranscription(new WordTranscription("transcriptionB", "noteB")).
                        addTranscription(new WordTranscription("transcriptionC", "noteC").
                                addSourceInfo(outerSource("source1")).
                                addSourceInfo(outerSource("source10")))
        ));

        Word actual = emptyWord(user.getId(), "wordA", "noteA", 1);
        wordOuterSourceBuffer.mergeFromOuterSource(actual);

        Assertions.assertThat(actual.getTranscriptions()).
                containsExactly(
                        new WordTranscription("transcriptionA", null).
                                addSourceInfo(outerSource("source5")),
                        new WordTranscription("transcriptionC", null).
                                addSourceInfo(outerSource("source1")).
                                addSourceInfo(outerSource("source10"))
                );
    }

    @Test
    @DisplayName("""
            mergeFromOuterSource(word):
             outer source buffer contains interpretations,
             filled word doesn't contain any interpretations
             => load interpretations from buffer
            """)
    public void mergeFromOuterSource3() {
        User user = commit(() -> userRepository.save(user(1)));
        commit(() -> wordOuterSourceBuffer.saveDataFromOuterSource(
                emptyWord(user.getId(), "wordA", "noteA", 1).
                        addInterpretation(new WordInterpretation("interpretationA").
                                addSourceInfo(outerSource("source5"))).
                        addInterpretation(new WordInterpretation("interpretationB")).
                        addInterpretation(new WordInterpretation("interpretationC").
                                addSourceInfo(outerSource("source1")).
                                addSourceInfo(outerSource("source10")))
        ));

        Word actual = emptyWord(user.getId(), "wordA", "noteA", 1);
        wordOuterSourceBuffer.mergeFromOuterSource(actual);

        Assertions.assertThat(actual.getInterpretations()).
                containsExactly(
                        new WordInterpretation("interpretationA").
                                addSourceInfo(outerSource("source5")),
                        new WordInterpretation("interpretationC").
                                addSourceInfo(outerSource("source1")).
                                addSourceInfo(outerSource("source10"))
                );
    }

    @Test
    @DisplayName("""
            mergeFromOuterSource(word):
             outer source buffer contains translates,
             filled word doesn't contain any translates
             => load translates from buffer
            """)
    public void mergeFromOuterSource4() {
        User user = commit(() -> userRepository.save(user(1)));
        commit(() -> wordOuterSourceBuffer.saveDataFromOuterSource(
                emptyWord(user.getId(), "wordA", "noteA", 1).
                        addTranslation(new WordTranslation("translateA", "noteA").
                                addSourceInfo(outerSource("source1"))).
                        addTranslation(new WordTranslation("translateB", "noteB")).
                        addTranslation(new WordTranslation("translateC", "noteC").
                                addSourceInfo(outerSource("source1")).
                                addSourceInfo(outerSource("source10")))
        ));

        Word actual = emptyWord(user.getId(), "wordA", "noteA", 1);
        wordOuterSourceBuffer.mergeFromOuterSource(actual);

        Assertions.assertThat(actual.getTranslations()).
                containsExactly(
                        new WordTranslation("translateA", null).
                                addSourceInfo(outerSource("source1")),
                        new WordTranslation("translateC", null).
                                addSourceInfo(outerSource("source1")).
                                addSourceInfo(outerSource("source10"))
                );
    }

    @Test
    @DisplayName("""
            mergeFromOuterSource(word):
             outer source buffer contains examples,
             all buffer examples not match the filled word examples
             => doesn't match examples from buffer
            """)
    public void mergeFromOuterSource5() {
        User user = commit(() -> userRepository.save(user(1)));
        Word word =  emptyWord(user.getId(), "wordA", "noteA", 1).
                addExample(new WordExample("exampleA", "translateA", null).
                        addSourceInfo(exampleOuterSource("source1", "translate1"))).
                addExample(new WordExample("exampleB", "translateB", null)).
                addExample(new WordExample("exampleC", "translateC", null).
                        addSourceInfo(exampleOuterSource("source1", "translate1")).
                        addSourceInfo(exampleOuterSource("source10", "translate10")));
        commit(() -> wordOuterSourceBuffer.saveDataFromOuterSource(word));

        Word actual = emptyWord(word.getId(), user.getId(), "wordA", "noteA", 1).
                addExample(new WordExample("exampleD", "translateD", "noteD")).
                addExample(new WordExample("exampleE", "translateE", "noteE")).
                addExample(new WordExample("exampleF", "translateF", "noteF"));
        wordOuterSourceBuffer.mergeFromOuterSource(actual);

        Assertions.assertThat(actual.getExamples()).
                containsExactly(
                        new WordExample("exampleD", "translateD", "noteD"),
                        new WordExample("exampleE", "translateE", "noteE"),
                        new WordExample("exampleF", "translateF", "noteF")
                );
    }

    @Test
    @DisplayName("""
            mergeFromOuterSource(word):
             outer source buffer contains transcriptions,
             filled word already contain some transcriptions
             => merge transcriptions from buffer to word
            """)
    public void mergeFromOuterSource6() {
        User user = commit(() -> userRepository.save(user(1)));
        commit(() -> wordOuterSourceBuffer.saveDataFromOuterSource(
                emptyWord(user.getId(), "wordA", "noteA", 1).
                        addTranscription(new WordTranscription("transcriptionA", null).
                                addSourceInfo(outerSource("source5"))).
                        addTranscription(new WordTranscription("transcriptionB", null)).
                        addTranscription(new WordTranscription("transcriptionC", null).
                                addSourceInfo(outerSource("source4")).
                                addSourceInfo(outerSource("source5"))).
                        addTranscription(new WordTranscription("transcriptionE", null).
                                addSourceInfo(outerSource("source3")))
        ));

        Word actual = emptyWord(user.getId(), "wordA", "noteA", 1).
                addTranscription(new WordTranscription("transcriptionA", "noteA").
                        addSourceInfo(outerSource("source5")).
                        addSourceInfo(outerSource("source6"))).
                addTranscription(new WordTranscription("transcriptionD", "noteD")).
                addTranscription(new WordTranscription("transcriptionC", "noteC").
                        addSourceInfo(outerSource("source5")).
                        addSourceInfo(outerSource("source10")));
        wordOuterSourceBuffer.mergeFromOuterSource(actual);

        Assertions.assertThat(actual.getTranscriptions()).
                containsExactly(
                        new WordTranscription("transcriptionA", "noteA").
                                addSourceInfo(outerSource("source5")).
                                addSourceInfo(outerSource("source6")),
                        new WordTranscription("transcriptionD", "noteD"),
                        new WordTranscription("transcriptionC", "noteC").
                                addSourceInfo(outerSource("source5")).
                                addSourceInfo(outerSource("source10")).
                                addSourceInfo(outerSource("source4")),
                        new WordTranscription("transcriptionE", null).
                                addSourceInfo(outerSource("source3"))
                );
    }

    @Test
    @DisplayName("""
            mergeFromOuterSource(word):
             outer source buffer contains interpretations,
             filled word already contain some interpretations
             => merge interpretations from buffer to word
            """)
    public void mergeFromOuterSource7() {
        User user = commit(() -> userRepository.save(user(1)));
        commit(() -> wordOuterSourceBuffer.saveDataFromOuterSource(
                emptyWord(user.getId(), "wordA", "noteA", 1).
                        addInterpretation(new WordInterpretation("interpretationA").
                                addSourceInfo(outerSource("source5"))).
                        addInterpretation(new WordInterpretation("interpretationB")).
                        addInterpretation(new WordInterpretation("interpretationC").
                                addSourceInfo(outerSource("source1")).
                                addSourceInfo(outerSource("source10"))).
                        addInterpretation(new WordInterpretation("interpretationE").
                                addSourceInfo(outerSource("source3")))
        ));

        Word actual = emptyWord(user.getId(), "wordA", "noteA", 1).
                addInterpretation(new WordInterpretation("interpretationA").
                        addSourceInfo(outerSource("source5")).
                        addSourceInfo(new OuterSource("https://source7.com", "source7", toDay()))).
                addInterpretation(new WordInterpretation("interpretationD")).
                addInterpretation(new WordInterpretation("interpretationC").
                        addSourceInfo(outerSource("source10")).
                        addSourceInfo(new OuterSource("https://source11.com", "source11", toDay())));
        wordOuterSourceBuffer.mergeFromOuterSource(actual);

        Assertions.assertThat(actual.getInterpretations()).
                containsExactly(
                        new WordInterpretation("interpretationA").
                                addSourceInfo(outerSource("source5")).
                                addSourceInfo(new OuterSource("https://source7.com", "source7", toDay())),
                        new WordInterpretation("interpretationD"),
                        new WordInterpretation("interpretationC").
                                addSourceInfo(outerSource("source10")).
                                addSourceInfo(new OuterSource("https://source11.com", "source11", toDay())).
                                addSourceInfo(outerSource("source1")),
                        new WordInterpretation("interpretationE").
                                addSourceInfo(outerSource("source3"))
                );
    }

    @Test
    @DisplayName("""
            mergeFromOuterSource(word):
             outer source buffer contains translates,
             filled word already contain some translates
             => merge translates from buffer to word
            """)
    public void mergeFromOuterSource8() {
        User user = commit(() -> userRepository.save(user(1)));
        commit(() -> wordOuterSourceBuffer.saveDataFromOuterSource(
                emptyWord(user.getId(), "wordA", "noteA", 1).
                        addTranslation(new WordTranslation("translateA", null).
                                addSourceInfo(outerSource("source5"))).
                        addTranslation(new WordTranslation("translateB", null)).
                        addTranslation(new WordTranslation("translateC", null).
                                addSourceInfo(outerSource("source1")).
                                addSourceInfo(outerSource("source10"))).
                        addTranslation(new WordTranslation("translateE", null).
                                addSourceInfo(outerSource("source3")))
        ));

        Word actual = emptyWord(user.getId(), "wordA", "noteA", 1).
                addTranslation(new WordTranslation("translateA", "noteA").
                        addSourceInfo(outerSource("source5")).
                        addSourceInfo(new OuterSource("https://source7.com", "source7", toDay()))).
                addTranslation(new WordTranslation("translateD", "noteD")).
                addTranslation(new WordTranslation("translateC", "noteC").
                        addSourceInfo(outerSource("source10")).
                        addSourceInfo(new OuterSource("https://source11.com", "source11", toDay())));
        wordOuterSourceBuffer.mergeFromOuterSource(actual);

        Assertions.assertThat(actual.getTranslations()).
                containsExactly(
                        new WordTranslation("translateA", "noteA").
                                addSourceInfo(outerSource("source5")).
                                addSourceInfo(new OuterSource("https://source7.com", "source7", toDay())),
                        new WordTranslation("translateD", "noteD"),
                        new WordTranslation("translateC", "noteC").
                                addSourceInfo(outerSource("source10")).
                                addSourceInfo(new OuterSource("https://source11.com", "source11", toDay())).
                                addSourceInfo(outerSource("source1")),
                        new WordTranslation("translateE", null).
                                addSourceInfo(outerSource("source3"))
                );
    }

    @Test
    @DisplayName("""
            mergeFromOuterSource(word):
             outer source buffer contains examples,
             all buffer examples match the filled word examples
             => match examples from buffer
            """)
    public void mergeFromOuterSource9() {
        User user = commit(() -> userRepository.save(user(1)));
        Word word = emptyWord(user.getId(), "wordA", "noteA", 1).
                addExample(new WordExample("exampleA", "translateA", null).
                        addSourceInfo(exampleOuterSource("source1", "translate1"))).
                addExample(new WordExample("exampleB", "translateB", null)).
                addExample(new WordExample("exampleC", "translateC", null).
                        addSourceInfo(exampleOuterSource("source2", "translate2")).
                        addSourceInfo(exampleOuterSource("source3", "translate3")));
        commit(() -> wordOuterSourceBuffer.saveDataFromOuterSource(word));

        Word actual = emptyWord(word.getId(), user.getId(), "wordA", "noteA", 1).
                addExample(new WordExample("exampleA", "translateA", "noteA")).
                addExample(new WordExample("exampleD", "translateD", "noteD")).
                addExample(new WordExample("exampleC", "translateC", "noteC").
                        addSourceInfo(exampleOuterSource("source1", "translate1")).
                        addSourceInfo(exampleOuterSource("source3", "translate3")));
        wordOuterSourceBuffer.mergeFromOuterSource(actual);

        Assertions.assertThat(actual.getExamples()).
                containsExactly(
                        new WordExample("exampleA", "translateA", "noteA").
                                addSourceInfo(exampleOuterSource("source1", "translate1")),
                        new WordExample("exampleD", "translateD", "noteD"),
                        new WordExample("exampleC", "translateC", "noteC").
                                addSourceInfo(exampleOuterSource("source1", "translate1")).
                                addSourceInfo(exampleOuterSource("source3", "translate3")).
                                addSourceInfo(exampleOuterSource("source2", "translate2"))
                );
    }

    @Test
    @DisplayName("""
            mergeFromOuterSource(word):
             outer source buffer doesn't contain any transcriptions,
             => doesn't change filled word transcriptions
            """)
    public void mergeFromOuterSource10() {
        User user = commit(() -> userRepository.save(user(1)));
        Word word = emptyWord(user.getId(), "wordA", "noteA", 1).
                addTranscription(new WordTranscription("transcriptionA", "noteA").
                        addSourceInfo(outerSource("source5")).
                        addSourceInfo(outerSource("source6"))).
                addTranscription(new WordTranscription("transcriptionD", "noteD")).
                addTranscription(new WordTranscription("transcriptionC", "noteC").
                        addSourceInfo(outerSource("source5")).
                        addSourceInfo(outerSource("source10")));

        wordOuterSourceBuffer.mergeFromOuterSource(word);

        Assertions.assertThat(word.getTranscriptions()).
                containsExactly(
                        new WordTranscription("transcriptionA", "noteA").
                                addSourceInfo(outerSource("source5")).
                                addSourceInfo(outerSource("source6")),
                        new WordTranscription("transcriptionD", "noteD"),
                        new WordTranscription("transcriptionC", "noteC").
                                addSourceInfo(outerSource("source5")).
                                addSourceInfo(outerSource("source10"))
                );
    }

    @Test
    @DisplayName("""
            mergeFromOuterSource(word):
             outer source buffer doesn't contain any interpretations,
             => doesn't change filled word interpretations
            """)
    public void mergeFromOuterSource11() {
        User user = commit(() -> userRepository.save(user(1)));
        Word word = emptyWord(user.getId(), "wordA", "noteA", 1).
                addInterpretation(new WordInterpretation("interpretationA").
                        addSourceInfo(outerSource("source5"))).
                addInterpretation(new WordInterpretation("interpretationB")).
                addInterpretation(new WordInterpretation("interpretationC").
                        addSourceInfo(outerSource("source1")).
                        addSourceInfo(outerSource("source10"))).
                addInterpretation(new WordInterpretation("interpretationE").
                        addSourceInfo(outerSource("source3")));

        wordOuterSourceBuffer.mergeFromOuterSource(word);

        Assertions.assertThat(word.getInterpretations()).
                containsExactly(
                        new WordInterpretation("interpretationA").
                                addSourceInfo(outerSource("source5")),
                        new WordInterpretation("interpretationB"),
                        new WordInterpretation("interpretationC").
                                addSourceInfo(outerSource("source1")).
                                addSourceInfo(outerSource("source10")),
                        new WordInterpretation("interpretationE").
                                addSourceInfo(outerSource("source3"))
                );
    }

    @Test
    @DisplayName("""
            mergeFromOuterSource(word):
             outer source buffer doesn't contain any translates,
             => doesn't change filled word translates
            """)
    public void mergeFromOuterSource12() {
        User user = commit(() -> userRepository.save(user(1)));
        Word word = emptyWord(user.getId(), "wordA", "noteA", 1).
                addTranslation(new WordTranslation("translateA", "noteA").
                        addSourceInfo(outerSource("source5"))).
                addTranslation(new WordTranslation("translateB", "noteB")).
                addTranslation(new WordTranslation("translateC", "noteC").
                        addSourceInfo(outerSource("source1")).
                        addSourceInfo(outerSource("source10"))).
                addTranslation(new WordTranslation("translateE", "noteE").
                        addSourceInfo(outerSource("source3")));

        wordOuterSourceBuffer.mergeFromOuterSource(word);

        Assertions.assertThat(word.getTranslations()).
                containsExactly(
                        new WordTranslation("translateA", "noteA").
                                addSourceInfo(outerSource("source5")),
                        new WordTranslation("translateB", "noteB"),
                        new WordTranslation("translateC", "noteC").
                                addSourceInfo(outerSource("source1")).
                                addSourceInfo(outerSource("source10")),
                        new WordTranslation("translateE", "noteE").
                                addSourceInfo(outerSource("source3"))
                );
    }

    @Test
    @DisplayName("""
            mergeFromOuterSource(word):
             outer source buffer doesn't contain any examples,
             => doesn't change filled word examples
            """)
    public void mergeFromOuterSource13() {
        User user = commit(() -> userRepository.save(user(1)));
        Word word = emptyWord(user.getId(), "wordA", "noteA", 1).
                addExample(new WordExample("exampleA", "translateA", "noteA")).
                addExample(new WordExample("exampleD", "translateD", "noteD")).
                addExample(new WordExample("exampleC", "translateC", "noteC").
                        addSourceInfo(exampleOuterSource("source1", "translate1")).
                        addSourceInfo(exampleOuterSource("source3", "translate3")));

        wordOuterSourceBuffer.mergeFromOuterSource(word);

        Assertions.assertThat(word.getExamples()).
                containsExactly(
                        new WordExample("exampleA", "translateA", "noteA"),
                        new WordExample("exampleD", "translateD", "noteD"),
                        new WordExample("exampleC", "translateC", "noteC").
                                addSourceInfo(exampleOuterSource("source1", "translate1")).
                                addSourceInfo(exampleOuterSource("source3", "translate3"))
                );
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
                addExample(new WordExample("exampleA", "exampleTranslate", "noteA")).
                addExample(new WordExample("exampleB", "exampleTranslate", "noteB")).
                addExample(new WordExample("exampleC", "exampleTranslate", "noteC"));
    }

    private Word emptyWord(UUID userId,
                           String value,
                           String note,
                           int interval) {
        return new Word(userId, interval, interval, clock).
                setValue(value).
                setNote(note);
    }

    private Word emptyWord(UUID wordId,
                           UUID userId,
                           String value,
                           String note,
                           int interval) {
        return new Word(
                wordId,
                userId,
                value,
                note,
                new ArrayList<>(),
                new ArrayList<>(),
                new ArrayList<>(),
                new ArrayList<>(),
                new RepeatDataFromEnglish(interval, LocalDate.now(clock)),
                new RepeatDataFromNative(interval, LocalDate.now(clock))
        );
    }
    
    private ExampleOuterSource exampleOuterSource(String outerSourceName, String translate) {
        return new ExampleOuterSource(
                "https://" + outerSourceName + ".com",
                outerSourceName,
                toDay(),
                translate
        );
    }

    private OuterSource outerSource(String outerSourceName) {
        return new OuterSource(
                "https://" + outerSourceName + ".com",
                outerSourceName,
                toDay()
        );
    }

    private LocalDate toDay() {
        return LocalDate.now(clock);
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