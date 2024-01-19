package com.bakuard.flashcards.dal.impl;

import com.bakuard.flashcards.dal.IntervalRepository;
import com.bakuard.flashcards.validation.exception.InvalidParameter;
import com.bakuard.flashcards.validation.exception.NotUniqueEntityException;
import com.bakuard.flashcards.validation.exception.UnknownEntityException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class IntervalRepositoryImpl implements IntervalRepository {

    private JdbcTemplate jdbcTemplate;

    public IntervalRepositoryImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void addAll(UUID userId, int... intervals) {
        Objects.requireNonNull(userId, "userId can't be null");
        Objects.requireNonNull(intervals, "intervals can't be null");
        assertIntervalsNotNegative(intervals);

        try {
            jdbcTemplate.batchUpdate("insert into intervals(user_id, number_days) values (?, ?);",
                    new BatchPreparedStatementSetter() {
                        @Override
                        public void setValues(PreparedStatement ps, int i) throws SQLException {
                            ps.setObject(1, userId);
                            ps.setInt(2, intervals[i]);
                        }

                        @Override
                        public int getBatchSize() {
                            return intervals.length;
                        }
                    });
        } catch(DuplicateKeyException e) {
            throw new NotUniqueEntityException(
                    "Intervals with values " + Arrays.toString(intervals) + " already exists",
                    e,
                    "RepeatInterval.unique",
                    false);
        } catch(DataIntegrityViolationException e) {
            throw new UnknownEntityException(
                    "Unknown user with id=" + userId, e, "User.unknownId", false);
        }
    }

    @Override
    public void add(UUID userId, int interval) {
        Objects.requireNonNull(userId, "userId can't be null");
        assertIntervalNotNegative(interval);

        try {
            jdbcTemplate.update(
                    "insert into intervals(user_id, number_days) values (?, ?);",
                    ps -> {
                        ps.setObject(1, userId);
                        ps.setInt(2, interval);
                    }
            );
        } catch(DuplicateKeyException e) {
            throw new NotUniqueEntityException(
                    "Interval with value " + interval + " already exists",
                    e,
                    "RepeatInterval.unique",
                    false);
        } catch(DataIntegrityViolationException e) {
            throw new UnknownEntityException(
                    "Unknown user with id=" + userId, e, "User.unknownId", false);
        }
    }

    @Override
    public void replace(UUID userId, int oldInterval, int newInterval) {
        List<Integer> intervals = findAll(userId);

        assertIntervalNotNegative(newInterval);
        assertUserHasInterval(oldInterval, userId, intervals);

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
    public List<Integer> findAll(UUID userId) {
        return jdbcTemplate.query(
                "select * from intervals where user_id=?;",
                ps -> ps.setObject(1 ,userId),
                rs -> {
                    ArrayList<Integer> result = new ArrayList<>();
                    while(rs.next()) result.add(rs.getInt("number_days"));
                    return result;
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


    private void assertIntervalsNotNegative(int... intervals) {
        for(int interval : intervals) assertIntervalNotNegative(interval);
    }

    private void assertIntervalNotNegative(int interval) {
        if(interval < 1) {
            throw new InvalidParameter(
                    "interval can't be less then 1. Actual: " + interval,
                    "RepeatInterval.notNegative"
            );
        }
    }

    private void assertUserHasInterval(int interval, UUID userId, List<Integer> intervals) {
        if(!intervals.contains(interval)) {
            throw new InvalidParameter(
                    "User with id=" + userId + " hasn't interval=" + interval,
                    "RepeatInterval.intervalNotExists");
        }
    }

}
