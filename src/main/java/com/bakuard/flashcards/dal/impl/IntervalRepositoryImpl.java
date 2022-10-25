package com.bakuard.flashcards.dal.impl;

import com.bakuard.flashcards.dal.IntervalRepository;
import com.bakuard.flashcards.validation.InvalidParameter;
import com.google.common.collect.ImmutableList;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.ArrayList;
import java.util.UUID;

public class IntervalRepositoryImpl implements IntervalRepository {

    private JdbcTemplate jdbcTemplate;

    public IntervalRepositoryImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void add(UUID userId, int interval) {
        if(interval <= 0) {
            throw new InvalidParameter(
                    "RepeatInterval.notNegative",
                    "interval can't be less then 1. Actual: " + interval
            );
        }

        jdbcTemplate.update(
                "insert into intervals(user_id, number_days) values (?, ?);",
                ps -> {
                    ps.setObject(1, userId);
                    ps.setInt(2, interval);
                }
        );
    }

    @Override
    public void replace(UUID userId, int oldInterval, int newInterval) {
        ImmutableList<Integer> intervals = findAll(userId);
        if(!intervals.contains(oldInterval)) {
            throw new InvalidParameter(
                    "Unknown oldInterval=" + oldInterval + " for user=" + userId,
                    "RepeatInterval.oldIntervalNotExists");
        }

        if(oldInterval != newInterval && !intervals.contains(newInterval)) {
            jdbcTemplate.update(
                    "update intervals set number_days = ? where user_id = ? and number_days = ?;",
                    ps -> {
                        ps.setInt(1, newInterval);
                        ps.setObject(2, userId);
                        ps.setInt(3, oldInterval);
                    }
            );
            changeWordIntervalsForRepeatFromEnglish(userId, oldInterval, newInterval);
            changeWordIntervalsForRepeatFromNative(userId, oldInterval, newInterval);
            changeExpressionIntervalsForRepeatFromEnglish(userId, oldInterval, newInterval);
            changeExpressionIntervalsForRepeatFromNative(userId, oldInterval, newInterval);
        } else if(oldInterval != newInterval) {
            changeWordIntervalsForRepeatFromEnglish(userId, oldInterval, newInterval);
            changeWordIntervalsForRepeatFromNative(userId, oldInterval, newInterval);
            changeExpressionIntervalsForRepeatFromEnglish(userId, oldInterval, newInterval);
            changeExpressionIntervalsForRepeatFromNative(userId, oldInterval, newInterval);
            jdbcTemplate.update(
                    "delete from intervals where user_id = ? and number_days = ?;",
                    ps -> {
                        ps.setObject(1, userId);
                        ps.setInt(2, oldInterval);
                    }
            );
        }
    }

    @Override
    public ImmutableList<Integer> findAll(UUID userId) {
        return jdbcTemplate.query(
                "select * from intervals where user_id=?;",
                ps -> ps.setObject(1 ,userId),
                rs -> {
                    ArrayList<Integer> result = new ArrayList<>();
                    while(rs.next()) result.add(rs.getInt("number_days"));
                    return ImmutableList.copyOf(result);
                }
        );
    }


    private void changeWordIntervalsForRepeatFromEnglish(UUID userId, int oldInterval, int newInterval) {
        jdbcTemplate.update("""
                    update words set repeat_interval_from_english = ?
                        where repeat_interval_from_english = ? and
                              user_id = ?;
                    """,
                ps -> {
                    ps.setInt(1, newInterval);
                    ps.setInt(2, oldInterval);
                    ps.setObject(3, userId);
                }
        );
    }

    private void changeWordIntervalsForRepeatFromNative(UUID userId, int oldInterval, int newInterval) {
        jdbcTemplate.update("""
                    update words set repeat_interval_from_native = ?
                        where repeat_interval_from_native = ? and
                              user_id = ?;
                    """,
                ps -> {
                    ps.setInt(1, newInterval);
                    ps.setInt(2, oldInterval);
                    ps.setObject(3, userId);
                }
        );
    }

    private void changeExpressionIntervalsForRepeatFromEnglish(UUID userId, int oldInterval, int newInterval) {
        jdbcTemplate.update("""
                    update expressions set repeat_interval_from_english = ?
                        where repeat_interval_from_english = ? and
                              user_id = ?;
                    """,
                ps -> {
                    ps.setInt(1, newInterval);
                    ps.setInt(2, oldInterval);
                    ps.setObject(3, userId);
                }
        );
    }

    private void changeExpressionIntervalsForRepeatFromNative(UUID userId, int oldInterval, int newInterval) {
        jdbcTemplate.update("""
                    update expressions set repeat_interval_from_native = ?
                        where repeat_interval_from_native = ? and
                              user_id = ?;
                    """,
                ps -> {
                    ps.setInt(1, newInterval);
                    ps.setInt(2, oldInterval);
                    ps.setObject(3, userId);
                }
        );
    }

}
