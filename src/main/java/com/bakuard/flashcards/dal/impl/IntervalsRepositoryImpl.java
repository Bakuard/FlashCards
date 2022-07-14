package com.bakuard.flashcards.dal.impl;

import com.bakuard.flashcards.dal.IntervalsRepository;
import com.google.common.collect.ImmutableList;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.ArrayList;
import java.util.UUID;

public class IntervalsRepositoryImpl implements IntervalsRepository {

    private JdbcTemplate jdbcTemplate;

    public IntervalsRepositoryImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void add(UUID userId, int interval) {
        jdbcTemplate.update(
                "insert into intervals(user_id, number_days) values (?, ?);",
                ps -> {
                    ps.setObject(1, userId);
                    ps.setInt(2, interval);
                }
        );
    }

    @Override
    public void remove(UUID userId, int interval) {
        jdbcTemplate.update(
                "delete from intervals where user_id=? and number_days=?;",
                ps -> {
                    ps.setObject(1, userId);
                    ps.setInt(2, interval);
                }
        );
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

}
