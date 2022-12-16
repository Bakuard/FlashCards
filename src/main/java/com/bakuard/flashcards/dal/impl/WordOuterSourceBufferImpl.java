package com.bakuard.flashcards.dal.impl;

import com.bakuard.flashcards.dal.WordOuterSourceBuffer;
import com.bakuard.flashcards.model.word.*;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Array;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@Transactional
public class WordOuterSourceBufferImpl implements WordOuterSourceBuffer {

    private static final Logger logger = LoggerFactory.getLogger(WordOuterSourceBufferImpl.class.getName());


    private JdbcTemplate jdbcTemplate;

    public WordOuterSourceBufferImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Transactional
    @Override
    public void saveDataFromOuterSource(Word word) {
        saveInterpretationsToBuffer(word.getValue(), word.getInterpretations());
        saveTranscriptionsToBuffer(word.getValue(), word.getTranscriptions());
        saveTranslationsToBuffer(word.getValue(), word.getTranslations());
        saveExamplesToBuffer(word.getExamples());
    }

    @Transactional(readOnly = true)
    @Override
    public void mergeFromOuterSource(Word word) {
        getTranscriptionsFromOuterSourceFor(word.getValue()).forEach(word::mergeTranscription);
        getInterpretationsFromOuterSourceFor(word.getValue()).forEach(word::mergeInterpretation);
        getTranslationsFromOuterSourceFor(word.getValue()).forEach(word::mergeTranslation);
        getExamplesFromOuterSourceFor(word.getExamples().stream().map(WordExample::getOrigin).toList()).
                forEach(word::mergeExampleIfPresent);
    }

    @Transactional
    @Override
    public void deleteUnusedOuterSourceExamples() {
        int deletedRowsNumber = jdbcTemplate.update("""
                delete from words_examples_outer_source
                    where words_examples_outer_source.example not in (
                        select words_examples.origin from words_examples
                    );
                """);

        logger.info("Delete unused examples from outer source. {} rows was deleted.", deletedRowsNumber);
    }


    private List<WordTranscription> getTranscriptionsFromOuterSourceFor(String wordValue) {
        return jdbcTemplate.query("""
                select *
                    from words_transcriptions_outer_source
                    where words_transcriptions_outer_source.word_value = ?
                    order by words_transcriptions_outer_source.transcription,
                             words_transcriptions_outer_source.outer_source_name;
                """,
                ps -> ps.setString(1, wordValue),
                rs -> {
                    List<WordTranscription> transcriptions = new ArrayList<>();
                    WordTranscription transcription = null;
                    while(rs.next()) {
                        String transcriptionValue = rs.getString("transcription");
                        if(transcription == null || !transcription.getValue().equals(transcriptionValue)) {
                            transcription = new WordTranscription(transcriptionValue, null);
                            transcriptions.add(transcription);
                        }

                        transcription.addSourceInfo(
                                new OuterSource(
                                        rs.getString("outer_source_url"),
                                        rs.getString("outer_source_name"),
                                        rs.getDate("recent_update_date").toLocalDate()
                                )
                        );
                    }
                    return transcriptions;
                });
    }

    private List<WordInterpretation> getInterpretationsFromOuterSourceFor(String wordValue) {
        return jdbcTemplate.query("""
                select *
                    from words_interpretations_outer_source
                    where words_interpretations_outer_source.word_value = ?
                    order by words_interpretations_outer_source.interpretation,
                             words_interpretations_outer_source.outer_source_name;
                """,
                ps -> ps.setString(1, wordValue),
                rs -> {
                    List<WordInterpretation> interpretations = new ArrayList<>();
                    WordInterpretation interpretation = null;
                    while(rs.next()) {
                        String interpretationValue = rs.getString("interpretation");
                        if(interpretation == null || !interpretation.getValue().equals(interpretationValue)) {
                            interpretation = new WordInterpretation(interpretationValue);
                            interpretations.add(interpretation);
                        }

                        interpretation.addSourceInfo(
                                new OuterSource(
                                        rs.getString("outer_source_url"),
                                        rs.getString("outer_source_name"),
                                        rs.getDate("recent_update_date").toLocalDate()
                                )
                        );
                    }
                    return interpretations;
                });
    }

    private List<WordTranslation> getTranslationsFromOuterSourceFor(String wordValue) {
        return jdbcTemplate.query("""
                select *
                    from words_translations_outer_source
                    where words_translations_outer_source.word_value = ?
                    order by words_translations_outer_source.translation,
                             words_translations_outer_source.outer_source_name;
                """,
                ps -> ps.setString(1, wordValue),
                rs -> {
                    List<WordTranslation> translations = new ArrayList<>();
                    WordTranslation translation = null;
                    while(rs.next()) {
                        String translationValue = rs.getString("translation");
                        if(translation == null || !translation.getValue().equals(translationValue)) {
                            translation = new WordTranslation(translationValue, null);
                            translations.add(translation);
                        }

                        translation.addSourceInfo(
                                new OuterSource(
                                        rs.getString("outer_source_url"),
                                        rs.getString("outer_source_name"),
                                        rs.getDate("recent_update_date").toLocalDate()
                                )
                        );
                    }
                    return translations;
                });
    }

    private List<WordExample> getExamplesFromOuterSourceFor(List<String> examples) {
        return jdbcTemplate.query(connection -> {
                    PreparedStatement ps = connection.prepareStatement("""
                                        select *
                                            from words_examples_outer_source
                                            where words_examples_outer_source.example = any(?)
                                            order by words_examples_outer_source.example,
                                                     words_examples_outer_source.outer_source_name;
                                        """);
                    Array array = connection.createArrayOf("VARCHAR", examples.toArray());
                    ps.setArray(1, array);
                    return ps;
                },
                rs -> {
                    ArrayList<WordExample> result = new ArrayList<>();
                    WordExample example = null;
                    while(rs.next()) {
                        String exampleOrigin = rs.getString("example");
                        if(example == null || !example.getOrigin().equals(exampleOrigin)) {
                            example = new WordExample(
                                    exampleOrigin,
                                    rs.getString("exampleTranslate"),
                                    null
                            );
                            result.add(example);
                        }

                        example.addSourceInfo(
                                new ExampleOuterSource(
                                        rs.getString("outer_source_url"),
                                        rs.getString("outer_source_name"),
                                        rs.getDate("recent_update_date").toLocalDate(),
                                        rs.getString("exampleTranslate")
                                )
                        );
                    }
                    return result;
                });
    }

    private void saveTranscriptionsToBuffer(String wordValue, List<WordTranscription> transcriptions) {
        jdbcTemplate.update("""
                delete from words_transcriptions_outer_source
                    where words_transcriptions_outer_source.word_value = ?;
                """,
                ps -> ps.setString(1, wordValue));

        List<Pair<WordTranscription, OuterSource>> pairs = transcriptions.stream().
                flatMap(transcription -> transcription.getOuterSource().stream().
                        map(s -> Pair.of(transcription, s))).
                toList();

        jdbcTemplate.batchUpdate("""
                        insert into words_transcriptions_outer_source(word_value,
                                                                      transcription,
                                                                      outer_source_name,
                                                                      outer_source_url,
                                                                      recent_update_date)
                            values(?, ?, ?, ?, ?);
                        """,
                new BatchPreparedStatementSetter() {
                    @Override
                    public void setValues(PreparedStatement ps, int i) throws SQLException {
                        Pair<WordTranscription, OuterSource> pair = pairs.get(i);

                        ps.setString(1, wordValue);
                        ps.setString(2, pair.getKey().getValue());
                        ps.setString(3, pair.getValue().sourceName());
                        ps.setString(4, pair.getValue().url());
                        ps.setDate(5, Date.valueOf(pair.getValue().recentUpdateDate()));
                    }

                    @Override
                    public int getBatchSize() {
                        return pairs.size();
                    }
                }
        );
    }

    private void saveInterpretationsToBuffer(String wordValue, List<WordInterpretation> interpretations) {
        jdbcTemplate.update("""
                delete from words_interpretations_outer_source
                    where words_interpretations_outer_source.word_value = ?;
                """,
                ps -> ps.setString(1, wordValue));

        List<Pair<WordInterpretation, OuterSource>> pairs = interpretations.stream().
                flatMap(interpretation -> interpretation.getOuterSource().stream().
                        map(s -> Pair.of(interpretation, s))).
                toList();

        jdbcTemplate.batchUpdate("""
                        insert into words_interpretations_outer_source(word_value,
                                                                       interpretation,
                                                                       outer_source_name,
                                                                       outer_source_url,
                                                                       recent_update_date)
                            values(?, ?, ?, ?, ?);
                        """,
                new BatchPreparedStatementSetter() {
                    @Override
                    public void setValues(PreparedStatement ps, int i) throws SQLException {
                        Pair<WordInterpretation, OuterSource> pair = pairs.get(i);

                        ps.setString(1, wordValue);
                        ps.setString(2, pair.getKey().getValue());
                        ps.setString(3, pair.getValue().sourceName());
                        ps.setString(4, pair.getValue().url());
                        ps.setDate(5, Date.valueOf(pair.getValue().recentUpdateDate()));
                    }

                    @Override
                    public int getBatchSize() {
                        return pairs.size();
                    }
                }
        );
    }

    private void saveTranslationsToBuffer(String wordValue, List<WordTranslation> translations) {
        jdbcTemplate.update("""
                delete from words_translations_outer_source
                    where words_translations_outer_source.word_value = ?;
                """,
                ps -> ps.setString(1, wordValue));

        List<Pair<WordTranslation, OuterSource>> pairs = translations.stream().
                flatMap(translation -> translation.getOuterSource().stream().
                        map(s -> Pair.of(translation, s))).
                toList();

        jdbcTemplate.batchUpdate("""
                        insert into words_translations_outer_source(word_value,
                                                                    translation,
                                                                    outer_source_name,
                                                                    outer_source_url,
                                                                    recent_update_date)
                            values(?, ?, ?, ?, ?);
                        """,
                new BatchPreparedStatementSetter() {
                    @Override
                    public void setValues(PreparedStatement ps, int i) throws SQLException {
                        Pair<WordTranslation, OuterSource> pair = pairs.get(i);

                        ps.setString(1, wordValue);
                        ps.setString(2, pair.getKey().getValue());
                        ps.setString(3, pair.getValue().sourceName());
                        ps.setString(4, pair.getValue().url());
                        ps.setDate(5, Date.valueOf(pair.getValue().recentUpdateDate()));
                    }

                    @Override
                    public int getBatchSize() {
                        return pairs.size();
                    }
                }
        );
    }

    private void saveExamplesToBuffer(List<WordExample> examples) {
        examples.forEach(example ->
                jdbcTemplate.batchUpdate("""
                            merge into words_examples_outer_source(example,
                                                                   exampleTranslate,
                                                                   outer_source_name,
                                                                   outer_source_url,
                                                                   recent_update_date)
                                key(example, outer_source_name)
                                values(?, ?, ?, ?, ?);
                            """,
                        new BatchPreparedStatementSetter() {
                            @Override
                            public void setValues(PreparedStatement ps, int i) throws SQLException {
                                ExampleOuterSource outerSource = example.getOuterSource().get(i);

                                ps.setString(1, example.getOrigin());
                                ps.setString(2, outerSource.translate());
                                ps.setString(3, outerSource.sourceName());
                                ps.setString(4, outerSource.url());
                                ps.setDate(5, Date.valueOf(outerSource.recentUpdateDate()));
                            }

                            @Override
                            public int getBatchSize() {
                                return example.getOuterSource().size();
                            }
                        }
                )
        );
    }

}
