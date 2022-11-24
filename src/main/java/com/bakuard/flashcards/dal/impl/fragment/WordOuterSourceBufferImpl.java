package com.bakuard.flashcards.dal.impl.fragment;

import com.bakuard.flashcards.model.word.*;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.data.jdbc.core.JdbcAggregateOperations;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.UUID;

public class WordOuterSourceBufferImpl implements WordOuterSourceBuffer<Word> {

    private JdbcTemplate jdbcTemplate;
    private JdbcAggregateOperations jdbcAggregateOperations;

    public WordOuterSourceBufferImpl(JdbcTemplate jdbcTemplate,
                                     JdbcAggregateOperations jdbcAggregateOperations) {
        this.jdbcTemplate = jdbcTemplate;
        this.jdbcAggregateOperations = jdbcAggregateOperations;
    }



    @Override
    public void saveDataFromOuterSource(Word word) {
        saveInterpretationsToBuffer(word.getValue(), word.getInterpretations());
        saveTranscriptionsToBuffer(word.getValue(), word.getTranscriptions());
        saveTranslationsToBuffer(word.getValue(), word.getTranslations());
        saveExamplesToBuffer(word.getId(), word.getExamples());
    }

    @Override
    public void mergeFromOuterSource(Word word) {
        getTranscriptionsFromOuterSourceFor(word.getValue()).forEach(word::mergeTranscription);
        getInterpretationsFromOuterSourceFor(word.getValue()).forEach(word::mergeInterpretation);
        getTranslationsFromOuterSourceFor(word.getValue()).forEach(word::mergeTranslation);
        getExamplesFromOuterSourceFor(word.getId()).forEach(word::mergeExample);
    }

    @Override
    public void deleteUnusedOuterSourceExamples() {
        jdbcTemplate.update("""
                delete from words_examples_outer_source
                    where words_examples_outer_source.example not in (
                        select words_examples.origin from words_examples
                    );
                """);
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

    private List<WordExample> getExamplesFromOuterSourceFor(UUID wordId) {
        return jdbcTemplate.query("""
                select *
                    from words_examples_outer_source
                    where words_examples_outer_source.word_id = ?
                    order by words_examples_outer_source.example,
                             words_examples_outer_source.outer_source_name;
                """,
                ps -> ps.setObject(1, wordId),
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
                flatMap(transcription -> transcription.getSourceInfo().stream().
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
                flatMap(interpretation -> interpretation.getSourceInfo().stream().
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
                flatMap(translation -> translation.getSourceInfo().stream().
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

    private void saveExamplesToBuffer(UUID wordId, List<WordExample> examples) {
        examples.forEach(example ->
                jdbcTemplate.batchUpdate("""
                            merge into words_examples_outer_source(word_id,
                                                                   example,
                                                                   exampleTranslate,
                                                                   outer_source_name,
                                                                   outer_source_url,
                                                                   recent_update_date)
                                key(word_id, example, outer_source_name)
                                values(?, ?, ?, ?, ?, ?);
                            """,
                        new BatchPreparedStatementSetter() {
                            @Override
                            public void setValues(PreparedStatement ps, int i) throws SQLException {
                                ExampleOuterSource outerSource = example.getSourceInfo().get(i);

                                ps.setObject(1, wordId);
                                ps.setString(2, example.getOrigin());
                                ps.setString(3, outerSource.translate());
                                ps.setString(4, outerSource.sourceName());
                                ps.setString(5, outerSource.url());
                                ps.setDate(6, Date.valueOf(outerSource.recentUpdateDate()));
                            }

                            @Override
                            public int getBatchSize() {
                                return example.getSourceInfo().size();
                            }
                        }
                )
        );
    }

}
