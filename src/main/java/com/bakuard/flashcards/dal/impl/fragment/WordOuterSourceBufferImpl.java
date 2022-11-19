package com.bakuard.flashcards.dal.impl.fragment;

import com.bakuard.flashcards.config.configData.ConfigData;
import com.bakuard.flashcards.model.word.*;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.data.jdbc.core.JdbcAggregateOperations;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.Clock;
import java.util.*;

public class WordOuterSourceBufferImpl implements WordOuterSourceBuffer<Word> {

    private JdbcTemplate jdbcTemplate;
    private JdbcAggregateOperations jdbcAggregateOperations;
    private ConfigData configData;
    private Clock clock;

    public WordOuterSourceBufferImpl(JdbcTemplate jdbcTemplate,
                                     JdbcAggregateOperations jdbcAggregateOperations,
                                     ConfigData configData,
                                     Clock clock) {
        this.jdbcTemplate = jdbcTemplate;
        this.jdbcAggregateOperations = jdbcAggregateOperations;
        this.configData = configData;
        this.clock = clock;
    }

    @Override
    public Word save(Word word) {
        jdbcAggregateOperations.save(word);

        word.getExamples().forEach(example ->
            jdbcTemplate.batchUpdate("""
                            merge into words_examples_outer_source(word_id,
                                                                   example,
                                                                   outer_source_name,
                                                                   outer_source_url,
                                                                   recent_update_date,
                                                                   index)
                                key(word_id, example, outer_source_name, outer_source_url)
                                values(?, ?, ?, ?, ?, ?);
                            """,
                    new BatchPreparedStatementSetter() {
                        @Override
                        public void setValues(PreparedStatement ps, int i) throws SQLException {
                            SourceInfo sourceInfo = example.getSourceInfo().get(i);

                            ps.setObject(1, word.getId());
                            ps.setString(2, example.getOrigin());
                            ps.setString(3, sourceInfo.sourceName());
                            ps.setString(4, sourceInfo.url());
                            ps.setDate(5, Date.valueOf(sourceInfo.recentUpdateDate()));
                            ps.setInt(6, i);
                        }

                        @Override
                        public int getBatchSize() {
                            return example.getSourceInfo().size();
                        }
                    }
            )
        );

        return word;
    }

    @Override
    public List<WordTranscription> getTranscriptionsFromOuterSourceFor(String wordValue) {
        return jdbcTemplate.query("""
                select *
                    from words_transcriptions_outer_source
                    where words_transcriptions_outer_source.word_value = ?;
                """,
                ps -> ps.setString(1, wordValue),
                rs -> {
                    Map<String, WordTranscription> transcriptions = new HashMap<>();
                    while(rs.next()) {
                        String transcriptionValue = rs.getString("transcription");
                        if(!transcriptions.containsKey(transcriptionValue)) {
                            transcriptions.put(
                                    transcriptionValue,
                                    new WordTranscription(transcriptionValue, null)
                            );
                        }

                        transcriptions.get(transcriptionValue).addSourceInfo(
                                new SourceInfo(
                                        rs.getString("outer_source_url"),
                                        rs.getString("outer_source_name"),
                                        rs.getDate("recent_update_date").toLocalDate()
                                )
                        );
                    }
                    return new ArrayList<>(transcriptions.values());
                });
    }

    @Override
    public List<WordInterpretation> getInterpretationsFromOuterSourceFor(String wordValue) {
        return jdbcTemplate.query("""
                select *
                    from words_interpretations_outer_source
                    where words_interpretations_outer_source.word_value = ?;
                """,
                ps -> ps.setString(1, wordValue),
                rs -> {
                    Map<String, WordInterpretation> interpretations = new HashMap<>();
                    while(rs.next()) {
                        String interpretationValue = rs.getString("interpretation");
                        if(!interpretations.containsKey(interpretationValue)) {
                            interpretations.put(
                                    interpretationValue,
                                    new WordInterpretation(interpretationValue)
                            );
                        }

                        interpretations.get(interpretationValue).addSourceInfo(
                                new SourceInfo(
                                        rs.getString("outer_source_url"),
                                        rs.getString("outer_source_name"),
                                        rs.getDate("recent_update_date").toLocalDate()
                                )
                        );
                    }
                    return new ArrayList<>(interpretations.values());
                });
    }

    @Override
    public List<WordTranslation> getTranslationsFromOuterSourceFor(String wordValue) {
        return jdbcTemplate.query("""
                select *
                    from words_translations_outer_source
                    where words_translations_outer_source.word_value = ?;
                """,
                ps -> ps.setString(1, wordValue),
                rs -> {
                    Map<String, WordTranslation> translations = new HashMap<>();
                    while(rs.next()) {
                        String translationValue = rs.getString("translation");
                        if(!translations.containsKey(translationValue)) {
                            translations.put(
                                    translationValue,
                                    new WordTranslation(translationValue, null)
                            );
                        }

                        translations.get(translationValue).addSourceInfo(
                                new SourceInfo(
                                        rs.getString("outer_source_url"),
                                        rs.getString("outer_source_name"),
                                        rs.getDate("recent_update_date").toLocalDate()
                                )
                        );
                    }
                    return new ArrayList<>(translations.values());
                });
    }

    @Override
    public List<WordExample> getExamplesFromOuterSourceFor(UUID wordId) {
        LinkedHashMap<String, WordExample> examples = jdbcTemplate.query("""
                select *
                    from words_examples
                    where words_examples.origin in (
                        select words_examples_outer_source.example
                            from words_examples_outer_source
                            where words_examples_outer_source.word_id = ?
                    )
                    order by words_examples.index;
                """,
                ps -> ps.setObject(1, wordId),
                rs -> {
                    LinkedHashMap<String, WordExample> result = new LinkedHashMap<>();
                    while(rs.next()) {
                        result.put(
                                rs.getString("origin"),
                                new WordExample(
                                        rs.getString("origin"),
                                        rs.getString("translate"),
                                        rs.getString("note")
                                )
                        );
                    }
                    return result;
                });

        jdbcTemplate.query("""
                select *
                    from words_examples_outer_source
                    where words_examples_outer_source.word_id = ?
                    order by words_examples_outer_source.example, words_examples_outer_source.index;
                """,
                ps -> ps.setObject(1, wordId),
                rs -> {
                    WordExample example = examples.get(rs.getString("example"));
                    example.addSourceInfo(
                            new SourceInfo(
                                    rs.getString("outer_source_url"),
                                    rs.getString("outer_source_name"),
                                    rs.getDate("recent_update_date").toLocalDate()
                            )
                    );
                });

        return new ArrayList<>(examples.values());
    }

    @Override
    public void saveTranscriptionsToBuffer(String wordValue, List<WordTranscription> transcriptions) {
        jdbcTemplate.update("""
                delete from words_transcriptions_outer_source
                    where words_transcriptions_outer_source.word_value = ?;
                """,
                ps -> ps.setString(1, wordValue));

        List<Pair<WordTranscription, SourceInfo>> pairs = transcriptions.stream().
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
                pairs,
                pairs.size(),
                (ps, pair) -> {
                    ps.setString(1, wordValue);
                    ps.setString(2, pair.getKey().getValue());
                    ps.setString(3, pair.getValue().sourceName());
                    ps.setString(4, pair.getValue().url());
                    ps.setDate(5, Date.valueOf(pair.getValue().recentUpdateDate()));
                }
        );
    }

    @Override
    public void saveInterpretationsToBuffer(String wordValue, List<WordInterpretation> interpretations) {
        jdbcTemplate.update("""
                delete from words_interpretations_outer_source
                    where words_interpretations_outer_source.word_value = ?;
                """,
                ps -> ps.setString(1, wordValue));

        List<Pair<WordInterpretation, SourceInfo>> pairs = interpretations.stream().
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
                pairs,
                pairs.size(),
                (ps, pair) -> {
                    ps.setString(1, wordValue);
                    ps.setString(2, pair.getKey().getValue());
                    ps.setString(3, pair.getValue().sourceName());
                    ps.setString(4, pair.getValue().url());
                    ps.setDate(5, Date.valueOf(pair.getValue().recentUpdateDate()));
                }
        );
    }

    @Override
    public void saveTranslationsToBuffer(String wordValue, List<WordTranslation> translations) {
        jdbcTemplate.update("""
                delete from words_translations_outer_source
                    where words_translations_outer_source.word_value = ?;
                """,
                ps -> ps.setString(1, wordValue));

        List<Pair<WordTranslation, SourceInfo>> pairs = translations.stream().
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
                pairs,
                pairs.size(),
                (ps, pair) -> {
                    ps.setString(1, wordValue);
                    ps.setString(2, pair.getKey().getValue());
                    ps.setString(3, pair.getValue().sourceName());
                    ps.setString(4, pair.getValue().url());
                    ps.setDate(5, Date.valueOf(pair.getValue().recentUpdateDate()));
                }
        );
    }

}
