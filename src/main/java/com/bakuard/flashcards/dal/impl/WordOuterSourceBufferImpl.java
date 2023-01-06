package com.bakuard.flashcards.dal.impl;

import com.bakuard.flashcards.dal.WordOuterSourceBuffer;
import com.bakuard.flashcards.model.word.WordInterpretation;
import com.bakuard.flashcards.model.word.WordTranscription;
import com.bakuard.flashcards.model.word.WordTranslation;
import com.bakuard.flashcards.model.word.supplementation.SupplementedWord;
import com.bakuard.flashcards.model.word.supplementation.SupplementedWordExample;
import com.bakuard.flashcards.validation.exception.NotUniqueEntityException;
import com.bakuard.flashcards.validation.exception.UnknownEntityException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;

import java.net.URI;
import java.net.URISyntaxException;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

public class WordOuterSourceBufferImpl implements WordOuterSourceBuffer {

    private JdbcTemplate jdbcTemplate;

    public WordOuterSourceBufferImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void save(SupplementedWord word) {
        Objects.requireNonNull(word, "word can't be null");

        saveRoot(word);
        saveInterpretations(word);
        saveTranscriptions(word);
        saveTranslations(word);
        saveExamples(word);
    }

    @Override
    public Optional<SupplementedWord> findByWordValueAndOuterSource(String outerSourceName,
                                                                    String wordValue,
                                                                    UUID examplesOwnerId) {
        Objects.requireNonNull(outerSourceName, "outerSourceName can't be null");
        Objects.requireNonNull(wordValue, "wordValue can't be null");
        Objects.requireNonNull(examplesOwnerId, "userId can't be null");

        return Optional.ofNullable(loadRoot(outerSourceName, wordValue, examplesOwnerId)).
                map(this::loadInterpretations).
                map(this::loadTranscriptions).
                map(this::loadTranslations).
                map(this::loadExamples);
    }

    @Override
    public int deleteUnusedExamples() {
        return jdbcTemplate.update("""
                delete from words_examples_outer_source w
                    where not exists (
                        select * from words_examples_outer_source
                        inner join used_words_examples_outer_source
                            on w.user_id = used_words_examples_outer_source.user_id
                                 and w.word_outer_source_id = used_words_examples_outer_source.word_outer_source_id
                                 and w.example = used_words_examples_outer_source.example
                    );
                """);
    }


    private void saveRoot(SupplementedWord word) {
        if(word.isNew()) {
            word.generateIdIfAbsent();
            try {
                jdbcTemplate.update("""
                                insert into word_outer_source(word_outer_source_id,
                                                              word_value,
                                                              outer_source_name,
                                                              recent_update_date,
                                                              outer_source_uri)
                                    values(?, ?, ?, ?, ?);
                                """,
                        ps -> {
                            ps.setObject(1, word.getId());
                            ps.setString(2, word.getValue());
                            ps.setString(3, word.getOuterSourceName());
                            ps.setDate(4, Date.valueOf(word.getRecentUpdateDate()));
                            ps.setString(5, word.getOuterSourceUri().toString());
                        });
            } catch(DuplicateKeyException e) {
                throw new NotUniqueEntityException(
                        "SupplementedWord " + word + " already exists",
                        e,
                        "SupplementedWord.unique",
                        true);
            }
        } else {
            try {
                jdbcTemplate.update("""
                        update word_outer_source set
                                word_value=?,
                                outer_source_name=?,
                                recent_update_date=?,
                                outer_source_uri=?
                            where word_outer_source_id = ?;
                        """,
                        ps -> {
                            ps.setString(1, word.getValue());
                            ps.setString(2, word.getOuterSourceName());
                            ps.setDate(3, Date.valueOf(word.getRecentUpdateDate()));
                            ps.setString(4, word.getOuterSourceUri().toString());
                            ps.setObject(5, word.getId());
                        });
            } catch (DataIntegrityViolationException e) {
                throw new UnknownEntityException(
                        "Unknown SupplementedWord: value=" + word.getValue() + ", outerSourceName=" + word.getOuterSourceName(),
                        e,
                        "SupplementedWord.unknownValueOrOuterSource",
                        true);
            }
        }
    }

    private void saveInterpretations(SupplementedWord word) {
        jdbcTemplate.update(
                "delete from words_interpretations_outer_source where word_outer_source_id = ?;",
                ps -> ps.setObject(1, word.getId()));

        jdbcTemplate.batchUpdate("""
                        insert into words_interpretations_outer_source(word_outer_source_id,
                                                                       interpretation,
                                                                       index)
                            values (?, ?, ?);
                        """,
                new BatchPreparedStatementSetter() {
                    @Override
                    public void setValues(PreparedStatement ps, int i) throws SQLException {
                        WordInterpretation interpretation = word.getInterpretations().get(i);
                        ps.setObject(1, word.getId());
                        ps.setString(2, interpretation.getValue());
                        ps.setInt(3, i);
                    }

                    @Override
                    public int getBatchSize() {
                        return word.getInterpretations().size();
                    }
                });
    }

    private void saveTranscriptions(SupplementedWord word) {
        jdbcTemplate.update(
                "delete from words_transcriptions_outer_source where word_outer_source_id = ?;",
                ps -> ps.setObject(1, word.getId()));

        jdbcTemplate.batchUpdate("""
                        insert into words_transcriptions_outer_source(word_outer_source_id,
                                                                      transcription,
                                                                      index)
                            values (?, ?, ?);
                        """,
                new BatchPreparedStatementSetter() {
                    @Override
                    public void setValues(PreparedStatement ps, int i) throws SQLException {
                        WordTranscription transcription = word.getTranscriptions().get(i);
                        ps.setObject(1, word.getId());
                        ps.setString(2, transcription.getValue());
                        ps.setInt(3, i);
                    }

                    @Override
                    public int getBatchSize() {
                        return word.getTranscriptions().size();
                    }
                });
    }

    private void saveTranslations(SupplementedWord word) {
        jdbcTemplate.update(
                "delete from words_translations_outer_source where word_outer_source_id = ?;",
                ps -> ps.setObject(1, word.getId()));

        jdbcTemplate.batchUpdate("""
                        insert into words_translations_outer_source(word_outer_source_id,
                                                                    translation,
                                                                    index)
                            values (?, ?, ?);
                        """,
                new BatchPreparedStatementSetter() {
                    @Override
                    public void setValues(PreparedStatement ps, int i) throws SQLException {
                        WordTranslation translation = word.getTranslations().get(i);
                        ps.setObject(1, word.getId());
                        ps.setString(2, translation.getValue());
                        ps.setInt(3, i);
                    }

                    @Override
                    public int getBatchSize() {
                        return word.getTranslations().size();
                    }
                });
    }

    private void saveExamples(SupplementedWord word) {
        jdbcTemplate.update(
                "delete from words_examples_outer_source where word_outer_source_id = ? and user_id = ?;",
                ps -> {
                    ps.setObject(1, word.getId());
                    ps.setObject(2, word.getExamplesOwnerId());
                });

        jdbcTemplate.batchUpdate("""
                        insert into words_examples_outer_source(user_id,
                                                                word_outer_source_id,
                                                                example,
                                                                exampleTranslate,
                                                                outer_source_uri_to_example,
                                                                index)
                            values (?, ?, ?, ?, ?, ?);
                        """,
                new BatchPreparedStatementSetter() {
                    @Override
                    public void setValues(PreparedStatement ps, int i) throws SQLException {
                        SupplementedWordExample example = word.getExamples().get(i);
                        ps.setObject(1, word.getExamplesOwnerId());
                        ps.setObject(2, word.getId());
                        ps.setString(3, example.getOrigin());
                        ps.setString(4, example.getTranslate());
                        ps.setString(5, example.getOuterSourceUri().toString());
                        ps.setInt(6, i);
                    }

                    @Override
                    public int getBatchSize() {
                        return word.getExamples().size();
                    }
                });
    }


    private SupplementedWord loadRoot(String outerSourceName, String wordValue, UUID examplesOwnerId) {
        return jdbcTemplate.query("""
                        select * from word_outer_source
                            where outer_source_name = ? and word_value = ?;
                        """,
                ps -> {
                    ps.setString(1, outerSourceName);
                    ps.setString(2, wordValue);
                },
                rs -> {
                    SupplementedWord result = null;
                    if(rs.next()) {
                        URI uri = null;
                        try {
                            uri = new URI(rs.getString("outer_source_uri"));
                        } catch (URISyntaxException e) {
                            throw new RuntimeException(e);
                        }

                        result = new SupplementedWord(
                                (UUID) rs.getObject("word_outer_source_id"),
                                examplesOwnerId,
                                rs.getString("word_value"),
                                rs.getString("outer_source_name"),
                                LocalDate.parse(rs.getString("recent_update_date")),
                                uri
                        );
                    }
                    return result;
                }
        );
    }

    private SupplementedWord loadInterpretations(SupplementedWord word) {
        jdbcTemplate.query("""
                select *
                    from words_interpretations_outer_source
                    where words_interpretations_outer_source.word_outer_source_id = ?
                    order by words_interpretations_outer_source.index;
                """,
                ps -> ps.setObject(1, word.getId()),
                rs -> {
                    word.addInterpretation(
                            new WordInterpretation(rs.getString("interpretation"))
                    );
                });

        return word;
    }

    private SupplementedWord loadTranscriptions(SupplementedWord word) {
         jdbcTemplate.query("""
                select *
                    from words_transcriptions_outer_source
                    where words_transcriptions_outer_source.word_outer_source_id = ?
                    order by words_transcriptions_outer_source.index;
                """,
                ps -> ps.setObject(1, word.getId()),
                rs -> {
                    word.addTranscription(
                            new WordTranscription(
                                    rs.getString("transcription"),
                                    null
                            )
                    );
                });

        return word;
    }

    private SupplementedWord loadTranslations(SupplementedWord word) {
        jdbcTemplate.query("""
                select *
                    from words_translations_outer_source
                    where words_translations_outer_source.word_outer_source_id = ?
                    order by words_translations_outer_source.index;
                """,
                ps -> ps.setObject(1, word.getId()),
                rs -> {
                    word.addTranslation(
                            new WordTranslation(
                                    rs.getString("translation"),
                                    null
                            )
                    );
                });

        return word;
    }

    private SupplementedWord loadExamples(SupplementedWord word) {
        jdbcTemplate.query("""
                select *
                    from words_examples_outer_source
                    where words_examples_outer_source.word_outer_source_id = ? and user_id = ?
                    order by words_examples_outer_source.index;
                """,
                ps -> {
                    ps.setObject(1, word.getId());
                    ps.setObject(2, word.getExamplesOwnerId());
                },
                rs -> {
                    URI uri = null;
                    try {
                        uri = new URI(rs.getString("outer_source_uri_to_example"));
                    } catch (URISyntaxException e) {
                        throw new RuntimeException("Fail to load SupplementedWordExample - wrong URL format.", e);
                    }

                    word.addExample(
                            new SupplementedWordExample(
                                    rs.getString("example"),
                                    rs.getString("exampleTranslate"),
                                    null,
                                    uri
                            )
                    );
                });

        return word;
    }

}
